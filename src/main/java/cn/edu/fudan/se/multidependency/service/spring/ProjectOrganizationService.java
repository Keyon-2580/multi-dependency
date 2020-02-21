package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProjectOrganizationService {
	
	private Map<Long, Project> projects;
	
	private Map<Project, List<FunctionDynamicCallFunction>> dynamicCalls;

	/*private Map<Project, List<Package>> packages;
	
	private Map<Project, List<ProjectFile>> files;
	
	private Map<Project, List<Type>> types;
	
	private Map<Project, List<Function>> functions;
	
	private Map<Project, List<Variable>> variables;
	
	private Map<Package, List<ProjectFile>> packageToFiles;*/
	
	public JSONArray projectsToTreeView() {
		JSONArray result = new JSONArray();
		
		for(Project project : allProjects()) {
//			List<TestCaseExecuteFeature> executes = featureExecutedByTestCases.get(feature);
//			JSONObject featureJson = new JSONObject();
//			featureJson.put("text", feature.getFeatureId() + ":" + feature.getFeatureName());
//			JSONArray tags = new JSONArray();
//			tags.add("feature");
//			featureJson.put("tags", tags);
//			featureJson.put("href", feature.getId());
//			
//			featureJson.put("nodes", testCases);
//			result.add(featureJson);
			JSONObject projectJson = new JSONObject();
			JSONArray tags = new JSONArray();
			tags.add("project");
			tags.add(project.getLanguage());
			projectJson.put("tags", tags);
			projectJson.put("text", project.getProjectName());
			
			// Package
			
			result.add(projectJson);
		}
		
		
		return result;
	}
	
	public Project findProjectById(Long id) {
		return projects.get(id);
	}
	
	public List<Project> allProjects() {
		List<Project> result = new ArrayList<>(projects.values());
		result.sort(new Comparator<Project>() {
			@Override
			public int compare(Project o1, Project o2) {
				return o1.getProjectName().compareTo(o2.getProjectName());
			}
		});
		return result;
	}
	
}
