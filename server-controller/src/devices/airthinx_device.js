var { runAction } = require("./actions");
const logger = require("./../logger");
const cfg = require("./../config");
const axios = require("axios");
const fs = require("fs");

var commandList = ["PowerOnCommand", "PowerOffCommand","LowCommand", "MedCommand", "HighCommand"];
var interval = null;
var mode = "auto";

axios.defaults.headers.common['Authorization'] = "Bearer 1339b161-9ea6-490b-877f-bd6e65674373";
axios.defaults.headers.common['Accept'] = "application/json";
axios.defaults.headers.post['Content-Type'] = "application/json";

const getAirThinxScore =  async ()=>{
	let data, dataPoint;
	let aq, time;
	let co2, //880
		pm, pm1, pm25, pm10, //25.4 
		voc, //1
		formaldehyde; //0.05
	let haveErr = false;
	/* get from air thinx server and callculate aq */
	data = await axios.post('https://api.environet.io/search/nodes',
	{"node_id":"5dc0391a8ba45e000102d77f", "last": 1}
		).then((res) => {
			return res.data
		})
		.catch((err) => {
			logger.error("airthinx" + JSON.stringify(err));
			haveErr = true;
		});
	if(haveErr) return false;
	dataPoint = data[0].data_points.find(x => x.name === 'AQ');
	time = dataPoint.measurements[0][0];
	
	aq = dataPoint.measurements[0][1];
	pm = data[0].data_points.find(x => x.name === 'PM').measurements[0][1];
	pm1 = data[0].data_points.find(x => x.name === 'PM1').measurements[0][1];
	pm25 = data[0].data_points.find(x => x.name === 'PM2.5').measurements[0][1];
	co2 = data[0].data_points.find(x => x.name.indexOf("CO") == 0 ).measurements[0][1];
	pm10 = data[0].data_points.find(x => x.name === "PM10" ).measurements[0][1];
	voc = data[0].data_points.find(x =>x.name === "VOC (Isobutylene)" ).measurements[0][1] + data[0].data_points.find(x =>x.name === "VOC (EtOH)" ).measurements[0][1];
	formaldehyde = data[0].data_points.find(x => x.name.indexOf("CH")==0).measurements[0][1];

	logger.debug("[tuanvd10] aq value: " +aq);
	
	if(
//		global.aq.aq == 0 || (global.aq.aq <=  cfg.goodPoint.aq && aq >  cfg.goodPoint.aq) || (global.aq.aq >  cfg.goodPoint.aq && aq <=  cfg.goodPoint.aq)
//		|| 	global.aq.pm == 0 || (global.aq.pm <= cfg.goodPoint.pm && pm > cfg.goodPoint.pm) || (global.aq.pm > cfg.goodPoint.pm && pm <= cfg.goodPoint.pm)
		global.aq.pm1 == 0 || (global.aq.pm1 <= cfg.goodPoint.pm && pm1 > cfg.goodPoint.pm) || (global.aq.pm1 > cfg.goodPoint.pm && pm1 <= cfg.goodPoint.pm)
		|| 	global.aq.pm25 == 0 || (global.aq.pm25 <= cfg.goodPoint.pm && pm25 > cfg.goodPoint.pm) || (global.aq.pm25 > cfg.goodPoint.pm && pm25 <= cfg.goodPoint.pm)
		|| 	global.aq.pm10 == 0 || (global.aq.pm10 <= cfg.goodPoint.pm && pm10 > cfg.goodPoint.pm) || (global.aq.pm10 > cfg.goodPoint.pm && pm10 <= cfg.goodPoint.pm)
		|| 	global.aq.co2 == 0 || (global.aq.co2 <= cfg.goodPoint.co2 && co2 >  cfg.goodPoint.co2) || (global.aq.co2 >  cfg.goodPoint.co2 && co2 <=  cfg.goodPoint.co2)
		|| 	global.aq.voc == 0 || (global.aq.voc <=  cfg.goodPoint.voc && voc > cfg.goodPoint.voc) || (global.aq.voc > cfg.goodPoint.voc && voc <= cfg.goodPoint.voc)
		|| 	global.aq.formaldehyde == 0 || (global.aq.formaldehyde <= cfg.goodPoint.formaldehyde && formaldehyde > cfg.goodPoint.formaldehyde) || (global.aq.formaldehyde > cfg.goodPoint.formaldehyde && formaldehyde <= cfg.goodPoint.formaldehyde)
	)
			global.aq.time = time;
		
	global.aq.aq = aq;
	global.aq.pm = pm;
	global.aq.pm1 = pm1;
	global.aq.pm25 = pm25;
	global.aq.pm10 = pm10;
	global.aq.co2 = co2;
	global.aq.voc = voc;
	global.aq.formaldehyde = formaldehyde;

	return true;
}

