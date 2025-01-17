copy ..\..\target\atom.zip plugins\
Expand-Archive -Path ..\..\target\atom.zip -DestinationPath plugins\ -Force
Remove-Item -Force plugins\atom.zip

php --php-ini php.ini -r "copy('http://getcomposer.org/installer', 'composer-setup.php');"
php -r "if (hash_file('sha384', 'composer-setup.php') === 'e21205b207c3ff031906575712edab6f13eb0b361f2085f1f1237b7126d785e826a450292b6cfd1d64d92e6563bbde02') { echo 'Installer verified'; } else { echo 'Installer corrupt'; unlink('composer-setup.php'); } echo PHP_EOL;"
php --php-ini php.ini composer-setup.php
php -r "unlink('composer-setup.php');"
$env:COMPOSER_VENDOR_DIR="plugins"
php --php-ini php.ini composer.phar require nikic/php-parser:4.18.0 --ignore-platform-reqs --optimize-autoloader

cd plugins\rubyastgen
.\setup.ps1
cd ..\..

Remove-Item -Force plugins\bin\racc* plugins\bin\ruby-parse* plugins\bin\ruby-rewrite*

npm ci

Remove-Item -Force composer.phar
Remove-Item -Force composer.json
Remove-Item -Force composer.lock
