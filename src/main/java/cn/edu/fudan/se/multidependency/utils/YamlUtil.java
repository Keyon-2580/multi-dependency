package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.ho.yaml.Yaml;

import cn.edu.fudan.se.multidependency.model.Language;
import lombok.Data;

public class YamlUtil {

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
		String forTest = (String) ((Map<?, ?>) yaml.get("data")).get("test");
		result.setForTest(forTest);
		String databasePath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("neo4j")).get("path");
		result.setNeo4jDatabasePath(databasePath);
		boolean delete = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("neo4j")).get("delete");
		result.setDeleteDatabase(delete);
		String projectsConfig = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("projects")).get("config");
		result.setProjectsConfig(projectsConfig);
		String dynamicDirectoryRootPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("dynamic")).get("directory_root_path");
		List<String> dynamicFileSuffix = (List<String>) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("dynamic")).get("dynamic_file_suffix");
		result.setDynamicDirectoryRootPath(dynamicDirectoryRootPath);
		result.setDynamicFileSuffix(dynamicFileSuffix);
		String featuresPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("dynamic")).get("features_path");
		result.setFeaturesPath(featuresPath);
//		String buildDirectoryRootPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("build")).get("directory_root_path");
//		String buildFilePath = buildDirectoryRootPath+"/"+FileUtil.extractFileName(projectPath)+".txt";
//		result.setBuildDirectoryRootPath(buildDirectoryRootPath);
//		result.setBuildFilePath(buildFilePath);
		String gitDirectoryRootPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("git")).get("directory_root_path");
		String issuesPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("git")).get("issues_path");
		String commitIdFrom = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("git")).get("commit_id_from");
		String commitIdTo = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("git")).get("commit_id_to");
		result.setGitDirectoryRootPath(gitDirectoryRootPath);
		result.setIssuesPath(issuesPath);
		result.setCommitIdFrom(commitIdFrom);
		result.setCommitIdTo(commitIdTo);
		String libsPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("lib")).get("libs_path");
		result.setLibsPath(libsPath);
		boolean analyseDynamic = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("dynamic")).get("analyse");
		boolean analyseGit = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("git")).get("analyse");
		boolean gitSelectRange = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("git")).get("select_range");
		boolean analyseLib = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("lib")).get("analyse");
		boolean analyseBuild = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("build")).get("analyse");
		result.setAnalyseDynamic(analyseDynamic);
		result.setAnalyseGit(analyseGit);
		result.setGitSelectRange(gitSelectRange);
		result.setAnalyseLib(analyseLib);
		result.setAnalyseBuild(analyseBuild);


		boolean analyseClone = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("clone")).get("analyse");
		Language cloneLanguage = Language.valueOf((String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("clone")).get("language"));
		String methodNameTablePath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("clone")).get("method_name_table_path");
		String methodResultPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("clone")).get("method_result_path");
		result.setAnalyseClone(analyseClone);
		result.setCloneLanguage(cloneLanguage);
		result.setMethodNameTablePath(methodNameTablePath);
		result.setMethodResultPath(methodResultPath);
		return result;
	}
	
	@Data
	public static class YamlObject {
		private String projectsConfig;
		
		private boolean deleteDatabase;
		private String neo4jDatabasePath;
		
		private boolean analyseDynamic;
		private String dynamicDirectoryRootPath;
		private List<String> dynamicFileSuffix;
		private String featuresPath;
		
		private boolean analyseBuild;
		private String buildDirectoryRootPath;
		private String buildFilePath;
		private String forTest;

		private boolean analyseGit;
		private String gitDirectoryRootPath;
		private String issuesPath;
		private boolean gitSelectRange;
		private String commitIdFrom;
		private String commitIdTo;
		
		private boolean analyseLib;
		private String libsPath;
		
		private boolean analyseClone;
		private Language cloneLanguage;
		private String methodNameTablePath;
		private String methodResultPath;
	}
}
