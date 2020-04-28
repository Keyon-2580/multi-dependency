package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.MicroServiceCallWithEntry;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

@Controller
@RequestMapping("/multiple/all")
public class MDAllController {

    @Autowired
    private GitAnalyseService gitAnalyseService;

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
	
	@PostMapping(value = "")
	@ResponseBody
	public JSONObject all(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showClonesInMicroService", true);
			boolean showMicroServiceCallLibs = (boolean) params.getOrDefault("showMicroServiceCallLibs", true);
			boolean showCntOfDevUpdMs = (boolean) params.getOrDefault("showCntOfDevUpdMs", true);
			System.out.println(showStructure + " " + showClonesInMicroService + " " + showMicroServiceCallLibs + " " + showCntOfDevUpdMs);
			MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(featureOrganizationService.allTestCases());
			callsWithEntry.setShowAllFeatures(true);
			callsWithEntry.setShowAllMicroServices(true);
			callsWithEntry.setShowAllScenarios(true);
			callsWithEntry.setShowStructure(showStructure);
			callsWithEntry.setShowClonesInMicroService(showClonesInMicroService);
			callsWithEntry.setShowMicroServiceCallLibs(showMicroServiceCallLibs);
			callsWithEntry.setShowCntOfDevUpdMs(showCntOfDevUpdMs);
			if(showClonesInMicroService) {
				callsWithEntry.setClonesInMicroService(msService.findMicroServiceClone(staticAnalyseService.findAllFunctionCloneFunctions(), true));
			}
			if(showMicroServiceCallLibs) {
				callsWithEntry.setMicroServiceCallLibraries(msService.findAllMicroServiceCallLibraries());
			}
			if(showCntOfDevUpdMs) {
				callsWithEntry.setCntOfDevUpdMs(gitAnalyseService.cntOfDevUpdMsList());
			}
			
			result.put("result", "success");
			result.put("value", callsWithEntry.toCytoscapeWithStructure());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@PostMapping(value = "/{TestcaseOrFeatureOrScenario}")
	@ResponseBody
	public JSONObject allTestCaseOrFeatureOrScenario(@PathVariable("TestcaseOrFeatureOrScenario") String testCaseOrFeatureOrScenario, 
			@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			@SuppressWarnings("unchecked")
			List<String> idsStr = (List<String>) params.getOrDefault("ids", new ArrayList<>());
			boolean showStructure = (boolean) params.getOrDefault("showStructure", true);
			boolean showClonesInMicroService = (boolean) params.getOrDefault("showClonesInMicroService", true);
			boolean showMicroServiceCallLibs = (boolean) params.getOrDefault("showMicroServiceCallLibs", true);
			boolean showCntOfDevUpdMs = (boolean) params.getOrDefault("showCntOfDevUpdMs", true);
			System.out.println(testCaseOrFeatureOrScenario + " " + showStructure + " " + showClonesInMicroService + " " + showMicroServiceCallLibs + " " + showCntOfDevUpdMs);
			List<Integer> ids = new ArrayList<>();
			for(String idStr : idsStr) {
				ids.add(Integer.parseInt(idStr));
			}
			Iterable<TestCase> allTestCases = featureOrganizationService.allTestCases();
			Iterable<TestCase> selectTestCases = null;
			
			switch(testCaseOrFeatureOrScenario) {
			case "testcase":
				List<TestCase> temp = new ArrayList<>();
				for(TestCase testCase :allTestCases) {
					int id = testCase.getTestCaseId();
					if(ids.contains(id)) {
						temp.add(testCase);
					}
				}
				selectTestCases = temp;
				break;
			case "feature":
				List<Feature> features = new ArrayList<>();
				for(Feature feature : featureOrganizationService.allFeatures()) {
					if(ids.contains(feature.getFeatureId())) {
						features.add(feature);
					}
				}
				selectTestCases = featureOrganizationService.relatedTestCaseWithFeatures(features);
				break;
			case "scenario":
				List<Scenario> scenarios = new ArrayList<>();
				for(Scenario scenario : featureOrganizationService.allScenarios()) {
					if(ids.contains(scenario.getScenarioId())) {
						scenarios.add(scenario);
					}
				}
				selectTestCases = featureOrganizationService.relatedTestCaseWithScenarios(scenarios);
				break;
			}
			result.put("result", "success");
			result.put("value", testCaseEdges(showStructure, showClonesInMicroService, showMicroServiceCallLibs, showCntOfDevUpdMs, selectTestCases));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	private JSONObject testCaseEdges(boolean showStructure, boolean showClonesInMicroService, 
			boolean showMicroServiceCallLibs, boolean showCntOfDevUpdMs,
			Iterable<TestCase> selectTestCases) {
		MicroServiceCallWithEntry callsWithEntry = featureOrganizationService.findMsCallMsByTestCases(selectTestCases);
		callsWithEntry.setMsDependOns(msService.msDependOns());
		callsWithEntry.setShowStructure(showStructure);
		callsWithEntry.setShowMicroServiceCallLibs(showMicroServiceCallLibs);
		callsWithEntry.setShowClonesInMicroService(showClonesInMicroService);
		callsWithEntry.setShowCntOfDevUpdMs(showCntOfDevUpdMs);
		
		if(showClonesInMicroService) {
			callsWithEntry.setClonesInMicroService(msService.findMicroServiceClone(staticAnalyseService.findAllFunctionCloneFunctions(), true));
		}
		if(showMicroServiceCallLibs) {
			callsWithEntry.setMicroServiceCallLibraries(msService.findAllMicroServiceCallLibraries());
		}
		if(showCntOfDevUpdMs) {
			callsWithEntry.setCntOfDevUpdMs(gitAnalyseService.cntOfDevUpdMsList());
		}
		return callsWithEntry.testCaseEdges();
	}
}
