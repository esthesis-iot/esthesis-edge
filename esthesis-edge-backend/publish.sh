#!/usr/bin/env bash

####################################################################################################
# Publishes esthesis-edge to a container registry.
#
# Environment variables:
#   ESTHESIS_REGISTRY_URL: 			The URL of the registry to push to
#   														(default: docker.io/esthesisiot).
#  	ESTHESIS_REGISTRY_TYPE:			aws: Login will be attempted using 'aws ecr-public get-login-password'.
#  															auth: Login will be attempted using username and password.
#  															open:	No login will be attempted.
#  															(default: auth).
#   ESTHESIS_REGISTRY_USERNAME:	The username to login to the 'auth' type registry.
#   ESTHESIS_REGISTRY_PASSWORD:	The password to login to the 'auth' type registry.
#   ESTHESIS_ARCHITECTURES: 		The architectures to build, e.g. linux/amd64,linux/arm64
#   														(default: linux/amd64,linux/arm64).
#   ESTHESIS_LOCAL_BUILD: 			If set to false, individual modules are not built. This is helpful
#																when this script is used as part of another script (for example,
#																a release script) which already performs a build (default: true).
#
# Usage examples:
#   ./publish.sh
#   ESTHESIS_REGISTRY_TYPE=open ESTHESIS_REGISTRY_URL=192.168.50.211:5000/esthesis ./publish.sh
####################################################################################################

# Trap exit.
set -e
exit_handler() {
    printError "Build failed with exit code $?"
    if [ -n "$BUILDX_NAME" ]; then
			printInfo "Deleting Docker buildx $BUILDX_NAME."
			docker buildx rm "$BUILDX_NAME"
		fi
		exit 1
}
trap exit_handler ERR

# Helper functions to print messages.
printError() {
	printf "\e[31m***ERROR: $1\e[0m\n"
}
printInfo() {
	printf "\e[32m***INFO: $1\e[0m\n"
}

# Check if Podman is installed.
if [ -x "$(command -v podman)" ]; then
    # Check if TESTCONTAINERS_RYUK_DISABLED is set
    if [ -z "$TESTCONTAINERS_RYUK_DISABLED" ]; then
        printInfo "Setting TESTCONTAINERS_RYUK_DISABLED=true due to Podman being detected."
        export TESTCONTAINERS_RYUK_DISABLED=true
    fi
fi

# If $ESTHESIS_REGISTRY_URL is empty, set it to aws.
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
  ESTHESIS_REGISTRY_URL="docker.io/esthesisiot"
fi

# If $ESTHESIS_REGISTRY_TYPE is empty, set it to aws.
if [ -z "$ESTHESIS_REGISTRY_TYPE" ]; then
  ESTHESIS_REGISTRY_TYPE="auth"
fi

# If $ESTHESIS_LOCAL_BUILD is empty, set it to true.
if [ -z "$ESTHESIS_LOCAL_BUILD" ]; then
  ESTHESIS_LOCAL_BUILD="true"
fi

# Set MVNW location.
MVNW="$(pwd)/mvnw"
printInfo "Using $MVNW."

# Check mandatory environment variables.
if [ "$ESTHESIS_REGISTRY_TYPE" = "auth" ]; then
	if [ -z "$ESTHESIS_REGISTRY_USERNAME" ]; then
			printError "ESTHESIS_REGISTRY_USERNAME is not set."
			exit 1
  fi
  if [ -z "$ESTHESIS_REGISTRY_PASSWORD" ]; then
			printError "ESTHESIS_REGISTRY_PASSWORD is not set."
			exit 1
	fi
fi

MAVEN_OPTIMISE_PARAMS="-DskipTests -Dmaven.test.skip=true -T 1C"

# Find the version of the package.
PACKAGE_VERSION=$(grep -A 1 '<artifactId>esthesis-edge</artifactId>' pom.xml | grep '<version>' | sed -E 's/.*<version>(.*)<\/version>.*/\1/')
if [ -z "$PACKAGE_VERSION" ]; then
    printError "Could not set PACKAGE_VERSION."
    exit 1
fi
printInfo "Package version: $PACKAGE_VERSION."
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io/esthesisiot" ]]; then
    printError "Cannot push a snapshot version to docker.io/esthesisiot."
    exit 1
fi

# Architectures to build, i.e. "linux/amd64,linux/arm64"
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64,linux/arm64"
fi

# Create a Docker buildx.
BUILDX_NAME=$(LC_CTYPE=C tr -dc 'a-zA-Z' < /dev/urandom | head -c 1)$(LC_CTYPE=C tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 11)
printInfo "Creating Docker buildx $BUILDX_NAME."
if test -f "buildkitd.toml"; then
    BUILDKIT_CONFIG="--config buildkitd.toml"
else
		BUILDKIT_CONFIG=""
fi
docker buildx create --name "$BUILDX_NAME" --use $BUILDKIT_CONFIG

# Login to remote registry.
if [ "$ESTHESIS_REGISTRY_TYPE" = "aws" ]; then
	aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin $ESTHESIS_REGISTRY_URL
elif [ "$ESTHESIS_REGISTRY_TYPE" = "auth" ]; then
	docker login "$ESTHESIS_REGISTRY_URL" --username "$ESTHESIS_REGISTRY_USERNAME" --password "$ESTHESIS_REGISTRY_PASSWORD"
fi

# Build and push the image.
IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesis-edge"
printInfo "Building source code."
$MVNW clean package "$MAVEN_OPTIMISE_PARAMS"
printInfo "Building $ESTHESIS_ARCHITECTURES for $IMAGE_NAME."
docker buildx build \
			-f src/main/docker/Dockerfile.jvm \
			--platform "$ESTHESIS_ARCHITECTURES" \
			-t "$IMAGE_NAME:$PACKAGE_VERSION" \
			-t "$IMAGE_NAME:latest" \
			--push .

# Delete the buildx.
printInfo "Deleting Docker buildx $BUILDX_NAME."
docker buildx rm "$BUILDX_NAME"
