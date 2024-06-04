package org.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class FileMergerAndServer {

    private static final long BLOCK_SIZE = 64 * 1024 * 1024; // 64MB
    private static final byte FILE_SEPARATOR = (byte) 0x1A; // U+001A SUBSTITUTE (SUB) 문자
    private static final int PORT = 12345;
    private static final String METADATA_FILE = "metadata.txt"; // 메타데이터 파일 경로
    private static final String TOPIC_NAME = "file_data"; // Kafka 토픽 이름

    public static void main(String[] args) {
        // Log4j 설정 파일 로드
        PropertyConfigurator.configure("log4j.properties");

        try {
            // Step 1: Produce data to Kafka
            produceDataToKafka();

            // Step 2: Consume data from Kafka and save to temporary files
            List<Path> tempFiles = consumeFromKafkaAndSaveToFile();

            // Step 3: Filter files based on size
            List<Path> filteredFiles = filterFiles(tempFiles);

            // Step 4: Merge files and create metadata
            mergeFiles(filteredFiles, METADATA_FILE);
            System.out.println("Files merged successfully. Starting server...");

            // Step 5: Start metadata server
            startMetadataServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void produceDataToKafka() {
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
                    producer.send(new ProducerRecord<>(TOPIC_NAME, filePath, line));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        producer.close();
        System.out.println("Data sent to Kafka topic.");
    }

    private static List<Path> consumeFromKafkaAndSaveToFile() throws IOException {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "file_consumer_group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));

        List<Path> tempFiles = new ArrayList<>();
        boolean running = true;

        while (running) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                String fileName = "temp_" + record.key() + "_" + UUID.randomUUID() + ".txt";
                Path filePath = Paths.get(fileName);
                Files.write(filePath, record.value().getBytes());
                tempFiles.add(filePath);
                System.out.println("Received and saved: " + fileName);
            }
            if (!records.isEmpty()) {
                running = false;
            }
        }
        consumer.close();
        return tempFiles;
    }

    private static List<Path> filterFiles(List<Path> files) throws IOException {
        List<Path> filteredFiles = new ArrayList<>();
        for (Path file : files) {
            if (Files.size(file) < BLOCK_SIZE) {
                filteredFiles.add(file);
            }
        }
        return filteredFiles;
    }

    public static void mergeFiles(List<Path> files, String metadataFile) throws IOException {
        BufferedOutputStream blockWriter = null;
        blockWriter = new BufferedOutputStream(new FileOutputStream("0.block"));
        try (
             BufferedWriter metadataWriter = new BufferedWriter(new FileWriter(metadataFile))) {
            long currentBlockSize = 0;
            for (Path file : files) {
                long fileSize = Files.size(file);
                int currentNumber = 0;
                String metadata = null;
                if (fileSize + currentBlockSize > BLOCK_SIZE) {
                    //이전블럭의 나머지공간을 패딩? 할필요 없지 않나 -> 우리가 쓴 currentBlockSize는 다시 계산
                    //블럭 넘버증가
                    currentNumber = (int) (currentBlockSize / BLOCK_SIZE);
                    currentBlockSize = (currentNumber+1) * BLOCK_SIZE;
                    currentNumber += 1;
                    // 블럭파일 새로 열기
                    blockWriter.close();
                    blockWriter = new BufferedOutputStream(new FileOutputStream(currentNumber+".block"));
                }
                int currentBlockPosition = (int) ((currentBlockSize % BLOCK_SIZE) / Math.pow(2, 17));//단편화위치
                metadata = file.getFileName().toString() + " " + currentBlockPosition * Math.pow(2, 20) + currentNumber + "\n";
                try (InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file.toFile()))) {
                    byte[] buffer = new byte[8192]; // 8KB 버퍼
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blockWriter.write(buffer, 0, bytesRead);
                        currentBlockSize += bytesRead;
                    }
                    //128KB 공간내에서 남은 부분 패딩
                    int paddingSize = (int) (Math.pow(2,17)-((currentBlockSize-1)%Math.pow(2,17)+1));
                    currentBlockSize += paddingSize;
                    byte[] buffer2 = new byte[paddingSize];
                    for(int i=0; i<buffer2.length; i++) buffer2[i] = FILE_SEPARATOR;
                    blockWriter.write(buffer2);
                }
                metadataWriter.write(metadata);
            }//마지막 블럭은 패딩안함
            blockWriter.close();
        }
    }

    public static void startMetadataServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for connection...");
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     OutputStream out = clientSocket.getOutputStream()) {
                    System.out.println("Client connected.");
                    sendMetadata(out);
                    System.out.println("Metadata sent.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMetadata(OutputStream out) throws IOException {
        Path metadataPath = Paths.get(METADATA_FILE);
        Files.copy(metadataPath, out);
    }
}
