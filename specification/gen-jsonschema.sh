#!/usr/bin/env bash
# curl -LO https://github.com/protocolbuffers/protobuf/releases/download/v23.3/protoc-23.3-linux-x86_64.zip
# unzip protoc-23.3-linux-x86_64.zip

go install github.com/chrusty/protoc-gen-jsonschema/cmd/protoc-gen-jsonschema@latest

protoc \
  --plugin=${HOME}/go/bin/protoc-gen-jsonschema \
  --jsonschema_opt=enforce_oneof \
  --jsonschema_opt=disallow_additional_properties \
  --jsonschema_out=schemas \
  ./atom.proto

cd schemas
pip install jsonschema2md
for i in `ls *.json`; do jsonschema2md $i $i.md; done