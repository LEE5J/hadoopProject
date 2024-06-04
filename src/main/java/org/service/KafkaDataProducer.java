package org.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class KafkaDataProducer {

    public static void main(String[] args) {
        String topicName = "file_data";
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);

        // 파일 경로 설정
        String[] files = {
                "pride_and_prejudice.txt",
                "war_and_peace.txt",
                "alice_in_wonderland.txt"
        };

        for (String filePath : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    producer.send(new ProducerRecord<>(topicName, filePath, line));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        producer.close();
        System.out.println("Data sent to Kafka topic.");
    }
}
