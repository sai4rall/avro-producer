package com.sai;

import avro.com.sai.ibmmq.raw.Value;
import com.github.javafaker.Faker;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

public class Producer {
    final static int MIN_AMT = 100; // one dollar
    final static int MAX_AMT = 10000; // one hundred dollars
    final static Logger logger = LoggerFactory.getLogger(Producer.class);

    final String topic;
    final KafkaProducer<String, Value> producer;


    public Producer(String bootstrapServers, String topic, String clientId, String schemaRegistry) {
        logger.info("Initializing Producer");
        this.topic = topic;
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 500);

        producer = new KafkaProducer<String, Value>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down producer");
            producer.close();
        }));
    }

    public void produce(String xmldata) throws Exception {
        Faker faker = new Faker();
        while(true) {
            Value orderValue = Value.newBuilder()
                    .setMessageId(UUID.randomUUID().toString())
                    .setMessageType(faker.name().fullName())
                    .setText(xmldata)
                    .build();
            ProducerRecord record = new ProducerRecord<String, Value>(topic, orderValue.getMessageId().toString(), orderValue);
            producer.send(record, ((metadata, exception) -> {
                logger.info("Produced record to topic {} partition {} at offset {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }));
            Thread.sleep(100);
        }
    }
}
