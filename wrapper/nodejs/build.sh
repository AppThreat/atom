#!/usr/bin/env bash
rm -rf plugins/bin plugins/lib
if [ -e "../../target/atom.zip" ]; then
    cp -rf ../../target/atom.zip plugins/
    unzip -q plugins/atom.zip -d plugins
    rm plugins/atom.zip
else
    echo "Build the atom project using 'sbt createDistribution' before running this script"
fi
php -r "copy('https://getcomposer.org/installer', 'composer-setup.php');"
php composer-setup.php
php -r "unlink('composer-setup.php');"
export COMPOSER_VENDOR_DIR=plugins
php composer.phar require nikic/php-parser:4.18.0 --ignore-platform-reqs --optimize-autoloader

cd plugins/rubyastgen
bash setup.sh
cd ../..

rm plugins/bin/racc plugins/bin/ruby-parse plugins/bin/ruby-rewrite

npm ci

rm composer.phar composer.json composer.lock
