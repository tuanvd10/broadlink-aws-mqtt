// from: https://raw.githubusercontent.com/lprhodes/homebridge-broadlink-rm/master/helpers/getDevice.js
const BroadlinkJS = require("./broadlink");
//const BroadlinkJS = require("./broadlink");
const broadlink = new BroadlinkJS();
const EventEmitter = require("events");
const myEmitter = new EventEmitter();
const logger = require("./../logger");
const awsDevice = require("../aws-iot/device-publish");

var {
  devices,
  deviceInfos
} = require("./actions");
const mqttClient = require("./../mqtt/mqtt-client");

const {sendControlData,getAirThinxScore} = require("./../devices/airthinx_device");

const discoveredDevices = {};
let discovering = false;
let cfg = require("./../config");
var mqttOptions = cfg.mqtt;



const discoverDevicesLoop = (count = 0) => {
  logger.info("Discover device", count);
  discovering = true;
  if (count === 0) {
    logger.info("Discover complete, broadcast devices");
    myEmitter.emit("discoverCompleted", Object.keys(discoveredDevices).length);

    Object.keys(discoveredDevices).forEach(device => {
      myEmitter.emit("device", discoveredDevices[device]);
      deviceInfos.push({
        "name": discoveredDevices[device].name,
        "id": discoveredDevices[device].host.id
      });
    });
    discovering = false;

    //Try to update device infos to mqtt
    try {
      awsDevice.awsPublishDeviceInfos(deviceInfos);
    } catch (error) {
      logger.error("power publish error", error);
    }
    return;
  }

  broadlink.discover();
  count--;

  setTimeout(() => {
    discoverDevicesLoop(count);
  }, 5 * 1000);
};

const discoverDevices = (count) => {
  if (discovering) return;
  discovering = true;
  discoverDevicesLoop(count);
};

broadlink.on("deviceReady", device => {
  const macAddressParts =
    device.mac.toString("hex").match(/[\s\S]{1,2}/g) || [];
  //const ipAddressParts = device.host.address.split('.');
  const macAddress = macAddressParts.join(":");
  device.host.macAddress = macAddress;
  const ipAddress = device.host.address;
  //logger.info("found device", device);
  //logger.info("Discover complete")

  if (discoveredDevices[ipAddress]) return;
  /*
    logger.info(
      `Discovered Broadlink RM device at ${device.host.macAddress} (${
        device.host.address
      })`
    );
  */

  //device.host.id = macAddressParts.join('').substring(0,4) + ipAddressParts.slice(2).join('');
  device.host.id = macAddressParts.join("");
  discoveredDevices[ipAddress] = device;
  //discoveredDevices[macAddress] = device;
  //myEmitter.emit("device", device);
});

// -------------------------------------
//            Setup Broadlink
// -------------------------------------
// a broadlink device is found

myEmitter.on("device", discoveredDevice => {
  logger.info("new device", discoverDevices);
  devices.push(discoveredDevice);
  logger.info("Broadlink Found Device", discoveredDevice.host);
  discoveredDevice.on("temperature", temperature => {
    logger.debug(`Broadlink Temperature ${temperature}`, discoveredDevice.host);
  });
  discoveredDevice.on("power", data => { //function return when check power command
    var payload;
    if (data === true) payload = 'ON';
    else payload = 'OFF'
    discoveredDevice.power = payload;
    logger.debug(`Broadlink Power ${payload}`, discoveredDevice.host);
    try {
      awsDevice.awsPublishPower(payload);
    } catch (error) {
      logger.error("power publish error", error);
    }
  });
  discoveredDevice.on("energy", data => { //function return when check energy 
    var speed = 0;
    //compare to get speed of devices 
    if (data < cfg.smartplug.LOW) speed = 0;
    else if (data < cfg.smartplug.MEDIUM) speed = 1;
    else if (data < cfg.smartplug.HIGH) speed = 2;
    else speed = 3;
    discoveredDevice.speed = speed;
    logger.debug(`Broadlink energy ${speed}`, discoveredDevice.host);
    try {
      awsDevice.awsPublishSpeed(speed);
    } catch (error) {
      logger.error("energy publish error", error);
    }
  })
  /*
  // IR or RF signal found
  device.on("rawData", data => {
    logger.debug("Broadlink RAW");
    //recordSave(data);
    //recordCancel();
  });
  // RF Sweep found something
  device.on("rawRFData", temp => {
    logger.debug("Broadlink RAW RF");
    recordMode = recordModeEnum.RecordRFSignal;
  });
  // Don't really know
  device.on("rawRFData2", temp => {
    logger.debug("Broadlink RAW 2");
    recordCancel();
  });
  */
});
// after a while this is triggered
myEmitter.on("discoverCompleted", numOfDevice => {
  logger.info(`Broadlink Discovery completed. Found ${numOfDevice} items.`);
  if (numOfDevice === 0) {
    logger.error("Broadlink device is missing");
  }
	
	setInterval(function (){
			getAirThinxScore();
	}, 20000);
});
module.exports = {
  broadlink: myEmitter,
  discoverDevices
};