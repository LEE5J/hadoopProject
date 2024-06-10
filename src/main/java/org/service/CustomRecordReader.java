//package org.service;
//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapreduce.InputSplit;
//import org.apache.hadoop.mapreduce.RecordReader;
//import org.apache.hadoop.mapreduce.TaskAttemptContext;
//import org.apache.hadoop.mapreduce.lib.input.FileSplit;
//import org.apache.hadoop.fs.FSDataInputStream;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//
//public class CustomRecordReader extends RecordReader<Text, Text> {
//    private FSDataInputStream in;
//    private Text key = new Text();
//    private Text value = new Text();
//    private boolean more = true;
//
//    @Override
//    public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
//        FileSplit split = (FileSplit) genericSplit;
//        Path file = split.getPath();
//        FileSystem fs = file.getFileSystem(context.getConfiguration());
//        in = fs.open(split.getPath());
//    }
//
//
//    @Override
//    public boolean nextKeyValue() throws IOException {
//        if (!more) {
//            Path path = new Path("/input/" + fileInfo.getBlockName());
//            Configuration conf = new Configuration();
//            FileSystem fs = FileSystem.get(conf);
//            try (InputStream in = fs.open(path);
//                 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
//                br.skip(fileInfo.getFilePosition());
//                String line = br.readLine();
//                value.set(line);
//                key.set(fileInfo.getFileName());
//                more = true;
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public Text getCurrentKey() {
//        return key;
//    }
//
//    @Override
//    public Text getCurrentValue() {
//        return value;
//    }
//
//    @Override
//    public float getProgress() throws IOException {
//        return more ? 0.0f : 1.0f;
//    }
//
//    @Override
//    public void close() throws IOException {
//        if (in != null) {
//            in.close();
//        }
//    }
//}
