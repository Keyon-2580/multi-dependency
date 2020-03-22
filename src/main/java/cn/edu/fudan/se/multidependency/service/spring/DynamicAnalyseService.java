package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;

public interface DynamicAnalyseService {
	
	/**
	 * 在给定测试用例下，找出静态调用了而动态没有调用的FunctionCallFunction
	 * 可选择是否去掉静态调用了，而动态没有调用，但是动态调用了子类重写的方法
	 * 
	 * @param removeCallSubCall
	 * @param testcases can be null
	 * @return
	 */
	Map<Function, List<FunctionCallFunction>> findFunctionCallFunctionNotDynamicCalled(Iterable<TestCase> testcases);

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

	List<FunctionDynamicCallFunction> findFunctionDynamicCallFunctionRelations(Project project, boolean isTraceRunForTestCase);

	Iterable<FunctionDynamicCallFunction> findAllFunctionDynamicCallFunctionRelations(boolean b);
}
