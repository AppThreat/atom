#!/usr/bin/env bash

pip install "betterproto[compiler]"
curl -LO https://github.com/join-com/protoc-gen-ts/releases/download/0.7.1/protoc-gen-ts.linux.amd64
chmod +x protoc-gen-ts.linux.amd64
mv protoc-gen-ts.linux.amd64 /usr/local/bin/protoc-gen-ts

protoc -I . --python_betterproto_out=bindings/python atom.proto
protoc -I . --ts_out=bindings/ts atom.proto
protoc -I . --java_out=bindings/java atom.proto
protoc -I . --go_out=bindings/go atom.proto
