package org.service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileMerger {

    private static final long BLOCK_SIZE = 64 * 1024 * 1024; // 64MB

    public static void main(String[] args) {
        String inputDir = "input_directory"; // 입력 디렉터리 경로
        String outputBlockFile = "output_block.txt"; // 출력 블록 파일 경로
        String metadataFile = "metadata.txt"; // 메타데이터 파일 경로

        try {
            mergeFiles(inputDir, outputBlockFile, metadataFile);
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

                blockWriter.write("\n--- FILE SEPARATOR ---\n".getBytes("ISO-8859-1"));
                currentBlockSize += "\n--- FILE SEPARATOR ---\n".length();

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
}
