const shell = require("shelljs");
const path = require("path");
const fs = require("fs");
const md5 = require("md5");
const md5File = require("md5-file");
const logger = require("./../logger");
const cfg = require("./../config");
const rmHandles = require("./rm-handles");
const spHandles = require("./sp-handles");
const awsDevice = require("../aws-iot/device-publish");
// -------------------------------------
//         Application Actions
// -------------------------------------

let actionIsRunning = false;

const commandsPath = cfg.recording.path || path.join(__dirname, "commands");
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
                .then(rmHandles.deviceEnterLearningIR)
                .then(rmHandles.recordIR)
                .then(rmHandles.deviceExitLearningIR)
                .then(rmHandles.recordSave)
                .then(data => {
                    logger.info("done", {
                        action: data.action,
                        filePath: data.filePath
                    });
                })
                .catch(err => {
                    logger.error("error occured", err);
                    prepareAction({
                        action,
                        topic,
                        origin
                    }).then(rmHandles.deviceExitLearningIR);
                });
        case "recordrf":
            return prepareAction({
                    action,
                    topic,
                    origin
                })
                .then(rmHandles.deviceEnterLearningRFSweep)
                .then(rmHandles.recordRFFrequence)
                .then(rmHandles.deviceEnterLearningIR)
                .then(rmHandles.recordRFCode)
                .then(rmHandles.deviceExitLearningIR)
                .then(rmHandles.recordSave)
                .then(data => {
                    logger.info("done", data);
                })
                .catch(err => {
                    logger.error("error occured", err);
                    prepareAction({
                        action,
                        topic,
                        origin
                    }).then(rmHandles.deviceExitLearningRF);
                });
        case "play":
            return prepareAction({
                    action,
                    topic,
                    origin
                })
                .then(rmHandles.playAction);
        case "temperature":
            return prepareAction({
                    action,
                    topic,
                    origin
                })
                .then(queryTemperature);
        case "setpower":
            return prepareAction({
                    action,
                    topic,
                    origin
                })
                .then(spHandles.setPowerAction)    
                .then( () => {
                    awsDevice.awsPublishPower("ON");
                });
        case "checkpower":      
            return prepareAction({
                action,
                topic,
                origin
            })
            .then(spHandles.getPowerAction)
            .then( (data) => {
                logger.info("Done get Power action", data);
            })
        case "checkspeed":      
            return prepareAction({
                action,
                topic,
                origin
            })
            .then(spHandles.getEnergyAction)
            .then( (data) => {
                logger.info("Done get speed action");
            })
        case "getinfo": 
            return getDeviceInfos();
		case "getairthinxmode":
			if(topic.indexOf(cfg.mqtt.subscribeBasePath+ "/airthinx/getcurrentmode") >=0)
				return getAirthinxMode();
			break;
		case "setairthinxmode":
			if(topic.indexOf(cfg.mqtt.subscribeBasePath+ "/airthinx/setmode") >=0){
				//parse action to get mode: auto = false; manual = true
				 if (action.indexOf("-") !== -1){
					let mode = action.substring(action.indexOf("-")+1, action.length);
					return setAirthinxMode(mode);
				 }
			}
			 break;
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
            if (data.topic.split("/").length < 2) {
                logger.debug(
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
            if (Broadlink.devices.length === 0) {
                logger.debug("No devices");
                return reject("No devices");
            } else if (data.action.indexOf("-") !== -1) {
                // we want to select specific device
                const deviceId = data.action.substring(data.action.indexOf("-") + 1);
                for (let i = 0; i < Broadlink.devices.length; i++) {
                    if (Broadlink.devices[i].host.id === deviceId) {
                        device = Broadlink.devices[i];
                        break;
                    }
                }
                if (!device) return reject("Requested device not found");
            } else if (Broadlink.devices.length > 1) {
                logger.debug("Multiple devices exists. Please specify one to use.");
                return reject("Multiple devices exists. Please specify one to use.");
            } else {
                device = Broadlink.devices[0];
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
const getDeviceInfos = () => 
    new Promise((resolve, reject) => {
        awsDevice.awsPublishDeviceInfos(Broadlink.deviceInfos);
        resolve(Broadlink.deviceInfos);
    });

const getDevicesInfo = () =>
    new Promise((resolve, reject) => {
        var devs = [];
        for (let i = 0; i < Broadlink.devices.length; i++) {
            devs.push(Object.assign({}, Broadlink.devices[i].host));
        }
        resolve(devs);
    });
const scanDevice = (count = cfg.numOfDiscover) => {
    Broadlink.discoverDevices(count);
}

const getAirthinxMode = () => {
	let mode = Broadlink.getAirthinxMode();
    awsDevice.awsPublishAirthinxMode(mode);
	return new Promise((resolve, reject) => resolve(mode));
}

const setAirthinxMode = (mode) => {
    Broadlink.setAirthinxMode(mode);
	awsDevice.awsPublishAirthinxMode(mode);
    return new Promise((resolve, reject) => resolve(1));
}

module.exports = {
    runAction,
    handleListAllActions,
    deleteFile,
    listFilestructure,
    getDevicesInfo,
    scanDevice
}    
var Broadlink = require("./device");