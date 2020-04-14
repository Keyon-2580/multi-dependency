package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.MicroServiceCallWithEntry;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;

@Controller
public class MDController {

	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private MicroserviceService msService;

	@GetMapping(value= {"/", "/index"})
	public String index(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
		return "index";
	}
	
	@GetMapping("/multiple/microservice")
	public String multipleMicroService(HttpServletRequest request) {
		Map<String, List<TestCase>> allTestCases = featureOrganizationService.allTestCasesGroupByTestCaseGroup();
		request.setAttribute("testCases", allTestCases);
		Iterable<Scenario> allScenarios = featureOrganizationService.allScenarios();
		request.setAttribute("scenarios", allScenarios);
		Iterable<Feature> allFeatures = featureOrganizationService.allFeatures();
		request.setAttribute("features", allFeatures);
		return "structure_testcase_microservice/multiple_microservice";
	}
	
	@PostMapping(value = "/multiple/microservice/query/edges")
	@ResponseBody
	public JSONObject getMicroServiceCallChainWithTestCaseId(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			List<String> idsStr = (List<String>) params.get("ids");
			boolean callChain = (boolean) params.getOrDefault("callChain", false);
			List<Long> ids = new ArrayList<>();
			for(String idStr : idsStr) {
				ids.add(Long.parseLong(idStr));
			}
			Iterable<TestCase> allTestCases = featureOrganizationService.allTestCases();
			List<TestCase> selectTestCases = new ArrayList<>();
			for(TestCase testCase :allTestCases) {
				Long id = testCase.getId();
				if(ids.contains(id)) {
					selectTestCases.add(testCase);
				}
			}
			MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
			JSONArray nodes = callsWithEntry.relatedMicroServiceIds();
			JSONArray edges = callsWithEntry.relatedEdgeIds(callChain);
			
			result.put("result", "success");
			result.put("edges", edges);
			result.put("nodes", nodes);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/multiple/microservice/query/scenario")
	@ResponseBody
	public JSONObject getMicroServiceScenario(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			@SuppressWarnings("unchecked")
			List<String> idsStr = (List<String>) params.getOrDefault("ids", new ArrayList<>());
			boolean showAllFeatures = (boolean) params.getOrDefault("showAllFeatures", true);
			boolean showAllMicroServices = (boolean) params.getOrDefault("showAllMicroServices", true);
			boolean showAllScenarios = (boolean) params.getOrDefault("showAllScenarios", true);
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
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
			MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
			callsWithEntry.setMsDependOns(msService.msDependOns());
			callsWithEntry.setShowAllFeatures(showAllFeatures);
			callsWithEntry.setShowAllMicroServices(showAllMicroServices);
			callsWithEntry.setShowStructure(showStructure);
			callsWithEntry.setShowAllScenarios(showAllScenarios);
			result.put("result", "success");
			result.put("value", callsWithEntry.toCytoscapeWithStructure());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/multiple/microservice/query/feature")
	@ResponseBody
	public JSONObject getMicroServiceFeature(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			@SuppressWarnings("unchecked")
			List<String> idsStr = (List<String>) params.getOrDefault("ids", new ArrayList<>());
			boolean showAllFeatures = (boolean) params.getOrDefault("showAllFeatures", true);
			boolean showAllMicroServices = (boolean) params.getOrDefault("showAllMicroServices", true);
			boolean showAllScenarios = (boolean) params.getOrDefault("showAllScenarios", true);
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
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
			MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
			callsWithEntry.setMsDependOns(msService.msDependOns());
			callsWithEntry.setShowAllFeatures(showAllFeatures);
			callsWithEntry.setShowAllMicroServices(showAllMicroServices);
			callsWithEntry.setShowStructure(showStructure);
			callsWithEntry.setShowAllScenarios(showAllScenarios);
			result.put("result", "success");
			result.put("value", callsWithEntry.toCytoscapeWithStructure());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/multiple/microservice/query/testcase")
	@ResponseBody
	public JSONObject getMicroServiceEntry(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			@SuppressWarnings("unchecked")
			List<String> idsStr = (List<String>) params.getOrDefault("ids", new ArrayList<>());
			boolean showAllFeatures = (boolean) params.getOrDefault("showAllFeatures", true);
			boolean showAllMicroServices = (boolean) params.getOrDefault("showAllMicroServices", true);
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
			boolean showAllScenarios = (boolean) params.getOrDefault("showAllScenarios", true);
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
			MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
			callsWithEntry.setMsDependOns(msService.msDependOns());
			callsWithEntry.setShowAllFeatures(showAllFeatures);
			callsWithEntry.setShowAllMicroServices(showAllMicroServices);
			callsWithEntry.setShowStructure(showStructure);
			callsWithEntry.setShowAllScenarios(showAllScenarios);
			result.put("result", "success");
			result.put("value", callsWithEntry.toCytoscapeWithStructure());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
}
