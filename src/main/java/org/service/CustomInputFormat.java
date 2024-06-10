//package org.service;
//
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapreduce.*;
//import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
//import org.dto.FileInfo;
//
//import java.io.IOException;
//import java.util.List;
//
//public class CustomInputFormat extends FileInputFormat<Text, Text> {
//    private List<FileInfo> fileInfoList;
//    public CustomInputFormat() {
//        try {
//            this.fileInfoList = FileDataLoader.loadFileInfo("/datafile.txt",20);
//        } catch (IOException e) {
//            System.err.println("Error loading metadata: " + e.getMessage());
//        }
//    }
//    @Override
//    public RecordReader<Text, Text> createRecordReader(InputSplit split, TaskAttemptContext context) {
//        return new CustomRecordReader();
//    }
//
//    @Override
//    protected boolean isSplitable(JobContext context, Path filename) {
//        return false;
//    }
//}
//
///* 특정 데이터소스 , 입력형식을 처리 하는 것
//레코드리더는 키-값 쌍으로 태스크에 제공 파싱이 특별히 필요 할때 사용된다.
//파싱해야할 부분이 뭐가 있을까?
//1A 가 끝났을때
//* */