{ pkgs, lib, inputs, config, ... }: {
  # Language-specific topions
  options = {
    profile = lib.mkOption {
      type = lib.types.enum [ "android" "ruby" "php" "c" "cplusplus" "basic" ];
      default = "basic";
      description = "Development profile to use";
    };
  };
  config = {
      android = {
        enable = lib.mkIf (config.profile == "android") true;
      };
      languages = {
        python = {
          enable = true;
          venv.enable = true;
          version = "3.13";
        };
        javascript = {
          enable = true;
          package = pkgs.nodejs_23;
          npm.enable = true;
        };
        java = {
          enable = true;
          jdk.package = pkgs.jdk23;
        };
        ruby = {
          enable = lib.mkIf (config.profile == "ruby") true;
          version = "3.4.4";
        };
        c = {
          enable = lib.mkIf (config.profile == "c") true;
        };
        cplusplus = {
          enable = lib.mkIf (config.profile == "cplusplus") true;
        };
        scala = {
          enable = true;
          sbt.enable = true;
        };
        php = {
          enable = lib.mkIf (config.profile == "php") true;
          extensions = [
            "openssl"
            "zip"
          ];
          packages = {
            composer = pkgs.phpPackages.composer;
          };
        };
      };
      packages = [
        pkgs.nodejs_23
        pkgs.python313Full
        (pkgs.python313.withPackages (ps: [ ps.pip ps.setuptools ]))
        pkgs.sbt
        pkgs.php
      ];
      # Useful features
      devcontainer.enable = true;
      difftastic.enable = true;
      # Compile atom and plugins
      # Setup the latest cdxgen
      enterShell = ''
        set -e
        mkdir -p /tmp/empty-cache
        export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 --enable-native-access=ALL-UNNAMED"
        sudo npm install --cache /tmp/empty-cache --prefer-online -g @cyclonedx/cdxgen
        cdxgen --version
        python3 --version
        python3 -m pip install atom-tools
        sbt clean stage createDistribution publishLocal
        cd wrapper/nodejs
        bash build.sh
        sudo npm install --cache /tmp/empty-cache --prefer-online -g .
        cd ../..
        which atom
        which astgen
        which phpastgen
        which rbastgen
        which scalasem
        which atom-tools
      '';

      # Tasks
  };
}
