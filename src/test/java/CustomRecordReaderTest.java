//import org.apache.hadoop.fs.FSDataInputStream;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.mapreduce.TaskAttemptContext;
//import org.apache.hadoop.mapreduce.lib.input.FileSplit;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.service.CustomRecordReader;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//
//import org.junit.jupiter.api.Assertions.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class CustomRecordReaderTest {
//    private CustomRecordReader reader;
//    private FSDataInputStream fsDataInputStream;
//    private FileSystem fileSystem;
//    private TaskAttemptContext context;
//    private FileSplit split;
//
//    @BeforeEach
//    public void setUp() throws IOException {
//        // Setting up the environment
//        context = mock(TaskAttemptContext.class);
//        fileSystem = mock(FileSystem.class);
//        split = new FileSplit(new Path("/fake/path"), 0, 100, new String[]{});
//
//        // Mock input stream setup
//        String data = "Hello world" + (char)0x1A + "This should not be read";
//        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());
//        fsDataInputStream = new FSDataInputStream(bais);
//
//        when(fileSystem.open(any(Path.class))).thenReturn(fsDataInputStream);
//        when(context.getConfiguration()).thenReturn(new org.apache.hadoop.conf.Configuration());
//        when(split.getPath()).thenReturn(new Path("/fake/path"));
//        when(fileSystem.getConf()).thenReturn(new org.apache.hadoop.conf.Configuration());
//
//        reader = new CustomRecordReader();
//        reader.initialize(split, context);
//    }
//
//    @Test
//    public void testReadRecord() throws IOException {
//        assertTrue(reader.nextKeyValue());
//        assertEquals("Hello world", reader.getCurrentValue().toString());
//        assertEquals("0", reader.getCurrentKey().toString());
//        assertFalse(reader.nextKeyValue()); // Should detect EOF and stop
//    }
//
//    @Test
//    public void testHandleEOFWithinData() throws IOException {
//        // Simulate reading data that contains EOF marker
//        String data = "Data before EOF" + (char)0x1A;
//        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());
//        when(fsDataInputStream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
//            byte[] buffer = invocation.getArgument(0);
//            System.arraycopy(data.getBytes(), 0, buffer, 0, data.length());
//            return data.length();
//        });
//        assertTrue(reader.nextKeyValue());
//        assertEquals("Data before EOF", reader.getCurrentValue().toString());
//        assertEquals("0", reader.getCurrentKey().toString());
//        assertFalse(reader.nextKeyValue()); // Should handle EOF correctly
//    }
//
//    @Test
//    public void testReadFromInvalidFile() {
//        assertThrows(IOException.class, () -> {
//            // Testing the behavior when the file cannot be read
//            when(fileSystem.open(any(Path.class))).thenThrow(new IOException("Failed to open file."));
//            reader.initialize(split, context);
//        });
//    }
//}
