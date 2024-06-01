package org.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import java.io.*;
import java.net.*;
import java.util.*;

public class MetadataClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String METADATA_FILE = "received_metadata.txt"; // 수신할 메타데이터 파일
    private static final String HDFS_BLOCK_FILE = "/path/to/hdfs/output_block.txt"; // HDFS 블록 파일 경로
    private static Configuration conf;

    public static void main(String[] args) {
        conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://namenode:9000"); // HDFS 설정

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             InputStream in = socket.getInputStream();
             FileOutputStream fos = new FileOutputStream(METADATA_FILE)) {

            System.out.println("Connected to server. Receiving metadata...");
            receiveMetadata(in, fos);
            System.out.println("Metadata received.");

            String desiredFile = "desired_file.txt"; // 원하는 파일 이름
            extractFileFromHDFS(desiredFile, METADATA_FILE, HDFS_BLOCK_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveMetadata(InputStream in, FileOutputStream fos) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
    }

    private static void extractFileFromHDFS(String fileName, String metadataFile, String hdfsBlockFile) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(metadataFile));
        long fileStartPos = -1;
        for (String line : lines) {
            String[] parts = line.split(": ");
            if (parts[0].equals(fileName)) {
                fileStartPos = Long.parseLong(parts[1]);
                break;
            }
        }

        if (fileStartPos == -1) {
            System.out.println("File not found in metadata.");
            return;
        }

        FileSystem fs = FileSystem.get(conf);
        try (InputStream hdfsInputStream = fs.open(new Path(hdfsBlockFile));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            hdfsInputStream.skip(fileStartPos);

            byte[] buffer = new byte[8192]; // 8KB 버퍼
            int bytesRead;
            boolean fileEndFound = false;

            while ((bytesRead = hdfsInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                String content = baos.toString("ISO-8859-1");
                if (content.contains("\n--- FILE SEPARATOR ---\n")) {
                    int endPos = content.indexOf("\n--- FILE SEPARATOR ---\n");
                    baos = new ByteArrayOutputStream();
                    baos.write(content.substring(0, endPos).getBytes("ISO-8859-1"));
                    fileEndFound = true;
                    break;
                }
            }

            if (fileEndFound) {
                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                    fos.write(baos.toByteArray());
                    System.out.println("File extracted: " + fileName);
                }
            } else {
                System.out.println("File separator not found.");
            }
        }
    }
}
