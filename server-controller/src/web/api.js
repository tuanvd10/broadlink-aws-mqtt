const express = require("express");
var router = express.Router();
const logger = require("./../logger");

const {
  getDevicesInfo,
  runAction,
  listFilestructure,
  deleteFile,
  scanDevice
} = require("./../devices/actions");

router.post("/play", function (req, res) {
  if (req.body.topic && req.body.topic !== "") {
    let action = "play";
    if (req.body.id) {
      action += "-" + req.body.id;
    }
    runAction(action, req.body.topic, "api")
      .then(() => {
        logger.debug("api done");
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
        logger.debug("api done", data);
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
        logger.debug("api done", data);
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
      logger.debug("files", data);
      res.json(data);
    })
    .catch(err => {
      logger.error("api:files:err", err);
      res.statusCode = 400;
      return res.json({
        errors: ["Error occured"],
        err
      });
    });
});
router.delete("/files", function (req, res) {
  logger.debug("delete", req.body.file);
  deleteFile(req.body.file)
    .then(obj => {
      logger.info("file is removed");
      res.json({
        success: true
      });
    })
    .catch(err => {
      logger.error("api:files:delete:err", err);
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
  scanDevice();
  res.json({
    success: true
  });
});

module.exports = router;