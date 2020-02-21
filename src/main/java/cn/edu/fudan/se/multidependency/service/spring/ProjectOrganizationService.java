package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProjectOrganizationService {
	
	private List<Project> projects;
	
	private Map<Project, List<FunctionDynamicCallFunction>> dynamicCalls;

	/*private Map<Project, List<Package>> packages;
	
	private Map<Project, List<ProjectFile>> files;
	
	private Map<Project, List<Type>> types;
	
	private Map<Project, List<Function>> functions;
	
	private Map<Project, List<Variable>> variables;
	
	private Map<Package, List<ProjectFile>> packageToFiles;*/
	
	public JSONArray projectsToTreeView() {
		JSONArray result = new JSONArray();
		
		for(Project project : projects) {
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
	
}
