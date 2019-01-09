#!/usr/bin/env bash
envsubst < /opt/iot-engine/config/config_env.json > /opt/iot-engine/config/config.json
envsubst < /opt/iot-engine/config/log4j2_env.xml > /opt/iot-engine/config/log4j2.xml

exec /opt/iot-engine/bin/iot-engine-start.sh /opt/iot-engine/config/config.json
