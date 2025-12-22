FROM ghcr.io/appthreat/base-al10:main

LABEL maintainer="appthreat" \
      org.opencontainers.image.authors="Team AppThreat <cloud@appthreat.com>" \
      org.opencontainers.image.source="https://github.com/appthreat/atom" \
      org.opencontainers.image.url="https://github.com/appthreat/atom" \
      org.opencontainers.image.version="2.4.x" \
      org.opencontainers.image.vendor="appthreat" \
      org.opencontainers.image.licenses="MIT" \
      org.opencontainers.image.title="atom" \
      org.opencontainers.image.description="Container image for AppThreat atom" \
      org.opencontainers.docker.cmd="docker run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -it ghcr.io/appthreat/atom atom -o /app/app.atom -l java /app"

ARG JAVA_VERSION=23.0.2-tem
ARG MAVEN_VERSION=3.9.10
ARG SCALA_VERSION=3.7.4
ARG SBT_VERSION=1.11.7
ARG RUBY_VERSION=3.4.8

ENV MAVEN_VERSION=$MAVEN_VERSION \
    MAVEN_HOME="/opt/maven/${MAVEN_VERSION}" \
    SCALA_VERSION=$SCALA_VERSION \
    SBT_VERSION=$SBT_VERSION \
    SCALA_HOME="/opt/scala/${SCALA_VERSION}" \
    SBT_HOME="/opt/sbt/${SBT_VERSION}" \
    ANDROID_HOME=/opt/android-sdk-linux \
    JAVA_VERSION=$JAVA_VERSION \
    JAVA_HOME="/opt/java/${JAVA_VERSION}" \
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -XX:MinRAMPercentage=30 -XX:MaxRAMPercentage=90" \
    PHP_PARSER_BIN=/opt/vendor/bin/php-parse \
    PYTHONUNBUFFERED=1 \
    PYTHONIOENCODING="utf-8" \
    CDXGEN_NO_BANNER=true \
    COMPOSER_ALLOW_SUPERUSER=1 \
    RUBY_VERSION=$RUBY_VERSION

ENV PATH=/opt/bin:/opt/vendor/bin:${PATH}:${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${SCALA_HOME}/bin:${SBT_HOME}/bin:/usr/local/bin/:/root/.local/bin:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:/root/.rbenv/bin:/root/.rbenv/versions/3.4.8/bin:

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
    microdnf install -y gcc git-core php php-cli php-curl php-zip php-bcmath php-json php-pear php-mbstring php-devel make \
        python3 python3-devel python3-pip \
        wget bash glibc-common glibc-all-langpacks \
        openssl-devel libffi-devel readline-devel libyaml zlib-devel ncurses ncurses-devel \
        pcre2 findutils which tar gzip zip unzip sudo nodejs rust \
    && microdnf install -y epel-release \
    && microdnf install --enablerepo=crb -y libyaml-devel \
    && curl -s "https://get.sdkman.io" | bash \
    && source "/root/.sdkman/bin/sdkman-init.sh" \
    && echo -e "sdkman_auto_answer=true\nsdkman_selfupdate_feature=false\nsdkman_auto_env=true\nsdkman_curl_connect_timeout=20\nsdkman_curl_max_time=0" >> /root/.sdkman/etc/config \
    && sdk install java $JAVA_VERSION \
    && sdk install maven $MAVEN_VERSION \
    && sdk install scala $SCALA_VERSION \
    && sdk install sbt $SBT_VERSION \
    && sdk offline enable \
    && mv /root/.sdkman/candidates/* /opt/ \
    && rm -rf /root/.sdkman \
    && java --version \
    && scalac --version \
    && sbt --version \
    && git clone https://github.com/rbenv/rbenv.git --depth=1 ~/.rbenv \
    && echo 'export PATH="/root/.rbenv/bin:$PATH"' >> ~/.bashrc \
    && echo 'eval "$(/root/.rbenv/bin/rbenv init - bash)"' >> ~/.bashrc \
    && source /root/.bashrc \
    && mkdir -p "$(rbenv root)/plugins" \
    && git clone https://github.com/rbenv/ruby-build.git --depth=1 "$(rbenv root)/plugins/ruby-build" \
    && rbenv install ${RUBY_VERSION} -- --disable-install-doc \
    && rbenv global ${RUBY_VERSION} \
    && ruby --version \
    && which ruby \
    && rm -rf /root/.rbenv/cache $RUBY_BUILD_BUILD_PATH \
    && python3 --version \
    && node --version \
    && python3 -m pip install --upgrade pip atom-tools>=0.8.2 \
    && atom-tools --version && atom-tools --help \
    && mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && curl -L https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -o ${ANDROID_HOME}/cmdline-tools/android_tools.zip \
    && unzip ${ANDROID_HOME}/cmdline-tools/android_tools.zip -d ${ANDROID_HOME}/cmdline-tools/ \
    && rm ${ANDROID_HOME}/cmdline-tools/android_tools.zip \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && yes | /opt/android-sdk-linux/cmdline-tools/latest/bin/sdkmanager --licenses --sdk_root=/opt/android-sdk-linux \
    && /opt/android-sdk-linux/cmdline-tools/latest/bin/sdkmanager 'platform-tools' --sdk_root=/opt/android-sdk-linux \
    && /opt/android-sdk-linux/cmdline-tools/latest/bin/sdkmanager 'platforms;android-34' --sdk_root=/opt/android-sdk-linux \
    && /opt/android-sdk-linux/cmdline-tools/latest/bin/sdkmanager 'build-tools;34.0.0' --sdk_root=/opt/android-sdk-linux \
    && npm install -g @cyclonedx/cdxgen --omit=optional \
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
    && cd /opt/nodejs && npm install --omit=dev && cd packages/atom-parsetools && bash build.sh && npm install -g . && cd /opt \
    && rm -rf atom.zip composer.json composer.lock composer-setup.php /usr/local/bin/atom \
    && /opt/bin/atom --help \
    && which astgen \
    && which phpastgen \
    && which rbastgen \
    && rbastgen --help \
    && microdnf clean all

WORKDIR /app

CMD ["/opt/bin/atom"]
