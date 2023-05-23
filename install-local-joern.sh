#!/usr/bin/env bash
set -o errexit
set -o pipefail
set -o nounset

# extract joern_version from build.sbt - parsing just like in project/Versions.scala
readonly JOERN_VERSION=$(grep 'val joernVersion = "' build.sbt | sed 's/.*"\(.*\)"/\1/')

# get script location, use as a root dir for this script
if [ "$(uname)" = 'Darwin' ]; then
  # https://unix.stackexchange.com/a/96238
  if [ "${BASH_SOURCE:-x}" != 'x' ]; then
    this_script=$BASH_SOURCE
  elif [ "${ZSH_VERSION:-x}" != 'x' ]; then
    setopt function_argzero
    this_script=$0
  elif eval '[[ -n ${.sh.file} ]]' 2>/dev/null; then
    eval 'this_script=${.sh.file}'
  else
    echo 1>&2 "Unsupported shell. Please use bash, ksh93 or zsh."
    exit 2
  fi
  relative_directory=$(dirname "$this_script")
  SCRIPT_ABS_DIR=$(cd "$relative_directory" && pwd)
else
  SCRIPT_ABS_PATH=$(readlink -f "$0")
  SCRIPT_ABS_DIR=$(dirname "$SCRIPT_ABS_PATH")
fi

# Check required tools are installed.
check_installed() {
  if ! type "$1" > /dev/null; then
    echo "Please ensure you have $1 installed."
    exit 1
  fi
}

readonly JOERN_INSTALL="$SCRIPT_ABS_DIR/joern-inst"

if [ -d "${JOERN_INSTALL}" ]; then
    echo "found existing local joern installation in $JOERN_INSTALL"
    echo "should we wipe it and start fresh? [y/N]"
    read ANSWER
    if [ ! -z $ANSWER ]; then
      if [ "y" == $ANSWER ] || [ "Y" == $ANSWER ]; then
        rm -rf "$JOERN_INSTALL"
      fi
    fi
fi

if [ ! -d "${JOERN_INSTALL}" ]; then
  echo "downloading and installing joern $JOERN_VERSION..."
  check_installed "curl"

  # Fetch installer
  echo "https://github.com/joernio/joern/releases/download/v$JOERN_VERSION/joern-install.sh"
  curl -L "https://github.com/joernio/joern/releases/download/v$JOERN_VERSION/joern-install.sh" -o "$SCRIPT_ABS_DIR/joern-install.sh"

  # Install into `joern-inst`
  chmod +x $SCRIPT_ABS_DIR/joern-install.sh
  $SCRIPT_ABS_DIR/joern-install.sh --install-dir="$JOERN_INSTALL" --version=v$JOERN_VERSION --without-plugins
  rm $SCRIPT_ABS_DIR/joern-install.sh
  rm joern-cli.zip
fi

readonly JAR_INSTALL_DIR=${JOERN_INSTALL}/joern-cli/lib/

echo "Building extension"
sbt clean stage

echo "Installing jars into: ${JAR_INSTALL_DIR}"
rm ${JAR_INSTALL_DIR}/io.shiftleft.codepropertygraph-domain-classes*
cp target/universal/stage/lib/io.appthreat.atom-* ${JAR_INSTALL_DIR}

echo "All done, you're ready to go in $JOERN_INSTALL"
