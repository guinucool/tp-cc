import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import filexcp.*;
import filexcp.FileNotFoundException;

public class Directory {
    private String path;
    private Map<String,NodeFile> files;

    /* Constructor */
    public Directory(){
        this.path = "";
        this.files = new HashMap<String,NodeFile>();
    }

    public Directory(String path) throws PathException,FileException, IOException, NoSuchAlgorithmException{
        this.setPath(path);
        this.files = new HashMap<String,NodeFile>();
        this.setFiles(path);
    }

    public Directory(Directory directory){
        this.path = directory.path;
        this.files = directory.files;
    }

    /* Setters */
    public void setPath(String path) throws PathNotFoundException{
        if(path.equals(""))
            throw new PathNotFoundException(path);
        this.path = path;
    }

    public void setFiles(String path) throws IOException, NoSuchAlgorithmException, PathIsEmptyException, FileException{
        File folder = new File(path);
        File[] fileList = folder.listFiles();
        
        if (fileList != null) {
            for (File file : fileList) {
                NodeFile nodeFile = new NodeFile(getFileHash(file),getFileName(file),getFileExtension(file),getFileSize(file));
                this.files.put(getFileName(file),nodeFile);
            }
        } else {
            throw new PathIsEmptyException(path);
        }
    }

    /* Getters */
    public String getPath(){
        return this.path;
    }

    public Map<String,NodeFile> getFiles(){
        return this.files;
    }

    public NodeFile getFile(String filename) throws FileException{
        if(!this.files.containsKey(filename))
            throw new FileNotFoundException(filename);
        return this.files.get(filename);
        
    }

    public String getFileHash(File file){
        String hash;
        String NHASH = "d41d8cd98f00b204e9800998ecf8427e";

        try {
            hash = md5checksum(file);
        }
        catch (Exception e) {
            hash = NHASH;
        }

        return hash;
    }

    public String getFileName(File file){
        String fileName = file.getName();

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        } else {
            return fileName;
        }
    }

    public String getFileExtension(File file){
        String fileName = file.getName();

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        } else {
            return "";
        }
    }

    public long getFileSize(File file){
        return file.length();
    }

    /* Auxiliares */
    public String md5checksum(File file) throws IOException, NoSuchAlgorithmException{
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            sb.append(String.format("%02x", aByte & 0xff));
        }

        return sb.toString();
    }

    public boolean equals(Object o) {

        Directory directory = (Directory) o;
        return this.path.equals(directory.getPath()) && this.files.equals(directory.getFiles());
    }

    public Object clone() {
        return new Directory(this);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
    
        builder.append("Path: ").append(path).append("\n");
        builder.append("Files:").append(this.files).append(";\n");
        builder.append(super.toString());
    
        return builder.toString();
    }
}
