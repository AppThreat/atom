FROM ghcr.io/appthreat/base:main

LABEL maintainer="appthreat" \
      org.opencontainers.image.authors="Team AppThreat <cloud@appthreat.com>" \
      org.opencontainers.image.source="https://github.com/appthreat/atom" \
      org.opencontainers.image.url="https://github.com/appthreat/atom" \
      org.opencontainers.image.version="2.0.x" \
      org.opencontainers.image.vendor="appthreat" \
      org.opencontainers.image.licenses="Apache-2.0" \
      org.opencontainers.image.title="atom" \
      org.opencontainers.image.description="Container image for AppThreat atom" \
      org.opencontainers.docker.cmd="docker run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -it ghcr.io/appthreat/atom atom -o /app/app.atom -l java /app"

ARG MAVEN_VERSION=3.9.8

ENV MAVEN_VERSION=$MAVEN_VERSION \
    MAVEN_HOME="/opt/maven/${MAVEN_VERSION}" \
    ANDROID_HOME=/opt/android-sdk-linux \
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" \
    PHP_PARSER_BIN=/opt/vendor/bin/php-parse \
    PYTHONUNBUFFERED=1 \
    PYTHONIOENCODING="utf-8" \
    CDXGEN_NO_BANNER=true \
    COMPOSER_ALLOW_SUPERUSER=1

ENV PATH=/opt/bin:/opt/vendor/bin:${PATH}:${MAVEN_HOME}/bin:/usr/local/bin/:/root/.local/bin:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:

WORKDIR /opt

RUN set -e; \
    ARCH_NAME="$(rpm --eval '%{_arch}')"; \
    url=; \
    case "${ARCH_NAME##*-}" in \
        'x86_64') \
            OS_ARCH_SUFFIX=''; \
            ;; \
        'aarch64') \
            OS_ARCH_SUFFIX='-aarch64'; \
            ;; \
        *) echo >&2 "error: unsupported architecture: '$ARCH_NAME'"; exit 1 ;; \
    esac; \
    echo -e "[nodejs]\nname=nodejs\nstream=20\nprofiles=\nstate=enabled\n" > /etc/dnf/modules.d/nodejs.module \
    && microdnf install -y gcc git-core php php-cli php-curl php-zip php-bcmath php-json php-pear php-mbstring php-devel make \
        python3.12 python3.12-devel python3.12-pip \
        wget bash glibc-common glibc-all-langpacks java-21-openjdk-headless \
        pcre2 findutils which tar gzip zip unzip sudo nodejs \
    && alternatives --install /usr/bin/python3 python /usr/bin/python3.12 10 \
    && alternatives --install /usr/bin/python3 python3 /usr/bin/python3.12 10 \
    && python3 --version \
    && node --version \
    && python3 -m pip install --upgrade pip poetry atom-tools \
    && curl -s "https://get.sdkman.io" | bash \
    && source "$HOME/.sdkman/bin/sdkman-init.sh" \
    && echo -e "sdkman_auto_answer=true\nsdkman_selfupdate_feature=false\nsdkman_auto_env=true\nsdkman_curl_connect_timeout=20\nsdkman_curl_max_time=0" >> $HOME/.sdkman/etc/config \
    && sdk install maven $MAVEN_VERSION \
    && sdk offline enable \
    && mv /root/.sdkman/candidates/* /opt/ \
    && rm -rf /root/.sdkman \
    && mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && curl -L https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -o ${ANDROID_HOME}/cmdline-tools/android_tools.zip \
    && unzip ${ANDROID_HOME}/cmdline-tools/android_tools.zip -d ${ANDROID_HOME}/cmdline-tools/ \
    && rm ${ANDROID_HOME}/cmdline-tools/android_tools.zip \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && yes | /opt/android-sdk-linux/cmdline-tools/latest/bin/sdkmanager --licenses --sdk_root=/opt/android-sdk-linux \
    && /opt/android-sdk-linux/cmdline-tools/latest/bin/sdkmanager 'platform-tools' --sdk_root=/opt/android-sdk-linux \
    && /opt/android-sdk-linux/cmdline-tools/latest/bin/sdkmanager 'platforms;android-34' --sdk_root=/opt/android-sdk-linux \
    && /opt/android-sdk-linux/cmdline-tools/latest/bin/sdkmanager 'build-tools;34.0.0' --sdk_root=/opt/android-sdk-linux \
    && sudo npm install -g @cyclonedx/cdxgen --omit=optional \
    && php -r "copy('https://getcomposer.org/installer', 'composer-setup.php');" && php composer-setup.php \
    && mv composer.phar /usr/local/bin/composer \
    && curl -LO https://raw.githubusercontent.com/AppThreat/chen/main/platform/frontends/php2atom/composer.lock \
    && curl -LO https://raw.githubusercontent.com/AppThreat/chen/main/platform/frontends/php2atom/composer.json

ENV LC_ALL=en_US.UTF-8 \
    LANG=en_US.UTF-8 \
    LANGUAGE=en_US.UTF-8
COPY ./target/atom.zip .
COPY ./wrapper .
RUN unzip -q atom.zip \
    && composer update --no-progress --prefer-dist --ignore-platform-reqs \
    && cd /opt/nodejs && npm install --only=production && cd /opt \
    && sudo npm install -g /opt/nodejs \
    && rm -rf atom.zip composer.json composer.lock composer-setup.php /usr/local/bin/atom \
    && /opt/bin/atom --help \
    && which astgen \
    && which phpastgen \
    && microdnf clean all

CMD ["/opt/bin/atom"]
