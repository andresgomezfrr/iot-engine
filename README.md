# iot-engine
IoT Engine using Kafka Streams.

## Execute

```bash
bin/kafka-topics.sh --create --partitions 20 --zookeeper localhost:2181 --replication-factor 1 --topic sensor-iot-agg-metrics
bin/kafka-topics.sh --create --partitions 20 --zookeeper localhost:2181 --replication-factor 1 --topic sensor-iot
bin/kafka-topics.sh --create --partitions 20 --zookeeper localhost:2181 --replication-factor 1 --topic iot-rules
bin/kafka-topics.sh --create --partitions 20 --zookeeper localhost:2181 --replication-factor 1 --topic sensor-iot-alerts
bin/kafka-topics.sh --create --partitions 20 --zookeeper localhost:2181 --replication-factor 1 --topic sensor-metadata
```

```bash
docker run -it -p 5574:5574 -e BOOTSTRAP_SERVERS=192.168.0.33:9092 -e APPLICATION_ID=iot-engine -e REST_SERVER_ENDPOINT=0.0.0.0:5574 -e TOPIC_AGG=sensor-iot-agg-metrics -e TOPIC_DATA=sensor-iot -e TOPIC_RULE=iot-rules -e TOPIC_ALERT=sensor-iot-alerts -e APPLICATION_SERVER=192.168.0.33:5574 iot-engine:latest
```

## Data Format

### Input Data Message Format

