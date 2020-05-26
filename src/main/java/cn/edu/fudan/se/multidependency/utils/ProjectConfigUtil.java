package cn.edu.fudan.se.multidependency.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.Language;
import lombok.Data;

public class ProjectConfigUtil {

	public static JSONConfigFile extract(JSONObject obj) throws Exception {
		JSONConfigFile result = new JSONConfigFile();

		JSONArray projectsConfigArray = obj.getJSONArray("projects");
		if(projectsConfigArray != null) {
			result.setProjectsConfig(extractProjectsConfig(projectsConfigArray));
		}

		JSONObject dependenciesObj = obj.getJSONObject("architectures");
		if(dependenciesObj != null) {
			JSONArray microServiceDependenciesArray = dependenciesObj.getJSONArray("microservices");
			if(microServiceDependenciesArray != null) {
				result.setMicroServiceDependencies(extractMicroServiceDependencies(microServiceDependenciesArray));
			}
		}

		JSONObject dynamicConfigObj = obj.getJSONObject("dynamics");
		if (dynamicConfigObj != null) {
			result.setDynamicsConfig(extractDynamicConfig(dynamicConfigObj));
		}

		JSONArray gitsConfigObj = obj.getJSONArray("gits");
		if (gitsConfigObj != null) {
			result.setGitsConfig(extractGitsConfig(gitsConfigObj));
		}

		JSONArray libsConfigObj = obj.getJSONArray("libs");
		if (libsConfigObj != null) {
			result.setLibsConfig(extractLibsConfig(libsConfigObj));
		}

		JSONArray clonesConfigObj = obj.getJSONArray("clones");
		if (clonesConfigObj != null) {
			result.setClonesConfig(extractClonesConfig(clonesConfigObj));
		}

		return result;
	}

