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
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.utils.ProjectUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MicroServiceCallWithEntry {
	
	private Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls = new HashMap<>();
	
	private Map<Trace, MicroService> traceToEntry = new HashMap<>();
	
	private Map<TestCase, List<MicroService>> testCaseToEntries = new HashMap<>();
	
	private Map<TestCase, List<Feature>> testCaseExecuteFeatures = new HashMap<>();
	
	private Map<Feature, Feature> featureToParentFeature = new HashMap<>();
	
	private Iterable<Feature> allFeatures = new ArrayList<>();
	
	private Iterable<MicroService> allMicroServices = new ArrayList<>();
	
	private Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns = new HashMap<>();
	
	public boolean containCall(MicroService caller, MicroService called) {
		return this.calls.getOrDefault(caller, new HashMap<>()) != null;
	}
	
	private boolean showAllFeatures = true;
	private boolean showAllMicroServices = true;
	private boolean showStructure = true;
	
	public JSONArray relatedTracesIds(boolean callChain) {
		JSONArray result = new JSONArray();
		
		return result;
	}
	
	public JSONArray relatedEdgeIds(boolean callChain) {
		JSONArray result = new JSONArray();
		
		if(callChain) {
			return result;
		} else {
			return relatedEdgeIds();
		}
		/*JSONObject msCallMsDetail = new JSONObject();
		for(MicroService ms : calls.keySet()) {
//			JSONObject info = new JSONObject();
//			info.put("from", ms);
			Map<MicroService, MicroServiceCallMicroService> callMss = calls.get(ms);
//			JSONObject toArray = new JSONObject();
			for(MicroService callMs : calls.keySet()) {
//				JSONObject to = new JSONObject();
//				to.put("to", callMs);
				MicroServiceCallMicroService mcm = callMss.get(callMs);
//				to.put("times", mcm.getTimes());
//				to.put("call", mcm.getSpanCallSpans());
//				toArray.put(callMs.getId().toString(), to);
				System.out.println(mcm.getSpanCallSpans().size());
			}
//			info.put("tos", toArray);
//			msCallMsDetail.put(ms.getId().toString(), info);
		}*/
	}
	
	public JSONArray relatedEdgeIds() {
		JSONArray result = new JSONArray();
		System.out.println("relatedEdgeIds");
		for(TestCase testCase : testCaseToEntries.keySet()) {
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				JSONObject obj = new JSONObject();
				obj.put("id", testCase.getId() + "_" + entry.getId());
				obj.put("source", testCase.getId());
				obj.put("target", entry.getId());
				obj.put("value", "");
				result.add(obj);
			}
		}
		for(MicroService ms : calls.keySet()) {
			for(MicroService callMs : calls.get(ms).keySet()) {
				JSONObject obj = new JSONObject();
				obj.put("id", ms.getId() + "_" + callMs.getId());
				obj.put("source", ms.getId());
				obj.put("target", callMs.getId());
				obj.put("value", calls.get(ms).get(callMs).getTimes());
				result.add(obj);
			}
		}
		return result;
	}
	
	public JSONArray relatedMicroServiceIds() {
		JSONArray result = new JSONArray();
		System.out.println("relatedNodeIds");
		Map<MicroService, Boolean> isMicroServiceNodeAdd = new HashMap<>();
		
		for(TestCase testCase : testCaseToEntries.keySet()) {
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				if(!isMicroServiceNodeAdd.getOrDefault(entry, false)) {
					System.out.println(entry.getId());
					JSONObject obj = new JSONObject();
					obj.put("id", entry.getId());
					result.add(obj);
					isMicroServiceNodeAdd.put(entry, true);
				}
			}
		}
		for(MicroService ms : calls.keySet()) {
			if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
				JSONObject obj = new JSONObject();
				obj.put("id", ms.getId());
				result.add(obj);
				isMicroServiceNodeAdd.put(ms, true);
			}
			for(MicroService callMs : calls.get(ms).keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
					JSONObject obj = new JSONObject();
					obj.put("id", callMs.getId());
					result.add(obj);
				}	
			}
		}
		return result;
	}
	
	public JSONObject toCytoscapeWithStructure() {
		System.out.println(showAllFeatures + " " + showAllMicroServices + " " + showStructure);
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		Map<MicroService, Boolean> isMicroServiceNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeParent = new HashMap<>();
		
		if(showAllMicroServices) {
			for(MicroService ms : allMicroServices) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
					nodes.add(ProjectUtil.microserviceToNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
			}
		}
		if(showAllFeatures) {
			for(Feature feature : allFeatures) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(ProjectUtil.featureToNode(feature, "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(ProjectUtil.featureToNode(parentFeature, "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(ProjectUtil.relationToEdge(parentFeature, feature, null, null));
					isFeatureNodeParent.put(feature, true);
				}
			}
		}
		
		for(TestCase testCase : testCaseToEntries.keySet()) {
			nodes.add(ProjectUtil.testCaseToNode(testCase, "TestCase_" + (testCase.isSuccess() ? "success" : "fail")));
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				if(!isMicroServiceNodeAdd.getOrDefault(entry, false)) {
					nodes.add(ProjectUtil.microserviceToNode(entry, "MicroService"));
					isMicroServiceNodeAdd.put(entry, true);
				}
				edges.add(ProjectUtil.relationToEdge(testCase, entry, "TestCaseExecuteMicroService", null));
			}
			List<Feature> features = testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>());
			for(Feature feature : features) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(ProjectUtil.featureToNode(feature, "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(ProjectUtil.featureToNode(parentFeature, "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(ProjectUtil.relationToEdge(parentFeature, feature, null, null));
					isFeatureNodeParent.put(feature, true);
				}
				edges.add(ProjectUtil.relationToEdge(feature, testCase, "TestCaseExecuteFeature", null));
			}
		}
		
		if(showStructure) {
			for(MicroService ms : msDependOns.keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false) && !msDependOns.get(ms).isEmpty()) {
					nodes.add(ProjectUtil.microserviceToNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
				for(MicroService callMs : msDependOns.get(ms).keySet()) {
					if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
						nodes.add(ProjectUtil.microserviceToNode(callMs, "MicroService"));
						isMicroServiceNodeAdd.put(callMs, true);
					}
					if(ProjectUtil.isMicroServiceCall(ms, callMs, calls)) {
						edges.add(ProjectUtil.relationToEdge(ms, callMs, "ShowStructureDependOnCall", null));
					} else {
						edges.add(ProjectUtil.relationToEdge(ms, callMs, "ShowStructureDependOn", null));
					}
				}
			}
			
			for(MicroService ms : calls.keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false) && !calls.get(ms).isEmpty()) {
					nodes.add(ProjectUtil.microserviceToNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
				for(MicroService callMs : calls.get(ms).keySet()) {
					if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
						nodes.add(ProjectUtil.microserviceToNode(callMs, "MicroService"));
						isMicroServiceNodeAdd.put(callMs, true);
					}
					if(!ProjectUtil.isMicroServiceDependOn(ms, callMs, msDependOns)) {
						edges.add(ProjectUtil.relationToEdge(ms, callMs, "ShowStructureCall", null));
					}
				}
			}
		} else {
			for(MicroService ms : calls.keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false) && !calls.get(ms).isEmpty()) {
					nodes.add(ProjectUtil.microserviceToNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
				for(MicroService callMs : calls.get(ms).keySet()) {
					if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
						nodes.add(ProjectUtil.microserviceToNode(callMs, "MicroService"));
						isMicroServiceNodeAdd.put(callMs, true);
					}
					edges.add(ProjectUtil.relationToEdge(ms, callMs, "NoStructureCall", null));
				}
			}
		}
		
		
		JSONObject data = new JSONObject();
		data.put("nodes", nodes);
		data.put("edges", edges);
		result.put("data", data);
		return result;	
	}
	
	public JSONObject toCytoscapeWithOutStructure() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		Map<MicroService, Boolean> isMicroServiceNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeParent = new HashMap<>();
		
		if(showAllMicroServices) {
			for(MicroService ms : allMicroServices) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
					nodes.add(ProjectUtil.microserviceToNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
			}
		}
		if(showAllFeatures) {
			for(Feature feature : allFeatures) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(ProjectUtil.featureToNode(feature, "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(ProjectUtil.featureToNode(parentFeature, "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(ProjectUtil.relationToEdge(parentFeature, feature, null, null));
					isFeatureNodeParent.put(feature, true);
				}
			}
		}
		
		for(TestCase testCase : testCaseToEntries.keySet()) {
			nodes.add(ProjectUtil.testCaseToNode(testCase, "TestCase_" + (testCase.isSuccess() ? "success" : "fail")));
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				if(!isMicroServiceNodeAdd.getOrDefault(entry, false)) {
					nodes.add(ProjectUtil.microserviceToNode(entry, "MicroService"));
					isMicroServiceNodeAdd.put(entry, true);
				}
				edges.add(ProjectUtil.relationToEdge(testCase, entry, null, null));
			}
			List<Feature> features = testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>());
			for(Feature feature : features) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(ProjectUtil.featureToNode(feature, "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(ProjectUtil.featureToNode(parentFeature, "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(ProjectUtil.relationToEdge(parentFeature, feature, null, "is child of"));
					isFeatureNodeParent.put(feature, true);
				}
				edges.add(ProjectUtil.relationToEdge(feature, testCase, null, null));
			}
		}
		for(MicroService ms : calls.keySet()) {
			if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
				nodes.add(ProjectUtil.microserviceToNode(ms, "MicroService"));
				isMicroServiceNodeAdd.put(ms, true);
			}
			for(MicroService callMs : calls.get(ms).keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
					nodes.add(ProjectUtil.microserviceToNode(callMs, "MicroService"));
					isMicroServiceNodeAdd.put(callMs, true);
				}
				edges.add(ProjectUtil.relationToEdge(ms, callMs, null, null));
			}
		}
		
		JSONObject data = new JSONObject();
		data.put("nodes", nodes);
		data.put("edges", edges);
		result.put("value", data);
		return result;
	}
	
}
