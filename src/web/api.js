const express = require("express");
var router = express.Router();
const logger = require("./../logger");
const {
  discoverDevices
} = require("./../devices/device");
const {
  handleListAllActions,
  getDevicesInfo,
  runAction,
  listFilestructure,
  deleteFile
} = require("./../devices/actions");

router.post("/play", function (req, res) {
  if (req.body.topic && req.body.topic !== "") {
    let action = "play";
    if (req.body.id) {
      action += "-" + req.body.id;
    }
    runAction(action, req.body.topic, "api")
      .then(() => {
        console.log("api done");
        res.json({
          message: "Sending message " + req.body.topic
        });
      })
      .catch(err => {
        logger.error("api play error", err);
        res.statusCode = 500;
        return res.json({
          errors: ["Failed " + err],
          err
        });
      });
  } else {
    res.statusCode = 400;
    return res.json({
      errors: ["POST JSON missing property topic"]
    });
  }
});
router.post("/recordir", function (req, res) {
  if (req.body.topic && req.body.topic !== "") {
    let action = "recordir";
    if (req.body.id) {
      action += "-" + req.body.id;
    }
    runAction(action, req.body.topic, "api")
      .then(data => {
        console.log("api done", data);
        res.json({
          message: "Sending message " + req.body.topic
        });
      })
      .catch(err => {
        logger.error("api recordir error", err);
        res.statusCode = 500;
        return res.json({
          errors: ["Failed " + err],
          err
        });
      });
  } else {
    res.statusCode = 400;
    return res.json({
      errors: ["POST JSON missing property topic"]
    });
  }
});
router.post("/recordrf", function (req, res) {
  if (req.body.topic && req.body.topic !== "") {
    let action = "recordrf";
    if (req.body.id) {
      action += "-" + req.body.id;
    }
    runAction(action, req.body.topic, "api")
      .then(data => {
        console.log("api done", data);
        res.json({
          message: "Sending message " + req.body.topic
        });
      })
      .catch(err => {
        logger.error("api recordrf error", err);
        res.statusCode = 500;
        return res.json({
          errors: ["Failed " + err],
          err
        });
      });
  } else {
    res.statusCode = 400;
    return res.json({
      errors: ["POST JSON missing property topic"]
    });
  }
});
router.get("/files", function (req, res) {
  listFilestructure("./commands")
    .then(data => {
      console.log("files", data);
      res.json(data);
    })
    .catch(err => {
      console.error("api:files:err", err);
      res.statusCode = 400;
      return res.json({
        errors: ["Error occured"],
        err
      });
    });
});
router.delete("/files", function (req, res) {
  console.log("delete", req.body.file);
  deleteFile(req.body.file)
    .then(obj => {
      console.log("file is removed");
      res.json({
        success: true
      });
    })
    .catch(err => {
      console.error("api:files:delete:err", err);
      res.statusCode = 400;
      return res.json({
        errors: ["Error occured"],
        err
      });
    });
});
router.get("/devices", function (req, res) {
  getDevicesInfo()
    .then(devs => {
      res.json(devs);
    })
    .catch(err => {
      res.statusCode = 400;
      return res.json({
        errors: ["Error occured"],
        err
      });
    });
});
router.get("/devices/discover", function (req, res) {
  logger.info("Rescan devices");
  devices = [];
  discoverDevices();
  res.json({
    success: true
  });
});

module.exports = router;