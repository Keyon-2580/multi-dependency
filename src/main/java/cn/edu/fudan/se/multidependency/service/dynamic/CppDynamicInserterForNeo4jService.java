package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import cn.edu.fudan.se.multidependency.utils.CppDynamicUtil;
import cn.edu.fudan.se.multidependency.utils.FileUtils;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil;

public class CppDynamicInserterForNeo4jService extends DynamicInserterForNeo4jService {

	@Override
	protected void extractScenarioAndTestCaseAndFeatures() {
		try(BufferedReader reader = new BufferedReader(new FileReader(markFile))) {
			String line = null;
			Node defineNode = null;
			while((line = reader.readLine()) != null) {
				if(StringUtils.isBlank(line)) {
					continue;
				}
				if(line.startsWith(NodeType.Scenario.name())) {
					Scenario extractScenario = JavaDynamicUtil.extractScenarioFromMarkLine(line);
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
					TestCase extractTestCase = JavaDynamicUtil.extractTestCaseFromMarkLine(line);
					if(extractTestCase == null) {
						continue;
					}
					if(defineNode != null && defineNode.getClass() == Function.class) {
						Function function = (Function) defineNode;
						String testCaseName = function.getFunctionName() + function.getParameters().toString().replace('[', '(').replace(']', ')');
						TestCase testCase = getNodes().findTestCaseByName(testCaseName);
						if(testCase == null) {
							testCase = extractTestCase;
							testCase.setEntityId(generateEntityId());
							testCase.setTestCaseName(testCaseName);
							addNode(testCase);
						}
						this.nodeEntityIdToTestCase.put(defineNode.getEntityId(), testCase);
						NodeIsTestCase isTestCase = new NodeIsTestCase();
						isTestCase.setNode(defineNode);
						isTestCase.setTestCase(testCase);
						addRelation(isTestCase);
					}
				} else if(line.startsWith(NodeType.Feature.name())) {
					Feature extractFeature = JavaDynamicUtil.extractFeatureFromMarkLine(line);
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
						if(functions == null) {
							System.out.println("名为 " + lineFunctionName + " 的方法在图中没有找到！");
						}
						functions = functions == null ? new ArrayList<>() : functions;
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

	@Override
	protected void extractNodesAndRelations() throws Exception {
		Map<String, List<Function>> functions = this.getNodes().allFunctionsByFunctionName();
		List<String> cppFiles = new ArrayList<>();
		List<File> functionCallFiles = new ArrayList<>();
		List<File> callgrindFiles = new ArrayList<>();
		for(File f : dynamicFunctionCallFiles) {
			String suffix = FileUtils.extractSuffix(f.getAbsolutePath());
			if(".dot".equals(suffix)) {
				functionCallFiles.add(f);
			} else if(".out".equals(suffix)) {
				callgrindFiles.add(f);
			}
		}
		for(File cppFile : callgrindFiles) {
			cppFiles.addAll(CppDynamicUtil.extractFile(cppFile, getNodes().getProject().getProjectPath()));
		}
		for(File dynamicFile : functionCallFiles) {
			Map<String, List<String>> result = CppDynamicUtil.extractFunctionCall(dynamicFile);
			for(String start : result.keySet()) {
				List<String> ends = result.get(start);
				List<Function> startFunctions = functions.get(start);
				Function startFunction = null;
				if(startFunctions != null && startFunctions.size() != 0) {
					if(startFunctions.size() == 1) {
						startFunction = startFunctions.get(0);
					} else {
						for(Function f : startFunctions) {
							for(String cppFile : cppFiles) {
								if(cppFile.lastIndexOf(f.getInFilePath()) >= 0) {
									startFunction = f;
								}
							}
						}
					}
				}
				if(startFunction == null) {
					/*if(startFunctions != null) {
						System.out.println("名为 " + start + " 的函数有 " + startFunctions.size() + " 个 " + startFunctions);
						for(Function f : startFunctions) {
							System.out.println(f.getInFilePath());
						}
					}*/
					continue;
				}
				for(String end : ends) {
					Function endFunction = null;
					List<Function> endFunctions = functions.get(end);
					if(endFunctions != null && endFunctions.size() != 0) {
						if(endFunctions.size() == 1) {
							endFunction = endFunctions.get(0);
						} else {
							for(Function f : endFunctions) {
								for(String cppFile : cppFiles) {
									if(cppFile.lastIndexOf(f.getInFilePath()) >= 0) {
										endFunction = f;
									}
								}
							}
						}
					}
					if(endFunction == null) {
						/*if(endFunctions != null) {
							System.out.println("名为 " + end + " 的函数有 " + endFunctions.size() + " 个 " + endFunctions);
							for(Function f : endFunctions) {
								System.out.println(f.getInFilePath());
							}
						}*/
						continue;
					} else {
						FunctionDynamicCallFunction dynamicCall = new FunctionDynamicCallFunction();
						dynamicCall.setFunction(startFunction);
						dynamicCall.setCallFunction(endFunction);
						addRelation(dynamicCall);
					}
				}
			}
		}
	}

}
