const winston = require("winston");
const CircularJSON = require("circular-json");
// -------------------------------------
//      SETUP LOGGER with Winston
// -------------------------------------
// Logger to be used in project
const logger = winston.createLogger({
    level: "debug",
    format: winston.format.json(),
    transports: [
        new winston.transports.File({
            filename: "output.log",
            tailable: true,
            maxsize: 2000000,
            maxFiles: 1
        })
        //new winston.transports.Http({ path: "log", port:3001 })
    ]
});

// Output stream to socket.io
logger.stream({ start: -1 }).on("log", function (log) {
    if (io !== null) {
        io.emit("log", log);
    }
});

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
logger.add(
    new winston.transports.Console({
        format: alignedWithColorsAndTime
    })
);

module.exports = logger;