package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.ho.yaml.Yaml;

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
		return result;
	}
	
	public static class YamlObject {
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
		public String getNeo4jDatabasePath() {
			return neo4jDatabasePath;
		}
		public void setNeo4jDatabasePath(String neo4jDatabasePath) {
			this.neo4jDatabasePath = neo4jDatabasePath;
		}
		public String getCodeProjectPath() {
			return codeProjectPath;
		}
		public void setCodeProjectPath(String codeProjectPath) {
			this.codeProjectPath = codeProjectPath;
		}
		public String getCodeLanguage() {
			return codeLanguage;
		}
		public void setCodeLanguage(String codeLanguage) {
			this.codeLanguage = codeLanguage;
		}
		public String getForTest() {
			return forTest;
		}
		public void setForTest(String forTest) {
			this.forTest = forTest;
		}
		public String getDirectoryRootPath() {
			return directoryRootPath;
		}
		public void setDirectoryRootPath(String directoryRootPath) {
			this.directoryRootPath = directoryRootPath;
		}
		public List<String> getDynamicJavaFileSuffix() {
			return dynamicJavaFileSuffix;
		}
		public void setDynamicJavaFileSuffix(List<String> dynamic_java_file_suffix) {
			this.dynamicJavaFileSuffix = dynamic_java_file_suffix;
		}
		public List<String> getDynamicCppFileSuffix() {
			return dynamicCppFileSuffix;
		}
		public void setDynamicCppFileSuffix(List<String> dynamic_cpp_file_suffix) {
			this.dynamicCppFileSuffix = dynamic_cpp_file_suffix;
		}
		public String getDynamicMarkSuffix() {
			return dynamicMarkSuffix;
		}
		public void setDynamicMarkSuffix(String dynamic_mark_suffix) {
			this.dynamicMarkSuffix = dynamic_mark_suffix;
		}
		public String getBuildDirectoryRootPath() {
			return buildDirectoryRootPath;
		}
		public void setBuildDirectoryRootPath(String buildDirectoryRootPath) {
			this.buildDirectoryRootPath = buildDirectoryRootPath;
		}
		public String getBuildFilePath() {
			return buildFilePath;
		}
		public void setBuildFilePath(String buildFilePath) {
			this.buildFilePath = buildFilePath;
		}
		public boolean isAnalyseDynamic() {
			return analyseDynamic;
		}
		public void setAnalyseDynamic(boolean analyseDynamic) {
			this.analyseDynamic = analyseDynamic;
		}
		public boolean isAnalyseBuild() {
			return analyseBuild;
		}
		public void setAnalyseBuild(boolean analyseBuild) {
			this.analyseBuild = analyseBuild;
		}
		
	}
}
