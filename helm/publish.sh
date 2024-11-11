#!/usr/bin/env bash
####################################################################################################
# Publishes the esthesis EDGE Helm Chart to a container registry.
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
#   ESTHESIS_SKIP_COMPOSE_PUB:  Skips publishing the esthesis EDGE Docker Compose file to Docker Hub.
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

# If $ESTHESIS_REGISTRY_URL is empty, set it to Docker Hub.
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
  ESTHESIS_REGISTRY_URL="docker.io/esthesisiot"
fi

# If $ESTHESIS_SKIP_COMPOSE_PUB is empty, set it to false.
if [ -z "$ESTHESIS_SKIP_COMPOSE_PUB" ]; then
  ESTHESIS_SKIP_COMPOSE_PUB="false"
fi

# If $ESTHESIS_REGISTRY_TYPE is empty, set it to 'auth'.
if [ -z "$ESTHESIS_REGISTRY_TYPE" ]; then
  ESTHESIS_REGISTRY_TYPE="auth"
fi

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

# Find versions and check if they are snapshots.
CHART_VERSION=$(grep '^version:' Chart.yaml | awk '{print $2}' | tr -d '"')
APP_VERSION=$(grep '^appVersion:' Chart.yaml | awk '{print $2}' | tr -d '"')
echo "CHART_VERSION: $CHART_VERSION"
echo "APP_VERSION: $APP_VERSION"
if [[ "${CHART_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io/esthesisiot" ]]; then
    printError "Cannot push a snapshot Chart version to docker.io/esthesisiot."
    exit 1
fi
if [[ "${APP_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io/esthesisiot" ]]; then
    printError "Cannot push a snapshot App version to docker.io/esthesisiot."
    exit 1
fi

# Login to remote registry.
if [ "$ESTHESIS_REGISTRY_TYPE" = "aws" ]; then
	aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin "$ESTHESIS_REGISTRY_URL"
elif [ "$ESTHESIS_REGISTRY_TYPE" = "auth" ]; then
	helm registry login --username "$ESTHESIS_REGISTRY_USERNAME" --password "$ESTHESIS_REGISTRY_PASSWORD" "$ESTHESIS_REGISTRY_URL"
fi

# Push release-version chart.
printInfo "Publishing release-version Helm chart."
helm package .
printInfo "Pushing the Helm chart '$CHART_VERSION' to the registry."
helm push esthesis-edge-helm-"$CHART_VERSION".tgz oci://"$ESTHESIS_REGISTRY_URL"
#rm esthesis-edge-helm-"$CHART_VERSION".tgz

printInfo "Publishing latest-version Helm chart."
find . -name Chart.yaml -print0 | xargs -0 "$SED" -i "0,/version:/s|version:.*$|version: latest|"
helm package .
printInfo "Pushing the Helm chart 'latest' to the registry."
helm push esthesis-edge-helm-latest.tgz oci://"$ESTHESIS_REGISTRY_URL"
#rm esthesis-edge-helm-"latest".tgz

# Switch chart version back to release.
find . -name Chart.yaml -print0 | xargs -0 "$SED" -i "0,/version:/s|version:.*$|version: \"$RELEASE_VERSION\"|"


