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

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
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
	
//	@Autowired
//	private DynamicAnalyseService dynamicAnalyseService;
	
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
	
	@GetMapping("/apis")
	@ResponseBody
	public JSONObject findProjectCallAPIs() {
		JSONObject result = new JSONObject();
		Iterable<Project> projects = staticAnalyseService.allProjects().values();
		JSONObject values = new JSONObject();
		for(Project project : projects) {
			CallLibrary<Project> call = staticAnalyseService.findProjectCallLibraries(project);
			System.out.println(call.getCallAPITimes());
			values.put(project.getName(), call);
		}
		result.put("result", "success");
		result.put("projectValues", values);
		
		values = new JSONObject();
		Iterable<MicroService> mss = msService.findAllMicroService().values();
		for(MicroService ms : mss) {
			CallLibrary<MicroService> call = msService.findMicroServiceCallLibraries(ms);
			values.put(ms.getName(), call);
		}
		result.put("msValues", values);
		return result;
	}
	
	@GetMapping("/clones")
	@ResponseBody
	public JSONObject findProjectClones() {
		JSONObject result = new JSONObject();
		Iterable<FunctionCloneFunction> allClones = staticAnalyseService.findAllFunctionCloneFunctions();
		Iterable<Clone<Project>> clones = staticAnalyseService.findProjectClone(allClones, true);
		result.put("result", "success");
		result.put("projectValues", clones);
		Iterable<Clone<MicroService>> msClones = msService.findMicroServiceClone(allClones, true);
		result.put("msValues", msClones);
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
			callsWithEntry.setShowClonesInMicroService(true);
			callsWithEntry.setShowMicroServiceCallLibs(true);
			
			callsWithEntry.setClonesInMicroService(msService.findMicroServiceClone(staticAnalyseService.findAllFunctionCloneFunctions(), true));
			callsWithEntry.setMicroServiceCallLibraries(msService.findAllMicroServiceCallLibraries());

			
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
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showClonesInMicroService", true);
			boolean showMicroServiceCallLibs = (boolean) params.getOrDefault("showMicroServiceCallLibs", true);
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
			result.put("result", "success");
			result.put("value", testCaseEdges(showStructure, showClonesInMicroService, showMicroServiceCallLibs, selectTestCases));
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
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showClonesInMicroService", true);
			boolean showMicroServiceCallLibs = (boolean) params.getOrDefault("showMicroServiceCallLibs", true);
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
			result.put("result", "success");
			result.put("value", testCaseEdges(showStructure, showClonesInMicroService, showMicroServiceCallLibs, selectTestCases));
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
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showClonesInMicroService", true);
			boolean showMicroServiceCallLibs = (boolean) params.getOrDefault("showMicroServiceCallLibs", true);
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
			result.put("result", "success");
			result.put("value", testCaseEdges(showStructure, showClonesInMicroService, showMicroServiceCallLibs, selectTestCases));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	private JSONObject testCaseEdges(boolean showStructure, boolean showClonesInMicroService, 
			boolean showMicroServiceCallLibs, 
			Iterable<TestCase> selectTestCases) {
		MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
		callsWithEntry.setMsDependOns(msService.msDependOns());
		callsWithEntry.setShowStructure(showStructure);
		callsWithEntry.setShowMicroServiceCallLibs(showMicroServiceCallLibs);
		callsWithEntry.setShowClonesInMicroService(showClonesInMicroService);
		
		if(showClonesInMicroService) {
			callsWithEntry.setClonesInMicroService(msService.findMicroServiceClone(staticAnalyseService.findAllFunctionCloneFunctions(), true));
		}
		if(showMicroServiceCallLibs) {
			callsWithEntry.setMicroServiceCallLibraries(msService.findAllMicroServiceCallLibraries());
		}
		return callsWithEntry.testCaseEdges();
	}
}
