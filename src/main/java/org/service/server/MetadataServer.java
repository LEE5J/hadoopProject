//package org.service;
//
//import java.io.*;
//import java.net.*;
//import java.nio.file.*;
//
//public class MetadataServer {
//
//    private static final int PORT = 12345;
//    private static final String METADATA_FILE = "metadata.txt"; // 전송할 메타데이터 파일
//
//    public static void main(String[] args) {
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            System.out.println("Server started. Waiting for connection...");
//            while (true) {
//                try (Socket clientSocket = serverSocket.accept();
//                     OutputStream out = clientSocket.getOutputStream()) {
//                    System.out.println("Client connected.");
//                    sendMetadata(out);
//                    System.out.println("Metadata sent.");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void sendMetadata(OutputStream out) throws IOException {
//        Path metadataPath = Paths.get(METADATA_FILE);
//        Files.copy(metadataPath, out);
//    }
//}
