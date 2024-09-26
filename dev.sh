#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate, comma-separated (e.g. profile1,profile2,profile3).
#   		'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Init arguments.
# If $1 is empty, set PROFILES to dev, otherwise append dev to $1.
PROFILES="dev"
if [ -n "$1" ]; then
  PROFILES="$1,dev"
fi

# Check if Quarkus console should be enabled.
CONSOLE=true
if [ "$TERM_PROGRAM" = tmux ]; then
  CONSOLE=false
  echo "Disabling Quarkus console in tmux."
fi

# Set environment variables.
echo "Using profiles: $PROFILES."

# Call starter script
cd esthesis-edge-backend/esthesis-edge-backend-impl && \
./mvnw quarkus:dev \
  -Dquarkus.console.enabled="$CONSOLE" \
  -Dquarkus.profile="$PROFILES" \
  -Dquarkus.http.port=9080 \
  -Ddebug=9081
