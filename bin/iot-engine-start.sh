#!/usr/bin/env bash

if [ $# -lt 1 ];
then
        echo "USAGE: $0 config.json"
        exit 1
fi

CURRENT=`pwd` && cd `dirname $0` && SOURCE=`pwd` && cd ${CURRENT} && PARENT=`dirname ${SOURCE}`

CLASSPATH=${CLASSPATH}:${PARENT}/config
for file in ${PARENT}/lib/*.jar;
do
    CLASSPATH=${CLASSPATH}:${file}
done

exec java ${JVM_OPTIONS} -cp ${CLASSPATH} kafkastreams.iot.IotEngine $1
