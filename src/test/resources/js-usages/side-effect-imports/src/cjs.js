module.exports = require('express');
exports.router = require('koa-router');
require('debug')('app');
async function load() {
  await import('pino-pretty');
}
if (process.env.TRACE) require('why-is-node-running');
register(require('ts-node'));
const maybe = process.env.X ? require('optional-dep') : null;
var _local = require('underscored');
require('dotenv').config();
module.exports.maybe = maybe;
module.exports.local = _local;
const config_1 = __importDefault(require("config"));
module.exports.config = config_1;
config_1.default.get('x');
const ns_1 = __importStar(require("ns-pkg"));
ns_1.pick({}, 'y');
