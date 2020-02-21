package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;

public interface DynamicAnalyseService {

	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByTraceAndMicroService(Trace trace, MicroService ms);
	
	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByTrace(Trace trace);
	
	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByTraceAndSpan(Trace trace, Span span);

	List<FunctionDynamicCallFunction> findFunctionCallsByTraceIdAndSpanId(String traceId, String spanId);
	
	List<TestCase> findTestCasesByFeatureName(String featureName);
	
	List<Feature> findAllFeatures();
	
	Iterable<TestCase> findAllTestCases();
	
	Iterable<Scenario> findAllScenarios();
	
	Map<TestCase, List<TestCaseExecuteFeature>> findAllTestCaseExecuteFeatures();
	
	Map<Feature, List<TestCaseExecuteFeature>> findAllFeatureExecutedByTestCases();
	
	Map<TestCase, List<TestCaseRunTrace>> findAllTestCaseRunTraces();

	Map<Feature, Feature> findAllFeatureToParentFeature();
	
	List<Feature> findFeaturesByFeatureId(Integer... featureIds);

	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByMicroService(MicroService ms);
	
	List<FunctionDynamicCallFunction> findFunctionDynamicCallsByProject(Project project);

	TestCase findTestCaseById(Long id);

	Feature findFeatureById(Long id);
}
