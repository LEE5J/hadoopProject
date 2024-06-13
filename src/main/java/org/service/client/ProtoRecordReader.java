package org.service.client;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class ProtoRecordReader extends RecordReader<LongWritable, Text> {

    private LongWritable key = new LongWritable();
    private Text value = new Text();
    private boolean more = true;
    private DataInputStream in;
    private long start;
    private long end;
    private long pos;

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException {
        FileSplit fileSplit = (FileSplit) split;
        Configuration conf = context.getConfiguration();
        Path path = fileSplit.getPath();
        FileSystem fs = path.getFileSystem(conf);

        this.in = new DataInputStream(fs.open(path));
        this.start = fileSplit.getStart();
        this.end = start + fileSplit.getLength();
        this.pos = start;

        in.skip(start);
    }

    @Override
    public boolean nextKeyValue() throws IOException {
        if (pos >= end || !more) {
            return false;
        }

        // 데이터 읽기
        byte[] buffer = new byte[256];
        int bytesRead;
        StringBuilder stringBuilder = new StringBuilder();

        while (pos < end && (bytesRead = in.read(buffer)) != -1) {
            for (int i = 0; i < bytesRead; i++) {
                if (buffer[i] == 0x1A) { // 패딩 값 0x1A 발견
                    continue; // 패딩 무시
                }
                stringBuilder.append((char) buffer[i]);
                pos++;
            }
        }

        if (stringBuilder.length() == 0) { // 데이터가 없는 경우
            more = false;
            return false;
        }

        value.set(stringBuilder.toString());
        key.set(pos); // 포지션을 키로 사용
        return true;
    }

    @Override
    public LongWritable getCurrentKey() {
        return key;
    }

    @Override
    public Text getCurrentValue() {
        return value;
    }

    @Override
    public float getProgress() throws IOException {
        if (end == start) {
            return 0.0f;
        } else {
            return Math.min(1.0f, (pos - start) / (float) (end - start));
        }
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }
}
