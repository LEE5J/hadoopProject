package org.service;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileMergerAndServer {

    private static final long BLOCK_SIZE = 64 * 1024 * 1024; // 64MB
    private static final byte FILE_SEPARATOR = (byte) 0x1A; // U+001A SUBSTITUTE (SUB) 문자
    private static final int PORT = 12345;
    private static final String METADATA_FILE = "metadata.txt"; // 메타데이터 파일 경로
    private static final String TOPIC_NAME = "file_data"; // Kafka 토픽 이름

    public static void main(String[] args) {

        try {

            Path directory = Paths.get("/media/test1/hdp/input/");
            List<Path> filteredFiles = Files.list(directory)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            // Step 4: Merge files and create metadata
            mergeFiles(filteredFiles, METADATA_FILE);
            System.out.println("Files merged successfully. Starting server...");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mergeFiles(List<Path> files, String metadataFile) throws IOException {
        BufferedOutputStream blockWriter = null;
        blockWriter = new BufferedOutputStream(new FileOutputStream("0.block"));
        try (
                BufferedWriter metadataWriter = new BufferedWriter(new FileWriter(metadataFile))) {
            long currentBlockSize = 0;
            int currentNumber = 0;
            for (Path file : files) {
                long fileSize = Files.size(file);
                String metadata = null;
                if (fileSize + (currentBlockSize-1)%BLOCK_SIZE > BLOCK_SIZE) {
                    System.out.println(currentBlockSize+","+currentNumber);
                    //이전블럭의 나머지공간을 패딩? 할필요 없지 않나 -> 우리가 쓴 currentBlockSize는 다시 계산
                    //블럭 넘버증가
                    currentNumber = (int) ((currentBlockSize-1) / BLOCK_SIZE);
                    currentBlockSize = (currentNumber+1) * BLOCK_SIZE;
                    currentNumber += 1;
                    // 블럭파일 새로 열기
                    blockWriter.close();
                    blockWriter = new BufferedOutputStream(new FileOutputStream(currentNumber+".block"));
                }
                int currentBlockPosition = (int) ((currentBlockSize % BLOCK_SIZE) / Math.pow(2, 17));//단편화위치
                metadata = file.getFileName().toString() + " " +(int)( currentBlockPosition * Math.pow(2, 20) + currentNumber) + "\n";
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
                    //if(currentNumber>50)break;//debuging
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
