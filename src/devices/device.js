// from: https://raw.githubusercontent.com/lprhodes/homebridge-broadlink-rm/master/helpers/getDevice.js
const BroadlinkJS = require("./broadlink");
//const BroadlinkJS = require("./broadlink");
const broadlink = new BroadlinkJS();
const EventEmitter = require("events");
const myEmitter = new EventEmitter();
const logger = require("./../logger");
const awsDevice = require("../aws-iot/device-publish");
var devices = []
var deviceInfos = [];

const {
  getCurrentAirthinxState, getCurrentAirthinxMode
} = require("./../devices/airthinx_device");

const discoveredDevices = {};


let discovering = false;
let cfg = require("./../config");

const discoverDevices = (count = 2) => {
  //Delete all devices have found
  devices.splice(0,devices.length);
  deviceInfos.splice(0,deviceInfos.length);
  if (discovering) return;
  discovering = true;
  discoverDevicesLoop(count);
};

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
  logger.info("new device");
  
  devices.push(discoveredDevice);
  logger.info("Broadlink Found Device", discoveredDevice.host);
 
   discoveredDevice.removeAllListeners("temperature");
    discoveredDevice.removeAllListeners("power");
   discoveredDevice.removeAllListeners("energy");

  discoveredDevice.on("temperature", temperature => {
    logger.debug(`Broadlink Temperature ${temperature}`, discoveredDevice.host);
  });
  discoveredDevice.on("power", data => { //function return when check power command
    var payload;
    if (data === true) payload = 'ON';
    else payload = 'OFF'
    discoveredDevice.state.spState = data;
    logger.debug(`Broadlink Power ${payload}`);
    try {
      awsDevice.awsPublishPower(payload);
    } catch (error) {
      logger.error("power publish error", error);
    }finally{
		discoveredDevice.checkPower = false;
		//discoveredDevice.mutex.release("Get Power Done");
	}
  });

  discoveredDevice.on("energy", data => { //function return when check energy 
    var speed = 0;
    //compare to get speed of devices 
    if (data < cfg.smartplug.LOW) speed = 0;
    else if (data < cfg.smartplug.MEDIUM) speed = 1;
    else if (data < cfg.smartplug.HIGH) speed = 2;
    else speed = 3;
	if (discoveredDevice.state.currentState.clientStatus != speed) {
		discoveredDevice.state.currentState.time = new Date().getTime();
	}
    discoveredDevice.state.currentState.clientStatus = speed;
    logger.debug(`Broadlink speed ${speed} in energy ${data})`);
    try {
      awsDevice.awsPublishSpeed(speed);
    } catch (error) {
      logger.error("energy publish error", error);
    }finally{
		discoveredDevice.regularCheck = false;
		//discoveredDevice.mutex.release("Get Energy Done");
	}
});

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
});

getCurrentAirthinxState(devices);

const setAirthinxMode = (mode) => {
    setAirthinxMode(devices, mode);
}

const getAirthinxMode = () => {
    return getCurrentAirthinxMode();
}
module.exports = {
  discoverDevices: discoverDevices,
  devices: devices,
  deviceInfos: deviceInfos,
setAirthinxMode:setAirthinxMode,
getAirthinxMode: getAirthinxMode
};
