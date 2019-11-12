const shell = require("shelljs");
const path = require("path");
const fs = require("fs");
const md5 = require("md5");
const md5File = require("md5-file");
const logger = require("./../logger");
const cfg = require("./../config");
// -------------------------------------
//         Application Actions
// -------------------------------------

let actionIsRunning = false;
var devices = [];
const commandsPath = cfg.recording.path || path.join(__dirname, "commands");
console.log(commandsPath);
function runAction(action, topic, origin) {
    action = action.toLowerCase();
    let actionMode = action;
    if (actionMode.indexOf("-") !== -1)
        actionMode = action.substring(0, action.indexOf("-"));
    switch (actionMode) {
        case "recordir":
            return prepareAction({
                    action,
                    topic,
                    origin
                })
                .then(deviceEnterLearningIR)
                .then(recordIR)
                .then(deviceExitLearningIR)
                .then(recordSave)
                .then(data => {
                    console.log("done", data);
                })
                .catch(err => {
                    console.log("error occured", err);
                    prepareAction({
                        action,
                        topic,
                        origin
                    }).then(deviceExitLearningIR);
                });
        case "recordrf":
            return prepareAction({
                    action,
                    topic,
                    origin
                })
                .then(deviceEnterLearningRFSweep)
                .then(recordRFFrequence)
                .then(deviceEnterLearningIR)
                .then(recordRFCode)
                .then(deviceExitLearningIR)
                .then(recordSave)
                .then(data => {
                    console.log("done", data);
                })
                .catch(err => {
                    console.log("error occured", err);
                    prepareAction({
                        action,
                        topic,
                        origin
                    }).then(deviceExitLearningRF);
                });
        case "play":
            return prepareAction({
                    action,
                    topic,
                    origin
                })
                .then(playAction)
                .then(mqttPublish);
        case "temperature":
            return prepareAction({
                    action,
                    topic,
                    origin
                })
                .then(queryTemperature)
        default:
            logger.error(`Action ${action} doesn't exists`);
            return handleActionError(`Action ${action} doesn't exists`);
            break;
    }
}
// Properly handle invalid action input to runAction
const handleActionError = data =>
    new Promise((resolve, reject) => {
        resolve(data);
    });

// Handle incoming actions from MQTT
const prepareAction = data =>
    new Promise((resolve, reject) => {
        logger.debug("prepareAction", data);
        if (data.topic.indexOf(cfg.mqtt.subscribeBasePath) === 0) {
            if (data.topic.split("/").length < 3) {
                logger.error(
                    "Topic is too short, should contain broadcast base e.g. 'broadlink' with following device and action. e.g. broadlink/tv/samsung/power"
                );
                reject("Stopped prepareAction");
                return;
            }

            data.topic = data.topic.toLowerCase();
            data.action = data.action.toLowerCase();
            const actionPath = data.topic.substr(
                cfg.mqtt.subscribeBasePath.length + 1
            );
            const filePath = path.join(commandsPath, actionPath) + ".bin";
            const folderPath = filePath.substr(0, filePath.lastIndexOf("/"));

            // find device to use
            let device;
            if (devices.length === 0) {
                return reject("No devices");
            } else if (data.action.indexOf("-") !== -1) {
                // we want to select specific device
                const deviceId = data.action.substring(data.action.indexOf("-") + 1);
                for (let i = 0; i < devices.length; i++) {
                    if (devices[i].host.id === deviceId) {
                        device = devices[i];
                        break;
                    }
                }
                if (!device) return reject("Requested device not found");
            } else if (devices.length > 1) {
                return reject("Multiple devices exists. Please specify one to use.");
            } else {
                device = devices[0];
            }

            data = Object.assign({}, data, {
                path: actionPath,
                folderPath,
                filePath,
                device
            });
            resolve(data);
        } else {
            logger.error("MQTT Message Failed with base path");
            reject("Stopped prepareAction");
        }
    });

