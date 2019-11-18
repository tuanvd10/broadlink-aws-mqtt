const logger = require("./../logger");

const setPowerAction = data => {
    new Promise((resolve, reject) => {
        logger.info("setPowerAction");
        try {
            data.device.setPower(false);
            resolve(data);
        } catch (error) {
            logger.error("Failed to set power");
            reject("Stopped at setPowerAction");
        };
    })
}
const getPowerAction = data => {
    new Promise((resolve, reject) => {
        logger.info("getPowerAction");
        try {
            data.device.getPower();
            resolve(data);
        } catch (error) {
            logger.error("Failed to query power");
            reject("Stopped at queryPowerState");
        }
    });
}
const getEnergyAction = data => {
    new Promise((resolve, reject) => {
        logger.info("getEnergyAction");
        try {
            data.device.getEnergy();
            resolve(data);
        } catch (error) {
            logger.error("Failed to query power");
            reject("Stopped at getEnergyAction");
        }
    });
}
module.exports = {
    setPowerAction,
    getPowerAction,
    getEnergyAction
}