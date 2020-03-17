package cn.edu.fudan.se.multidependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.model.relation.microservice.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.SpanCallSpan;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.JaegerService;
import cn.edu.fudan.se.multidependency.service.spring.ProjectOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = {"cn.edu.fudan.se.multidependency.repository"})
public class MultipleDependencyApp {
	
	public static void main(String[] args) {
		SpringApplication.run(MultipleDependencyApp.class, args);
	}
	
	@Bean 
	public ProjectOrganizationService organizeProject(StaticAnalyseService staticAnalyseService, DynamicAnalyseService dynamicAnalyseService) {
		Map<Long, Project> projects = staticAnalyseService.findAllProjects();
		Map<Project, List<FunctionDynamicCallFunction>> dynamicCalls = new HashMap<>();
		for(Project project : projects.values()) {
			List<FunctionDynamicCallFunction> calls = dynamicAnalyseService.findFunctionDynamicCallsByProject(project);
			dynamicCalls.put(project, calls);
		}
//		ProjectOrganizationService organization = new ProjectOrganizationService(projects, dynamicCalls);
		ProjectOrganizationService organization = new ProjectOrganizationService(projects);
		return organization;
	}

	@Bean
	public FeatureOrganizationService organize(JaegerService jaegerService, DynamicAnalyseService dynamicAnalyseService) {
		Map<String, MicroService> allMicroService = jaegerService.findAllMicroService();
		Map<Feature, List<TestCaseExecuteFeature>> featureExecutedByTestCases = dynamicAnalyseService.findAllFeatureExecutedByTestCases();
		Map<TestCase, List<TestCaseExecuteFeature>> testCaseExecuteFeatures = dynamicAnalyseService.findAllTestCaseExecuteFeatures();
		Map<TestCase, List<TestCaseRunTrace>> testCaseRunTraces = dynamicAnalyseService.findAllTestCaseRunTraces();
		Map<Trace, List<Span>> traceToSpans = new HashMap<>();
		Map<Span, List<SpanCallSpan>> spanCallSpans = new HashMap<>();
		Map<Span, MicroServiceCreateSpan> spanBelongToMicroService = new HashMap<>();
		Map<Feature, Feature> featureToParentFeature = dynamicAnalyseService.findAllFeatureToParentFeature();
		
		for (List<TestCaseRunTrace> runs : testCaseRunTraces.values()) {
			for(TestCaseRunTrace run : runs) {
				Trace trace = run.getTrace();
				List<Span> spans = jaegerService.findSpansByTrace(trace);
				traceToSpans.put(trace, spans);
				for (Span span : spans) {
					List<SpanCallSpan> callSpans = jaegerService.findSpanCallSpans(span);
					spanCallSpans.put(span, callSpans);
					MicroServiceCreateSpan microServiceCreateSpan = jaegerService.findMicroServiceCreateSpan(span);
					spanBelongToMicroService.put(span, microServiceCreateSpan);
				}
			}
		}
		FeatureOrganizationService organization = new FeatureOrganizationService(
				allMicroService, testCaseExecuteFeatures, featureExecutedByTestCases, 
				featureToParentFeature, testCaseRunTraces, traceToSpans, spanCallSpans, spanBelongToMicroService);

		return organization;
	}	
}
