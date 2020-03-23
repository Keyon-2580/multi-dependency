package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.TestCaseCoverageService;

@Controller
@RequestMapping("/testcase")
public class TestCaseController {

	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private TestCaseCoverageService testCaseCoverageService;
	
	@GetMapping(value = {"/", "/index"})
	public String index(HttpServletRequest request) {
		Map<String, List<TestCase>> testCases = featureOrganizationService.allTestCasesGroupByTestCaseGroup();
		Iterable<Feature> features = featureOrganizationService.allFeatures();
		Map<TestCase, List<Feature>> testCaseToFeatures = new LinkedHashMap<>();
		Map<Feature, Boolean> isFeatureExecuted = new LinkedHashMap<>();
		List<TestCase> testCasesSortByGroup = new ArrayList<>();
		for(Collection<TestCase> groups : testCases.values()) {
			for(TestCase testCase : groups) {
				testCasesSortByGroup.add(testCase);
				List<Feature> hasExecute = new ArrayList<>();
				List<Feature> executeFeatures = featureOrganizationService.findTestCaseExecutionFeatures(testCase);
				for(Feature feature : features) {
					if(executeFeatures.contains(feature)) {
						isFeatureExecuted.put(feature, true);
						hasExecute.add(feature);
					} else {
						if(isFeatureExecuted.get(feature) == null) {
							isFeatureExecuted.put(feature, false);
						}
						hasExecute.add(null);
					}
				}
				testCaseToFeatures.put(testCase, hasExecute);
			}
		}
		System.out.println(isFeatureExecuted.keySet());
		List<Feature> executeFeatures = new ArrayList<>(isFeatureExecuted.keySet().size());
		for(Feature key : isFeatureExecuted.keySet()) {
			if(isFeatureExecuted.getOrDefault(key, false)) {
				executeFeatures.add(key);
			} else {
				executeFeatures.add(null);
			}
		}
		System.out.println(executeFeatures.size());
		request.setAttribute("features", features);
		request.setAttribute("testCaseToFeatures", testCaseToFeatures);
		request.setAttribute("executeFeatures", executeFeatures);
		request.setAttribute("testCases", testCases);
		request.setAttribute("testCaseNoGroup", featureOrganizationService.allTestCases());

		
		Iterable<Project> projects = staticAnalyseService.allProjects().values();
		
		Map<TestCase, List<Double>> testCaseToPercent = new LinkedHashMap<>();
		for(TestCase testCase : testCasesSortByGroup) {
			List<Double> percents = new ArrayList<>();
			percents.add(1 - testCaseCoverageService.findFunctionCallFunctionNotDynamicCalled(testCase).propertionOfNotDynamicCalls());
			for(Project project : projects) {
				percents.add(1 - testCaseCoverageService.findFunctionCallFunctionNotDynamicCalled(testCase, project).propertionOfNotDynamicCalls());
			}
			testCaseToPercent.put(testCase, percents);
		}
		List<Double> mergeCoverages = new ArrayList<>();
		System.out.println("merge");
		mergeCoverages.add(1 - testCaseCoverageService.findFunctionCallFunctionNotDynamicCalled(testCasesSortByGroup).propertionOfNotDynamicCalls());
		for(Project project : projects) {
			mergeCoverages.add(1 - testCaseCoverageService.findFunctionCallFunctionNotDynamicCalled(testCasesSortByGroup, project).propertionOfNotDynamicCalls());
		}
		
		request.setAttribute("projects", projects);
		request.setAttribute("testCaseToCoverages", testCaseToPercent);
		request.setAttribute("mergeCoverages", mergeCoverages);
		
		return "testcase";
	}
	
	private Iterable<TestCase> allTestCases() {
		return featureOrganizationService.allTestCases();
	}
	
}
