package org.service;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class MetadataClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String METADATA_FILE = "received_metadata.txt"; // 수신할 메타데이터 파일
    private static final String BLOCK_FILE = "output_block.txt"; // 블록 파일 경로
    private static final byte[] FILE_SEPARATOR = {(byte) 0x1A}; // U+001A SUBSTITUTE (SUB) 문자

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             InputStream in = socket.getInputStream();
             FileOutputStream fos = new FileOutputStream(METADATA_FILE)) {

            System.out.println("Connected to server. Receiving metadata...");
            receiveMetadata(in, fos);
            System.out.println("Metadata received.");

            extractFile("desired_file.txt", METADATA_FILE, BLOCK_FILE);
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

    private static void extractFile(String fileName, String metadataFile, String blockFile) throws IOException {
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

        try (RandomAccessFile raf = new RandomAccessFile(blockFile, "r")) {
            raf.seek(fileStartPos);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192]; // 8KB 버퍼
            int bytesRead;
            boolean fileEndFound = false;

            while ((bytesRead = raf.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                byte[] content = baos.toByteArray();
                for (int i = 0; i < content.length; i++) {
                    if (content[i] == FILE_SEPARATOR[0]) {
                        fileEndFound = true;
                        baos.write(content, 0, i);
                        break;
                    }
                }
                if (fileEndFound) break;
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
