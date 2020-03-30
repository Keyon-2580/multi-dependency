package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
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
	
	public boolean containCall(MicroService caller, MicroService called) {
		Map<MicroService, MicroServiceCallMicroService> calls = this.calls.getOrDefault(caller, new HashMap<>());
		return calls.get(called) != null;
	}
	
//	public List<MicroServiceCallMicroService> listCall() {
//		List<MicroServiceCallMicroService> calls = new ArrayList<>();
//	}
	
	public JSONObject toCytoscape(String type) {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		for(TestCase testCase : testCaseToEntries.keySet()) {
			JSONObject testCaseJson = new JSONObject();
			JSONObject testCaseDataValue = new JSONObject();
			testCaseDataValue.put("type", "TestCase");
			testCaseDataValue.put("id", testCase.getId());
			testCaseDataValue.put("name", testCase.getTestCaseName());
			testCaseJson.put("data", testCaseDataValue);
			nodes.add(testCaseJson);
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", testCase.getId() + "_" + entry.getId());
				value.put("source", testCase.getId());
				value.put("target", entry.getId());
//				value.put("type", "TestCase_Call_" + type);
				value.put("type", "TestCase_Call");
				value.put("index", type);
				edge.put("data", value);
				edges.add(edge);
			}
		}
		for(MicroService ms : calls.keySet()) {
			for(MicroService callMs : calls.get(ms).keySet()) {
				JSONObject edge = new JSONObject();	
				JSONObject value = new JSONObject();
//				value.put("id", ms.getId() + "_" + callMs.getId() + "_" + type);
				value.put("source", ms.getId());
				value.put("target", callMs.getId());
//				value.put("type", "TestCase_Call_" + type);
				value.put("type", "TestCase_Call");
				value.put("index", type);
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
