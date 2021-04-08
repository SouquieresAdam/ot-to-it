# PLC Field Change Subscription Source Connector

## Running in development

### Run unit tests. Build the project.
 
```bash
mvn clean install
```

### Run standalone connect and submit the connector

```bash
mkdir /tmp/input
mkdir /tmp/finished

export CLASSPATH="$(find target -type f -name '*.jar' | grep '\-with-dependencies' | tr '\n' ':')"
export CONFLUENT_HOME="/PATH"

${CONFLUENT_HOME}/bin/connect-standalone config/worker.properties config/solitary-connector.properties

kafka-topics --bootstrap-server=kafka:9092 --list
kafka-topics --bootstrap-server=kafka:9092 --create --topic=testing --replication-factor=1 --partitions=2
kafka-topics --bootstrap-server=kafka:9092 --describe --topic=testing

kafka-console-consumer --bootstrap-server localhost:9092 --topic testing --from-beginning

curl -s -XGET -H "Content-Type: application/json; charset=UTF-8" http://localhost:8083/connectors/

curl -s -XPOST -H "Content-Type: application/json; charset=UTF-8" http://localhost:8083/connectors/ -d '
{
    "name": "solitary-file-source",
    "config": {
      "connector.class":"io.michelin.connect.SolitaryFileSourceConnector",
      "tasks.max":"1",
      "topic":"testing",
      "input.path":"/tmp/input",
      "input.file.pattern":"oom.*\\.txt",
      "finished.path":"/tmp/finished",
      "file.poll.interval.ms":"1000"
    }
}'
```