FROM openjdk:8-jre-slim

MAINTAINER andresgomezfrr@gmail.com

RUN apt-get update -y && apt-get install -y gettext-base && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /var/log/iot-engine

ADD iot-engine-dist.tar.gz /opt/
RUN rm -f /opt/iot-engine/config/*

ADD log4j2_env.xml /opt/iot-engine/config/
ADD config_env.json /opt/iot-engine/config/
ADD iot-engine-start.sh /bin

ENV APPLICATION_ID=iot-engine-app \
    NUM_STREAM_THREADS=1 \
    TOPIC_DATA=data \
    TOPIC_ALERT=alerts \
    TOPIC_RULE=rules \
    TOPIC_AGG=agg-metrics \
    LOG_LEVEL=info \
    REST_SERVER_ENDPOINT="0.0.0.0:5577" \
    GENERAL_LOG_LEVEL=info \
    JVM_OPTIONS="-Xmx512m -Xms512m"

CMD exec /bin/iot-engine-start.sh


