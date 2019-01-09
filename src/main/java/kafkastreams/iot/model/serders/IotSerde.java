package kafkastreams.iot.model.serders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class IotSerde<T> implements Serde<T> {
    ObjectMapper mapper = new ObjectMapper();
    Class<T> typeParameterClass;

    public IotSerde(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<T> serializer() {
        return new Serializer<T>() {
            private final Logger log = LoggerFactory.getLogger(Serializer.class);

            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {

            }

            @Override
            public byte[] serialize(String topic, T data) {
                byte[] rawData;

                try {
                    rawData = mapper.writeValueAsBytes(data);
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage(), e);
                    rawData = new byte[0];
                }

                return rawData;
            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public Deserializer<T> deserializer() {
        return new Deserializer<T>() {
            private final Logger log = LoggerFactory.getLogger(Deserializer.class);

            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {

            }

            @Override
            public T deserialize(String topic, byte[] data) {
                T rule = null;
                try {
                    rule = mapper.readValue(data, typeParameterClass);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

                return rule;
            }

            @Override
            public void close() {

            }
        };
    }
}
