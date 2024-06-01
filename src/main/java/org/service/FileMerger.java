//package org.service;
//
//import java.io.*;
//import java.util.Arrays;
//
//public class FileMerger {
//    private static final int BLOCK_SIZE = 128 * 1024; // 128KB
//    private static final byte EOF_MARKER = 0x1A; // 파일 끝 마커 및 패딩 값
//
//    public static void processFiles(String[] inputFiles, String outputFilePath) throws IOException {
//        byte[] buffer = new byte[BLOCK_SIZE];
//        int bytesRead;
//
//        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFilePath))) {
//            for (String filePath : inputFiles) {
//                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
//                    while ((bytesRead = bis.read(buffer)) != -1) {
//                        if (bytesRead < BLOCK_SIZE) {
//                            // Write the actual data read
//                            bos.write(buffer, 0, bytesRead);
//                            // Calculate padding length including EOF marker for the rest of the block
//                            int paddingLength = BLOCK_SIZE - bytesRead;
//                            byte[] padding = new byte[paddingLength];
//                            Arrays.fill(padding, EOF_MARKER);
//                            bos.write(padding); // Write EOF marker as padding
//                        } else {
//                            // If full block, just write it
//                            bos.write(buffer, 0, bytesRead);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        try {
//            String[] files = {"input1.bin", "input2.bin"};
//            processFiles(files, "output.bin");
//            System.out.println("Files processed and merged successfully.");
//        } catch (IOException e) {
//            System.err.println("Error processing files: " + e.getMessage());
//        }
//    }
//}
