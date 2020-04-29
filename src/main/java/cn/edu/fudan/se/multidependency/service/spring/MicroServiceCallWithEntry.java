package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.utils.MicroServiceUtil;
import cn.edu.fudan.se.multidependency.utils.CytoscapeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MicroServiceCallWithEntry {
	
	private Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls = new HashMap<>();
	
	private Map<Trace, MicroService> traceToEntry = new HashMap<>();
	
	private Map<TestCase, List<MicroService>> testCaseToEntries = new HashMap<>();
	
	private Map<TestCase, List<Feature>> testCaseExecuteFeatures = new HashMap<>();
	
	private Map<TestCase, Scenario> scenarioDefineTestCases = new HashMap<>();
	
	private Map<Feature, Feature> featureToParentFeature = new HashMap<>();
	
	private Iterable<Feature> allFeatures = new ArrayList<>();
	
	private Iterable<MicroService> allMicroServices = new ArrayList<>();
	
	private Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns = new HashMap<>();
	
	private Iterable<Scenario> allScenarios = new ArrayList<>();
	
	private Iterable<Clone<MicroService>> clonesInMicroService = new ArrayList<>();
	
	private Iterable<CallLibrary<MicroService>> microServiceCallLibraries = new ArrayList<>();
	
	private Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMs;
	
	public boolean containCall(MicroService caller, MicroService called) {
		return this.calls.getOrDefault(caller, new HashMap<>()) != null;
	}
	
	private boolean showAllScenarios = true;
	private boolean showAllFeatures = true;
	private boolean showAllMicroServices = true;
	private boolean showStructure = true;
	private boolean showClonesInMicroService = true;
	private int showClonesMinPair = 3;
	private boolean showMicroServiceCallLibs = true;
	private boolean showCntOfDevUpdMs = true;
	
	public JSONArray relatedTracesIds(boolean callChain) {
		JSONArray result = new JSONArray();
		
		return result;
	}
	
	public List<CytoscapeEdge> relatedEdgeObjs() {
		List<CytoscapeEdge> result = new ArrayList<>();
		for(TestCase testCase : testCaseToEntries.keySet()) {
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				result.add(new CytoscapeEdge(testCase.getId() + "_" + entry.getId(), testCase.getId(), entry.getId()));
			}
		}
		for(MicroService ms : calls.keySet()) {
			for(MicroService callMs : calls.get(ms).keySet()) {
				JSONObject obj = new JSONObject();
				obj.put("id", ms.getId() + "_" + callMs.getId());
				obj.put("source", ms.getId());
				obj.put("target", callMs.getId());
				obj.put("value", calls.get(ms).get(callMs).getTimes());
				result.add(new CytoscapeEdge(ms.getId() + "_" + callMs.getId(), ms.getId(), callMs.getId()));
			}
		}
		return result;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	public static class CytoscapeEdge {
		private String id;
		private Long source;
		private Long target;
		
		public JSONObject toJson(String value, String type) {
			JSONObject obj = new JSONObject();
			obj.put("id", id);
			obj.put("source", source);
			obj.put("target", target);
			obj.put("value", value == null ? "" : value);
			obj.put("type", type == null ? "" : type);
			return obj;
		}
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
	
	private void showAllScenarios(JSONArray nodes, JSONArray edges, Map<Scenario, Boolean> isScenarioNodeAdd) {
		for(Scenario scenario : allScenarios) {
			if(!isScenarioNodeAdd.getOrDefault(scenario, false)) {
				nodes.add(CytoscapeUtil.toCytoscapeNode(scenario, scenario.getScenarioId() + " : " + scenario.getName(),  "Scenario"));
				isScenarioNodeAdd.put(scenario, true);
			}
		}
	}
	
	private void showClonesInMicroService(JSONArray edges) {
		for(Clone<MicroService> clone : clonesInMicroService) {
			if(clone.sizeOfChildren() >= showClonesMinPair) {
				JSONObject edge = CytoscapeUtil.relationToEdge(clone.getNode1(), clone.getNode2(), "all_MicroService_clone_MicroService", clone.calculateValue(), false);
				edges.add(edge);
				edge.getJSONObject("data").put("id", clone.getId());
				
			}
		}
	}
	
	public JSONObject toCytoscapeWithStructure() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		Map<MicroService, Boolean> isMicroServiceNodeAdd = new HashMap<>();
		Map<Scenario, Boolean> isScenarioNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeParent = new HashMap<>();
		Map<Library, Boolean> isLibraryNodeAdd = new HashMap<>();
		Map<String, Boolean> isLibraryWithoutVersionNodeAdd = new HashMap<>();
		Map<Developer, Boolean> isDeveloperNodeAdd = new HashMap<>();
		
		if(showAllScenarios) {
			showAllScenarios(nodes, edges, isScenarioNodeAdd);
		}
		if(showAllMicroServices) {
			for(MicroService ms : allMicroServices) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
			}
		}
		if(showCntOfDevUpdMs) {
			for(DeveloperUpdateNode<MicroService> update : cntOfDevUpdMs) {
				MicroService ms = update.getNode();
				Developer developer = update.getDeveloper();
				if(ms == null || developer == null) {
					continue;
				}
				int times = update.getTimes();
				if(!isDeveloperNodeAdd.getOrDefault(developer, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(developer, "Developer"));
					isDeveloperNodeAdd.put(developer, true);
				}
				edges.add(CytoscapeUtil.relationToEdge(ms, developer, "MicroServiceUpdatedByDeveloper", times + "", false));
			}
		}
		if(showAllFeatures) {
			for(Feature feature : allFeatures) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(feature, feature.getFeatureId() + " : " + feature.getName(), "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(CytoscapeUtil.toCytoscapeNode(parentFeature, parentFeature.getFeatureId() + " : " + parentFeature.getName(), "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(CytoscapeUtil.relationToEdge(parentFeature, feature, null, null, true));
					isFeatureNodeParent.put(feature, true);
				}
			}
		}
		
		if(showClonesInMicroService) {
			showClonesInMicroService(edges);
		}
		
		if(showMicroServiceCallLibs) {
			Map<Long, Map<String, JSONObject>> hasLibraryToVersionEdge = new HashMap<>();
			for(CallLibrary<MicroService> call : microServiceCallLibraries) {
				for(Library lib : call.getCallLibraries()) {
					if(!isLibraryNodeAdd.getOrDefault(lib, false)) {
						nodes.add(CytoscapeUtil.toCytoscapeNode(lib, lib.getFullName(), "Library"));
						isLibraryNodeAdd.put(lib, true);
					}
					String libraryGroupAndName = lib.groupIdAndName();
					if(!isLibraryWithoutVersionNodeAdd.getOrDefault(libraryGroupAndName, false)) {
						nodes.add(CytoscapeUtil.toCytoscapeNode(libraryGroupAndName, libraryGroupAndName, "Library"));
						isLibraryWithoutVersionNodeAdd.put(libraryGroupAndName, true);
					}
					Map<String, JSONObject> temp = hasLibraryToVersionEdge.getOrDefault(lib.getId(), new HashMap<>());
					JSONObject edge = temp.get(libraryGroupAndName);
					if(edge == null) {
						edge = CytoscapeUtil.relationToEdge(lib.getId(), libraryGroupAndName, "LibraryVersionIsFromLibrary", "", false);
						edges.add(edge);
						temp.put(libraryGroupAndName, edge);
						hasLibraryToVersionEdge.put(lib.getId(), temp);
					}
					edges.add(CytoscapeUtil.relationToEdge(call.getCaller(), lib, "MicroServiceCallLibrary", call.timesOfCallLib(lib) + "", false));
				}
			}
		}
		
		for(TestCase testCase : testCaseToEntries.keySet()) {
			nodes.add(CytoscapeUtil.toCytoscapeNode(testCase, String.join(" : ", " " + testCase.getTestCaseId(), testCase.getName() + " "), "TestCase_" + (testCase.isSuccess() ? "success" : "fail")));
			Scenario scenario = this.scenarioDefineTestCases.get(testCase);
			if(scenario != null) {
				if(!isScenarioNodeAdd.getOrDefault(scenario, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(scenario, scenario.getScenarioId() + " : " + scenario.getName(),  "Scenario"));
					isScenarioNodeAdd.put(scenario, true);
				}
				edges.add(CytoscapeUtil.relationToEdge(scenario, testCase, "ScenarioDefineTestCase", null, true));
			}
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				if(!isMicroServiceNodeAdd.getOrDefault(entry, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(entry, "MicroService"));
					isMicroServiceNodeAdd.put(entry, true);
				}
				edges.add(CytoscapeUtil.relationToEdge(testCase, entry, "TestCaseExecuteMicroService", null, true));
			}
			List<Feature> features = testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>());
			for(Feature feature : features) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(feature, feature.getFeatureId() + " : " + feature.getName(), "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(CytoscapeUtil.toCytoscapeNode(parentFeature, parentFeature.getFeatureId() + " : " + parentFeature.getName(), "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(CytoscapeUtil.relationToEdge(parentFeature, feature, null, null, true));
					isFeatureNodeParent.put(feature, true);
				}
				System.out.println(testCase.getName() + " " + feature.getName());
				edges.add(CytoscapeUtil.relationToEdge(feature, testCase, "FeatureExecutedByTestCase", null, true));
			}
		}
		
		if(showStructure) {
			for(MicroService ms : msDependOns.keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false) && !msDependOns.get(ms).isEmpty()) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
				for(MicroService callMs : msDependOns.get(ms).keySet()) {
					if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
						nodes.add(CytoscapeUtil.toCytoscapeNode(callMs, "MicroService"));
						isMicroServiceNodeAdd.put(callMs, true);
					}
					if(MicroServiceUtil.isMicroServiceCall(ms, callMs, calls)) {
						edges.add(CytoscapeUtil.relationToEdge(ms, callMs, "ShowStructureDependOnCall", null, true));
					} else {
						edges.add(CytoscapeUtil.relationToEdge(ms, callMs, "ShowStructureDependOn", null, true));
					}
				}
			}
			
			for(MicroService ms : calls.keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false) && !calls.get(ms).isEmpty()) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
				for(MicroService callMs : calls.get(ms).keySet()) {
					if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
						nodes.add(CytoscapeUtil.toCytoscapeNode(callMs, "MicroService"));
						isMicroServiceNodeAdd.put(callMs, true);
					}
					if(!MicroServiceUtil.isMicroServiceDependOn(ms, callMs, msDependOns)) {
						edges.add(CytoscapeUtil.relationToEdge(ms, callMs, "ShowStructureCall", null, true));
					}
				}
			}
		} else {
			for(MicroService ms : calls.keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false) && !calls.get(ms).isEmpty()) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
				for(MicroService callMs : calls.get(ms).keySet()) {
					if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
						nodes.add(CytoscapeUtil.toCytoscapeNode(callMs, "MicroService"));
						isMicroServiceNodeAdd.put(callMs, true);
					}
					edges.add(CytoscapeUtil.relationToEdge(ms, callMs, "NoStructureCall", null, true));
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
					nodes.add(CytoscapeUtil.toCytoscapeNode(ms, "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
			}
		}
		if(showAllFeatures) {
			for(Feature feature : allFeatures) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(feature, feature.getFeatureId() + " : " + feature.getName(), "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(CytoscapeUtil.toCytoscapeNode(parentFeature, parentFeature.getFeatureId() + " : " + parentFeature.getName(), "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(CytoscapeUtil.relationToEdge(parentFeature, feature, null, null, true));
					isFeatureNodeParent.put(feature, true);
				}
			}
		}
		
		for(TestCase testCase : testCaseToEntries.keySet()) {
			nodes.add(CytoscapeUtil.toCytoscapeNode(testCase, String.join(" : ", " " + testCase.getTestCaseId(), testCase.getName() + " "), "TestCase_" + (testCase.isSuccess() ? "success" : "fail")));
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				if(!isMicroServiceNodeAdd.getOrDefault(entry, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(entry, "MicroService"));
					isMicroServiceNodeAdd.put(entry, true);
				}
				edges.add(CytoscapeUtil.relationToEdge(testCase, entry, null, null, true));
			}
			List<Feature> features = testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>());
			for(Feature feature : features) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(feature, feature.getFeatureId() + " : " + feature.getName(), "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(CytoscapeUtil.toCytoscapeNode(parentFeature, parentFeature.getFeatureId() + " : " + parentFeature.getName(), "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(CytoscapeUtil.relationToEdge(parentFeature, feature, null, "is child of", true));
					isFeatureNodeParent.put(feature, true);
				}
				edges.add(CytoscapeUtil.relationToEdge(feature, testCase, null, null, true));
			}
		}
		for(MicroService ms : calls.keySet()) {
			if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
				nodes.add(CytoscapeUtil.toCytoscapeNode(ms, "MicroService"));
				isMicroServiceNodeAdd.put(ms, true);
			}
			for(MicroService callMs : calls.get(ms).keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
					nodes.add(CytoscapeUtil.toCytoscapeNode(callMs, "MicroService"));
					isMicroServiceNodeAdd.put(callMs, true);
				}
				edges.add(CytoscapeUtil.relationToEdge(ms, callMs, null, null, true));
			}
		}
		
		JSONObject data = new JSONObject();
		data.put("nodes", nodes);
		data.put("edges", edges);
		result.put("value", data);
		return result;
	}
	
	public JSONArray structureEdges(String type) {
		JSONArray edges = new JSONArray();
		for(MicroService ms : msDependOns.keySet()) {
			for(MicroService callMs : msDependOns.get(ms).keySet()) {
				System.out.println("all_MicroService_DependOn_MicroService");
				edges.add(CytoscapeUtil.relationToEdge(ms, callMs, type, null, false));
			}
		}
		return edges;
	}
	
	public JSONArray parentFeatureEdges(String type) {
		JSONArray edges = new JSONArray();
		for(Feature feature : allFeatures) {
			Feature parentFeature = featureToParentFeature.get(feature);
			if(parentFeature != null) {
				edges.add(CytoscapeUtil.relationToEdge(parentFeature, feature, type, null, true));
			}
		}
		return edges;
	}

	public JSONObject testCaseEdges() {
		System.out.println("testCaseEdges");
		JSONObject result = new JSONObject();
		JSONArray edges = new JSONArray();
		
		if(showStructure) {
			System.out.println("showStructure " + msDependOns.size());
			JSONArray structureEdges = structureEdges("all_MicroService_DependOn_MicroService");
			for(int i = 0; i < structureEdges.size(); i++) {
				edges.add(structureEdges.get(i));
			}
		}
		
		JSONArray parentFeatureEdges = parentFeatureEdges("all_Feature_Contain_Feature");
		for(int i = 0; i < parentFeatureEdges.size(); i++) {
			edges.add(parentFeatureEdges.get(i));
		}
		
		
		for(TestCase testCase : testCaseToEntries.keySet()) {
			Scenario scenario = this.scenarioDefineTestCases.get(testCase);
			if(scenario != null) {
				edges.add(CytoscapeUtil.relationToEdge(scenario, testCase, "all_ScenarioDefineTestCase", null, true));
			}
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				edges.add(CytoscapeUtil.relationToEdge(testCase, entry, "all_TestCaseExecuteMicroService", null, true));
			}
			List<Feature> features = testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>());
			for(Feature feature : features) {
				edges.add(CytoscapeUtil.relationToEdge(testCase, feature, "all_FeatureExecutedByTestCase", null, true));
			}
		}
		
		for(MicroService ms : calls.keySet()) {
			for(MicroService callMs : calls.get(ms).keySet()) {
				edges.add(CytoscapeUtil.relationToEdge(ms, callMs, "all_MicroService_call_MicroService", null, true));
			}
		}
		
		if(showClonesInMicroService) {
			showClonesInMicroService(edges);
		}
		
		if(showMicroServiceCallLibs) {
			Map<Long, Map<String, JSONObject>> hasLibraryToVersionEdge = new HashMap<>();
			for(CallLibrary<MicroService> call : microServiceCallLibraries) {
				for(Library lib : call.getCallLibraries()) {
					String libraryGroupAndName = lib.groupIdAndName();
					Map<String, JSONObject> temp = hasLibraryToVersionEdge.getOrDefault(lib.getId(), new HashMap<>());
					JSONObject edge = temp.get(libraryGroupAndName);
					if(edge == null) {
						edge = CytoscapeUtil.relationToEdge(lib.getId(), libraryGroupAndName, "LibraryVersionIsFromLibrary", "", false);
						edges.add(edge);
						temp.put(libraryGroupAndName, edge);
						hasLibraryToVersionEdge.put(lib.getId(), temp);
					}
					edges.add(CytoscapeUtil.relationToEdge(call.getCaller(), lib, "MicroServiceCallLibrary", call.timesOfCallLib(lib) + "", false));
				}
			}
		}
		if(showCntOfDevUpdMs) {
			for(DeveloperUpdateNode<MicroService> update : cntOfDevUpdMs) {
				MicroService ms = update.getNode();
				Developer developer = update.getDeveloper();
				if(ms == null || developer == null) {
					continue;
				}
				int times = update.getTimes();
				edges.add(CytoscapeUtil.relationToEdge(ms, developer, "MicroServiceUpdatedByDeveloper", times + "", false));
			}
		}
		JSONObject data = new JSONObject();
		data.put("edges", edges);
		result.put("value", data);
		return result;
	}
	
	public Map<String, List<FunctionCloneFunction>> cloneDetails() {
		Map<String, List<FunctionCloneFunction>> result = new HashMap<>();
		for(Clone<MicroService> clone : getClonesInMicroService()) {
			result.put(clone.getId(), clone.getChildren());
		}
		return result;
	}
}
