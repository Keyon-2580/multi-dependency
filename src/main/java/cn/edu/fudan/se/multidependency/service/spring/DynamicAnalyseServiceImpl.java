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
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.repository.node.testcase.FeatureRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.ScenarioRepository;
import cn.edu.fudan.se.multidependency.repository.node.testcase.TestCaseRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;
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
	private FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;
	
	@Autowired
	private ContainRepository containRepository;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
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
		Iterable<TestCase> testcases = testCaseRepository.findAll();
		for(TestCase t : testcases) {
			result.put(t, new ArrayList<>());
		}
		Iterable<TestCaseExecuteFeature> testCaseExecuteFeatures = testCaseExecuteFeatureRepository.findAll();
		for(TestCaseExecuteFeature testCaseExecuteFeature : testCaseExecuteFeatures) {
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
		Iterable<Feature> features = featureRepository.findAll();
		for(Feature feature : features) {
			result.put(feature, new ArrayList<>());
		}
		Iterable<TestCaseExecuteFeature> testCaseExecuteFeatures = testCaseExecuteFeatureRepository.findAll();
		for(TestCaseExecuteFeature testCaseExecuteFeature : testCaseExecuteFeatures) {
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
		Iterable<TestCase> testcases = testCaseRepository.findAll();
		for(TestCase t : testcases) {
			result.put(t, new ArrayList<>());
		}
		Iterable<TestCaseRunTrace> testCaseRunTraces = testCaseRunTraceRepository.findAll();
		for(TestCaseRunTrace tt : testCaseRunTraces) {
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

	@Override
	public Map<Function, List<FunctionCallFunction>> findFunctionCallFunctionNotDynamicCalled(boolean removeCallSubClass,
			List<TestCase> testcases) {
		Map<Function, List<FunctionCallFunction>> result = new HashMap<>();
		if(testcases == null) {
			// 静态调用了而动态没有调用
			List<FunctionCallFunction> all = functionDynamicCallFunctionRepository.findFunctionCallFunctionNotDynamicCalled();
			List<FunctionCallFunction> removeCallSubClassAll = new ArrayList<>();
			if(removeCallSubClass) {
				System.out.println("加上调用子类的重写方法");
				// 添加动态调用子类的重写方法
				for(FunctionCallFunction call : all) {
					Function caller = call.getFunction();
					Function called = call.getCallFunction();
					List<FunctionDynamicCallFunction> dynamicCalls = functionDynamicCallFunctionRepository.findDynamicCalls(caller.getId());
					boolean callSubTypeFunction = false;
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
							System.out.println(called.getFunctionName() + " 没有Type");
							continue;
						}
						Type dynamicCalledType = staticAnalyseService.findFunctionBelongToType(dynamicCalled);
						if(dynamicCalledType == null) {
							System.out.println(dynamicCalled.getFunctionName() + " 没有Type");
							continue;
						}
						
						if(staticAnalyseService.isSubType(calledType, dynamicCalledType)) {
							callSubTypeFunction = true;
							break;
						}
					}
					if(!callSubTypeFunction) {
						List<FunctionCallFunction> group = result.getOrDefault(caller, new ArrayList<>());
						group.add(call);
						result.put(caller, group);	
					}
				}
			} else {
				// 不添加动态调用子类的重写方法
				for(FunctionCallFunction call : all) {
					Function caller = call.getFunction();
					List<FunctionCallFunction> group = result.getOrDefault(caller, new ArrayList<>());
					group.add(call);
					result.put(caller, group);
				}
			}
		} else {
		}
		
		return result;
	}


}
