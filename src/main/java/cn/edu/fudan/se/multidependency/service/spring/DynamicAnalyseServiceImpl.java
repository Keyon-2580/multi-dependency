package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.repository.node.testcase.FeatureRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.ScenarioRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.TestCaseRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.TestCaseExecuteFeatureRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.TestCaseRunTraceRepository;
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
	private TestCaseRunTraceRepository testCaseRunTraceRepository;

	@Autowired
	private FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private ContainRepository containRepository;
	
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
		for(Project project : projects) {
			result.addAll(findFunctionDynamicCallsByProject(project));
		}
		return result;
	}

	@Override
	public List<FunctionDynamicCallFunction> findFunctionDynamicCallsByProject(Project project) {
		return functionDynamicCallFunctionRepository.findFunctionCallsByProjectNameAndLanguage(project.getProjectName(), project.getLanguage());
	}

	@Override
	public Map<TestCase, List<TestCaseExecuteFeature>> findAllTestCaseExecuteFeatures() {
		Map<TestCase, List<TestCaseExecuteFeature>> result = new HashMap<>();
		Iterable<TestCaseExecuteFeature> testCaseExecuteFeatures = testCaseExecuteFeatureRepository.findAll();
		for(TestCaseExecuteFeature testCaseExecuteFeature : testCaseExecuteFeatures) {
			TestCase testcase = testCaseExecuteFeature.getTestCase();
			List<TestCaseExecuteFeature> executes = result.get(testcase);
			executes = executes == null ? new ArrayList<>() : executes;
			executes.add(testCaseExecuteFeature);
			result.put(testcase, executes);
		}
		return result;
	}

	@Override
	public Map<Feature, List<TestCaseExecuteFeature>> findAllFeatureExecutedByTestCases() {
		Map<Feature, List<TestCaseExecuteFeature>> result = new HashMap<>();
		Iterable<TestCaseExecuteFeature> testCaseExecuteFeatures = testCaseExecuteFeatureRepository.findAll();
		for(TestCaseExecuteFeature testCaseExecuteFeature : testCaseExecuteFeatures) {
			Feature feature = testCaseExecuteFeature.getFeature();
			List<TestCaseExecuteFeature> executes = result.get(feature);
			executes = executes == null ? new ArrayList<>() : executes;
			executes.add(testCaseExecuteFeature);
			result.put(feature, executes);
		}
		return result;
	}

	@Override
	public Map<TestCase, List<TestCaseRunTrace>> findAllTestCaseRunTraces() {
		Map<TestCase, List<TestCaseRunTrace>> result = new HashMap<>();
		Iterable<TestCaseRunTrace> testCaseRunTraces = testCaseRunTraceRepository.findAll();
		for(TestCaseRunTrace tt : testCaseRunTraces) {
			TestCase t = tt.getTestCase();
			List<TestCaseRunTrace> runs = result.get(t);
			runs = runs == null ? new ArrayList<>() : runs;
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

}
