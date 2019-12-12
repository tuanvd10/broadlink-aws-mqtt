const shell = require("shelljs");
const path = require("path");
const fs = require("fs");
const logger = require("./../logger");
const cfg = require("./../config");


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
                //try 3 time
                let retry = 0;
                //current state
                //expect state
                while (retry < 3){
                    data.device.sendData(fileData, false);
                    //sleep 500ms
                    //get state => auto publish current state
                    data.spDevice.getState();
                    retry++;
                    //sleep 500ms
                }
                resolve(data);
            }
        });
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


module.exports = {
    playAction,
    deviceEnterLearningIR,
    deviceExitLearningIR,
    deviceEnterLearningRFSweep,
    deviceEnterLearningRFSweep,
    deviceEnterLearningRF,
    deviceExitLearningRF,
    recordSave,
    recordIR,
    recordRFCode,
    recordRFFrequence
}