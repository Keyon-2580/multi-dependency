package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.MicroServiceCallWithEntry;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

@Controller
@RequestMapping("/multiple/all")
public class MDAllController {

	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private MicroserviceService msService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@GetMapping("")
	public String multipleMicroServiceAll(HttpServletRequest request) {
		Map<String, List<TestCase>> allTestCases = featureOrganizationService.allTestCasesGroupByTestCaseGroup();
		request.setAttribute("testCases", allTestCases);
		Iterable<Scenario> allScenarios = featureOrganizationService.allScenarios();
		request.setAttribute("scenarios", allScenarios);
		Iterable<Feature> allFeatures = featureOrganizationService.allFeatures();
		request.setAttribute("features", allFeatures);
		return "structure_testcase_microservice/multiple_microservice_all";
	}
	
	@GetMapping("/clones")
	@ResponseBody
	public JSONObject findProjectClones() {
		JSONObject result = new JSONObject();
		Iterable<FunctionCloneFunction> allClones = staticAnalyseService.findAllFunctionCloneFunctions();
		Iterable<Clone> clones = staticAnalyseService.findProjectClone(allClones, true);
		result.put("result", "success");
		result.put("projectValues", clones);
		clones = msService.findMicroServiceClone(allClones, true);
		result.put("msValues", clones);
		return result;
	}
	
	@PostMapping(value = "")
	@ResponseBody
	public JSONObject all() {
		JSONObject result = new JSONObject();
		try {
			MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(featureOrganizationService.allTestCases());
			callsWithEntry.setShowAllFeatures(true);
			callsWithEntry.setShowAllMicroServices(true);
			callsWithEntry.setShowStructure(true);
			callsWithEntry.setShowAllScenarios(true);
			result.put("result", "success");
			result.put("value", callsWithEntry.toCytoscapeWithStructure());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/testcase")
	@ResponseBody
	public JSONObject allTestCases(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			@SuppressWarnings("unchecked")
			List<String> idsStr = (List<String>) params.getOrDefault("ids", new ArrayList<>());
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showCloneInMicroService", true);
			List<Integer> ids = new ArrayList<>();
			for(String idStr : idsStr) {
				ids.add(Integer.parseInt(idStr));
			}
			Iterable<TestCase> allTestCases = featureOrganizationService.allTestCases();
			List<TestCase> selectTestCases = new ArrayList<>();
			for(TestCase testCase :allTestCases) {
				int id = testCase.getTestCaseId();
				if(ids.contains(id)) {
					selectTestCases.add(testCase);
				}
			}
//			MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
//			callsWithEntry.setMsDependOns(msService.msDependOns());
//			callsWithEntry.setShowStructure(showStructure);
			result.put("result", "success");
//			result.put("value", callsWithEntry.testCaseEdges());
			result.put("value", testCaseEdges(showStructure, showClonesInMicroService, selectTestCases));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/scenario")
	@ResponseBody
	public JSONObject getMicroServiceScenario(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			@SuppressWarnings("unchecked")
			List<String> idsStr = (List<String>) params.getOrDefault("ids", new ArrayList<>());
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showCloneInMicroService", true);
			List<Integer> ids = new ArrayList<>();
			for(String idStr : idsStr) {
				ids.add(Integer.parseInt(idStr));
			}
			List<Scenario> scenarios = new ArrayList<>();
			for(Scenario scenario : featureOrganizationService.allScenarios()) {
				if(ids.contains(scenario.getScenarioId())) {
					scenarios.add(scenario);
				}
			}
			Iterable<TestCase> selectTestCases = featureOrganizationService.relatedTestCaseWithScenarios(scenarios);
//			MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
//			callsWithEntry.setMsDependOns(msService.msDependOns());
//			callsWithEntry.setShowStructure(showStructure);
			result.put("result", "success");
//			result.put("value", callsWithEntry.testCaseEdges());
			result.put("value", testCaseEdges(showStructure, showClonesInMicroService, selectTestCases));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/feature")
	@ResponseBody
	public JSONObject getMicroServiceFeature(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			@SuppressWarnings("unchecked")
			List<String> idsStr = (List<String>) params.getOrDefault("ids", new ArrayList<>());
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showCloneInMicroService", true);
			List<Integer> ids = new ArrayList<>();
			for(String idStr : idsStr) {
				ids.add(Integer.parseInt(idStr));
			}
			List<Feature> features = new ArrayList<>();
			for(Feature feature : featureOrganizationService.allFeatures()) {
				if(ids.contains(feature.getFeatureId())) {
					features.add(feature);
				}
			}
			Iterable<TestCase> selectTestCases = featureOrganizationService.relatedTestCaseWithFeatures(features);
			/*MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
			callsWithEntry.setMsDependOns(msService.msDependOns());
			callsWithEntry.setShowStructure(showStructure);
			
			if(showClonesInMicroService) {
				Iterable<FunctionCloneFunction> allClones = staticAnalyseService.findAllFunctionCloneFunctions();
				Iterable<Clone> clones = staticAnalyseService.findProjectClone(allClones, true);
				clones = msService.findMicroClone(allClones, true);
				callsWithEntry.setClonesInMicroService(clones);
				callsWithEntry.setShowClonesInMicroService(showClonesInMicroService);
			}*/
			result.put("result", "success");
//			result.put("value", callsWithEntry.testCaseEdges());
			result.put("value", testCaseEdges(showStructure, showClonesInMicroService, selectTestCases));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	private JSONObject testCaseEdges(boolean showStructure, boolean showClonesInMicroService, Iterable<TestCase> selectTestCases) {
		MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
		callsWithEntry.setMsDependOns(msService.msDependOns());
		callsWithEntry.setShowStructure(showStructure);
		
		if(showClonesInMicroService) {
			Iterable<FunctionCloneFunction> allClones = staticAnalyseService.findAllFunctionCloneFunctions();
			Iterable<Clone> clones = staticAnalyseService.findProjectClone(allClones, true);
			clones = msService.findMicroServiceClone(allClones, true);
			callsWithEntry.setClonesInMicroService(clones);
			callsWithEntry.setShowClonesInMicroService(showClonesInMicroService);
		}
		return callsWithEntry.testCaseEdges();
	}
}
