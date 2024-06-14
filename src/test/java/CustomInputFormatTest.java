//import org.apache.hadoop.mapreduce.Job;
//import org.apache.hadoop.mapreduce.TaskAttemptContext;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.Mock;
//import org.service.CustomInputFormat;
//import org.service.CustomRecordReader;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//public class CustomInputFormatTest {
//
//    @Mock
//    private FileSystem fileSystem;
//
//    @Mock
//    private Job job;
//
//    @Mock
//    private TaskAttemptContext taskContext;
//
//    @Test
//    public void testCreateRecordReader() throws Exception {
//        Path path = new Path("input/test.dat");
//        when(taskContext.getConfiguration()).thenReturn(job.getConfiguration());
//
//        CustomInputFormat inputFormat = new CustomInputFormat();
//        // Assume CustomRecordReader is the RecordReader implementation
//        CustomRecordReader recordReader = (CustomRecordReader) inputFormat.createRecordReader(null, taskContext);
//
//        assertNotNull(recordReader, "RecordReader should not be null");
//    }
//}