// learn ir
const deviceEnterLearningIR = data =>
    new Promise((resolve, reject) => {
        logger.debug("deviceEnterLearningIR");
        data.device.enterLearning();
        resolve(data);
    });

// Stops ir
const deviceExitLearningIR = data =>
    new Promise((resolve, reject) => {
        logger.debug("deviceExitLearningIR");
        data.device.cancelLearn();
        resolve(data);
    });

// rf sweep frq
const deviceEnterLearningRFSweep = data =>
    new Promise((resolve, reject) => {
        logger.debug("deviceEnterLearningRFSweep");
        data.device.enterRFSweep();
        resolve(data);
    });
// enter rf learning
const deviceEnterLearningRF = data =>
    new Promise((resolve, reject) => {
        logger.debug("deviceEnterLearningRF");
        data.device.enterLearning();
        resolve(data);
    });
// stops rf
const deviceExitLearningRF = data =>
    new Promise((resolve, reject) => {
        logger.debug("deviceExitLearningRF");
        data.device.cancelLearn();
        resolve(data);
    });

// Save action
const recordSave = data =>
    new Promise((resolve, reject) => {
        logger.info("recordSave");
        logger.info(`Save data to file ${data.filePath}`);
        shell.mkdir("-p", data.folderPath);
        fs.writeFile(data.filePath, data.signal, {
            flag: "w"
        }, err => {
            if (err) {
                logger.error("Failed to create file", err);
                reject("Stopped at recordSave");
                return;
            }
            logger.info("File saved successfully");
            resolve(data);
        });
    });

// Record a IR Signal
const recordIR = data =>
    new Promise((resolve, reject) => {
        logger.info("recordIR: Press an IR signal");
        let timeout = cfg.recording.timeout.ir;
        let intervalSpeed = 1;
        let interval = setInterval(() => {
            logger.info("recordIR: Timeout in " + timeout);
            data.device.checkData();
            timeout -= intervalSpeed;
            if (timeout <= 0) {
                clearInterval(interval);
                logger.error("IR Timeout");
                reject("Stopped at recordIR");
            }
        }, intervalSpeed * 1000);

        // IR signal received
        const callback = dataRaw => {
            clearInterval(interval);
            logger.debug("Broadlink IR RAW");
            data.device.removeListener("rawData", callback);
            data.signal = dataRaw;
            resolve(data);
        };
        data.device.on("rawData", callback);
    });

// Record RF Signal (after a frequence is found)
const recordRFCode = data =>
    new Promise((resolve, reject) => {
        logger.info("recordRFCode: Press RF button");
        setTimeout(() => {
            let timeout = cfg.recording.timeout.rf;
            let intervalSpeed = 1;
            let interval = setInterval(() => {
                logger.info("recordRFCode: Timeout in " + timeout);
                data.device.checkData();
                timeout -= intervalSpeed;
                if (timeout <= 0) {
                    clearInterval(interval);
                    logger.error("RF Timeout");
                    reject("Stopped at recordRFCode");
                }
            }, intervalSpeed * 1000);

            // IR or RF signal found
            const callback = dataRaw => {
                logger.debug("Broadlink RF RAW");
                data.signal = dataRaw;
                clearInterval(interval);
                data.device.removeListener("rawData", callback);
                resolve(data);
            };
            data.device.on("rawData", callback);
        }, 3000);
    });

// Record RF, scans for frequence
const recordRFFrequence = data =>
    new Promise((resolve, reject) => {
        logger.info("recordRFFrequence: Hold and RF button");
        let timeout = cfg.recording.timeout.rf;
        let intervalSpeed = 1;
        let interval = setInterval(() => {
            logger.info("recordRFFrequence: Timeout in " + timeout);
            data.device.checkRFData();
            timeout -= intervalSpeed;
            if (timeout <= 0) {
                clearInterval(interval);
                logger.error("RF Sweep Timeout");
                reject("Stopped at recordRFFrequence");
            }
        }, intervalSpeed * 1000);

        // RF Sweep found something
        const callback = dataRaw => {
            clearInterval(interval);
            data.device.removeListener("rawRFData", callback);
            data.frq = dataRaw;
            resolve(data);
        };
        data.device.on("rawRFData", callback);
    });

