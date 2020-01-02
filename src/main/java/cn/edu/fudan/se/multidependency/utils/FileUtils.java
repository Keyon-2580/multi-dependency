package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	
	public static String findDirectoryFromFile(String fileName) {
		String packageName = null;
		if(fileName.contains("\\")) {
			packageName = fileName.substring(0, fileName.lastIndexOf("\\"));
		} else if(fileName.contains("/")) {
			packageName = fileName.substring(0, fileName.lastIndexOf("/"));
		} else {
			packageName = "default";
		}
		return packageName;
	}
	
	public static String extractSuffix(String filePath) {
		int lastIndex = filePath.lastIndexOf(".");
		return lastIndex >= 0 ? filePath.substring(lastIndex) : "";
	}

	public static boolean delFile(File file) {
        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f);
            }
        }
        return file.delete();
    }
	
	public static void listFiles(File directory, List<File> files) {
		if(directory.isFile()) {
			files.add(directory);
			return;
		}
		for(File file : directory.listFiles()) {
			listFiles(file, files);
		}
	}
	
	public static void main(String[] args) {
		List<File> files = new ArrayList<>();
		File directory = new File("src/main/resources");
		listFiles(directory, files);
	}
}
