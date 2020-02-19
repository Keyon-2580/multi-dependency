package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.dynamic.CallNode;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFileDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFunctionDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.repository.node.testcase.FeatureRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.ScenarioRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.TestCaseRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.TestCaseExecuteFeatureRepository;
import cn.edu.fudan.se.multidependency.service.RepositoryService;

@Service
public class DynamicAnalyseServiceImpl implements DynamicAnalyseService {
	
	RepositoryService repository = RepositoryService.getInstance();
	
	@Autowired
	private FeatureRepository featureRepository;
	
	@Autowired
	private ScenarioRepository scenarioRepository;
	
	@Autowired
	private TestCaseRepository testCaseRepository;
	
	@Autowired
	private TestCaseExecuteFeatureRepository testCaseExecuteFeatureRepository;

	@Autowired
	private FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private ContainRepository containRepository;
	
	/**
	 * 找某个特性对应的所有测试用例依赖了哪些方法及其细节调用
	 * @param featureName
	 * @return
	 */
	@Override
	@Deprecated
	public List<DynamicTestCaseToFunctionDependency> findDependencyFunctionsByFeatureName(String featureName) {
		List<DynamicTestCaseToFunctionDependency> result = new ArrayList<>();
		Feature feature = featureRepository.findByFeatureName(featureName);
		if(feature == null) {
			System.out.println("feature is null: " + featureName);
			return new ArrayList<>();
		}
		/**
		 * 该特性对应的所有测试用例
		 */
		List<TestCase> testCases = testCaseExecuteFeatureRepository.findTestCasesExecuteFeatureByFeatureName(featureName);
		for(TestCase testCase : testCases) {
			result.add(findDependencyFunctionsByTestCaseName(testCase));
		}
		return result;
	}
	
	/**
	 * 找某个特性对应的所有测试用例依赖了哪些文件及其细节调用
	 * @param featureName
	 * @return
	 */
	@Override
	@Deprecated
	public List<DynamicTestCaseToFileDependency> findDependencyFilesByFeatureName(String featureName) {
		List<DynamicTestCaseToFileDependency> result = new ArrayList<>();
		Feature feature = featureRepository.findByFeatureName(featureName);
		if(feature == null) {
			System.out.println("feature is null: " + featureName);
			return new ArrayList<>();
		}
		/**
		 * 该特性对应的测试用例
		 */
		List<TestCase> testCases = testCaseExecuteFeatureRepository.findTestCasesExecuteFeatureByFeatureName(featureName);
		for(TestCase testCase : testCases) {
			result.add(findDependencyFilesByTestCaseName(testCase));
		}
		return result;
	}
	
	/**
	 * 找某个特性对应的所有测试用例依赖了哪些文件
	 * @param featureName
	 * @return
	 */
	@Override
	@Deprecated
	public List<ProjectFile> findAllDependencyFilesByFeatureName(String featureName) {
		List<ProjectFile> files = new ArrayList<>();
		List<DynamicTestCaseToFileDependency> allDependencies = findDependencyFilesByFeatureName(featureName);
		for(DynamicTestCaseToFileDependency dependencies : allDependencies) {
			dependencies.getProjectFiles().forEach((id, projectFile) -> {
				if(!files.contains(projectFile)) {
					files.add(projectFile);
				}
			});
		}
		files.sort(new Comparator<ProjectFile>() {
			@Override
			public int compare(ProjectFile o1, ProjectFile o2) {
				return o1.getPath().compareTo(o2.getPath());
			}
		});
		return files;
	}

	/**
	 * 某个测试用例依赖的所有方法
	 * @param testCase
	 * @return
	 */
	@Override
	public DynamicTestCaseToFunctionDependency findDependencyFunctionsByTestCaseName(TestCase testCase) {
		DynamicTestCaseToFunctionDependency dependencies = new DynamicTestCaseToFunctionDependency();
		dependencies.setTestCase(testCase);
		List<FunctionDynamicCallFunction> calls = functionDynamicCallFunctionRepository.findDynamicCallsByTestCaseName(testCase.getTestCaseName());
		for(FunctionDynamicCallFunction call : calls) {
			dependencies.addFunctionDynamicCallFunction(call);
		}
		return dependencies;
	}

