package org.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.dto.FileInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import static org.service.FileDataLoader.loadFileInfo;

public class HadoopFileReader {
    private static LinkedList<FileInfo> fileInfoList = loadFileInfo(String filePath,int blockAssign);

    public HadoopFileReader() {
        String filePath= ;
        int blockAssign = 20;
        this.fileInfoList = (LinkedList<FileInfo>) loadFileInfo("/absolutepath/datafile.txt",20);;
        // 가정: fileInfoList가 여기서 초기화되거나 어딘가에서 채워진다.
    }

    public String readFileFromHadoop(String fileName) {
        FileInfo fileInfo = findFileInfo(fileName);
        if (fileInfo == null) {
            return "File not found in fileInfo list.";
        }

        try {
            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(conf);
            Path path = new Path("/path/to/hadoop/directory/" + fileInfo.getBlockName()); // 경로는 예시입니다.
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

    private FileInfo findFileInfo(String fileName) {
        for (FileInfo fi : fileInfoList) {
            if (fi.getFileName().equals(fileName)) {
                return fi;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        HadoopFileReader reader = new HadoopFileReader();
        String fileContent = reader.readFileFromHadoop("desiredFileName.txt");
        System.out.println(fileContent);
    }
}
