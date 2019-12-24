const winston = require("winston");
require('winston-daily-rotate-file');
const CircularJSON = require("circular-json");
let cfg = require("./config");
const fs = require('fs');
const path = require('path');
// -------------------------------------
//      SETUP LOGGER with Winston
// -------------------------------------
// try to make some pretty output
const alignedWithColorsAndTime = winston.format.combine(
    winston.format.colorize(),
    winston.format.timestamp(),
    winston.format.align(),
    winston.format.printf(info => {
        const { timestamp, level, message, ...args } = info;
        const ts = timestamp.slice(0, 19).replace("T", " ");
        return `${ts} [${level}]: ${message} ${
            Object.keys(args).length ? CircularJSON.stringify(args, null, 2) : ""
            }`;
    })
);

const timestamp = () =>  {
    const d = new Date();
    return "-"+d.getDate() + d.getMonth()+d.getFullYear();
};

var transport = new (winston.transports.DailyRotateFile)({
    filename: 'application-%DATE%.log',
    datePattern: 'DD-MM-YYYY',
    zippedArchive: true,
    maxSize: '20m',
    maxFiles: '30d'
});

// Logger to be used in project
const logger = winston.createLogger({
    level: cfg.log.level,
    format: alignedWithColorsAndTime, 
    transports: [
        transport
        //new winston.transports.Http({ path: "log", port:3001 })
    ]
});

// Output stream to socket.io
logger.stream({ start: -1 }).on("log", function (log) {
    if (io !== null) {
        io.emit("log", log);
    }
});

logger.add(
    new winston.transports.Console({
        format: alignedWithColorsAndTime
    })
);

module.exports = logger;
