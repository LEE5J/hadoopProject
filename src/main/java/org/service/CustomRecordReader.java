package org.service;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class CustomRecordReader extends RecordReader<Text, Text> {
    private FSDataInputStream in;
    private Text key = new Text();
    private Text value = new Text();
    private boolean more = true;

    @Override
    public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
        FileSplit split = (FileSplit) genericSplit;
        Path file = split.getPath();
        FileSystem fs = file.getFileSystem(context.getConfiguration());
        in = fs.open(split.getPath());
    }

    @Override
    public boolean nextKeyValue() throws IOException {
        if (!more) return false;
        byte[] buffer = new byte[128 * 1024]; // 128KB
        int bytesRead = in.read(buffer);
        if (bytesRead == -1) {
            more = false;
            return false;
        }

        // Find EOF marker or read to the end of the buffer
        int end = 0;
        while (end < bytesRead && buffer[end] != 0x1A) end++;
        // in.getPos() 는 입력스트림의 위치를 반환한다. 매퍼가 데이터의 위치를 식별하는 것을 알려주는데 도움이 됨
        key.set(Long.toString(in.getPos())); // Position as key
        value.set(buffer, 0, end); // Text from start to EOF marker or end
        return true;
    }

    @Override
    public Text getCurrentKey() {
        return key;
    }

    @Override
    public Text getCurrentValue() {
        return value;
    }

    @Override
    public float getProgress() throws IOException {
        return more ? 0.0f : 1.0f;
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }
}
