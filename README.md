# iot-engine
IoT Engine using Kafka Streams.

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
|  rules | Rules to verify by IoT sensor [  |  ARRAY[[IotSensorRule](https://github.com/andresgomezfrr/iot-engine/blob/master/service/src/main/java/kafkastreams/iot/model/IotSensorRule.java)] | `[{"ruleName":"min_humidity","metricName":"hum","metricValue":50,"condition":"<"}]`|   

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
|  payload |  The condition that was compared. `{metricValue} {condition} {ruleMetricValue}` | STRING  |  `"22 < 45"` |   

Full example:

```json
{
  "id": "A",
  "metricName": "hum",
  "ruleName": "min_humidity",
  "payload": "49 < 50"
}
```


### Alert Message Format

* [IotAlertMessage](https://github.com/andresgomezfrr/iot-engine/blob/master/service/src/main/java/kafkastreams/iot/model/IotAlertMessage.java)

| Field  | Description  | Type  | Example |
|---|---|---|---|
|  ruleName |  Rule name's. It is used to indetify the rule | STRING  |  `"Min humidity reached!"` |   
|  metricName |  The metric name that is inside of IotDataMessage.Metrics | STRING  |  `"hum"` |   
|  metricValue |  The metric value that is inside of IotDataMessage.Metrics | INTEGER  |  `22` |   
|  condition |  The condition to compare. `==` `!=` `<` `>` `>=` `<=` | STRING  |  `"<"` |   


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
|  TOPIC_ALERTS |  Topic to send alerts  | `alerts`  |    `alerts`  |    
|  APPLICATION_SERVER |  Host and port to advertise the KStream APP | - | `127.0.0.1:5577`  |
|  NUM_STREAM_THREADS |  The kafka streams thredas | `1`  |    `1` |
|  LOG_LEVEL |  Iot Engine log level | `info`  |  `info` | 
|  ROOT_LOG_LEVEL |  Other dependencies log level | `info`  |  `info` | 
|  JVM_OPTIONS |  java properties to configure the JVM | `"-Xmx512m -Xms512m"`  | `"-Xmx512m -Xms512m"` |

Sample usage:

```
docker run -it -p 5574:5574 -e BOOTSTRAP_SERVERS=172.20.10.5:9092 -e APPLICATION_ID=iot-engine -e REST_SERVER_ENDPOINT=0.0.0.0:5574 -e TOPIC_AGG=iot-agg-data -e TOPIC_DATA=iot-data -e APPLICATION_SERVER=172.20.10.5:5574 iot-engine:latest
```

## Compiling Sources

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

## Contributing

1. [Fork it](https://github.com/andresgomezfrr/iot-engine/fork)
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Create a new Pull Request
