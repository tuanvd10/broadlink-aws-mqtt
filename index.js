// Make the imports
const {
    discoverDevices
} = require("./src/devices/device");
//Require a web server code
const logger = require("./src/logger");
require("./src/web/server");;

const mqttClient = require("./src/mqtt/mqtt-client");
const awsDevice = require("./src/aws-iot/device-connect");
const handleMsg = require("./src/devices/actions");

//Handle aws iot device message
awsDevice.on('message', function (topic, payload) {
    logger.info('message', topic, payload.toString());
    // message is Buffer
    const msg = payload.toString();
    logger.debug("MQTT AWS IOT Message", {
        topic,
        msg
    });
    handleMsg.runAction(msg, topic, "mqtt")
        .then(data => logger.debug("mqtt done"))
        .catch(err => logger.eror("mqtt failed on message", err));
});

//Handle mqtt local message
mqttClient.on("message", function (topic, message) {
    // message is Buffer
    const msg = message.toString();
    logger.debug("MQTT Message", {
        topic,
        msg
    });
    handleMsg.runAction(msg, topic, "mqtt")
        .then(data => logger.debug("mqtt done"))
        .catch(err => logger.eror("mqtt failed on message", err));
});

logger.info("Starting Broadlink MQTT NodeJS Application");

discoverDevices(2);
