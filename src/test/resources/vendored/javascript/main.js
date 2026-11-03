const lib = require("./third_party/lib");
const { run } = require("./third_party/lib");
const util = require("./src/not_third_party/util");
const fs = require("fs");

class Child extends lib.Base {}

lib.run(1);
run(2);
util.helper();
fs.readFileSync("x");
