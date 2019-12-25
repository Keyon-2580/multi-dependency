package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.Map;

import org.ho.yaml.Yaml;

public class YamlUtils {

	public static YamlObject getDataBasePath(String yamlPath) throws Exception {
		File file = new File(yamlPath);
		YamlObject result = new YamlObject();
		Map<?, ?> yaml = (Map<?, ?>) Yaml.load(file);
		String applicationUser = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("spring")).get("profiles")).get("active");
		result.setApplicationUser(applicationUser);
		StringBuilder userYamlPath = new StringBuilder();
		userYamlPath.append(yamlPath.substring(0, yamlPath.lastIndexOf(".yml")))
			.append("-").append(applicationUser).append(".yml");
		file = new File(userYamlPath.toString());
		yaml = (Map<?, ?>) Yaml.load(file);
		String databasePath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("neo4j")).get("path");
		String projectPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("code")).get("path");
		String language = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("code")).get("language");
		String forTest = (String) ((Map<?, ?>) yaml.get("data")).get("test");
		result.setNeo4jDatabasePath(databasePath);
		result.setCodeLanguage(language);
		result.setCodeProjectPath(projectPath);
		result.setForTest(forTest);
		return result;
	}
	
	
	public static class YamlObject {
		private String applicationUser;
		private String neo4jDatabasePath;
		private String codeProjectPath;
		private String codeLanguage;
		private String forTest;
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
		public String getApplicationUser() {
			return applicationUser;
		}
		public void setApplicationUser(String applicationUser) {
			this.applicationUser = applicationUser;
		}
		
	}
}
