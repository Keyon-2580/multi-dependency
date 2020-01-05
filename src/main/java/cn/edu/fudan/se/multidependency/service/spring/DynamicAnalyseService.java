package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFileDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFunctionDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;

public interface DynamicAnalyseService {
	
	public List<ProjectFile> findAllDependencyFilesByFeatureName(String featureName);
	
	public List<DynamicTestCaseToFunctionDependency> findDependencyFunctionsByFeatureName(String featureName);
	public List<DynamicTestCaseToFileDependency> findDependencyFilesByFeatureName(String featureName);
	
	public List<TestCase> findTestCasesByFeatureName(String featureName);
	
	public Iterable<Feature> findAllFeatures();
	
	public Iterable<TestCase> findAllTestCases();
	
	public Iterable<Scenario> findAllScenarios();
	
	public DynamicTestCaseToFunctionDependency findDependencyFunctionsByTestCaseName(TestCase testCase);
	
	public DynamicTestCaseToFileDependency findDependencyFilesByTestCaseName(TestCase testCase);
}
