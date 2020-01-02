package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.List;

public class FileUtils {
	
	/**
	 * 文件所在目录
	 * @param filePath
	 * @return
	 */
	public static String extractDirectoryFromFile(String filePath) {
		if(filePath.contains("\\")) {
			return filePath.substring(0, filePath.lastIndexOf("\\"));
		} else if(filePath.contains("/")) {
			return filePath.substring(0, filePath.lastIndexOf("/"));
		} else {
			return "/";
		}
	}
	
	public static String extractFileName(String filePath) {
		if(filePath.contains("\\")) {
			return filePath.substring(filePath.lastIndexOf("\\") + 1);
		} else if(filePath.contains("/")) {
			return filePath.substring(filePath.lastIndexOf("/") + 1);
		} else {
			return filePath;
		}
	}
	
	/**
	 * 提取文件名后缀
	 * @param filePath
	 * @return
	 */
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
	
}
