package cn.edu.fudan.se.multidependency.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.Language;
import lombok.Data;

public class ProjectUtil {
	
	public static JSONConfigFile extract(JSONObject obj) throws Exception {
		JSONConfigFile result = new JSONConfigFile();
		JSONArray projectsConfigArray = obj.getJSONArray("projects");
		if(projectsConfigArray != null) {
			result.setProjectConfigs(extractProjectsConfig(projectsConfigArray));
		}
		JSONObject dependenciesObj = obj.getJSONObject("architectures");
		if(dependenciesObj != null) {
			JSONArray microServiceDependenciesArray = dependenciesObj.getJSONArray("microservices");
			if(microServiceDependenciesArray != null) {
				result.setMicroServiceDependencies(extractMicroServiceDependencies(microServiceDependenciesArray));
			}
		}
		
		return result;
	}
	
	private static Collection<MicroServiceDependency> extractMicroServiceDependencies(JSONArray array) throws Exception {
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
	
	private static Collection<ProjectConfig> extractProjectsConfig(JSONArray array) throws Exception {
		List<ProjectConfig> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			ProjectConfig config = extractProjectsConfig(json);
			result.add(config);
		}
		return result;
	}
	
	private static ProjectConfig extractProjectsConfig(JSONObject projectJson) throws Exception {
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
	
	private static RestfulAPIConfig extractAPIConfig(JSONObject json) throws Exception {
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
	
	@Data
	public static class JSONConfigFile {
		private Iterable<ProjectConfig> projectConfigs = new ArrayList<>();
		private Iterable<MicroServiceDependency> microServiceDependencies = new ArrayList<>();
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
}
