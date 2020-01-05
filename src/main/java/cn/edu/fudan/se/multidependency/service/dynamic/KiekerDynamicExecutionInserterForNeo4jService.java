package cn.edu.fudan.se.multidependency.service.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.utils.DynamicUtil;
import cn.edu.fudan.se.multidependency.utils.DynamicUtil.DynamicFunctionExecutionFromKieker;

public class KiekerDynamicExecutionInserterForNeo4jService extends KiekerDynamicInserterForNeo4jService {
	
	protected void extractNodesAndRelations() throws Exception {
		Map<String, List<Function>> functions = this.getNodes().allFunctionsByFunctionName();
		Map<String, Map<Integer, List<DynamicFunctionExecutionFromKieker>>> allDynamicFunctionFromKiekers = DynamicUtil.readKiekerExecutionFile(executeFiles);
		TestCase currentTestCase = null;
		for(Map<Integer, List<DynamicFunctionExecutionFromKieker>> groups : allDynamicFunctionFromKiekers.values()) {
			for(List<DynamicFunctionExecutionFromKieker> group : groups.values()) {
				for(DynamicFunctionExecutionFromKieker calledDynamicFunction : group) {
					if(calledDynamicFunction.getDepth() == 0 && calledDynamicFunction.getBreadth() == 0) {
						// 是某段程序入口
						List<Function> calledFunctions = functions.get(calledDynamicFunction.getFunctionName());
						if(calledFunctions == null) {
							continue;
						}
						Function calledFunction = DynamicUtil.findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
						if(calledFunction == null) {
							continue;
						}
						currentTestCase = this.nodeEntityIdToTestCase.get(calledFunction.getEntityId());
						continue;
					}
					// 找出Kieker中调用calledDynamicFunction的方法callerDynamicFunction
					List<Integer> list = new ArrayList<>();
					for(DynamicFunctionExecutionFromKieker test : groups.get(calledDynamicFunction.getDepth() - 1)) {
						list.add(test.getBreadth());
					}
//					DynamicFunctionFromKieker callerDynamicFunction = DynamicUtil.findCallerFunction(calledDynamicFunction, groups.get(calledDynamicFunction.getDepth() - 1));
					DynamicFunctionExecutionFromKieker callerDynamicFunction = null;
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
					Function calledFunction = DynamicUtil.findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
					Function callerFunction = DynamicUtil.findFunctionWithDynamic(callerDynamicFunction, callerFunctions);
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
	
	
}
