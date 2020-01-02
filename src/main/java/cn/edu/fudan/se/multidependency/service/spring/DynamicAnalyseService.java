package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.dynamic.CallNode;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFileDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFunctionDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;

public interface DynamicAnalyseService {
	
	
	
	public List<DynamicTestCaseToFunctionDependency> findDependencyFunctions(String featureName);
	public List<DynamicTestCaseToFileDependency> findDependencyFiles(String featureName);
	
	public Iterable<Feature> findAllFeatures();
	
	public Iterable<TestCase> findAllTestCases();
	
	public Iterable<Scenario> findAllScenarios();
	
	public CallNode findCallTree(Function function, int depth);
	
}
