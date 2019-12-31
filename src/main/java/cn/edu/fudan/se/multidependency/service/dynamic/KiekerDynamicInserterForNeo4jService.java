package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.utils.DynamicUtil;
import cn.edu.fudan.se.multidependency.utils.DynamicUtil.DynamicFunctionFromKieker;

public class KiekerDynamicInserterForNeo4jService extends DynamicInserterForNeo4jService {

	/**
	 * 从文件名中提取出场景、特性、测试用例名称
	 */
	@Override
	protected void extractScenarioAndTestCaseAndFeatures() {
		featureName.clear();
		String fileName = executeFile.getName();
		String[] splits = fileName.split(",");
		scenarioName = splits[0];
		testcaseName = splits[1];
		for(int i = 2; i < splits.length; i++) {
			featureName.add(splits[i]);
		}
	}
	
	protected void addNodesAndRelations(String scenarioName, List<String> featureNames, String testCaseName,
			File executeFile) throws Exception {
		Scenario scenario = this.getNodes().findScenarioByName(scenarioName);
		if(scenario == null) {
			scenario = new Scenario();
			scenario.setScenarioName(scenarioName);
			scenario.setEntityId(generateEntityId());
			addNode(scenario);
		}
		TestCase testCase = new TestCase();
		testCase.setEntityId(generateEntityId());
		testCase.setTestCaseName(testCaseName);
		addNode(testCase);
		Contain contain = new Contain();
		contain.setStart(scenario);
		contain.setEnd(testCase);
		addRelation(contain);
		for(String featureName : featureNames) {
			Feature feature = this.getNodes().findFeatureByFeature(featureName);
			if(feature == null) {
				feature = new Feature();
				feature.setEntityId(generateEntityId());
				feature.setFeatureName(featureName);
				addNode(feature);
			}
			contain = new Contain();
			contain.setStart(testCase);
			contain.setEnd(feature);
			addRelation(contain);
		}
		extractFunctionNodes(executeFile, testCase);
	}
	
	private void extractFunctionNodes(File executeFile, TestCase testCase) {
		Map<String, List<Function>> functions = this.getNodes().allFunctionsByFunctionName();
		Map<String, Map<Integer, List<DynamicFunctionFromKieker>>> allDynamicFunctionFromKiekers = DynamicUtil.readKiekerFile(executeFile);
		for(Map<Integer, List<DynamicFunctionFromKieker>> groups : allDynamicFunctionFromKiekers.values()) {
			for(List<DynamicFunctionFromKieker> group : groups.values()) {
				for(DynamicFunctionFromKieker calledDynamicFunction : group) {
					if(calledDynamicFunction.getDepth() == 0 && calledDynamicFunction.getBreadth() == 0) {
						List<Function> calledFunctions = functions.get(calledDynamicFunction.getFunctionName());
						if(calledFunctions == null) {
							continue;
						}
						Function calledFunction = findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
						if(calledFunction == null) {
							continue;
						}
						Contain contain = new Contain();
						contain.setStart(testCase);
						contain.setEnd(calledFunction);
						addRelation(contain);
						continue;
					}
					if(calledDynamicFunction.getDepth() < 1) {
						continue;
					}
					// 找出Kieker中调用calledDynamicFunction的方法callerDynamicFunction
					List<Integer> list = new ArrayList<>();
					for(DynamicFunctionFromKieker test : groups.get(calledDynamicFunction.getDepth() - 1)) {
						list.add(test.getBreadth());
					}
//					DynamicFunctionFromKieker callerDynamicFunction = DynamicUtil.findCallerFunction(calledDynamicFunction, groups.get(calledDynamicFunction.getDepth() - 1));
					DynamicFunctionFromKieker callerDynamicFunction = null;
					if(DynamicUtil.find(calledDynamicFunction.getBreadth(), list) != -1) {
						callerDynamicFunction = groups.get(calledDynamicFunction.getDepth() - 1).get(DynamicUtil.find(calledDynamicFunction.getBreadth(), list));
					}
					if(callerDynamicFunction == null) {
						continue;
					}
					// 找出在静态分析中对应的calledFunction和callerFunction
					// 可能存在方法名相同，通过参数精确判断出哪个方法
					List<Function> calledFunctions = functions.get(calledDynamicFunction.getFunctionName());
					List<Function> callerFunctions = functions.get(callerDynamicFunction.getFunctionName());
					if(calledFunctions == null || callerFunctions == null) {
//						System.out.println("list is null");
//						System.out.println("calledDynamicFunction: " + calledDynamicFunction);
//						System.out.println("callerDynamicFunction: " + callerDynamicFunction);
						continue;
					}
					Function calledFunction = findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
					Function callerFunction = findFunctionWithDynamic(callerDynamicFunction, callerFunctions);
					if(calledFunction == null || callerFunction == null) {
//						System.out.println("list is not null");
//						System.out.println("calledDynamicFunction: " + calledDynamicFunction);
//						System.out.println("callerDynamicFunction: " + callerDynamicFunction);
						continue;
					}
					FunctionDynamicCallFunction relation = new FunctionDynamicCallFunction(callerFunction, calledFunction);
					relation.setOrder(callerDynamicFunction.getBreadth() + ":" + callerDynamicFunction.getDepth() + " -> " + calledDynamicFunction.getBreadth() + ":" + calledDynamicFunction.getDepth());
					addRelation(relation);
				}
			}
		}
		System.out.println(this.getNodes().size());
	}
	
	private Function findFunctionWithDynamic(DynamicFunctionFromKieker dynamicFunction, List<Function> functions) {
		for(Function function : functions) {
			if(!dynamicFunction.getFunctionName().equals(function.getFunctionName())) {
				return null;
			}
			if(function.getParameters().size() != dynamicFunction.getParametersType().size()) {
				continue;
			}
			boolean flag = false;
			for(int i = 0; i < function.getParameters().size(); i++) {
				if(dynamicFunction.getParametersType().get(i).indexOf(function.getParameters().get(i)) < 0) {
					flag = true;
				}
			}
			if(flag) {
				continue;
			}
			return function;
		}
		return null;
	}

}
