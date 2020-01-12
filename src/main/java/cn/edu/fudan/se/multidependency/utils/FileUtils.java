package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	
	/**
	 * 提取文件所在目录
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
	
	/**
	 * 提取路径最后一个名字
	 * @param filePath
	 * @return
	 */
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

	/**
	 * 删除文件夹及其子文件夹的所有文件
	 * @param file
	 * @return
	 */
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
	
	/**
	 * 列出目录下所有文件，将结果保存到result中
	 * @param directory
	 * @param result
	 */
	public static void listFiles(File directory, List<File> result) {
		if(directory.isFile()) {
			result.add(directory);
			return;
		}
		for(File file : directory.listFiles()) {
			listFiles(file, result);
		}
	}
	
	public static void main(String[] args) {
		File directory = new File("src/main/resources/dynamic/kieker");
		directory = new File("src/main/resources/dynamic");
		List<File> files = new ArrayList<>();
//		listFiles(directory, files, ".dat");
//		System.out.println(files.size());
		listDirectories(directory, 2, files);
		System.out.println(files.size());
	}
	
	public static void listDirectories(File rootDirectory, int depth, List<File> result) {
		if(rootDirectory.isFile()) {
			return;
		}
		if(depth == 0 && rootDirectory.isDirectory()) {
			result.add(rootDirectory);
			return;
		}
		for(File file : rootDirectory.listFiles()) {
			listDirectories(file, depth - 1, result);
		}
	}
	
	/**
	 * 列出目录下所有指定后缀的文件，并将结果保存在result中
	 * @param directory
	 * @param result
	 * @param suffixes
	 */
	public static void listFiles(File directory, List<File> result, String... suffixes) {
		if(directory.isFile()) {
			for(String suffix : suffixes) {
				if(suffix.equals(extractSuffix(directory.getPath()))) {
					result.add(directory);
				}
			}
			return;
		}
		for(File file : directory.listFiles()) {
			listFiles(file, result, suffixes);
		}
	}
	
}
