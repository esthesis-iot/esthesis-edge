#!/usr/bin/env bash
docker rm writerside-esthesis-edge
docker run  \
	-v .:/opt/sources \
	--name writerside-esthesis-edge \
	jetbrains/writerside-builder:242.21870 \
	/bin/bash -c "
	export DISPLAY=:99 &&
	Xvfb :99 &
	/opt/builder/bin/idea.sh helpbuilderinspect \
	--source-dir /opt/sources \
	--product edge-docs/esthesis-edge \
	--runner other \
	--output-dir /opt/target
	"
rm -rf web-archive
mkdir web-archive
docker cp writerside-esthesis-edge:/opt/target/. web-archive
cd web-archive || exit
find . -name "*.zip" -exec unzip -o -d website {} \;
