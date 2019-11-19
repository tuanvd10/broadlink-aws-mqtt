let cfg = require("./../config");
const logger = require("./../logger");
const mqtt = require("mqtt");

// -------------------------------------
//         Setup MQTT and listen
// -------------------------------------
// Options settings to use, see IClientOptions in MQTT
// https://github.com/mqttjs/MQTT.js > Client Options
//
// If you want to listen to MQTT events listen to mqtt.subscribeBasePath/#
// E.g. broadlink/#
var mqttOptions = cfg.mqtt;
logger.info("MQTT Options", mqttOptions);
var mqttClient = mqtt.connect("", mqttOptions);
mqttClient.on("connect", function (connack) {
  logger.info("MQTT Connected", connack);
  // listen to actions
  mqttClient.subscribe(`${mqttOptions.subscribeBasePath}/#`, function (err) {
    if (err) {
      logger.error("MQTT Failed to Subscribe", err);
    }
  });
});
mqttClient.on("reconnect", function () {
  logger.info("MQTT Reconnected");
});
mqttClient.on("close", function () {
  logger.error("MQTT Closed");
});
mqttClient.on("offline", function () {
  logger.error("MQTT Offline");
});
mqttClient.on("error", function (err) {
  logger.error("MQTT Error", err);
});


module.exports = mqttClient;