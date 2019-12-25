package cn.edu.fudan.se.multidependency.utils;

import java.io.File;

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
}
