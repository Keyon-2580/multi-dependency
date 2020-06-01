package cn.edu.fudan.se.multidependency.service.spring.data;

import java.util.ArrayList;
import java.util.Collection;
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
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.utils.CytoscapeUtil;
import cn.edu.fudan.se.multidependency.utils.CytoscapeUtil.CytoscapeEdge;
import cn.edu.fudan.se.multidependency.utils.CytoscapeUtil.CytoscapeNode;
import cn.edu.fudan.se.multidependency.utils.MicroServiceUtil;
import cn.edu.fudan.se.multidependency.utils.ZTreeUtil.ZTreeNode;
import lombok.Data;
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
	
	private Collection<Clone<MicroService, FunctionCloneFunction>> clonesInMicroServiceFromFunctionClone = new ArrayList<>();
	
	private Collection<Clone<MicroService, FileCloneFile>> clonesInMicroServiceFromFileClone = new ArrayList<>();
	
	private Iterable<CallLibrary<MicroService>> microServiceCallLibraries = new ArrayList<>();
	
	private Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMs;
	
	private Map<MicroService, CloneLineValue<MicroService>> msToCloneLineValue = new HashMap<>();
	
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
	
	public List<CytoscapeEdge> relatedEdgeObjs() {
		List<CytoscapeEdge> result = new ArrayList<>();
		for(TestCase testCase : testCaseToEntries.keySet()) {
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				result.add(new CytoscapeEdge(testCase.getId() + "_" + entry.getId(), testCase.getId().toString(), entry.getId().toString(), ""));
			}
		}
		for(MicroService ms : calls.keySet()) {
			for(MicroService callMs : calls.get(ms).keySet()) {
				JSONObject obj = new JSONObject();
				obj.put("id", ms.getId() + "_" + callMs.getId());
				obj.put("source", ms.getId());
				obj.put("target", callMs.getId());
				obj.put("value", calls.get(ms).get(callMs).getTimes());
				result.add(new CytoscapeEdge(ms.getId() + "_" + callMs.getId(), ms.getId().toString(), callMs.getId().toString(), ""));
			}
		}
		return result;
	}
	
	public JSONArray relatedEdgeIds() {
		JSONArray result = new JSONArray();
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
	
	private void showClonesInMicroService(List<CytoscapeEdge> edges, Class<? extends CloneRelation> relationClass) {
		if(relationClass == FunctionCloneFunction.class) {
			for(Clone<MicroService, FunctionCloneFunction> clone : clonesInMicroServiceFromFunctionClone) {
				if(clone.sizeOfChildren() >= showClonesMinPair) {
//					CytoscapeEdge edge = new CytoscapeEdge(clone.getId(), clone.getNode1().getId().toString(), clone.getNode2().getId().toString(), "all_MicroService_clone_MicroService", clone.calculateValue());
					CytoscapeEdge edge = new CytoscapeEdge(clone.getId(), clone.getNode1().getId().toString(), clone.getNode2().getId().toString(), "all_MicroService_clone_MicroService", "");
					edges.add(edge);
				}
			}
		} else {
			for(Clone<MicroService, FileCloneFile> clone : clonesInMicroServiceFromFileClone) {
				if(clone.sizeOfChildren() >= showClonesMinPair) {
//					CytoscapeEdge edge = new CytoscapeEdge(clone.getId(), clone.getNode1().getId().toString(), clone.getNode2().getId().toString(), "all_MicroService_clone_MicroService", clone.calculateValue());
					CytoscapeEdge edge = new CytoscapeEdge(clone.getId(), clone.getNode1().getId().toString(), clone.getNode2().getId().toString(), "all_MicroService_clone_MicroService", "");
					edges.add(edge);
				}
			}
		}
	}
	
	public JSONObject toCytoscapeWithStructure() {
		JSONObject result = new JSONObject();
		List<CytoscapeNode> nodes = new ArrayList<>();
		List<CytoscapeEdge> edges = new ArrayList<>();
		Map<MicroService, Boolean> isMicroServiceNodeAdd = new HashMap<>();
		Map<Scenario, Boolean> isScenarioNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeParent = new HashMap<>();
		Map<Library, Boolean> isLibraryNodeAdd = new HashMap<>();
		Map<String, Boolean> isLibraryWithoutVersionNodeAdd = new HashMap<>();
		Map<Developer, Boolean> isDeveloperNodeAdd = new HashMap<>();
		
		JSONArray ztreeNodes = new JSONArray();
		result.put("ztreeNodes", ztreeNodes);
		ZTreeNode msRoot = new ZTreeNode(ZTreeNode.DEFAULT_ID, "微服务", false, ZTreeNode.DEFAULT_TYPE, true);
		ZTreeNode testcaseRoot = new ZTreeNode(ZTreeNode.DEFAULT_ID, "测试用例", false, ZTreeNode.DEFAULT_TYPE, true);
		ZTreeNode featureRoot = new ZTreeNode(ZTreeNode.DEFAULT_ID, "特性", false, ZTreeNode.DEFAULT_TYPE, true);
		ZTreeNode libRoot = new ZTreeNode(ZTreeNode.DEFAULT_ID, "三方库", false, ZTreeNode.DEFAULT_TYPE, true);
		ZTreeNode scenarioRoot = new ZTreeNode(ZTreeNode.DEFAULT_ID, "场景", false, ZTreeNode.DEFAULT_TYPE, true);
		ZTreeNode developerRoot = new ZTreeNode(ZTreeNode.DEFAULT_ID, "开发者", false, ZTreeNode.DEFAULT_TYPE, true);
		
		if(showAllScenarios) {
			for(Scenario scenario : allScenarios) {
				if(!isScenarioNodeAdd.getOrDefault(scenario, false)) {
					nodes.add(new CytoscapeNode(scenario.getId(), scenario.getScenarioId() + " : " + scenario.getName(),  "Scenario"));
					isScenarioNodeAdd.put(scenario, true);
					
					scenarioRoot.addChild(new ZTreeNode(scenario, false));
				}
			}
		}
		if(showAllMicroServices) {
			for(MicroService ms : allMicroServices) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
					nodes.add(new CytoscapeNode(ms.getId(), ms.getName(), "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
					
					msRoot.addChild(new ZTreeNode(ms, false));
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
					nodes.add(new CytoscapeNode(developer.getId(), developer.getName(), "Developer"));
					isDeveloperNodeAdd.put(developer, true);
					
					developerRoot.addChild(new ZTreeNode(developer, false));
				}
				edges.add(new CytoscapeEdge(null, ms.getId().toString(), developer.getId().toString(), "MicroServiceUpdatedByDeveloper", times + ""));
			}
		}
		if(showAllFeatures) {
			for(Feature feature : allFeatures) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(new CytoscapeNode(feature.getId(), feature.getFeatureId() + " : " + feature.getName(), "Feature"));
					isFeatureNodeAdd.put(feature, true);
					featureRoot.addChild(new ZTreeNode(feature, false));
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(new CytoscapeNode(parentFeature.getId(), parentFeature.getFeatureId() + " : " + parentFeature.getName(), "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
						featureRoot.addChild(new ZTreeNode(parentFeature, false));
					}
					edges.add(new CytoscapeEdge(parentFeature, feature, "", ""));
					isFeatureNodeParent.put(feature, true);
				}
			}
		}
		
		if(showClonesInMicroService) {
			showClonesInMicroService(edges, FileCloneFile.class);
		}
		
		if(showMicroServiceCallLibs) {
			Map<Long, Map<String, CytoscapeEdge>> hasLibraryToVersionEdge = new HashMap<>();
			for(CallLibrary<MicroService> call : microServiceCallLibraries) {
				for(Library lib : call.getCallLibraries()) {
					if(!isLibraryNodeAdd.getOrDefault(lib, false)) {
						nodes.add(new CytoscapeNode(lib.getId(), lib.getFullName(), "Library"));
						isLibraryNodeAdd.put(lib, true);
						///FIXME
						libRoot.addChild(new ZTreeNode(lib, false));
					}
					String libraryGroupAndName = lib.groupIdAndName();
					if(!isLibraryWithoutVersionNodeAdd.getOrDefault(libraryGroupAndName, false)) {
						nodes.add(new CytoscapeNode(libraryGroupAndName, libraryGroupAndName, "Library", ""));
						isLibraryWithoutVersionNodeAdd.put(libraryGroupAndName, true);
					}
					Map<String, CytoscapeEdge> temp = hasLibraryToVersionEdge.getOrDefault(lib.getId(), new HashMap<>());
					CytoscapeEdge edge = temp.get(libraryGroupAndName);
					if(edge == null) {
						edge = new CytoscapeEdge(null, String.valueOf(lib.getId()), libraryGroupAndName, "LibraryVersionIsFromLibrary", "");
						edges.add(edge);
						temp.put(libraryGroupAndName, edge);
						hasLibraryToVersionEdge.put(lib.getId(), temp);
					}
					edges.add(new CytoscapeEdge(null, call.getCaller().getId().toString(), lib.getId().toString(), "MicroServiceCallLibrary", call.timesOfCallLib(lib) + ""));
				}
			}
		}
		
		for(TestCase testCase : testCaseToEntries.keySet()) {
			nodes.add(new CytoscapeNode(testCase.getId(), String.join(" : ", " " + testCase.getTestCaseId(), testCase.getName() + " "), "TestCase_" + (testCase.isSuccess() ? "success" : "fail")));
			
			testcaseRoot.addChild(new ZTreeNode(testCase, false));
			
			Scenario scenario = this.scenarioDefineTestCases.get(testCase);
			if(scenario != null) {
				if(!isScenarioNodeAdd.getOrDefault(scenario, false)) {
					nodes.add(new CytoscapeNode(scenario.getId(), scenario.getScenarioId() + " : " + scenario.getName(),  "Scenario"));
					isScenarioNodeAdd.put(scenario, true);
					
					scenarioRoot.addChild(new ZTreeNode(scenario, false));
				}
				edges.add(new CytoscapeEdge(scenario, testCase, "ScenarioDefineTestCase", ""));
			}
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				if(!isMicroServiceNodeAdd.getOrDefault(entry, false)) {
					nodes.add(new CytoscapeNode(entry.getId(), entry.getName(), "MicroService"));
					isMicroServiceNodeAdd.put(entry, true);
					
					msRoot.addChild(new ZTreeNode(entry, false));
				}
				edges.add(new CytoscapeEdge(testCase, entry, "TestCaseExecuteMicroService", ""));
			}
			List<Feature> features = testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>());
			for(Feature feature : features) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(new CytoscapeNode(feature.getId(), feature.getFeatureId() + " : " + feature.getName(), "Feature"));
					isFeatureNodeAdd.put(feature, true);
					
					featureRoot.addChild(new ZTreeNode(feature, false));
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(new CytoscapeNode(parentFeature.getId(), parentFeature.getFeatureId() + " : " + parentFeature.getName(), "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
						
						featureRoot.addChild(new ZTreeNode(parentFeature, false));
					}
					edges.add(new CytoscapeEdge(parentFeature, feature, "", ""));
					isFeatureNodeParent.put(feature, true);
				}
				edges.add(new CytoscapeEdge(feature, testCase, "FeatureExecutedByTestCase", ""));
			}
		}
		
		if(showStructure) {
			for(MicroService ms : msDependOns.keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false) && !msDependOns.get(ms).isEmpty()) {
					nodes.add(new CytoscapeNode(ms.getId(), ms.getName(), "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
					
					msRoot.addChild(new ZTreeNode(ms, false));
				}
				for(MicroService callMs : msDependOns.get(ms).keySet()) {
					if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
						nodes.add(new CytoscapeNode(callMs.getId(), callMs.getName(), "MicroService"));
						isMicroServiceNodeAdd.put(callMs, true);
						
						msRoot.addChild(new ZTreeNode(callMs, false));
					}
					if(MicroServiceUtil.isMicroServiceCall(ms, callMs, calls)) {
						edges.add(new CytoscapeEdge(ms, callMs, "ShowStructureDependOnCall"));
					} else {
						edges.add(new CytoscapeEdge(ms, callMs, "ShowStructureDependOn"));
					}
				}
			}
			
			for(MicroService ms : calls.keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false) && !calls.get(ms).isEmpty()) {
					nodes.add(new CytoscapeNode(ms.getId(), ms.getName(), "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);

					msRoot.addChild(new ZTreeNode(ms, false));
				}
				for(MicroService callMs : calls.get(ms).keySet()) {
					if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
						nodes.add(new CytoscapeNode(callMs.getId(), callMs.getName(), "MicroService"));
						isMicroServiceNodeAdd.put(callMs, true);

						msRoot.addChild(new ZTreeNode(callMs, false));
					}
					if(!MicroServiceUtil.isMicroServiceDependOn(ms, callMs, msDependOns)) {
						edges.add(new CytoscapeEdge(ms, callMs, "ShowStructureCall"));
					}
				}
			}
		} else {
			for(MicroService ms : calls.keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false) && !calls.get(ms).isEmpty()) {
					nodes.add(new CytoscapeNode(ms.getId(), ms.getName(), "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);

					msRoot.addChild(new ZTreeNode(ms, false));
				}
				for(MicroService callMs : calls.get(ms).keySet()) {
					if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
						nodes.add(new CytoscapeNode(callMs.getId(), callMs.getName(), "MicroService"));
						isMicroServiceNodeAdd.put(callMs, true);
						
						msRoot.addChild(new ZTreeNode(callMs, false));
					}
					edges.add(new CytoscapeEdge(ms, callMs, "NoStructureCall"));
				}
			}
		}

		ztreeNodes.add(msRoot.toJSON());
		ztreeNodes.add(testcaseRoot.toJSON());
		ztreeNodes.add(featureRoot.toJSON());
		ztreeNodes.add(libRoot.toJSON());
		ztreeNodes.add(scenarioRoot.toJSON());
		ztreeNodes.add(developerRoot.toJSON());
		
		JSONObject data = new JSONObject();
		data.put("nodes", CytoscapeUtil.toNodes(nodes));
		data.put("edges", CytoscapeUtil.toEdges(edges));
		result.put("data", data);
		return result;	
	}
	
	public JSONObject toCytoscapeWithOutStructure() {
		JSONObject result = new JSONObject();
		List<CytoscapeNode> nodes = new ArrayList<>();
		List<CytoscapeEdge> edges = new ArrayList<>();
		Map<MicroService, Boolean> isMicroServiceNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeAdd = new HashMap<>();
		Map<Feature, Boolean> isFeatureNodeParent = new HashMap<>();
		
		if(showAllMicroServices) {
			for(MicroService ms : allMicroServices) {
				if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
					nodes.add(new CytoscapeNode(ms.getId(), ms.getName(), "MicroService"));
					isMicroServiceNodeAdd.put(ms, true);
				}
			}
		}
		if(showAllFeatures) {
			for(Feature feature : allFeatures) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(new CytoscapeNode(feature.getId(), feature.getFeatureId() + " : " + feature.getName(), "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(new CytoscapeNode(parentFeature.getId(), parentFeature.getFeatureId() + " : " + parentFeature.getName(), "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(new CytoscapeEdge(parentFeature, feature, "", ""));
					isFeatureNodeParent.put(feature, true);
				}
			}
		}
		
		for(TestCase testCase : testCaseToEntries.keySet()) {
			nodes.add(new CytoscapeNode(testCase.getId(), String.join(" : ", " " + testCase.getTestCaseId(), testCase.getName() + " "), "TestCase_" + (testCase.isSuccess() ? "success" : "fail")));
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				if(!isMicroServiceNodeAdd.getOrDefault(entry, false)) {
					nodes.add(new CytoscapeNode(entry.getId(), entry.getName(), "MicroService"));
					isMicroServiceNodeAdd.put(entry, true);
				}
				edges.add(new CytoscapeEdge(testCase, entry, "", ""));
			}
			List<Feature> features = testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>());
			for(Feature feature : features) {
				if(!isFeatureNodeAdd.getOrDefault(feature, false)) {
					nodes.add(new CytoscapeNode(feature.getId(), feature.getFeatureId() + " : " + feature.getName(), "Feature"));
					isFeatureNodeAdd.put(feature, true);
				}
				Feature parentFeature = featureToParentFeature.get(feature);
				if(parentFeature != null && !isFeatureNodeParent.getOrDefault(feature, false)) {
					if(!isFeatureNodeAdd.getOrDefault(parentFeature, false)) {
						nodes.add(new CytoscapeNode(parentFeature.getId(), parentFeature.getFeatureId() + " : " + parentFeature.getName(), "Feature"));
						isFeatureNodeAdd.put(parentFeature, true);
					}
					edges.add(new CytoscapeEdge(parentFeature, feature, "", "is child of"));
					isFeatureNodeParent.put(feature, true);
				}
				edges.add(new CytoscapeEdge(feature, testCase, "", ""));
			}
		}
		for(MicroService ms : calls.keySet()) {
			if(!isMicroServiceNodeAdd.getOrDefault(ms, false)) {
				nodes.add(new CytoscapeNode(ms.getId(), ms.getName(), "MicroService"));
				isMicroServiceNodeAdd.put(ms, true);
			}
			for(MicroService callMs : calls.get(ms).keySet()) {
				if(!isMicroServiceNodeAdd.getOrDefault(callMs, false)) {
					nodes.add(new CytoscapeNode(callMs.getId(), callMs.getName(), "MicroService"));
					isMicroServiceNodeAdd.put(callMs, true);
				}
				edges.add(new CytoscapeEdge(ms, callMs, "", ""));
			}
		}
		
		JSONObject data = new JSONObject();
		data.put("nodes", CytoscapeUtil.toNodes(nodes));
		data.put("edges", CytoscapeUtil.toEdges(edges));
		result.put("value", data);
		return result;
	}
	
	public List<CytoscapeEdge> structureEdges(String type) {
		List<CytoscapeEdge> edges = new ArrayList<>();
		for(MicroService ms : msDependOns.keySet()) {
			for(MicroService callMs : msDependOns.get(ms).keySet()) {
				edges.add(new CytoscapeEdge(null, ms.getId().toString(), callMs.getId().toString(), type));
			}
		}
		return edges;
	}
	
	public List<CytoscapeEdge> parentFeatureEdges(String type) {
		List<CytoscapeEdge> edges = new ArrayList<>();
		for(Feature feature : allFeatures) {
			Feature parentFeature = featureToParentFeature.get(feature);
			if(parentFeature != null) {
				edges.add(new CytoscapeEdge(parentFeature, feature, type));
			}
		}
		return edges;
	}

	public JSONObject testCaseEdges() {
		JSONObject result = new JSONObject();
		List<CytoscapeEdge> edges = new ArrayList<>();
		
		if(showStructure) {
			System.out.println("showStructure " + msDependOns.size());
			List<CytoscapeEdge> structureEdges = structureEdges("all_MicroService_DependOn_MicroService");
			edges.addAll(structureEdges);
		}
		
		List<CytoscapeEdge> parentFeatureEdges = parentFeatureEdges("all_Feature_Contain_Feature");
		edges.addAll(parentFeatureEdges);
		
		for(TestCase testCase : testCaseToEntries.keySet()) {
			Scenario scenario = this.scenarioDefineTestCases.get(testCase);
			if(scenario != null) {
				edges.add(new CytoscapeEdge(scenario, testCase, "all_ScenarioDefineTestCase"));
			}
			List<MicroService> entries = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			for(MicroService entry : entries) {
				edges.add(new CytoscapeEdge(testCase, entry, "all_TestCaseExecuteMicroService"));
			}
			List<Feature> features = testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>());
			for(Feature feature : features) {
				edges.add(new CytoscapeEdge(testCase, feature, "all_FeatureExecutedByTestCase"));
			}
		}
		
		for(MicroService ms : calls.keySet()) {
			for(MicroService callMs : calls.get(ms).keySet()) {
				edges.add(new CytoscapeEdge(ms, callMs, "all_MicroService_call_MicroService"));
			}
		}
		
		if(showClonesInMicroService) {
			showClonesInMicroService(edges, FileCloneFile.class);
		}
		
		if(showMicroServiceCallLibs) {
			Map<Long, Map<String, CytoscapeEdge>> hasLibraryToVersionEdge = new HashMap<>();
			for(CallLibrary<MicroService> call : microServiceCallLibraries) {
				for(Library lib : call.getCallLibraries()) {
					String libraryGroupAndName = lib.groupIdAndName();
					Map<String, CytoscapeEdge> temp = hasLibraryToVersionEdge.getOrDefault(lib.getId(), new HashMap<>());
					CytoscapeEdge edge = temp.get(libraryGroupAndName);
					if(edge == null) {
						edge = new CytoscapeEdge(null, lib.getId().toString(), libraryGroupAndName, "LibraryVersionIsFromLibrary", "");
						edges.add(edge);
						temp.put(libraryGroupAndName, edge);
						hasLibraryToVersionEdge.put(lib.getId(), temp);
					}
					edges.add(new CytoscapeEdge(null, call.getCaller().getId().toString(), lib.getId().toString(), "MicroServiceCallLibrary", call.timesOfCallLib(lib) + ""));
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
				edges.add(new CytoscapeEdge(null, ms.getId().toString(), developer.getId().toString(), "MicroServiceUpdatedByDeveloper", times + ""));
			}
		}
		JSONObject data = new JSONObject();
		data.put("edges", CytoscapeUtil.toEdges(edges));
		result.put("value", data);
		return result;
	}
	
	public Map<String, Collection<FunctionCloneFunction>> cloneDetails() {
		Map<String, Collection<FunctionCloneFunction>> result = new HashMap<>();
		for(Clone<MicroService, FunctionCloneFunction> clone : getClonesInMicroServiceFromFunctionClone()) {
			result.put(clone.getId(), clone.getChildren());
		}
		return result;
	}
}
