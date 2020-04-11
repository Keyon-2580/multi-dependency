package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MicroServiceCallWithEntry {
	
	private Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls = new HashMap<>();
	
	private Map<Trace, MicroService> traceToEntry = new HashMap<>();
	
	private Map<TestCase, List<MicroService>> testCaseToEntries = new HashMap<>();
	
	private Map<TestCase, List<Feature>> testCaseExecuteFeatures = new HashMap<>();
	
	public boolean containCall(MicroService caller, MicroService called) {
		return this.calls.getOrDefault(caller, new HashMap<>()) != null;
	}
	
	public JSONObject toCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		Map<MicroService, Boolean> isMicroServiceNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeAdd = new HashMap<>();
		for(TestCase testCase : testCaseToEntries.keySet()) {
			JSONObject testCaseJson = new JSONObject();
			JSONObject testCaseDataValue = new JSONObject();
			testCaseDataValue.put("type", "TestCase");
			testCaseDataValue.put("id", testCase.getId());
			testCaseDataValue.put("name", testCase.getTestCaseName());
			testCaseDataValue.put("length", testCase.getTestCaseName().length() * 20);
			testCaseJson.put("data", testCaseDataValue);
			nodes.add(testCaseJson);
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				if(!isMicroServiceNodeAdd.getOrDefault(entry, false)) {
					JSONObject microServiceJson = new JSONObject();
					JSONObject microServiceDataValue = new JSONObject();
					microServiceDataValue.put("type", "MicroService");
					microServiceDataValue.put("id", entry.getId());
					microServiceDataValue.put("name", entry.getName());
					microServiceDataValue.put("length", entry.getName().length() * 10);
					microServiceJson.put("data", microServiceDataValue);
					nodes.add(microServiceJson);
					isMicroServiceNodeAdd.put(entry, true);
				}
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("source", testCase.getId());
				value.put("target", entry.getId());
				edge.put("data", value);
				edges.add(edge);
			}
			List<Feature> features = testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>());
			for(Feature feature : features) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					JSONObject featureJson = new JSONObject();
					JSONObject featureDataValue = new JSONObject();
					featureDataValue.put("type", "Feature");
					featureDataValue.put("id", feature.getId());
					featureDataValue.put("name", feature.getFeatureName());
					featureDataValue.put("length", feature.getFeatureName().length() * 20);
					featureJson.put("data", featureDataValue);
					nodes.add(featureJson);
					isFeatureNodeAdd.put(feature, true);
				}
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("source", testCase.getId());
				value.put("target", feature.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		for(MicroService ms : calls.keySet()) {
			if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
				JSONObject microServiceJson = new JSONObject();
				JSONObject microServiceDataValue = new JSONObject();
				microServiceDataValue.put("type", "MicroService");
				microServiceDataValue.put("id", ms.getId());
				microServiceDataValue.put("name", ms.getName());
				microServiceDataValue.put("length", ms.getName().length() * 10);
				microServiceJson.put("data", microServiceDataValue);
				nodes.add(microServiceJson);
				isMicroServiceNodeAdd.put(ms, true);
			}
			for(MicroService callMs : calls.get(ms).keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
					JSONObject microServiceJson = new JSONObject();
					JSONObject microServiceDataValue = new JSONObject();
					microServiceDataValue.put("type", "MicroService");
					microServiceDataValue.put("id", callMs.getId());
					microServiceDataValue.put("name", callMs.getName());
					microServiceDataValue.put("length", callMs.getName().length() * 10);
					microServiceJson.put("data", microServiceDataValue);
					nodes.add(microServiceJson);
					isMicroServiceNodeAdd.put(callMs, true);
				}
				JSONObject edge = new JSONObject();	
				JSONObject value = new JSONObject();
				value.put("source", ms.getId());
				value.put("target", callMs.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		JSONObject value = new JSONObject();
		value.put("nodes", nodes);
		value.put("edges", edges);
		result.put("value", value);
		return result;
	}
}
