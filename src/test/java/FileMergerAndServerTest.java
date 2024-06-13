//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.common.TopicPartition;
//import org.junit.jupiter.api.*;
//import org.mockito.*;
//import org.service.server.FileMergerAndServer;
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.nio.file.*;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class FileMergerAndServerTest {
//
//    @Test
//    public void testConsumeFromKafkaAndSaveToFile() throws IOException {
//        // Mock KafkaConsumer
//        KafkaConsumer<String, String> consumerMock = Mockito.mock(KafkaConsumer.class);
//        whenNew(KafkaConsumer.class).withAnyArguments().thenReturn(consumerMock);
//
//        // Mock ConsumerRecords
//        ConsumerRecord<String, String> record = new ConsumerRecord<>("file_data", 0, 0, "key1", "value1");
//        List<ConsumerRecord<String, String>> recordsList = Collections.singletonList(record);
//        ConsumerRecords<String, String> records = new ConsumerRecords<>(Map.of(new TopicPartition("file_data", 0), recordsList));
//        when(consumerMock.poll(anyLong())).thenReturn(records);
//
//        // Call the method
//        List<Path> tempFiles = FileMergerAndServer.consumeFromKafkaAndSaveToFile();
//
//        // Verify the result
//        assertEquals(1, tempFiles.size());
//        assertTrue(Files.exists(tempFiles.get(0)));
//        assertEquals("temp_key1.txt", tempFiles.get(0).getFileName().toString());
//
//        // Cleanup
//        Files.deleteIfExists(tempFiles.get(0));
//    }
//
//    @Test
//    public void testFilterFiles() throws IOException {
//        // Create temporary files
//        Path tempFile1 = Files.createTempFile("testFile1", ".txt");
//        Path tempFile2 = Files.createTempFile("testFile2", ".txt");
//        Files.write(tempFile1, "test".getBytes());
//        Files.write(tempFile2, new byte[(int) FileMergerAndServer.BLOCK_SIZE + 1]);
//
//        List<Path> files = Arrays.asList(tempFile1, tempFile2);
//
//        // Call the method
//        List<Path> filteredFiles = FileMergerAndServer.filterFiles(files);
//
//        // Verify the result
//        assertEquals(1, filteredFiles.size());
//        assertTrue(filteredFiles.contains(tempFile1));
//        assertFalse(filteredFiles.contains(tempFile2));
//
//        // Cleanup
//        Files.deleteIfExists(tempFile1);
//        Files.deleteIfExists(tempFile2);
//    }
//
//    @Test
//    public void testMergeFiles() throws IOException {
//        // Create temporary files
//        Path tempFile1 = Files.createTempFile("testFile1", ".txt");
//        Path tempFile2 = Files.createTempFile("testFile2", ".txt");
//        Files.write(tempFile1, "content1".getBytes());
//        Files.write(tempFile2, "content2".getBytes());
//
//        String outputBlockFile = "output_block.txt";
//        String metadataFile = "metadata.txt";
//
//        // Call the method
//        FileMergerAndServer.mergeFiles(Arrays.asList(tempFile1, tempFile2), outputBlockFile, metadataFile);
//
//        // Verify the result
//        Path outputPath = Paths.get(outputBlockFile);
//        Path metadataPath = Paths.get(metadataFile);
//
//        assertTrue(Files.exists(outputPath));
//        assertTrue(Files.exists(metadataPath));
//
//        // Cleanup
//        Files.deleteIfExists(tempFile1);
//        Files.deleteIfExists(tempFile2);
//        Files.deleteIfExists(outputPath);
//        Files.deleteIfExists(metadataPath);
//    }
//
//    @Test
//    public void testStartMetadataServer() throws IOException {
//        // Mock ServerSocket and Socket
//        ServerSocket serverSocketMock = mock(ServerSocket.class);
//        Socket clientSocketMock = mock(Socket.class);
//        OutputStream outputStreamMock = mock(OutputStream.class);
//
//        when(serverSocketMock.accept()).thenReturn(clientSocketMock);
//        when(clientSocketMock.getOutputStream()).thenReturn(outputStreamMock);
//
//        // Mock ServerSocket creation
//        try (MockedConstruction<ServerSocket> mocked = mockConstruction(ServerSocket.class,
//                (mock, context) -> when(mock.accept()).thenReturn(clientSocketMock))) {
//            new Thread(() -> FileMergerAndServer.startMetadataServer()).start();
//        }
//
//        // Verify that metadata was sent
//        verify(outputStreamMock, times(1)).write(any(byte[].class));
//
//        // Cleanup
//        serverSocketMock.close();
//        clientSocketMock.close();
//    }
//}
//
