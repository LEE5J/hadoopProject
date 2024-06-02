package org.service;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileMergerAndServer {

    private static final long BLOCK_SIZE = 64 * 1024 * 1024; // 64MB
    private static final byte[] FILE_SEPARATOR = {(byte) 0x1A}; // U+001A SUBSTITUTE (SUB) 문자
    private static final int PORT = 12345;
    private static final String METADATA_FILE = "metadata.txt"; // 메타데이터 파일 경로
    private static final String BLOCK_FILE = "output_block.txt"; // 블록 파일 경로

    public static void main(String[] args) {
        String inputDir = "input_directory"; // 입력 디렉터리 경로

        try {
            mergeFiles(inputDir, BLOCK_FILE, METADATA_FILE);
            System.out.println("Files merged successfully. Starting server...");
            startMetadataServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mergeFiles(String inputDir, String outputBlockFile, String metadataFile) throws IOException {
        List<Path> files = filterFiles(inputDir);
        try (BufferedOutputStream blockWriter = new BufferedOutputStream(new FileOutputStream(outputBlockFile));
             BufferedWriter metadataWriter = new BufferedWriter(new FileWriter(metadataFile))) {

            long currentBlockSize = 0;
            for (Path file : files) {
                long fileSize = Files.size(file);
                String metadata = file.getFileName().toString() + ": " + currentBlockSize + "\n";

                try (InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file.toFile()))) {
                    byte[] buffer = new byte[8192]; // 8KB 버퍼
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blockWriter.write(buffer, 0, bytesRead);
                        currentBlockSize += bytesRead;
                    }
                }

                blockWriter.write(FILE_SEPARATOR);
                currentBlockSize += FILE_SEPARATOR.length;

                metadataWriter.write(metadata);
            }
        }
    }

    private static List<Path> filterFiles(String dir) throws IOException {
        List<Path> filteredFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry) && Files.size(entry) < BLOCK_SIZE) {
                    filteredFiles.add(entry);
                }
            }
        }
        return filteredFiles;
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
