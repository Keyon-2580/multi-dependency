package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.NodeIsFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.NodeIsScenario;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.NodeIsTestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.ScenarioDefineTestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.utils.DynamicUtil;
import cn.edu.fudan.se.multidependency.utils.DynamicUtil.DynamicFunctionFromKieker;

public class KiekerDynamicInserterForNeo4jService extends DynamicInserterForNeo4jService {
	
	/**
	 * 从文件名中提取出场景、特性、测试用例名称
	 */
	@Override
	protected void extractScenarioAndTestCaseAndFeatures() {
		/*featureName.clear();
		String fileName = executeFile.getName();
		String[] splits = fileName.split(",");
		scenarioName = splits[0];
		testcaseName = splits[1];
		for(int i = 2; i < splits.length; i++) {
			featureName.add(splits[i]);
		}*/
		try(BufferedReader reader = new BufferedReader(new FileReader(markFile))) {
			String line = null;
			Node defineNode = null;
			while((line = reader.readLine()) != null) {
				if(line.startsWith(NodeType.Scenario.name())) {
					String scenarioNamesLine = line.substring(NodeType.Scenario.name().length() + 1);
					System.out.println(scenarioNamesLine);
					String[] names = scenarioNamesLine.split(",");
					for(String name : names) {
						Scenario scenario = this.getNodes().findScenarioByName(name);
						if(scenario == null) {
							scenario = new Scenario();
							scenario.setScenarioName(name);
							scenario.setEntityId(generateEntityId());
							addNode(scenario);
						}
						NodeIsScenario isScenario = new NodeIsScenario();
						if(defineNode != null) {
							isScenario.setStartNode(defineNode);
							isScenario.setScenario(scenario);
							addRelation(isScenario);
						}
					}
				} else if(line.startsWith(NodeType.TestCase.name())) {
					String testCaseNamesLine = line.substring(NodeType.TestCase.name().length() + 1);
					String[] names = testCaseNamesLine.split(",");
					for(String name : names) {
						TestCase testCase = this.getNodes().findTestCaseByName(name);
						if(testCase == null) {
							testCase = new TestCase();
							testCase.setTestCaseName(name);
							testCase.setEntityId(generateEntityId());
							addNode(testCase);
						}
						NodeIsTestCase isScenario = new NodeIsTestCase();
						if(defineNode != null) {
							isScenario.setNode(defineNode);
							isScenario.setTestCase(testCase);;
							addRelation(isScenario);
						}
					}
				} else if(line.startsWith(NodeType.Feature.name())) {
					String featureNamesLine = line.substring(NodeType.Feature.name().length() + 1);
					String[] names = featureNamesLine.split(",");
					for(String name : names) {
						Feature feature = this.getNodes().findFeatureByFeature(name);
						if(feature == null) {
							feature = new Feature();
							feature.setFeatureName(name);
							feature.setEntityId(generateEntityId());
							addNode(feature);
						}
						NodeIsFeature isFeature = new NodeIsFeature();
						if(defineNode != null) {
							isFeature.setStartNode(defineNode);
							isFeature.setFeature(feature);
							addRelation(isFeature);
						}
					}
				} else {
					for(Type type : getNodes().findTypes().values()) {
						if(line.equals(type.getTypeName())) {
							defineNode = type;
							break;
						}
					}
					if(defineNode != null) {
						continue;
					}
					if(line.contains("(") && line.contains(")")) {
						String lineFunctionName = line.substring(0, line.indexOf("("));
						List<Function> functions = getNodes().allFunctionsByFunctionName().get(lineFunctionName);
						for(Function function : getNodes().findFunctions().values()) {
							String functionName = function.getFunctionName();
							String[] lineParameters = line.substring(line.indexOf("("), line.lastIndexOf(")")).split(",");
							
						}
					}
					
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
		ScenarioDefineTestCase define = new ScenarioDefineTestCase();
		define.setScenario(scenario);
		define.setTestCase(testCase);
		addRelation(define);
		for(String featureName : featureNames) {
			Feature feature = this.getNodes().findFeatureByFeature(featureName);
			if(feature == null) {
				feature = new Feature();
				feature.setEntityId(generateEntityId());
				feature.setFeatureName(featureName);
				addNode(feature);
			}
			TestCaseExecuteFeature execute = new TestCaseExecuteFeature();
			execute.setTestCase(testCase);
			execute.setFeature(feature);
			addRelation(execute);
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
					relation.setTestCaseName(testCase.getTestCaseName());
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
