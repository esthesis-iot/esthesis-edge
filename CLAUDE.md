# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

esthesis EDGE: a standalone Quarkus (Java 21) service that pulls data from third-party energy providers via pluggable modules, queues it as eLP entries in MariaDB, and syncs it to a local InfluxDB and/or pushes it to esthesis CORE over MQTT. Single code module: `esthesis-edge-backend/`. Docs sources in `esthesis-edge-docs/` (Writerside; build with its `build.sh`, uses Docker).

This repo is normally one of several sibling git repos in a single workspace directory (e.g. `~/git/esthesis/`), alongside `esthesis-bom`, `esthesis-common`, `esthesis-core`, etc.

## Build order (critical)

Nothing is published to a remote Maven repo. Install the sibling repos into local `~/.m2` first:

    cd ../esthesis-bom && ./mvnw clean install
    cd ../esthesis-common && ./mvnw clean install

## Commands

    cd _dev && docker compose up -d                  # dev infra: MariaDB :4306, InfluxDB :9086
    cd esthesis-edge-backend && ./dev.sh             # quarkus:dev on :9080 (debug :9081); optional extra profiles as first arg
    cd esthesis-edge-backend && ./mvnw clean install # build + tests

- `dev.sh` sources an optional git-ignored `local-env.sh` for per-developer env overrides (module toggles, CORE push URL, cron overrides).
- Tests use in-memory H2 (scheduler disabled) plus Testcontainers (hivemq, influxdb). Podman users: `export TESTCONTAINERS_RYUK_DISABLED=true`.
- Single test: `./mvnw test -Dtest=<TestClassName>`.

## Structure (`esthesis-edge-backend/src/main/java/esthesis/edge/`)

- `modules/{enedis,fronius,deddie}/` — the pluggable provider modules, each with `client`, `config`, `dto`, `resource`, `service`, `templates` (Qute self-registration pages).
- `services/` — `DeviceService`, `QueueService`, `SyncService`, `EsthesisCoreService`.
- `jobs/` — scheduled `SyncJob`, `PurgeJob`, `CoreRegistrationJob`.
- `clients/` — `EsthesisAgentServiceClient` (registration with CORE), `MqttPublisher` (data push).
- `security/` — `@AdminEndpoint` / `@ModuleEndpoint` request filters.

Persistence: MariaDB via Hibernate ORM Panache + Liquibase (`src/main/resources/db/changeLog.xml`, migrate-at-start). Config in `application.yaml` (+ `-dev`, `-test` variants).

## API access

Admin endpoints authenticate via the `X-ESTHESIS-EDGE-ADMIN-SECRET` header (secret in `application.yaml`). Swagger UI at `/api/openapi-ui`, OpenAPI at `/api/openapi`. Ready-made `.http` request files in `_dev/rest-test/`.

## Conventions

Conventional Commits (`feat:`, `fix:`, `chore:`, `doc:`). Lombok throughout. Quality gate is SonarQube via Jenkins (no local linter). Container publishing via `esthesis-edge-backend/publish.sh` (refuses to push SNAPSHOTs to docker.io).
