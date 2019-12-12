// Make the imports
//Require a web server code
const logger = require("./src/logger");
require("./src/web/server");;

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
    handleMsg.runAction(msg, topic, "aws")
        .then(data => logger.debug("mqtt done"))
        .catch(err => logger.error("mqtt aws iot failed on message", err));
});

logger.info("Starting Broadlink MQTT NodeJS Application");

handleMsg.scanDevice(2);

