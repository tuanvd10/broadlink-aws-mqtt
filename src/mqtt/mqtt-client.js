let cfg = require("./../config");
const logger = require("./../logger");
const mqtt = require("mqtt");

const {
  runAction
} = require("./../devices/actions");

const {sendControlData} = require("./../devices/airthinx_device");

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
  
    mqttClient.subscribe(`${mqttOptions.subscribeBaseState}/#`, function (err) {
    if (err) {
      logger.error("MQTT Failed to Subscribe " + mqttOptions.subscribeBaseState , err);
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

mqttClient.on("message", function (topic, message) {
  // message is Buffer
  const msg = message.toString();
  logger.debug("MQTT Message", {
    topic,
    msg
  });
  /*check if topic is state of client
	call once per 5s
  */
  if(topic.indexOf(cfg.mqtt.subscribeBaseState)==0){
	  //message is state of client
	  global.currentState.clientStatus = parseInt(message);
	  sendControlData();
  }
  else
  runAction(msg, topic, "mqtt")
    .then(data => console.log("mqtt done", data))
    .catch(err => console.error("mqtt failed on message", err));
});

module.exports = mqttClient;