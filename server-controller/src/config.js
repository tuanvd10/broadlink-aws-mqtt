
const config = require("config");
const cfg = config.util.toObject();
if (process.env.DOCKER && process.env.DOCKER === "true") {
    //  process.env["NODE_CONFIG_DIR"] = "/config";
    const cfgLocal = config.util.loadFileConfigs("../config");
    if (cfgLocal) {
        cfg = Object.assign({}, cfg, cfgLocal);
    }
}
module.exports = cfg;