	/**
	 * 某个测试用例依赖的所有文件
	 * @param testCase
	 * @return
	 */
	@Override
	public DynamicTestCaseToFileDependency findDependencyFilesByTestCaseName(TestCase testCase) {
		DynamicTestCaseToFileDependency dependencies = new DynamicTestCaseToFileDependency();
		dependencies.setTestCase(testCase);
		List<FunctionDynamicCallFunction> calls = functionDynamicCallFunctionRepository.findDynamicCallsByTestCaseName(testCase.getTestCaseName());
		for(FunctionDynamicCallFunction call : calls) {
			Function startFunction = call.getFunction();
			Function endFunction = call.getCallFunction();
			// 找到函数所在的文件
			ProjectFile startFile = staticAnalyseService.findFunctionBelongToCodeFile(startFunction);
			ProjectFile endFile = staticAnalyseService.findFunctionBelongToCodeFile(endFunction);
			dependencies.addFunctionDynamicCallFunction(call, startFile, endFile);
		}
		return dependencies;
	}

	public CallNode findCallTree(Function function, int depth) {
		CallNode root = new CallNode(function);
		root.setFunction(function);
		if(depth >= 1) {
			functionDynamicCallFunctionRepository.findCallFunctions(function.getId()).forEach(callFunction -> {
				root.addChild(findCallTree(callFunction, depth - 1));
			});
		}
		return root;
	}

	/**
	 * 找出所有特性
	 */
	@Override
	public List<Feature> findAllFeatures() {
		return featureRepository.findAllFeatures();
	}

	/**
	 * 找出所有测试用例
	 */
	@Override
	public Iterable<TestCase> findAllTestCases() {
		return testCaseRepository.findAll();
	}

	/**
	 * 找出所有场景
	 */
	@Override
	public Iterable<Scenario> findAllScenarios() {
		return scenarioRepository.findAll();
	}

	/**
	 * 找出某特性对应的所有测试用例
	 */
	@Override
	public List<TestCase> findTestCasesByFeatureName(String featureName) {
		return testCaseRepository.findTestCasesByFeatureName(featureName);
	}

	@Override
	public List<Feature> findFeaturesByFeatureId(Integer... featureIds) {
		List<Integer> idList = Arrays.asList(featureIds);
		List<Feature> result = new ArrayList<>();
		for(Feature feature : findAllFeatures()) {
			if(idList.contains(feature.getFeatureId())) {
				result.add(feature);
			}
		}
		return result;
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionCallsByTraceIdAndSpanId(String traceId, String spanId) {
		return functionDynamicCallFunctionRepository.findFunctionCallsByTraceIdAndSpanId(traceId, spanId);
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionDynamicCallsByTrace(Trace trace) {
		return functionDynamicCallFunctionRepository.findFunctionCallsByTraceId(trace.getTraceId());
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionDynamicCallsByTraceAndSpan(Trace trace, Span span) {
		return functionDynamicCallFunctionRepository.findFunctionCallsByTraceIdAndSpanId(trace.getTraceId(), span.getSpanId());
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionDynamicCallsByTraceAndMicroService(Trace trace,
			MicroService ms) {
		List<FunctionDynamicCallFunction> result = new ArrayList<>();
		List<FunctionDynamicCallFunction> calls = findFunctionDynamicCallsByTrace(trace);
		List<Project> projects = containRepository.findMicroServiceContainProject(ms.getId());
		Project project = null;
		if(projects.size() > 0) {
			project = projects.get(0);
		} else {
			return result;
		}
		for(FunctionDynamicCallFunction call : calls) {
			if(project.getProjectName().equals(call.getProjectName())) {
				result.add(call);
			}
		}
		return result;
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionDynamicCallsByMicroService(MicroService ms) {
		List<FunctionDynamicCallFunction> result = new ArrayList<>();
		List<Project> projects = containRepository.findMicroServiceContainProject(ms.getId());
		Project project = null;
		if(projects.size() > 0) {
			project = projects.get(0);
			return findFunctionDynamicCallsByProject(project);
		} else {
			return result;
		}
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionDynamicCallsByProject(Project project) {
		return functionDynamicCallFunctionRepository.findFunctionCallsByProjectName(project.getProjectName());
	}

}
