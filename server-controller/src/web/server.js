const express = require("express");
const bodyParser = require("body-parser");
const http = require("http");
const socket = require("socket.io");
let cfg = require("./../config");
const routerAPI = require("./api")
const logger = require("./../logger");
const {
  handleListAllActions,
  getDevicesInfo,
  runAction,
  scanDevice
} = require("./../devices/actions");

// -------------------------------------
//             Webserver
// -------------------------------------
// Output a simple GUI to interact with
// Setup socket.io so we can talk back and forth
const app = express();
app.use(express.static("html"));
app.use(bodyParser.urlencoded({
  extended: true
}));
app.use(bodyParser.json());
// API
app.use("/api", routerAPI);


// server is alive
var server = http.createServer(app);
server.listen(cfg.gui.port, () =>
  logger.info(`GUI Web listen on port ${cfg.gui.port}`)
);

// websocket actions
io = socket.listen(server);
io.on("connection", socket => {
  logger.info("Web a client connected");
  io.emit("config", cfg);
  socket.on("disconnect", () => {
    logger.info("Web a client disconnected");
  });
  socket.on("action", msg => {
    logger.info("Web User want action", msg);
    runAction(msg.action, msg.topic, "web")
      .then(data => logger.info("web done", {
        action: data.action,
        path: data.path,
        device: data.device.host.id
      }))
      .catch(err => logger.error("web failed", err));
  });
  socket.on("getActions", () => {
    logger.info("Loading saved actions");
    handleListAllActions()
      .then(files => {
        logger.info("Actions on disk", files);
        io.emit("actions", files);
      })
      .catch(err => logger.error("Failed to load " + err));
  });
  socket.on("getDevices", () => {
    logger.info("Loading Connected devices");
    getDevicesInfo()
      .then(devs => {
        logger.info("Connected devices", devs);
        io.emit("devices", devs);
      })
      .catch(err => logger.error("Failed to load " + err));
  });
  socket.on("rescanDevices", () => {
    logger.info("Rescan devices");
    scanDevice();
  });
});
module.export = io;