const sendControlData = (spDevice) => {
	/* device.state.currentState.clientStatus: 
		0: OFF 
		1: level 1 
		2: level 2 
		3: level 3
	*/
	let currentTime = new Date().getTime();
	//var spDevice = Broadlink.devices.find(x => x.host.id === cfg.airthinx.spDeviceId);
	if(spDevice.state.spState === false){
		logger.debug("[tuanvd10] sendControlData SP state: " + spDevice.state.spState);
		return;
	}
	logger.debug("[tuanvd10] current state: ", spDevice.state);
	logger.debug("[tuanvd10] current aq: ", global.aq);
	logger.debug("[tuanvd10] current time: " + currentTime);

	if(!checkAirCondition()){//not good
		if(spDevice.state.currentState.clientStatus==3) {
			logger.debug("[tuanvd10] max level, cannot increase");
		}else{
			{
					//increase 1 level if it keep aq and state too long
					if(spDevice.state.currentState.clientStatus==0){
						logger.info("[tuanvd10] turn ON level");
						//runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + cfg.airthinx.commandPower, "airthinx");
						//sendCommandMultitime(1);//if only 1 button
						sendAirthinxCommand(0);
					}else if (currentTime - global.aq.time > cfg.airthinx.interval_time && currentTime-spDevice.state.currentState.time > cfg.airthinx.interval_time){
						logger.info("[tuanvd10] increse 1 level");
						//runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + cfg.airthinx.commandIncrease, "airthinx");
						//sendCommandMultitime(1);//if only 1 button
						sendAirthinxCommand(spDevice.state.currentState.clientStatus+2);
					}
			}
		}
	}else{ //good
		if(spDevice.state.currentState.clientStatus==0){
			//do nothing
			logger.debug("[tuanvd10] OFF level");
		}else{
			//decrease a level
			let currentTime = new Date().getTime();
			{
					//decrease 1 level or OFF if it keep state too long
					if(spDevice.state.currentState.clientStatus==1){
						logger.info("[tuanvd10] Turn OFF level");
						//runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + cfg.airthinx.commandPower, "airthinx");
						//sendCommandMultitime(3);//if only 1 button
						sendAirthinxCommand(1);
					}else if(currentTime - global.aq.time > cfg.airthinx.interval_time && currentTime-spDevice.state.currentState.time > cfg.airthinx.interval_time){
						logger.info("[tuanvd10] decrease 1 level");
						//runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + cfg.airthinx.commandDecrease, "airthinx");
						//sendCommandMultitime(3);//if only 1 button
						sendAirthinxCommand(spDevice.state.currentState.clientStatus);
					}
			}
		}
	}
}

function sendAirthinxCommand(level){
	let command = cfg.airthinx[commandList[level]];
	//console.log("[tuanvd10] sendAirthinxCommand take command: " + command);
	runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + command, "airthinx");
}

function sleep(ms){
     return new Promise(resolve=>{
         setTimeout(resolve,ms)
     })
}

const sendCommandMultitime = async (time) =>{
	let i =0;
	for(i = 0; i< time; i++) {
		await 	runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + cfg.airthinx.commandPower, "airthinx");
		await  sleep(500);
	}
};

async function doAction(devices){
	logger.debug("[tuanvd10] START ACTION");
	var spDevice = devices.find(x => x.host.id === cfg.airthinx.spDeviceId);
	if(!spDevice) return;
	//console.log("[tuanvd10]: SP device " + JSON.stringify(spDevice));
	//await spDevice.getState();
	if(spDevice.getState() && getAirThinxScore()){
		if(mode==="auto"){
			await sleep(1000);
			sendControlData(spDevice);
		}
	} 
	logger.debug("[tuanvd10] DONE ACTION");
}

function getCurrentAirthinxState(discoverDevices, requsetMode = "auto") {
	setInterval(function () {
		doAction(discoverDevices);
	}, cfg.airthinx.time_delay);
}

function setCurrentAirthinxMode(requsetMode = "auto"){
	logger.debug("[tuanvd10] change mode: " + mode + " => " + requsetMode);
	if("auto" !== requsetMode && "manual" !== requsetMode) {
		logger.debug("[tuanvd10] request mode not correct");
		return;
	}
	mode = requsetMode;
}

function getCurrentAirthinxMode(){
	return mode;
}

function checkAirCondition(){
	if( global.aq.co2 > cfg.goodPoint.co2 
		|| global.aq.pm1 > cfg.goodPoint.pm
		|| global.aq.pm25 > cfg.goodPoint.pm
		|| global.aq.pm10 > cfg.goodPoint.pm
		|| global.aq.voc > cfg.goodPoint.voc 
		|| global.aq.formaldehyde > cfg.goodPoint.formaldehyde 
		)
		return false;
	return true;//good
}

//remove interval when have request from app
function removeInterval(){
		clearInterval(interval);
		interval = null;
}

global.aq = {
	"co2" : -1, 
	"pm": -1, "pm1": -1, "pm25": -1, "pm10": -1, 
	"voc": -1, 
	"formaldehyde": -1, 
	"aq" : -1,
	"time" : -1
}
module.exports.getCurrentAirthinxState = getCurrentAirthinxState;
module.exports.getCurrentAirthinxMode = getCurrentAirthinxMode;
module.exports.setCurrentAirthinxMode = setCurrentAirthinxMode;