package org.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaDataProducer {

    public static void main(String[] args) {
        String topicName = "file_data";
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);

        // 예제 데이터 전송
        for (int i = 1; i <= 10; i++) {
            String key = "key" + i;
            String value = "This is file content for file" + i;
            producer.send(new ProducerRecord<>(topicName, key, value));
        }

        producer.close();
        System.out.println("Data sent to Kafka topic.");
    }
}
