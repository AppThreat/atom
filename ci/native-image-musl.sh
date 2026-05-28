#!/usr/bin/env bash
# Build Linux musl native images in a musl-capable GraalVM container.
#
# Examples:
#   bash ci/native-image-musl.sh --arch amd64 --output target/graalvm-native-image/atom-linux-musl-amd64
#   bash ci/native-image-musl.sh --arch arm64 --output target/graalvm-native-image/atom-linux-musl-arm64

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

IMAGE="${ATOM_GRAALVM_MUSL_IMAGE:-ghcr.io/graalvm/native-image-community:25-muslib}"
NERDCTL_BIN_DEFAULT="$HOME/.rd/bin/nerdctl"
ENGINE="${ATOM_CONTAINER_ENGINE:-}"
ARCH=""
OUTPUT=""
VERIFY_BINARY=true

usage() {
  cat <<'EOF'
Usage: bash ci/native-image-musl.sh --arch <amd64|arm64> --output <path> [options]

Options:
  --arch <arch>         Target architecture (amd64 or arm64).
  --output <path>       Output binary path.
  --image <image>       Override build container image.
  --engine <binary>     Override container engine binary.
  --skip-verify         Skip post-build binary verification in Alpine.
  -h, --help            Show help.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --arch)
      ARCH="${2:-}"
      shift 2
      ;;
    --output)
      OUTPUT="${2:-}"
      shift 2
      ;;
    --image)
      IMAGE="${2:-}"
      shift 2
      ;;
    --engine)
      ENGINE="${2:-}"
      shift 2
      ;;
    --skip-verify)
      VERIFY_BINARY=false
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "${ARCH}" || -z "${OUTPUT}" ]]; then
  echo "Both --arch and --output are required." >&2
  usage
  exit 1
fi

if [[ "${ARCH}" != "amd64" && "${ARCH}" != "arm64" ]]; then
  echo "Unsupported arch '${ARCH}'. Expected amd64 or arm64." >&2
  exit 1
fi

if [[ -z "${ENGINE}" ]]; then
  if [[ -x "${NERDCTL_BIN_DEFAULT}" ]]; then
    ENGINE="${NERDCTL_BIN_DEFAULT}"
  elif command -v nerdctl >/dev/null 2>&1; then
    ENGINE="$(command -v nerdctl)"
  elif command -v docker >/dev/null 2>&1; then
    ENGINE="$(command -v docker)"
  elif command -v podman >/dev/null 2>&1; then
    ENGINE="$(command -v podman)"
  else
    echo "No supported container engine found (nerdctl/docker/podman)." >&2
    exit 1
  fi
fi

if [[ ! -x "${ENGINE}" ]]; then
  echo "Container engine '${ENGINE}' is not executable." >&2
  exit 1
fi

if [[ -z "${GITHUB_TOKEN:-}" && -n "${GH_TOKEN:-}" ]]; then
  export GITHUB_TOKEN="${GH_TOKEN}"
fi

if [[ -z "${GITHUB_TOKEN:-}" ]]; then
  # Some sbt tasks only require the token key to exist, not a valid credential.
  export GITHUB_TOKEN="dummy"
  echo "GITHUB_TOKEN was not set; falling back to 'dummy'." >&2
fi

mkdir -p "${REPO_ROOT}/target/graalvm-native-image"
mkdir -p "${REPO_ROOT}/.cache/native-image-musl"

SBT_CACHE_DIR="${REPO_ROOT}/.cache/native-image-musl/sbt"
IVY_CACHE_DIR="${REPO_ROOT}/.cache/native-image-musl/ivy2"
COURSIER_CACHE_DIR="${REPO_ROOT}/.cache/native-image-musl/coursier"
BOOT_CACHE_DIR="${REPO_ROOT}/.cache/native-image-musl/boot"

mkdir -p "${SBT_CACHE_DIR}" "${IVY_CACHE_DIR}" "${COURSIER_CACHE_DIR}" "${BOOT_CACHE_DIR}"

OUTPUT_ABS="${REPO_ROOT}/${OUTPUT}"

echo "Building linux/${ARCH} musl native image with ${ENGINE} using ${IMAGE}"

"${ENGINE}" run --rm \
  --platform "linux/${ARCH}" \
  --entrypoint /bin/bash \
  -e HOME=/tmp/home \
  -e GITHUB_TOKEN="${GITHUB_TOKEN}" \
  -e ATOM_GRAALVM_LIBC=musl \
  -e COURSIER_CACHE=/tmp/home/.cache/coursier \
  -v "${REPO_ROOT}:/workspace" \
  -v "${SBT_CACHE_DIR}:/tmp/home/.sbt" \
  -v "${IVY_CACHE_DIR}:/tmp/home/.ivy2" \
  -v "${COURSIER_CACHE_DIR}:/tmp/home/.cache/coursier" \
  -v "${BOOT_CACHE_DIR}:/tmp/home/.cache/sbt/boot" \
  -w /workspace \
  "${IMAGE}" \
  -lc '
    set -euo pipefail
    microdnf install -y git findutils tar gzip unzip which >/dev/null
    if [ ! -f /usr/local/bin/sbt-launch.jar ]; then
      curl -fsSL https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.11.7/sbt-launch-1.11.7.jar -o /usr/local/bin/sbt-launch.jar
    fi
    cat > /usr/local/bin/sbt <<"SBT"
#!/usr/bin/env bash
exec java -Xms512M -Xmx4G -jar /usr/local/bin/sbt-launch.jar "$@"
SBT
    chmod +x /usr/local/bin/sbt
    sbt "GraalVMNativeImage / packageBin"
  '

if [[ ! -f "${REPO_ROOT}/target/graalvm-native-image/atom" ]]; then
  echo "native-image build did not produce target/graalvm-native-image/atom" >&2
  exit 1
fi

cp "${REPO_ROOT}/target/graalvm-native-image/atom" "${OUTPUT_ABS}"
chmod +x "${OUTPUT_ABS}"

echo "Created ${OUTPUT}"

if [[ "${VERIFY_BINARY}" == true ]]; then
  echo "Verifying ${OUTPUT} in alpine..."
  "${ENGINE}" run --rm \
    --platform "linux/${ARCH}" \
    --entrypoint /bin/sh \
    -v "${REPO_ROOT}:/workspace" \
    -w /workspace \
    alpine:3.22 \
    -c "${OUTPUT} --help >/dev/null"
  echo "Verification passed."
fi
