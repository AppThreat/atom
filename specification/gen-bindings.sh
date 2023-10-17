#!/usr/bin/env bash

pip install "betterproto[compiler]"
curl -LO https://github.com/join-com/protoc-gen-ts/releases/download/2.3.0/protoc-gen-tsx.linux.amd64
chmod +x protoc-gen-tsx.linux.amd64
sudo mv protoc-gen-tsx.linux.amd64 /usr/local/bin/protoc-gen-tsx

protoc -I . --python_betterproto_out=bindings/python atom.proto
protoc -I . --tsx_out=bindings/ts atom.proto
protoc -I . --java_out=bindings/java atom.proto
protoc -I . --go_out=bindings/go atom.proto

cargo install protobuf-codegen
protoc -I . --rust_out=bindings/rust atom.proto
