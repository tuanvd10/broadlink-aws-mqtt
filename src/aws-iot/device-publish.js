var device = require("./device-connect");
const logger = require("./../logger");
let cfg = require("./../config");
var mqttOptions = cfg.mqtt;

//Publish power to AWS IOT
var awsPublishPower = (power) => {
    logger.debug(`Publish Devices Power to ${mqttOptions.subscribeBasePath}-stat/airpurifier/power`);
    try {
        device.publish(`${mqttOptions.subscribeBasePath}-stat/airpurifier/power`, power.toString());
    } catch (error) {
        logger.error("AWS IOT MQTT Publish Power Failed", error);
    }
}
//Publish power to AWS IOT
var awsPublishSpeed = (speed) => {
    logger.debug(`Publish Devices Speed to ${mqttOptions.subscribeBasePath}-stat/airpurifier/speed`);
    try {
        device.publish(`${mqttOptions.subscribeBasePath}-stat/airpurifier/speed`, speed.toString());
    } catch (error) {
        logger.error("AWS IOT MQTT Publish Speed Failed", error);
    }
}
//Publish power to AWS IOT
var awsPublishDeviceInfos = (data) => {
    logger.debug(`Publish Devices Info to ${mqttOptions.subscribeBasePath}-stat/airpurifier/info`);
    logger.debug(JSON.stringify(data));
    try {
        device.publish(`${mqttOptions.subscribeBasePath}-stat/airpurifier/info`, JSON.stringify(data));
    } catch (error) {
        logger.error("AWS IOT MQTT Publish Device Infos Failed", error);
    }
}

var awsPublishAirthinxMode = (mode) => {
	logger.debug(`Publish AirthinxMode ${mode} to ${mqttOptions.subscribeBasePath}-stat/airthinx/currentmode`);
    try {
        device.publish(`${mqttOptions.subscribeBasePath}-stat/airthinx/currentmode`, mode.toString());
    } catch (error) {
        logger.error("AWS IOT MQTT Publish AirthinxMode Failed", error);
    }
}

module.exports = {
    awsPublishPower,
    awsPublishSpeed,
    awsPublishDeviceInfos,
	awsPublishAirthinxMode
}