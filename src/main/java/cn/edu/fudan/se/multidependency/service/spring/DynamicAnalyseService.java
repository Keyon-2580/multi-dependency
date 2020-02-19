package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFileDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFunctionDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;

public interface DynamicAnalyseService {

	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByTraceAndMicroService(Trace trace, MicroService ms);
	
	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByTrace(Trace trace);
	
	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByTraceAndSpan(Trace trace, Span span);

	List<FunctionDynamicCallFunction> findFunctionCallsByTraceIdAndSpanId(String traceId, String spanId);
	
	List<ProjectFile> findAllDependencyFilesByFeatureName(String featureName);
	
	List<DynamicTestCaseToFunctionDependency> findDependencyFunctionsByFeatureName(String featureName);
	
	List<DynamicTestCaseToFileDependency> findDependencyFilesByFeatureName(String featureName);
	
	List<TestCase> findTestCasesByFeatureName(String featureName);
	
	List<Feature> findAllFeatures();
	
	Iterable<TestCase> findAllTestCases();
	
	Iterable<Scenario> findAllScenarios();
	
	DynamicTestCaseToFunctionDependency findDependencyFunctionsByTestCaseName(TestCase testCase);
	
	DynamicTestCaseToFileDependency findDependencyFilesByTestCaseName(TestCase testCase);

	List<Feature> findFeaturesByFeatureId(Integer... featureIds);

	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByMicroService(MicroService ms);
	
	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByProject(Project project);
}
