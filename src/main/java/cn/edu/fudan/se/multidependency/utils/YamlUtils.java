package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.ho.yaml.Yaml;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class YamlUtils {

	public static YamlObject getDataBasePathDefault(String yamlPath) throws Exception {
		File file = new File(yamlPath);
		Map<?, ?> yaml = (Map<?, ?>) Yaml.load(file);
		String applicationUser = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("spring")).get("profiles")).get("active");
		StringBuilder userYamlPath = new StringBuilder();
		userYamlPath.append(yamlPath.substring(0, yamlPath.lastIndexOf(".yml")))
			.append("-").append(applicationUser).append(".yml");
		
		return getDataBasePath(userYamlPath.toString());
	}
	
	@SuppressWarnings("unchecked")
	public static YamlObject getDataBasePath(String yamlPath) throws Exception {
		File file = new File(yamlPath);
		YamlObject result = new YamlObject();
		Map<?, ?> yaml = (Map<?, ?>) Yaml.load(file);
		yaml = (Map<?, ?>) Yaml.load(file);
		String databasePath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("neo4j")).get("path");
		String projectPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("code")).get("path");
		String language = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("code")).get("language");
		String forTest = (String) ((Map<?, ?>) yaml.get("data")).get("test");
		result.setNeo4jDatabasePath(databasePath);
		result.setCodeLanguage(language);
		result.setCodeProjectPath(projectPath);
		result.setForTest(forTest);
		String directoryRootPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("dynamic")).get("directory_root_path");
		String dynamicMarkSuffix = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("dynamic")).get("dynamic_mark_suffix");
		List<String> dynamicJavaFileSuffix = (List<String>) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("dynamic")).get("dynamic_java_file_suffix");
		List<String> dynamicCppFileSuffix = (List<String>) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("dynamic")).get("dynamic_cpp_file_suffix");
		result.setDirectoryRootPath(directoryRootPath);
		result.setDynamicCppFileSuffix(dynamicCppFileSuffix);
		result.setDynamicJavaFileSuffix(dynamicJavaFileSuffix);
		result.setDynamicMarkSuffix(dynamicMarkSuffix);
		String buildDirectoryRootPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("build")).get("directory_root_path");
		String buildFilePath = buildDirectoryRootPath+"/"+FileUtils.extractFileName(projectPath)+".txt";
		result.setBuildDirectoryRootPath(buildDirectoryRootPath);
		result.setBuildFilePath(buildFilePath);
		boolean analyseBuild = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("build")).get("analyse");
		boolean analyseDynamic = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("dynamic")).get("analyse");
		result.setAnalyseBuild(analyseBuild);
		result.setAnalyseDynamic(analyseDynamic);
		boolean delete = (boolean) ((Map<?, ?>) yaml.get("data")).get("delete");
		result.setDeleteDatabase(delete);
		int depth = (int) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("code")).get("depth");
		result.setDepth(depth);
		String rootPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("code")).get("rootPath");
		result.setRootPath(rootPath);
		return result;
	}
	
	@Data
	public static class YamlObject {
		private boolean deleteDatabase;
		private String neo4jDatabasePath;
		private String codeProjectPath;
		private String codeLanguage;
		private String forTest;
		private String directoryRootPath;
		private List<String> dynamicJavaFileSuffix;
		private List<String> dynamicCppFileSuffix;
		private String dynamicMarkSuffix;
		private String buildDirectoryRootPath;
		private String buildFilePath;
		private boolean analyseDynamic;
		private boolean analyseBuild;
		private String rootPath;
		private int depth;
	}
}
