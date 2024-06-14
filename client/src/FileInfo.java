public class FileInfo {
    private String fileName;
    private String blockName;
    private int filePosition;

    public FileInfo(String fileName, String blockName, int filePosition) {
        this.fileName = fileName;
        this.blockName = blockName;
        this.filePosition = filePosition;
    }

    public String getFileName() {
        return fileName;
    }

    public String getBlockName() {
        return blockName;
    }

    public int getFilePosition() {
        return filePosition;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileName='" + fileName + '\'' +
                ", blockName=" + blockName +
                ", filePosition=" + filePosition +
                '}';
    }
}
