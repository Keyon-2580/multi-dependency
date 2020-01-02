package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.dynamic.CallNode;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFileDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFunctionDependency;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.repository.node.testcase.FeatureRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.ScenarioRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.TestCaseRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.ScenarioDefineTestCaseRepository;
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
	private ScenarioDefineTestCaseRepository scenarioDefineTestCaseRepository;
	
	@Autowired
	private TestCaseExecuteFeatureRepository testCaseExecuteFeatureRepository;

	@Autowired
	private FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	/**
	 * 找某个特性对应的测试用例依赖了哪些方法
	 * @param featureName
	 * @return
	 */
	@Override
	public List<DynamicTestCaseToFunctionDependency> findDependencyFunctions(String featureName) {
		List<DynamicTestCaseToFunctionDependency> result = new ArrayList<>();
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
			DynamicTestCaseToFunctionDependency dependencies = new DynamicTestCaseToFunctionDependency();
			dependencies.setTestCase(testCase);
			List<FunctionDynamicCallFunction> calls = functionDynamicCallFunctionRepository.findDynamicCallsByTestCaseName(testCase.getTestCaseName());
			for(FunctionDynamicCallFunction call : calls) {
				dependencies.addFunctionDynamicCallFunction(call);
			}
			result.add(dependencies);
		}
		return result;
	}
	
	/**
	 * 找某个特性对应的测试用例依赖了哪些文件
	 * @param featureName
	 * @return
	 */
	@Override
	public List<DynamicTestCaseToFileDependency> findDependencyFiles(String featureName) {
		List<DynamicTestCaseToFileDependency> result = new ArrayList<>();
		Feature feature = featureRepository.findByFeatureName(featureName);
		if(feature == null) {
			System.out.println("feature is null: " + featureName);
			return new ArrayList<>();
		}
		/**
		 * 该特性对应的测试用例
		 */
		System.out.println(featureName);
		List<TestCase> testCases = testCaseExecuteFeatureRepository.findTestCasesExecuteFeatureByFeatureName(featureName);
		System.out.println(testCases);
		for(TestCase testCase : testCases) {
			DynamicTestCaseToFileDependency dependencies = new DynamicTestCaseToFileDependency();
			dependencies.setTestCase(testCase);
			List<FunctionDynamicCallFunction> calls = functionDynamicCallFunctionRepository.findDynamicCallsByTestCaseName(testCase.getTestCaseName());
			System.out.println("calls " + calls.size() + " " + calls);
			for(FunctionDynamicCallFunction call : calls) {
				Function startFunction = call.getFunction();
				Function endFunction = call.getCallFunction();
				ProjectFile startFile = staticAnalyseService.findFunctionBelongToCodeFile(startFunction);
				ProjectFile endFile = staticAnalyseService.findFunctionBelongToCodeFile(endFunction);
				dependencies.addFunctionDynamicCallFunction(call, startFile, endFile);
			}
			result.add(dependencies);
		}
		return result;
	}

	/*public void insertToNeo4jDataBase(String scenarioName, List<String> featureName, String testcaseName,
			File executeFile) throws Exception {
		
		
		insertRelations();
	}
	
	private void insertRelations() {
		repository.getRelations().getAllRelations().forEach((relationType, rs) -> {
			rs.forEach(relation -> {
				if(relation.getRelationType() == RelationType.DEPENDENCY_DYNAMIC_FUNCTION_CALL_FUNCTION) {
					functionDynamicCallFunctionRepository.save((FunctionDynamicCallFunction) relation);
				}
			});
		});
	}*/
	
	@Override
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

	@Override
	public Iterable<Feature> findAllFeatures() {
		return featureRepository.findAll();
	}

	@Override
	public Iterable<TestCase> findAllTestCases() {
		return testCaseRepository.findAll();
	}

	@Override
	public Iterable<Scenario> findAllScenarios() {
		return scenarioRepository.findAll();
	}

}
