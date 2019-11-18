const { runAction} = require("./../devices/actions");
const logger = require("./../logger");
const cfg = require("./../config");
const axios = require("axios");
const fs = require("fs");

axios.defaults.headers.common['Authorization'] = "Bearer 1339b161-9ea6-490b-877f-bd6e65674373";
axios.defaults.headers.common['Accept'] = "application/json";
axios.defaults.headers.post['Content-Type'] = "application/json";

const getAirThinxScore =  async ()=>{
	let data, dataPoint;
	let aq, time;
	let co2, pm25;
	/* get from air thinx server and callculate aq */
	data = await axios.post('https://api.environet.io/search/nodes',
	{"node_id":"5dc0391a8ba45e000102d77f", "last": 1}
		).then((res) => {
			return res.data
		})
		.catch((err) => logger.error("airthinx" + JSON.stringify(err)));
		
	dataPoint = data[0].data_points.find(x => x.name === 'AQ');
	aq = dataPoint.measurements[0][1];
	time = dataPoint.measurements[0][0];
	pm25 = data[0].data_points.find(x => x.name === 'PM2.5').measurements[0][1];
	pm25 = data[0].data_points.find(x => x.name.indexOf("CO") == 0 ).measurements[0][1];

	logger.info("aq value: " +aq);
	if(global.aq.aq == 0 || (global.aq.aq <= 90 && aq > 90) || (global.aq.aq > 90 && aq <= 90))
			global.aq.time = time;
		
	global.aq.aq = aq;

}

const sendControlData = () => {
	/* global.currentState.clientStatus: 
		"DISCONNECT"
		0: OFF 
		1: level 1 
		2: level 2 
		3: level 3
	*/
	let currentTime = new Date().getTime();

	logger.debug("[tuanvd10] current state: ", global.currentState);
	logger.debug("[tuanvd10] current aq: ", global.aq);
	console.log("[tuanvd10] current time: " + currentTime);

	if(global.aq.aq <=90){
		if(global.currentState.clientStatus==3) {
			logger.info("[tuanvd10] max level, cannot increase");
		}else{
			if(currentTime - global.aq.time > cfg.airthinx.interval_time){
					//increase 1 level if it keep state too long
					if(global.currentState.clientStatus==0){
						logger.info("[tuanvd10] turn ON level");
						runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + cfg.airthinx.commandPower, "airthinx");
					}else{
						logger.info("[tuanvd10] increse 1 level");
						runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + cfg.airthinx.commandIncrease, "airthinx");
					}
					//global.currentState.clientStatus+=1;
			}
		}
	}else if(global.aq.aq >90){
		if(global.currentState.clientStatus==0){
			//do nothing
			
			logger.info("[tuanvd10] OFF level");
		}else{
			//decrease a level
			let currentTime = new Date().getTime();
			if(currentTime - global.aq.time > cfg.airthinx.interval_time){
					//decrease 1 level or OFF if it keep state too long
					if(global.currentState.clientStatus==1){
						logger.info("[tuanvd10] Turn OFF level");
						runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + cfg.airthinx.commandPower, "airthinx");
					}else{
						logger.info("[tuanvd10] decrease 1 level");
						runAction("play-" + cfg.airthinx.deviceid, cfg.mqtt.subscribeBasePath + cfg.airthinx.commandDecrease, "airthinx");
					}
					//global.currentState.clientStatus-=1;
			}
		}
	}else{
		
	}
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

global.currentState = {
	"clientStatus" : 0,
	"time" : 0
}
global.aq = {
	"aq" : -1,
	"time" : 0
}

module.exports =  {sendControlData,getAirThinxScore}