package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FileUtil {

	public static void main(String[] args) {
		JSONArray array = readDirectoryToGenerateProjectJSONFile(
				new File("D:\\multiple-dependency-project\\train-ticket"), 1, "java", true, "train-ticket");
		System.out.println(array);
	}

	public static JSONArray readDirectoryToGenerateProjectJSONFile(
			File rootDirectory, int depth, String defaultLanguage,
			boolean isAllMicroservice, String serviceGroupName) {
		JSONArray result = new JSONArray();
		List<File> projectDirectories = new ArrayList<>();
		FileUtil.listDirectories(rootDirectory, depth, projectDirectories);

		for(File projectDirectory : projectDirectories) {
			JSONObject projectJson = new JSONObject();

			projectJson.put("project", projectDirectory.getName());
			projectJson.put("path", projectDirectory.getAbsolutePath());
			projectJson.put("language", defaultLanguage == null ? "" : defaultLanguage);
			projectJson.put("isMicroservice", isAllMicroservice);
			if(isAllMicroservice && serviceGroupName != null) {
				projectJson.put("serviceGroupName", serviceGroupName);
			}
			result.add(projectJson);
		}

		return result;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

	/**
	 * 提取文件所在目录
	 * @param filePath
	 * @return
	 */
	public static String extractDirectoryFromFile(String filePath) {
//		LOGGER.info("extractDirectoryFromFile " + filePath);
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
//		LOGGER.info("extractFileName " + filePath);
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

	/**
	 * 提取文件所属项目名
	 * @param filePath
	 * @return
	 */
	public static String extractProjectNameFromFile(String filePath) {
		int idx1 = -1, idx2 = -1;
		if(filePath.contains("\\")) {
			idx1 = filePath.indexOf("\\");
			idx2 = filePath.indexOf("\\",1);
		} else if(filePath.contains("/")) {
			idx1 = filePath.indexOf("/");
			idx2 = filePath.indexOf("/",1);
		}
		return idx1 >= 0 && idx2 >= 0 ? filePath.substring(idx1+1, idx2) : "";
	}

	/**
	 * 判断文件后缀是否属于指定后缀，若不是，则应该过滤掉
	 *
	 * @param filePath
	 * @param suffixes
	 * @return
	 */
	public static boolean isFiltered(String filePath, String[] suffixes) {
		for (String suffix : suffixes) {
			if (filePath.endsWith(suffix)) {
				return false;
			}
		}
		return true;
	}
}
