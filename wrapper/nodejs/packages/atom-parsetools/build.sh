#!/usr/bin/env bash
set -e
rm -rf plugins/bin

if command -v php >/dev/null 2>&1; then
  php -r "copy('https://getcomposer.org/installer', 'composer-setup.php');"
  php composer-setup.php
  php -r "unlink('composer-setup.php');"
  export COMPOSER_VENDOR_DIR=plugins
  php composer.phar require nikic/php-parser:5.7.0 --ignore-platform-reqs --optimize-autoloader
  rm composer.phar composer.json composer.lock
else
  echo "PHP plugins not built."
fi

if command -v ruby >/dev/null 2>&1 && command -v bundle >/dev/null 2>&1; then
  cd plugins/rubyastgen
  bash setup.sh
  cd ../..
  rm plugins/bin/racc plugins/bin/ruby-parse plugins/bin/ruby-rewrite
else
  echo "Ruby plugins not built."
fi