	private static Collection<ProjectConfig> extractProjectsConfig(JSONArray array) throws Exception {
		List<ProjectConfig> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			ProjectConfig config = extractProjectConfig(json);
			result.add(config);
		}
		return result;
	}

	private static ProjectConfig extractProjectConfig(JSONObject projectJson) throws Exception {
		ProjectConfig result = new ProjectConfig();
		Language language = Language.valueOf(projectJson.getString("language"));
		String projectPath = projectJson.getString("path");
		boolean isMicroservice = projectJson.getBooleanValue("isMicroservice");
		String microserviceName = projectJson.getString("microserviceName");
		if(microserviceName == null) {
			microserviceName = "";
		}
		String serviceGroupName = projectJson.getString("serviceGroupName");
		serviceGroupName = serviceGroupName == null ? "" : serviceGroupName;
		String projectName = projectJson.getString("project");
		boolean autoInclude = projectJson.getBooleanValue("autoInclude");
		JSONArray excludesArray = projectJson.getJSONArray("excludes");
		if(excludesArray != null) {
			for(int i = 0; i < excludesArray.size(); i++) {
				result.addExclude(excludesArray.getString(i));
			}
		}
		JSONArray includeDirsArray = projectJson.getJSONArray("includeDirs");
		if(includeDirsArray != null) {
			for(int i = 0; i < includeDirsArray.size(); i++) {
				result.addIncludeDir(includeDirsArray.getString(i));
			}
		}
		result.setMicroserviceName(microserviceName);
		result.setLanguage(language);
		result.setPath(projectPath);
		result.setProject(projectName);
		result.setServiceGroupName(serviceGroupName);
		result.setMicroService(isMicroservice);
		result.setAutoInclude(autoInclude);
		RestfulAPIConfig apiConfig = extractAPIConfig(projectJson.getJSONObject("restfulAPIs"));
		result.setApiConfig(apiConfig);
		return result;
	}

	private static RestfulAPIConfig extractAPIConfig(JSONObject json) {
		if(json == null) {
			return null;
		}
		RestfulAPIConfig config = new RestfulAPIConfig();
		String path = json.getString("path");
		String framework = json.getString("framework");
		config.setPath(path);
		config.setFramework(framework);
		JSONArray excludeTagsArray = json.getJSONArray("excludeTags");
		if(excludeTagsArray != null) {
			for(int i = 0; i < excludeTagsArray.size(); i++) {
				config.addExcludeTag(excludeTagsArray.getString(i));
			}
		}
		return config;
	}

	private static Collection<MicroServiceDependency> extractMicroServiceDependencies(JSONArray array) {
		List<MicroServiceDependency> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			MicroServiceDependency dependency = new MicroServiceDependency();
			JSONObject json = array.getJSONObject(i);
			String name = json.getString("microservice");
			dependency.setMicroService(name);
			JSONArray dependenciesArray = json.getJSONArray("dependencies");
			for(int j = 0; j < dependenciesArray.size(); j++) {
				String dependencyMicroService = dependenciesArray.getString(j);
				dependency.addDependency(dependencyMicroService);
			}
			result.add(dependency);
		}
		return result;
	}

	private static DynamicConfig extractDynamicConfig(JSONObject json) {
		DynamicConfig result = new DynamicConfig();
		JSONArray fileSuffixes = json.getJSONArray("file_suffixes");
		if(fileSuffixes != null) {
			for(int i = 0; i < fileSuffixes.size(); i++) {
				result.addFileSuffix(fileSuffixes.getString(i));
			}
		}
		result.setLogsPath(json.getString("logs_path"));
		result.setFeaturesPath(json.getString("features_path"));
		return result;
	}

	private static Collection<GitConfig> extractGitsConfig(JSONArray array) {
		List<GitConfig> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			GitConfig config = new GitConfig();
			config.setPath(json.getString("path"));
			config.setCommitIdFrom(json.getString("commit_id_from"));
			config.setCommitIdTo(json.getString("commit_id_to"));
			config.setIssueFilePath(json.getString("issues_path"));
			result.add(config);
		}
		return result;
	}

	private static Collection<LibConfig> extractLibsConfig(JSONArray array) {
		List<LibConfig> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			LibConfig config = new LibConfig();
			config.setLanguage(Language.valueOf(json.getString("language")));
			config.setPath(json.getString("path"));
			result.add(config);
		}
		return result;
	}

	private static Collection<CloneConfig> extractClonesConfig(JSONArray array) {
		List<CloneConfig> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			CloneConfig config = new CloneConfig();
			config.setLanguage(Language.valueOf(json.getString("language")));
			config.setGranularity(Granularity.valueOf(json.getString("granularity")));
			config.setNamePath(json.getString("name_path"));
			config.setResultPath(json.getString("result_path"));
			result.add(config);
		}
		return result;
	}

	@Data
	public static class JSONConfigFile {
		private Collection<ProjectConfig> projectsConfig;
		private Collection<MicroServiceDependency> microServiceDependencies;
		private DynamicConfig dynamicsConfig;
		private Collection<GitConfig> gitsConfig;
		private Collection<LibConfig> libsConfig;
		private Collection<CloneConfig> clonesConfig;
	}

	@Data
	public static class ProjectConfig {
		private String path;
		private String project;
		private Language language;
		private boolean isMicroService;
		private String microserviceName;
		private String serviceGroupName;
		private List<String> excludes = new ArrayList<>();
		private List<String> includeDirs = new ArrayList<>();
		private boolean autoInclude;
		private RestfulAPIConfig apiConfig;

		public void addExclude(String exclude) {
			this.excludes.add(exclude);
		}

		public void addIncludeDir(String includeDir) {
			this.includeDirs.add(includeDir);
		}

		public String[] includeDirsArray() {
			String[] result = new String[includeDirs.size()];
			for(int i = 0; i < result.length; i++) {
				result[i] = includeDirs.get(i);
			}
			return result;
		}
	}

	@Data
	public static class RestfulAPIConfig {
		private String framework;
		private List<String> excludeTags = new ArrayList<>();
		private String path;
		public static final String FRAMEWORK_SWAGGER = "swagger";

		public void addExcludeTag(String excludeTag) {
			this.excludeTags.add(excludeTag);
		}
	}

	@Data
	public static class MicroServiceDependency {
		private String microService;
		private List<String> dependencies = new ArrayList<>();

		public void addDependency(String dependency) {
			this.dependencies.add(dependency);
		}
	}

	@Data
	public static class DynamicConfig {
		private List<String> fileSuffixes = new ArrayList<>();;
		private String logsPath;
		private String featuresPath;

		public void addFileSuffix(String fileSuffix) {
			this.fileSuffixes.add(fileSuffix);
		}
	}

	@Data
	public static class GitConfig {
		private String path;
		private String commitIdFrom;
		private String commitIdTo;
		private String issueFilePath;
	}

	@Data
	public static class LibConfig {
		private Language language;
		private String path;
	}

	@Data
	public static class CloneConfig {
		private Language language;
		private Granularity granularity;
		private String namePath;
		private String resultPath;
	}

	public enum Granularity {
		function, file
	}

}
