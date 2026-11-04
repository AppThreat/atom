import 'zone.js';
import 'core-js/es6/reflect';
import 'classlist.js';
import 'web-animations-js';
import { computed } from 'vue';
import type { Article } from 'flatted';
import * as ns from 'axios';
import {} from 'mitt';
export * from 'left-pad';
require('dot-prop');
const flat = require('flatted');
var pino = require('pino');

export const c = computed(() => 1);
export const a: Article = flat;
export const n = ns;
export const p = pino;
import vim = require('vim');
import ioredis = require('ioredis');

export const clients = [vim, ioredis];
