package org.service;
import org.dto.FileInfo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class FileDataLoader {
    private static final LinkedList<FileInfo> fileInfoList = loadFileInfo("/absolutepath/datafile.txt",20);
    public static LinkedList<FileInfo> loadFileInfo(String filePath,int blockAssign) { //블럭넘버할당
        int locateAssign = 32-blockAssign;
        LinkedList<FileInfo> fileInfoList = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    String fileName = parts[0];
                    int _metadata = Integer.parseInt(parts[1]);//2^20 = 1048576
                    String blockName = (int)_metadata%(Math.pow(2,blockAssign))+ ".block";;
                    int filePosition = _metadata/((int)Math.pow(2,blockAssign));
                    fileInfoList.add(new FileInfo(fileName, blockName, filePosition));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return fileInfoList;
    }
    public static LinkedList<FileInfo> getFileInfoList(){
        return FileDataLoader.fileInfoList;
    }

//    public static void main(String[] args) {
//        List<FileInfo> fileInfoList = loadFileInfo("/absolutepath/datafile.txt",20);
//        fileInfoList.forEach(System.out::println);
//    }
}
