package org.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.dto.FileInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.service.FileDataLoader.getFileInfoList;

public class HadoopFileReader {
    private static LinkedList<FileInfo> fileInfoList = getFileInfoList();

    public HadoopFileReader() {
    }
    public String readFileFromHadoop(String fileName) {
        List<FileInfo> fileInfoList1 = findFileInfo(fileName);
        if (fileInfoList1.get(0) == null) {
            return "File not found in fileInfo list.";
        }
        FileInfo fileInfo = fileInfoList1.get(0);
        int endpoint = fileInfoList1.get(1).getFilePosition();
        try {
            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(conf);
            Path path = new Path("/input/" + fileInfo.getBlockName()); // 경로는 하드코딩으로 하면 안될듯?
            try (InputStream in = fs.open(path);
                 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                // 파일 위치로 스킵
                br.skip(fileInfo.getFilePosition());

                // 파일 데이터 읽기
                String line = br.readLine(); // 예를 들어, 한 줄을 읽는다고 가정
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading file from HDFS.";
        }
    }

    public List<FileInfo> findFileInfo(String fileName) {//다음 파일까지 같이 넘겨받아야 종료 위치를 알수 있음
        FileInfo target = null ,nextTarget = null;
        for (FileInfo fi : fileInfoList) {
            if (!target.equals(null)){
                nextTarget = fi;
                break;
            }
            if (fi.getFileName().equals(fileName)) {
                target =  fi;
            }
        }
        List<FileInfo> fileInfoList1 = Arrays.asList(target, nextTarget);
        return fileInfoList1;
    }

    public static void main(String[] args) {
        HadoopFileReader reader = new HadoopFileReader();
        String fileContent = reader.readFileFromHadoop("desiredFileName.txt");
        System.out.println(fileContent);
    }
}
