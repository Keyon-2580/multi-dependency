package cn.edu.fudan.se.multidependency.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;

public class ProjectUtil {
	
	public static JSONObject projectToNode(Project project, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", project.getId());
		String name = project.getProjectName();
		data.put("name", name);
		data.put("length", name.length() * 10);
		result.put("data", data);
		return result;
	}
	
	public static JSONObject packageToNode(Package pck, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", pck.getId());
		String name = "Package: " + pck.getPackageName();
		data.put("name", name);
		data.put("length", name.length() * 10);
		result.put("data", data);
		return result;
	}
	
	public static JSONObject fileToNode(ProjectFile file, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", file.getId());
		String name = "File: " + file.getFileName();
		data.put("name", name);
		data.put("length", name.length() * 10);
		result.put("data", data);
		return result;
	}
	
	public static JSONObject typeToNode(Type t, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", t.getId());
		String name = "Type: " + t.getTypeName();
		data.put("name", name);
		data.put("length", name.length() * 10);
		result.put("data", data);
		return result;
	}
	
	public static JSONObject functionToNode(Function function, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", function.getId());
		String name = "Function: " + function.getFunctionName();
		data.put("name", name);
		data.put("length", name.length() * 10);
		result.put("data", data);
		return result;
	}
	
	public static JSONObject scenarioToNode(Scenario scenario, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", scenario.getId());
		String name = scenario.getScenarioId() + " : " + scenario.getName();
		data.put("name", name);
		data.put("length", name.length() * 20);
		result.put("data", data);
		return result;
	}
	
	public static JSONObject microserviceToNode(MicroService ms, String type) {
		JSONObject result = new JSONObject();
		JSONObject microServiceDataValue = new JSONObject();
		microServiceDataValue.put("type", type);
		microServiceDataValue.put("id", ms.getId());
		microServiceDataValue.put("name", ms.getName());
		microServiceDataValue.put("length", ms.getName().length() * 10);
		result.put("data", microServiceDataValue);
		return result;
	}

	public static JSONObject restfulAPIToNode(RestfulAPI api, String type) {
		JSONObject result = new JSONObject();
		JSONObject microServiceDataValue = new JSONObject();
		microServiceDataValue.put("type", type);
		microServiceDataValue.put("id", api.getId());
		microServiceDataValue.put("name", api.getApiFunctionSimpleName());
		microServiceDataValue.put("length", api.getApiFunctionSimpleName().length() * 10);
		result.put("data", microServiceDataValue);
		return result;
	}
	
	public static JSONObject featureToNode(Feature feature, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", feature.getId());
		String name = feature.getFeatureId() + " : " + feature.getFeatureName();
		data.put("name", name);
		int length = name.length() * 20;
		if(feature.getFeatureName().matches(".*[a-zA-Z].*")) {
			length = (int) (length / 1.5);
		}
		data.put("length", length);
		result.put("data", data);
		return result;
	}
	
	public static JSONObject testCaseToNode(TestCase testCase, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", testCase.getId());
		String name = String.join(" : ", " " + testCase.getTestCaseId(), testCase.getTestCaseName() + " ");
		data.put("name", name);
		int length = (int)(name.length() * 20 / 1.3);
		data.put("length", length);
		result.put("data", data);
		return result;
	}
	
	public static JSONObject relationToEdge(Node start, Node end, String type, String value, boolean autoId) {
		JSONObject edge = new JSONObject();
		JSONObject data = new JSONObject();
		if(autoId) {
			data.put("id", start.getId() + "_" + end.getId());
		}
		data.put("source", start.getId());
		data.put("target", end.getId());
		if(!StringUtils.isBlank(type)) {
			data.put("type", type);
		}
		data.put("value", StringUtils.isBlank(value) ? "" : value);
		edge.put("data", data);
		return edge;
	}
	
	public static boolean isMicroServiceCall(MicroService start, MicroService end, Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls) {
		if(msCalls == null) {
			return false;
		}
		return msCalls.getOrDefault(start, new HashMap<>()).get(end) != null;
	}

	public static boolean isMicroServiceDependOn(MicroService start, MicroService end, Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns) {
		if(msDependOns == null) {
			return false;
		}
		return msDependOns.getOrDefault(start, new HashMap<>()).get(end) != null;
	}
}
