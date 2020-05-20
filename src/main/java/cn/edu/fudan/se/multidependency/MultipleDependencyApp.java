package cn.edu.fudan.se.multidependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.ScenarioDefineTestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanInstanceOfRestfulAPI;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = {"cn.edu.fudan.se.multidependency.repository"})
public class MultipleDependencyApp {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultipleDependencyApp.class);
	
	public static void main(String[] args) {
		LOGGER.info("MultipleDependencyApp");
		if(args == null || args.length == 0) {
			SpringApplication.run(MultipleDependencyApp.class, args);
		} else {
			String operation = args[0];
			LOGGER.info(operation);
			String[] parameters = new String[args.length - 1];
			for(int i = 0; i < parameters.length; i++) {
				parameters[i] = args[i + 1];
			}
			switch(operation) {
			case "-i":
				InsertDataMain.insert(parameters);
				break;
			case "-m":
				SpringApplication.run(MultipleDependencyApp.class, args);
				break;
			case "-p":
				try {
					cn.edu.fudan.se.multidependency.utils.FileUtil.writeToFileForProjectJSONFile(parameters);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	@Bean
	public FeatureOrganizationService organize(MicroserviceService microserviceService, DynamicAnalyseService dynamicAnalyseService, ContainRelationService containRelationService) {
		System.out.println("organizeFeature");
		Map<String, MicroService> allMicroService = microserviceService.findAllMicroService();
		Map<Feature, List<TestCaseExecuteFeature>> featureExecutedByTestCases = dynamicAnalyseService.findAllFeatureExecutedByTestCases();
		Map<TestCase, List<TestCaseExecuteFeature>> testCaseExecuteFeatures = dynamicAnalyseService.findAllTestCaseExecuteFeatures();
		Map<TestCase, List<TestCaseRunTrace>> testCaseRunTraces = dynamicAnalyseService.findAllTestCaseRunTraces();
		Map<Trace, List<Span>> traceToSpans = new HashMap<>();
		Map<Span, List<SpanCallSpan>> spanCallSpans = new HashMap<>();
		Map<Span, MicroServiceCreateSpan> spanBelongToMicroService = new HashMap<>();
		Map<Feature, Feature> featureToParentFeature = dynamicAnalyseService.findAllFeatureToParentFeature();
		Map<Scenario, List<ScenarioDefineTestCase>> scenarioDefineTestCases = dynamicAnalyseService.findAllScenarioDefineTestCases();
		
		for (List<TestCaseRunTrace> runs : testCaseRunTraces.values()) {
			for(TestCaseRunTrace run : runs) {
				Trace trace = run.getTrace();
				List<Span> spans = containRelationService.findTraceContainSpans(trace);
				traceToSpans.put(trace, spans);
				for (Span span : spans) {
					List<SpanCallSpan> callSpans = microserviceService.findSpanCallSpans(span);
					spanCallSpans.put(span, callSpans);
					MicroServiceCreateSpan microServiceCreateSpan = microserviceService.findMicroServiceCreateSpan(span);
					spanBelongToMicroService.put(span, microServiceCreateSpan);
				}
			}
		}
		Map<MicroService, List<RestfulAPI>> microServiceContainAPIs = microserviceService.microServiceContainsAPIs();
		Map<Span, SpanInstanceOfRestfulAPI> spanInstanceOfRestfulAPIs = microserviceService.findAllSpanInstanceOfRestfulAPIs();
		FeatureOrganizationService organization = new FeatureOrganizationService(
				allMicroService, testCaseExecuteFeatures, featureExecutedByTestCases, 
				featureToParentFeature, testCaseRunTraces, traceToSpans, spanCallSpans, spanBelongToMicroService, scenarioDefineTestCases,
				microServiceContainAPIs, spanInstanceOfRestfulAPIs);

		return organization;
	}	
}
