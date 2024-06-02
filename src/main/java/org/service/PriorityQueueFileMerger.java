//import java.io.*;
//import java.nio.file.*;
//import java.util.*;
//
//public class PriorityQueueFileMerger {
//
//    private static final long BLOCK_SIZE = 64 * 1024 * 1024; // 64MB
//    private static final byte[] FILE_SEPARATOR = {(byte) 0x1A}; // U+001A SUBSTITUTE (SUB) 문자
//
//    public static void main(String[] args) {
//        String inputDir = "input_directory";
//        String outputBlockFile = "output_block.txt";
//        String metadataFile = "metadata.txt";
//
//        try {
//            mergeFiles(inputDir, outputBlockFile, metadataFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void mergeFiles(String inputDir, String outputBlockFile, String metadataFile) throws IOException {
//        PriorityQueue<Path> fileQueue = filterFiles(inputDir);
//        try (BufferedOutputStream blockWriter = new BufferedOutputStream(new FileOutputStream(outputBlockFile));
//             BufferedWriter metadataWriter = new BufferedWriter(new FileWriter(metadataFile))) {
//
//            long currentBlockSize = 0;
//            while (!fileQueue.isEmpty()) {
//                Path file = fileQueue.poll();
//                long fileSize = Files.size(file);
//                String metadata = file.getFileName().toString() + ": " + currentBlockSize + "\n";
//
//                try (InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file.toFile()))) {
//                    byte[] buffer = new byte[8192];
//                    int bytesRead;
//                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//                        blockWriter.write(buffer, 0, bytesRead);
//                        currentBlockSize += bytesRead;
//                    }
//                }
//
//                blockWriter.write(FILE_SEPARATOR);
//                currentBlockSize += FILE_SEPARATOR.length;
//
//                metadataWriter.write(metadata);
//            }
//        }
//    }
//
//    private static PriorityQueue<Path> filterFiles(String dir) throws IOException {
//        PriorityQueue<Path> fileQueue = new PriorityQueue<>(Comparator.comparingLong(file -> {
//            try {
//                return Files.size(file);
//            } catch (IOException e) {
//                throw new UncheckedIOException(e);
//            }
//        }));
//        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
//            for (Path entry : stream) {
//                if (Files.isRegularFile(entry) && Files.size(entry) < BLOCK_SIZE) {
//                    fileQueue.add(entry);
//                }
//            }
//        }
//        return fileQueue;
//    }
//}
