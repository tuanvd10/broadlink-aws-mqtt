var mqttClient = require("./mqtt-client");
const logger = require("./../logger");
let cfg = require("./../config");
var mqttOptions = cfg.mqtt;

//Publish power to mqtt local
var mqttPublishPower = (power) => {
    try {
        mqttClient.publish(`${mqttOptions.subscribeBasePath}-stat/airpurifier/power`, power.toString());
    } catch (error) {
        logger.error("AWS IOT MQTT Publish Power Failed", error);
    }
}
//Publish speed to mqtt local
var mqttPublishSpeed = (speed) => {
    try {
        mqttClient.publish(`${mqttOptions.subscribeBasePath}-stat/airpurifier/speed`, speed.toString());
    } catch (error) {
        logger.error("AWS IOT MQTT Publish Speed Failed", error);
    }
}
//Publish speed to mqtt local
var mqttPublishDeviceInfos = (data) => {
    try {
        mqttClient.publish(`${mqttOptions.subscribeBasePath}-stat/devices/info`, JSON.stringify(data));
    } catch (error) {
        logger.error("AWS IOT MQTT Publish Device Infos Failed", error);
    }
}
module.exports = {
    mqttPublishPower,
    mqttPublishSpeed,
    mqttPublishDeviceInfos
}