const playAction = data =>
    new Promise((resolve, reject) => {
        logger.info("playAction");
        fs.readFile(data.filePath, (err, fileData) => {
            if (err) {
                logger.error("Failed to read file", {
                    err
                });
                reject("Stopped at playAction");
                return;
            } else {
                data.device.sendData(fileData, false);
                resolve(data);
            }
        });
    });

const queryTemperature = data =>
    new Promise((resolve, reject) => {
        logger.info("queryTemperature");
        try {
            data.device.checkTemperature();
            resolve(data);
        } catch (error) {
            logger.error("Failed to query temperature");
            reject("Stopped at queryTemperature");
        }
    });

const handleListAllActions = data =>
    new Promise((resolve, reject) => {
        var files = [];
        shell.ls("commands/**/*.bin").forEach(function (file) {
            const topic = file.substring(0, file.length - 4);
            files.push(topic);
        });
        files.sort();
        resolve(files);
    });

const mqttPublish = data =>
    new Promise((resolve, reject) => {
        if (data.origin !== "mqtt") {
            //@TODO implement
            //logger.info("broadcast action, how to");
            //mqttClient.publish()
        }
        resolve(data);
    });

// -------------- HELPERS --------------

const deleteFile = path =>
    new Promise((resolve, reject) => {
        logger.info(`delete file  ${path}`);
        fs.unlink(path, err => {
            if (err) {
                logger.error("Failed to delete file", {
                    err
                });
                reject("Stopped at deleteFile");
                return;
            } else {
                resolve({});
            }
        });
    });

// Return json file structure
const listFilestructure = dir => {
    const walk = entry => {
        return new Promise((resolve, reject) => {
            fs.exists(entry, exists => {
                if (!exists) {
                    return resolve({});
                }
                return resolve(
                    new Promise((resolve, reject) => {
                        fs.lstat(entry, (err, stats) => {
                            if (err) {
                                return reject(err);
                            }
                            if (!stats.isDirectory()) {
                                return resolve(
                                    new Promise((resolve, reject) => {
                                        md5File(entry, (err, hash) => {
                                            if (err) {
                                                return reject(err);
                                            }
                                            resolve({
                                                path: entry,
                                                type: "file",
                                                text: path.basename(entry),
                                                time: stats.mtime,
                                                size: stats.size,
                                                id: md5(entry),
                                                hash,
                                                icon: path.extname(entry) === ".bin" ? "fas fa-bolt" : null
                                            });
                                        });
                                    })
                                );
                                /*
                                return resolve({
                                  path: entry,
                                  type: "file",
                                  text: path.basename(entry),
                                  time: stats.mtime,
                                  size: stats.size,
                                  id: md5(entry),
                                  icon: path.extname(entry) === ".bin" ? "fas fa-bolt" : null
                                });
                                */
                            }
                            resolve(
                                new Promise((resolve, reject) => {
                                    fs.readdir(entry, (err, files) => {
                                        if (err) {
                                            return reject(err);
                                        }
                                        Promise.all(
                                                files.map(child => walk(path.join(entry, child)))
                                            )
                                            .then(children => {
                                                resolve({
                                                    path: entry,
                                                    type: "folder",
                                                    text: path.basename(entry),
                                                    time: stats.mtime,
                                                    children,
                                                    id: md5(entry)
                                                });
                                            })
                                            .catch(err => {
                                                reject(err);
                                            });
                                    });
                                })
                            );
                        });
                    })
                );
            });
        });
    };

    return walk(dir);
};

const getDevicesInfo = () =>
    new Promise((resolve, reject) => {
        var devs = [];
        for (let i = 0; i < devices.length; i++) {
            devs.push(Object.assign({}, devices[i].host));
        }
        resolve(devs);
    });
module.exports = {
    runAction,
    handleListAllActions,
    deleteFile,
    listFilestructure,
    getDevicesInfo,
    devices
}