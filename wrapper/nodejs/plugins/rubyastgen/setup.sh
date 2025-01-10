#! /usr/bin/env bash

export GEM_HOME=.
bundle install --binstubs=../bin --no-cache --standalone=frontend
rm -rf bundle/ruby/3.4.0/bundler/gems/ruby_ast_gen-1c6e88faec50/.git*
rm -rf bundle/ruby/3.4.0/cache
