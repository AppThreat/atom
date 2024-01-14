FROM almalinux:9.3-minimal

LABEL maintainer="appthreat" \
      org.opencontainers.image.authors="Team AppThreat <cloud@appthreat.com>" \
      org.opencontainers.image.source="https://github.com/appthreat/chen" \
      org.opencontainers.image.url="https://github.com/appthreat/atom" \
      org.opencontainers.image.version="1.8.x" \
      org.opencontainers.image.vendor="appthreat" \
      org.opencontainers.image.licenses="Apache-2.0" \
      org.opencontainers.image.title="atom" \
      org.opencontainers.image.description="Container image for AppThreat atom" \
      org.opencontainers.docker.cmd="docker run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -it ghcr.io/appthreat/atom"

ARG MAVEN_VERSION=3.9.6
ARG JAVA_VERSION=21.0.1-graalce

ENV JAVA_VERSION=$JAVA_VERSION \
    MAVEN_VERSION=$MAVEN_VERSION \
    JAVA_HOME="/opt/java/${JAVA_VERSION}" \
    MAVEN_HOME="/opt/maven/${MAVEN_VERSION}" \
    JAVA_OPTS="-XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:+UnlockDiagnosticVMOptions -XX:G1SummarizeRSetStatsPeriod=1" \
    ANDROID_HOME=/opt/android-sdk-linux \
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" \
    PHP_PARSER_BIN=/opt/vendor/bin/php-parse \
    COMPOSER_ALLOW_SUPERUSER=1

ENV PATH=/opt/bin:/opt/vendor/bin:${PATH}:${JAVA_HOME}/bin:${MAVEN_HOME}/bin:/usr/local/bin/:/root/.local/bin:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:

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
        wget bash glibc-common glibc-all-langpacks \
        pcre2 findutils which tar gzip zip unzip sudo nodejs \
    && curl -s "https://get.sdkman.io" | bash \
    && source "$HOME/.sdkman/bin/sdkman-init.sh" \
    && echo -e "sdkman_auto_answer=true\nsdkman_selfupdate_feature=false\nsdkman_auto_env=true\nsdkman_curl_connect_timeout=20\nsdkman_curl_max_time=0" >> $HOME/.sdkman/etc/config \
    && sdk install java $JAVA_VERSION \
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

ENTRYPOINT ["/opt/bin/atom"]