* [IotDataMessage](https://github.com/andresgomezfrr/iot-engine/blob/master/service/src/main/java/kafkastreams/iot/model/IotDataMessage.java)

| Field  | Description  | Type  | Example |
|---|---|---|---|
|  id |  The ID to identify a speficic IoT sensor | STRING  |  `"A"` |   
|  timestamp | The unix timestamp in seconds  |  LONG | `123456789` |   
|  metrics |  Databag with the IoT metrics |  JSON_OBJECT<STRING, INTEGER> | `{"metric1":22, "metric2": 90}` |   

Full example:

```json
{
  "id": "A",
  "timestamp": 123456789,
  "metrics": {
    "hum": 49,
    "temp": 34
  }
}
```

### Rules Message Format

* [IotSensorRules](https://github.com/andresgomezfrr/iot-engine/blob/master/service/src/main/java/kafkastreams/iot/model/IotSensorRules.java)

| Field  | Description  | Type  | Example |
|---|---|---|---|
|  id |  The ID to identify a speficic IoT sensor | STRING  |  `"A"` |   
|  rules | Rules to verify by IoT sensor |  ARRAY[[IotSensorRule](https://github.com/andresgomezfrr/iot-engine/blob/master/service/src/main/java/kafkastreams/iot/model/IotSensorRule.java)] | `[{"ruleName":"min_humidity","metricName":"hum","metricValue":50,"condition":"<"}]`|   

Full example:

```json
{
  "id": "A",
  "rules": [
    {
      "ruleName": "max_temperature",
      "metricName": "temp",
      "metricValue": 22,
      "condition": ">"
    },
    {
      "ruleName": "min_humidity",
      "metricName": "hum",
      "metricValue": 50,
      "condition": "<"
    }
  ]
}
```

* [IotSensorRule](https://github.com/andresgomezfrr/iot-engine/blob/master/service/src/main/java/kafkastreams/iot/model/IotSensorRule.java)

| Field  | Description  | Type  | Example |
|---|---|---|---|
|  id |  The ID to identify a speficic IoT sensor | STRING  |  `"A"` |   
|  ruleName |  Rule name's. It is used to indetify the rule | STRING  |  `"Min humidity reached!"` |   
|  metricName |  The metric name that is inside of IotDataMessage.Metrics | STRING  |  `"hum"` |   
|  metricValue |  The metric value that is inside of IotDataMessage.Metrics | INTEGER  |  `22` |   
|  condition |  The condition to compare. `==` `!=` `<` `>` `>=` `<=` | STRING  |  `"<"` |   
|  controlAction |  The action to send to control topic by id. *Cloud be `null`* | STRING  |  `"ON"` |   

```json
    {
      "ruleName": "min_humidity",
      "metricName": "hum",
      "metricValue": 50,
      "condition": "<"
    }
```



### Alert Message Format

* [IotAlertMessage](https://github.com/andresgomezfrr/iot-engine/blob/master/service/src/main/java/kafkastreams/iot/model/IotAlertMessage.java)

| Field  | Description  | Type  | Example |
|---|---|---|---|
|  id |  The ID to identify a speficic IoT sensor | STRING  |  `"A"` |   
|  ruleName |  Rule name's. It is used to indetify the rule | STRING  |  `"Min humidity reached!"` |   
|  metricName |  The metric name that is inside of IotDataMessage.Metrics | STRING  |  `"hum"` |   
|  payload |  The condition that was compared. `{metricValue} {condition} {ruleMetricValue}` | STRING  |  `"22 < 45"` |   
|  controlAction |  The action to send to control topic by id. *Cloud be `null`* | STRING  |  `"ON"` |   

Full example:

```json
{
  "id": "A",
  "metricName": "hum",
  "ruleName": "min_humidity",
  "payload": "49 < 50",
  "controlAction": "ON"
}
```

### Control Message Format

* [IotControlMessage](https://github.com/andresgomezfrr/iot-engine/blob/master/service/src/main/java/kafkastreams/iot/model/IotControlMessage.java)

| Field  | Description  | Type  | Example |
|---|---|---|---|
|  id |  The ID to identify a speficic IoT sensor | STRING  |  `"A"` |   
|  controlAction |  The action to send to control topic by id. *Cloud be `null`* | STRING  |  `"ON"` |   


## Rest Service

 * POST `/iot-engine/query/rules`
    * update the rules for specific sensor has the provided store.
    * Params:
        * `id`: String sensor ID
    * Payload: `IotSensorRules`
    * Return: `MessageResponse`

 * PUT `/iot-engine/query/rules/{id}`
    * update the rules for specific sensor has the provided store.
    * Params:
        * `id`: String sensor ID
    * Payload: `IotSensorRules`
    * Return: `MessageResponse`

 * GET `/iot-engine/query/rules/{id}`
    * Get the rules for specific sensor has the provided store.
    * Params:
        * `id`: String sensor ID
    * Return (200): `IotSensorRules`
    * Return: `MessageResponse`
    
 * GET `/iot-engine/query/metrics/{id}/{start}/{end}`
    * Get aggregation metrics from specifc sensor and interval
    * Params:
        * `id`: String sensor ID
        * `start`: Start time. *Example: `2018-12-03T10:15:30.00Z`*
        * `end`: End time. *Example: `2019-12-03T10:15:30.00Z`*
    * Return (200): `List<IotDataMessage>`
    * Return: `MessageResponse`

 * GET `/iot-engine/instances`
    * Get the metadata for all of the instances of this Kafka Streams application
    * Return (200): `List<HostStoreInfo>`
    * Return: `MessageResponse`

 * GET `/iot-engine/instances/{storeName}`
    * Get the metadata for all instances of this Kafka Streams application that currently has the provided store.
    * Return (200): `List<HostStoreInfo>`
    * Return: `MessageResponse`

## Docker

Enviroment Vars:

| ENV  | Description  | Default Value  | Example |
|---|---|---|---|
|  BOOTSTRAP_SERVERS |  Kafka servers | -  | `locahost:9092`|  
|  APPLICATION_ID |  Indetify the app. It is used to create consumer groups | `iot-engine-app`  | `iot-engine-app` |    
|  REST_SERVER_ENDPOINT |  Host and port where the REST Service perform the binding | `0.0.0.0:5577`  |    `0.0.0.0:5577` |
|  TOPIC_AGG |  Topic to send aggregation metrics | `agg-metrics`  |    `agg-metrics` |
|  TOPIC_DATA |  Topic to receive input metrics  | `data`  |    `data`  |    
|  TOPIC_RULE |  Topic to receive rules  | `rules`  |    `rules`  |    
|  TOPIC_SENSOR_METADATA |  Topic to receive sensor metadata | `sensor-metadata`  |    `sensor-metadata`  |
|  TOPIC_ALERT |  Topic to send alerts  | `alerts`  |    `alerts`  |    
|  TOPIC_CONTROL |  Topic to send control messages | `control`  |    `control`  |    
|  APPLICATION_SERVER |  Host and port to advertise the KStream APP | - | `127.0.0.1:5577`  |
|  NUM_STREAM_THREADS |  The kafka streams thredas | `1`  |    `1` |
|  LOG_LEVEL |  Iot Engine log level | `info`  |  `info` | 
|  GENERAL_LOG_LEVEL |  Other dependencies log level | `info`  |  `info` |
|  JVM_OPTIONS |  java properties to configure the JVM | `"-Xmx512m -Xms512m"`  | `"-Xmx512m -Xms512m"` |

Sample usage:

```
docker run -it -p 5574:5574 -e BOOTSTRAP_SERVERS=172.20.10.5:9092 -e APPLICATION_ID=iot-engine -e REST_SERVER_ENDPOINT=0.0.0.0:5574 -e TOPIC_AGG=iot-agg-data -e TOPIC_DATA=iot-data -e APPLICATION_SERVER=172.20.10.5:5574 iot-engine:latest
```

## Development

### Compiling Sources

To build this project you can use `maven` tool. 

If you want to build the JAR of the project you can do:

```
mvn clean package
```

If you want to check if it passes all the test:

```
mvn test
```

If you want to build the distribution tar.gz:

```
mvn clean package -P dist
```

If you want to build the docker image, make sure that you have the docker service running:

```
mvn clean package -P docker
```

### Contributing

1. [Fork it](https://github.com/andresgomezfrr/iot-engine/fork)
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Create a new Pull Request
