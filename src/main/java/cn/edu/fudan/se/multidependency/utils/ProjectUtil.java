package cn.edu.fudan.se.multidependency.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;

public class ProjectUtil {
	
	public static JSONObject scenarioToNode(Scenario scenario, String type) {
		JSONObject result = new JSONObject();
		JSONObject scenarioDataValue = new JSONObject();
		scenarioDataValue.put("type", type);
		scenarioDataValue.put("id", scenario.getId());
		scenarioDataValue.put("name", scenario.getName());
		scenarioDataValue.put("length", scenario.getName().length() * 20);
		result.put("data", scenarioDataValue);
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
	
	public static JSONObject featureToNode(Feature feature, String type) {
		JSONObject result = new JSONObject();
		JSONObject featureDataValue = new JSONObject();
		featureDataValue.put("type", type);
		featureDataValue.put("id", feature.getId());
		featureDataValue.put("name", feature.getFeatureName());
		int length = feature.getFeatureName().length() * 20;
		if(feature.getFeatureName().matches(".*[a-zA-Z].*")) {
			length = (int) (length / 1.5);
		}
		featureDataValue.put("length", length);
		result.put("data", featureDataValue);
		return result;
	}
	
	public static JSONObject testCaseToNode(TestCase testCase, String type) {
		JSONObject result = new JSONObject();
		JSONObject testCaseDataValue = new JSONObject();
		testCaseDataValue.put("type", type);
		testCaseDataValue.put("id", testCase.getId());
		testCaseDataValue.put("name", testCase.getTestCaseName());
		testCaseDataValue.put("length", testCase.getTestCaseName().length() * 20);
		result.put("data", testCaseDataValue);
		return result;
	}
	
	public static JSONObject relationToEdge(Node start, Node end, String type, String value) {
		JSONObject edge = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("id", start.getId() + "_" + end.getId());
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
