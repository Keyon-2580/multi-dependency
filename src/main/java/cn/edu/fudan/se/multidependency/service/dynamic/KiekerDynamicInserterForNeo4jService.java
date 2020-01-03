package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.NodeIsFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.NodeIsScenario;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.NodeIsTestCase;
import cn.edu.fudan.se.multidependency.utils.DynamicUtil;
import cn.edu.fudan.se.multidependency.utils.DynamicUtil.DynamicFunctionFromKieker;

public class KiekerDynamicInserterForNeo4jService extends DynamicInserterForNeo4jService {
	
	/**
	 * 从文件名中提取出场景、特性、测试用例名称
	 */
	@Override
	protected void extractScenarioAndTestCaseAndFeatures() {
		try(BufferedReader reader = new BufferedReader(new FileReader(markFile))) {
			String line = null;
			Node defineNode = null;
			while((line = reader.readLine()) != null) {
				if("".equals(line.trim())) {
					continue;
				}
				if(line.startsWith(NodeType.Scenario.name())) {
					Scenario extractScenario = DynamicUtil.extractScenarioFromMarkLine(line);
					if(extractScenario == null) {
						continue;
					}
					Scenario scenario = this.getNodes().findScenarioByName(extractScenario.getScenarioName());
					if(scenario == null) {
						scenario = extractScenario;
						scenario.setEntityId(generateEntityId());
						addNode(scenario);
					}
					if(defineNode != null) {
						NodeIsScenario isScenario = new NodeIsScenario();
						isScenario.setStartNode(defineNode);
						isScenario.setScenario(scenario);
						addRelation(isScenario);
						List<Scenario> scenarios = this.nodeEntityIdToScenarios.get(defineNode.getEntityId());
						scenarios = scenarios == null ? new ArrayList<>() : scenarios;
						scenarios.add(scenario);
						this.nodeEntityIdToScenarios.put(defineNode.getEntityId(), scenarios);
					}
				} else if(line.startsWith(NodeType.TestCase.name())) {
					TestCase extractTestCase = DynamicUtil.extractTestCaseFromMarkLine(line);
					if(extractTestCase == null) {
						continue;
					}
					TestCase testCase = this.getNodes().findTestCaseByName(extractTestCase.getTestCaseName());
					if(testCase == null) {
						testCase = extractTestCase;
						testCase.setEntityId(generateEntityId());
						addNode(testCase);
					}
					if(defineNode != null) {
						if(defineNode.getClass() == Function.class) {
							Function function = (Function) defineNode;
							testCase.setTestCaseName(function.getFunctionName() + function.getParameters().toString().replace('[', '(').replace(']', ')'));
							this.nodeEntityIdToTestCase.put(defineNode.getEntityId(), testCase);
						}
						NodeIsTestCase isScenario = new NodeIsTestCase();
						isScenario.setNode(defineNode);
						isScenario.setTestCase(testCase);
						addRelation(isScenario);
					}
				} else if(line.startsWith(NodeType.Feature.name())) {
					Feature extractFeature = DynamicUtil.extractFeatureFromMarkLine(line);
					if(extractFeature == null) {
						continue;
					}
					Feature feature = this.getNodes().findFeatureByFeature(extractFeature.getFeatureName());
					if(feature == null) {
						feature = extractFeature;
						feature.setEntityId(generateEntityId());
						addNode(feature);
					}
					if(defineNode != null) {
						NodeIsFeature isFeature = new NodeIsFeature();
						isFeature.setStartNode(defineNode);
						isFeature.setFeature(feature);
						addRelation(isFeature);
						List<Feature> features = this.nodeEntityIdToFeatures.get(defineNode.getEntityId());
						features = features == null ? new ArrayList<>() : features;
						features.add(feature);
						this.nodeEntityIdToFeatures.put(defineNode.getEntityId(), features);
					}
				} else {
					defineNode = null;
					if(line.contains("(") && line.contains(")")) {
						String lineFunctionName = line.substring(0, line.indexOf("("));
						String lineParameter = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
						List<Function> functions = getNodes().allFunctionsByFunctionName().get(lineFunctionName);
						for(Function function : functions) {
							if(StringUtils.isBlank(lineParameter)) {
								if(function.getParameters().size() == 0) {
									defineNode = function;
								}
							} else {
								String[] lineParameters = line.substring(line.indexOf("("), line.lastIndexOf(")")).split(",");
								if(lineParameters.length != function.getParameters().size()) {
									continue;
								}
								for(int i = 0; i < lineParameters.length; i++) {
									if(function.getParameters().get(i).lastIndexOf(lineParameters[i]) < 0) {
										continue;
									}
								}
								defineNode = function;
							}
						}
					} else {
						for(Type type : getNodes().findTypes().values()) {
							if(line.equals(type.getTypeName())) {
								defineNode = type;
								break;
							}
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
	
	protected void extractNodesAndRelations() throws Exception {
		Map<String, List<Function>> functions = this.getNodes().allFunctionsByFunctionName();
		Map<String, Map<Integer, List<DynamicFunctionFromKieker>>> allDynamicFunctionFromKiekers = DynamicUtil.readKiekerFile(executeFile);
		TestCase currentTestCase = null;
		for(Map<Integer, List<DynamicFunctionFromKieker>> groups : allDynamicFunctionFromKiekers.values()) {
			for(List<DynamicFunctionFromKieker> group : groups.values()) {
				for(DynamicFunctionFromKieker calledDynamicFunction : group) {
					if(calledDynamicFunction.getDepth() == 0 && calledDynamicFunction.getBreadth() == 0) {
						// 是某段程序入口
						List<Function> calledFunctions = functions.get(calledDynamicFunction.getFunctionName());
						if(calledFunctions == null) {
							continue;
						}
						Function calledFunction = findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
						if(calledFunction == null) {
							continue;
						}
						currentTestCase = this.nodeEntityIdToTestCase.get(calledFunction.getEntityId());
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
					if(currentTestCase != null) {
						relation.setTestCaseName(currentTestCase.getTestCaseName());
					}
					addRelation(relation);
				}
			}
		}
		System.out.println(this.getNodes().size());
	}
	
	/**
	 * 对应dynamicFunction与Function
	 * @param dynamicFunction
	 * @param functions
	 * @return
	 */
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
