var awsIot = require('aws-iot-device-sdk');
const logger = require("./../logger");
let cfg = require("./../config");
var mqttOptions = cfg.mqtt;

const device = awsIot.device({
  keyPath: './cert/broadlink.private.key',
  certPath: './cert/broadlink.cert.pem',
  caPath: './cert/root-CA.crt',
  clientId: 'sdk-nodejs-c347ce22-4717-4190-8e89-ef701aa31a8ef',
  host: "a3oosh7oql9nlc-ats.iot.us-east-1.amazonaws.com",
  region: "us-east-1",
  baseReconnectTimeMs: 10,
});

//
// Do a simple publish/subscribe demo based on the test-mode passed
// in the command line arguments.  If test-mode is 1, subscribe to
// 'topic_1' and publish to 'topic_2'; otherwise vice versa.  Publish
// a message every four seconds.
//
device.on('connect', function () {
  logger.info('AWS IOT device connect');
   //Subscie base path of aws iot device
   device.subscribe(`${mqttOptions.subscribeBasePath}/airpurifier/#`, function (err) {
  if (err) {
      logger.error("AWS IOT MQTT Failed to Subscribe", err);
  }
   });
   device.subscribe(`${mqttOptions.subscribeBasePath}/airthinx/#`, function (err) {
	  if (err) {
		  logger.error("AWS IOT MQTT Failed to Subscribe", err);
	  }
   });
});
device.on('close', function () {
   logger.error('AWS IOT device close');
});
device.on('reconnect', function () {
   logger.error('AWS IOT device reconnect');
});
device.on('offline', function () {
   logger.error('AWS IOT device offline');
});
device.on('error', function (error) {
   logger.error('AWS IOT device error', error);
});

module.exports = device;
