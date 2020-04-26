package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.ScenarioDefineTestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.repository.node.microservice.TraceRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.FeatureRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.ScenarioRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.TestCaseRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.ScenarioDefineTestCaseRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.TestCaseExecuteFeatureRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.TestCaseRunTraceRepository;
import cn.edu.fudan.se.multidependency.service.nospring.RepositoryService;

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
	private TestCaseRunTraceRepository testCaseRunTraceRepository;
	
	@Autowired
	private ScenarioDefineTestCaseRepository scenarioDefineTestCaseRepository;

	@Autowired
	private FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;
	
	@Autowired
	private ContainRepository containRepository;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	private Iterable<Feature> allFeatures = null;
	
	@Autowired
	private TraceRepository traceRepository;	
	
	@Override
	public Trace findTraceByTraceId(String traceId) {
		return traceRepository.findTraceByTraceId(traceId);
	}
	
	@Override
	public Trace findTraceById(Long id) {
		return traceRepository.findById(id).get();
	}

	/**
	 * 找出所有特性
	 */
	@Override
	public Iterable<Feature> findAllFeatures() {
		if(allFeatures == null) {
			allFeatures = featureRepository.findAll();
		}
		return allFeatures;
	}

	private Iterable<TestCase> allTestCasesCache = null;
	/**
	 * 找出所有测试用例
	 */
	@Override
	public Iterable<TestCase> findAllTestCases() {
		if(allTestCasesCache == null) {
			allTestCasesCache = testCaseRepository.findAll();
		}
		return allTestCasesCache;
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
		List<Project> projects = containRepository.findMicroServiceContainProjects(ms.getId());
		Project project = null;
		if(projects.size() > 0) {
			project = projects.get(0);
		} else {
			return result;
		}
		for(FunctionDynamicCallFunction call : calls) {
			if(project.getName().equals(call.getProjectName())) {
				result.add(call);
			}
		}
		return result;
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionDynamicCallsByMicroService(MicroService ms) {
		List<FunctionDynamicCallFunction> result = new ArrayList<>();
		List<Project> projects = containRepository.findMicroServiceContainProjects(ms.getId());
		for(Project project : projects) {
			result.addAll(findFunctionDynamicCallsByProject(project));
		}
		return result;
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionDynamicCallsByProject(Project project) {
		return functionDynamicCallFunctionRepository.findFunctionCallsByProjectNameAndLanguage(project.getName(), project.getLanguage());
	}

	private Iterable<TestCaseExecuteFeature> allTestCaseExecuteFeaturesCache = null;
	private Iterable<TestCaseExecuteFeature> findAllTestCaseExecuteFeaturesCache() {
		if(allTestCaseExecuteFeaturesCache == null) {
			allTestCaseExecuteFeaturesCache = testCaseExecuteFeatureRepository.findAll();
		}
		return allTestCaseExecuteFeaturesCache;
	}
	private Iterable<TestCaseRunTrace> allTestCaseRunTraceCache = null;
	private Iterable<TestCaseRunTrace> findAllTestCaseRunTraceCache() {
		if(allTestCaseRunTraceCache == null) {
			allTestCaseRunTraceCache = testCaseRunTraceRepository.findAll();
		}
		return allTestCaseRunTraceCache;
	}
	@Override
	public Map<TestCase, List<TestCaseExecuteFeature>> findAllTestCaseExecuteFeatures() {
		Map<TestCase, List<TestCaseExecuteFeature>> result = new HashMap<>();
		for(TestCase t : findAllTestCases()) {
			result.put(t, new ArrayList<>());
		}
		for(TestCaseExecuteFeature testCaseExecuteFeature : findAllTestCaseExecuteFeaturesCache()) {
			TestCase testcase = testCaseExecuteFeature.getTestCase();
			List<TestCaseExecuteFeature> executes = result.get(testcase);
			executes.add(testCaseExecuteFeature);
			result.put(testcase, executes);
		}
		return result;
	}

	@Override
	public Map<Feature, List<TestCaseExecuteFeature>> findAllFeatureExecutedByTestCases() {
		Map<Feature, List<TestCaseExecuteFeature>> result = new HashMap<>();
		for(Feature feature : findAllFeatures()) {
			result.put(feature, new ArrayList<>());
		}
		for(TestCaseExecuteFeature testCaseExecuteFeature : findAllTestCaseExecuteFeaturesCache()) {
			Feature feature = testCaseExecuteFeature.getFeature();
			List<TestCaseExecuteFeature> executes = result.get(feature);
			executes.add(testCaseExecuteFeature);
			result.put(feature, executes);
		}
		return result;
	}

	@Override
	public Map<TestCase, List<TestCaseRunTrace>> findAllTestCaseRunTraces() {
		Map<TestCase, List<TestCaseRunTrace>> result = new HashMap<>();
		for(TestCase t : findAllTestCases()) {
			result.put(t, new ArrayList<>());
		}
		for(TestCaseRunTrace tt : findAllTestCaseRunTraceCache()) {
			TestCase t = tt.getTestCase();
			List<TestCaseRunTrace> runs = result.get(t);
			runs.add(tt);
			result.put(t, runs);
		}
		return result;
	}

	@Override
	public TestCase findTestCaseById(Long id) {
		return testCaseRepository.findById(id).get();
	}

	@Override
	public Feature findFeatureById(Long id) {
		return featureRepository.findById(id).get();
	}
	
	@Override
	public TestCase findTestCaseByTestCaseId(Integer id) {
		return null;
	}

	@Override
	public Feature findFeatureByFeatureId(Integer id) {
		return null;
	}

	@Override
	public Map<Feature, Feature> findAllFeatureToParentFeature() {
		Map<Feature, Feature> result = new HashMap<>();
		List<Contain> featureContainFeatures = containRepository.findAllFeatureContainFeatures();
		for(Contain fcf : featureContainFeatures) {
			Feature parentFeature = (Feature) fcf.getStart();
			Feature feature = (Feature) fcf.getEnd();
			result.put(feature, parentFeature);
		}
		return result;
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionDynamicCallFunctionRelations(Project project, boolean isTraceRunForTestCase) {
		if(isTraceRunForTestCase) {
			/// FIXME
		}
		return functionDynamicCallFunctionRepository.findProjectContainFunctionDynamicCallFunctionRelations(project.getId());
	}

	@Override
	public Iterable<FunctionDynamicCallFunction> findAllFunctionDynamicCallFunctionRelations(boolean b) {
		if(b) {
			/// FIXME
		}
		return functionDynamicCallFunctionRepository.findAll();
	}

	private Map<TestCase, List<FunctionCallFunction>> functionCallFunctionDynamicCalledCache = new HashMap<>();
	public List<FunctionCallFunction> findFunctionCallFunctionDynamicCalled(TestCase testCase) {
		List<FunctionCallFunction> result = functionCallFunctionDynamicCalledCache.get(testCase);
		if(result == null) {
			result = functionDynamicCallFunctionRepository.findFunctionCallFunctionDynamicCalled(testCase.getTestCaseId());
			functionCallFunctionDynamicCalledCache.put(testCase, result);
		}
		return result;
	}
	
	private Map<TestCase, List<FunctionCallFunction>> functionCallFunctionNotDynamicCalledCache = new HashMap<>();
	public List<FunctionCallFunction> findFunctionCallFunctionNotDynamicCalled(TestCase testCase) {
		List<FunctionCallFunction> result = functionCallFunctionNotDynamicCalledCache.get(testCase);
		if(result == null) {
			result = functionDynamicCallFunctionRepository.findFunctionCallFunctionNotDynamicCalled(testCase.getTestCaseId());
			functionCallFunctionNotDynamicCalledCache.put(testCase, result);
		}
		return result;
	}
	
	public List<FunctionDynamicCallFunction> findDynamicCallsByCallerIdAndCalledIdAndTestCaseId(Function caller, Function called, TestCase testCase) {
		List<FunctionDynamicCallFunction> result = new ArrayList<>();
		List<FunctionDynamicCallFunction> calls = findDynamicCallsByCallerIdAndTestCaseId(caller, testCase);
		for(FunctionDynamicCallFunction call : calls) {
			if(call.getCallFunction().equals(called)) {
				result.add(call);
			}
		}
		return result;
	}
	
	private Map<TestCase, Map<Function, List<FunctionDynamicCallFunction>>> testCaseAndCallerToDynamicCallsCache = new HashMap<>();
	public List<FunctionDynamicCallFunction> findDynamicCallsByCallerIdAndTestCaseId(Function caller, TestCase testCase) {
		List<FunctionDynamicCallFunction> result = new ArrayList<>();
		Map<Function, List<FunctionDynamicCallFunction>> calls = testCaseAndCallerToDynamicCallsCache.getOrDefault(testCase, new HashMap<>());
		result = calls.get(caller);
		if(result == null) {
			result = functionDynamicCallFunctionRepository.findDynamicCallsByCallerIdAndTestCaseId(caller.getId(), testCase.getTestCaseId());
			calls.put(caller, result);
			testCaseAndCallerToDynamicCallsCache.put(testCase, calls);
		}
		return result;
	}

	
	/**
	 * 指定测试用例下动态调用了的静态调用，并返回次数
	 * @param testCases
	 * @return
	 */
	@Override
	public Map<Function, Map<Function, FunctionCallPropertionDetail>> findFunctionCallFunctionDynamicCalled(Iterable<TestCase> testCases) {
		if(testCases == null) {
			return new HashMap<>();
		}
		Map<Function, Map<Function, FunctionCallPropertionDetail>> result = new HashMap<>();
		for(TestCase testCase : testCases) {
			// 动态直接调用了的静态调用
			List<FunctionCallFunction> dynamicCallStaticCalls = findFunctionCallFunctionDynamicCalled(testCase);
			for(FunctionCallFunction dynamicCall : dynamicCallStaticCalls) {
				Function caller = dynamicCall.getFunction();
				Function called = dynamicCall.getCallFunction();
				Map<Function, FunctionCallPropertionDetail> calledFunctionToDetail = result.getOrDefault(caller, new HashMap<>());
				FunctionCallPropertionDetail detail = calledFunctionToDetail.getOrDefault(called, new FunctionCallPropertionDetail());
				List<FunctionDynamicCallFunction> dynamic = findDynamicCallsByCallerIdAndCalledIdAndTestCaseId(caller, called, testCase);
				detail.addTestCaseCall(testCase, dynamic.size());
				calledFunctionToDetail.put(called, detail);
				result.put(caller, calledFunctionToDetail);
			}
			
			// 从动态没有直接调用的静态调用里
			// 找到可能存在调用子类的调用
			List<FunctionCallFunction> dynamicNotCalls = findFunctionCallFunctionNotDynamicCalled(testCase);
			for(FunctionCallFunction call : dynamicNotCalls) {
				Function caller = call.getFunction();
				Function called = call.getCallFunction();
				List<FunctionDynamicCallFunction> dynamicCalls = findDynamicCallsByCallerIdAndTestCaseId(caller, testCase);
				Function callSubTypeFunction = null;
				for(FunctionDynamicCallFunction dynamicCall : dynamicCalls) {
					Function dynamicCaller = dynamicCall.getFunction();
					Function dynamicCalled = dynamicCall.getCallFunction();
					if(!caller.equals(dynamicCaller) || !called.getSimpleName().equals(dynamicCalled.getSimpleName())
							|| called.getParameters().size() != dynamicCalled.getParameters().size()) {
						///FIXME
						// 暂时只通过方法名simpleName和方法参数数量判断是否可能为重写方法
						continue;
					}
					Type calledType = staticAnalyseService.findFunctionBelongToType(called);
					if(calledType == null) {
						continue;
					}
					Type dynamicCalledType = staticAnalyseService.findFunctionBelongToType(dynamicCalled);
					if(dynamicCalledType == null) {
						continue;
					}
					if(staticAnalyseService.isSubType(dynamicCalledType, calledType)) {
						callSubTypeFunction = dynamicCalled;
						break;
					}
				}
				if(callSubTypeFunction != null) {
					Map<Function, FunctionCallPropertionDetail> group = result.getOrDefault(caller, new HashMap<>());
					FunctionCallPropertionDetail detail = group.getOrDefault(called, new FunctionCallPropertionDetail());
					List<FunctionDynamicCallFunction> dynamic = findDynamicCallsByCallerIdAndCalledIdAndTestCaseId(caller, callSubTypeFunction, testCase);
					detail.addTestCaseCall(testCase, dynamic.size());
					group.put(called, detail);
					result.put(caller, group);	
				}
			}
		}
		return result;
	}

	@Override
	public Map<Scenario, List<ScenarioDefineTestCase>> findAllScenarioDefineTestCases() {
		Map<Scenario, List<ScenarioDefineTestCase>> result = new HashMap<>();
		for(Scenario s : findAllScenarios()) {
			result.put(s, new ArrayList<>());
		}
		for(ScenarioDefineTestCase st : scenarioDefineTestCaseRepository.findAll()) {
			Scenario s = st.getScenario();
			List<ScenarioDefineTestCase> defines = result.get(s);
			defines.add(st);
			result.put(s, defines);
		}
		return result;
	}

}
