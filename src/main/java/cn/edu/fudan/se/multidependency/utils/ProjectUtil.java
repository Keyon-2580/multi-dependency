package cn.edu.fudan.se.multidependency.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.Language;
import lombok.Data;

public class ProjectUtil {
	
	public static Collection<ProjectConfig> extract(JSONArray array) throws Exception {
		List<ProjectConfig> configs = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			ProjectConfig config = extract(json);
			configs.add(config);
		}
		return configs;
	}
	
	public static ProjectConfig extract(JSONObject projectJson) throws Exception {
		ProjectConfig config = new ProjectConfig();
		Language language = Language.valueOf(projectJson.getString("language"));
		String projectPath = projectJson.getString("path");
		boolean isMicroservice = projectJson.getBooleanValue("isMicroservice");
		String serviceGroupName = projectJson.getString("serviceGroupName");
		serviceGroupName = serviceGroupName == null ? "" : serviceGroupName;
		String projectName = projectJson.getString("project");
		boolean autoInclude = projectJson.getBooleanValue("autoInclude");
		JSONArray excludesArray = projectJson.getJSONArray("excludes");
		if(excludesArray != null) {
			for(int j = 0; j < excludesArray.size(); j++) {
				config.addExclude(excludesArray.getString(j));
			}
		}
		config.setLanguage(language);
		config.setPath(projectPath);
		config.setProject(projectName);
		config.setServiceGroupName(serviceGroupName);
		config.setMicroService(isMicroservice);
		config.setAutoInclude(autoInclude);
		RestfulAPIConfig apiConfig = extractAPIConfig(projectJson.getJSONObject("restfulAPIs"));
		config.setApiConfig(apiConfig);
		return config;
	}
	
	public static RestfulAPIConfig extractAPIConfig(JSONObject json) throws Exception {
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
	public static class ProjectConfig {
		private String path;
		private String project;
		private Language language;
		private boolean isMicroService;
		private String serviceGroupName;
		private List<String> excludes = new ArrayList<>();
		private boolean autoInclude;
		private RestfulAPIConfig apiConfig;
		
		public void addExclude(String exclude) {
			this.excludes.add(exclude);
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
