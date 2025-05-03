#! /usr/bin/env bash
set -e
export GEM_HOME=.
bundle install --binstubs=../bin --no-cache --standalone=frontend
rm -rf bundle/ruby/*/bundler/gems/ruby_ast_gen-*/.git*
rm -rf bundle/ruby/*/cache
