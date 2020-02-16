package cn.edu.fudan.se.multidependency.service.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.stub.DynamicFunctionExecutionFromStub;
import cn.edu.fudan.se.multidependency.stub.JavaStubDynamicExtractorUtil;

public class StubJavaDynamicInserter extends DynamicInserterForNeo4jService {

	@Override
	protected void extractScenarioAndTestCaseAndFeatures() {
		// do nothing
	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		Map<String, Map<Long, List<DynamicFunctionExecutionFromStub>>> allDynamicFunctionFromKiekers = JavaStubDynamicExtractorUtil
				.readStubLogs(dynamicFunctionCallFiles);
		Map<String, List<Function>> functions = null;// 获取到所分析项目的所有方法，由于涉及到方法重载，所以一个方法名，可能对应几个方法
		for (Map<Long, List<DynamicFunctionExecutionFromStub>> groups : allDynamicFunctionFromKiekers.values()) {
			for (List<DynamicFunctionExecutionFromStub> group : groups.values()) {
				for (DynamicFunctionExecutionFromStub calledDynamicFunction : group) {
					System.out.println("calledDynamicFunction " + calledDynamicFunction);
					if(functions == null) {
						functions = this.getNodes().findFunctionsInProject(this.getNodes().findProjectByNameAndLanguage(calledDynamicFunction.getProject(), Language.java.toString()));
					}
					if (calledDynamicFunction.getDepth() == 0 && calledDynamicFunction.getOrder() == 0) {
						// 是某段程序入口
						List<Function> calledFunctions = functions.get(calledDynamicFunction.getFunctionName());
						if (calledFunctions == null) {
							continue;
						}
						Function calledFunction = JavaStubDynamicExtractorUtil.findFunctionWithDynamic(calledDynamicFunction,
								calledFunctions);
						System.out.println("calledFunction " + calledFunction);
						continue;
					}
					List<Long> list = new ArrayList<>();
					System.out.println(calledDynamicFunction.getDepth() + " " + calledDynamicFunction.getFunctionName());
					for (DynamicFunctionExecutionFromStub test : groups.get(calledDynamicFunction.getDepth() - 1)) {
						list.add(test.getOrder());
					}
					DynamicFunctionExecutionFromStub callerDynamicFunction = null;
					if (JavaStubDynamicExtractorUtil.find(calledDynamicFunction.getOrder(), list) != -1) {
						callerDynamicFunction = groups.get(calledDynamicFunction.getDepth() - 1)
								.get(JavaStubDynamicExtractorUtil.find(calledDynamicFunction.getOrder(), list));
					}
					if (callerDynamicFunction == null) {
						continue;
					}
					// 找出在静态分析中对应的calledFunction和callerFunction
					// 可能存在方法名相同，通过参数精确判断出哪个方法
					List<Function> calledFunctions = functions.get(calledDynamicFunction.getFunctionName());
					List<Function> callerFunctions = functions.get(callerDynamicFunction.getFunctionName());
					if (calledFunctions == null || callerFunctions == null) {
						// System.out.println("list is null");
						// System.out.println("calledDynamicFunction: " + calledDynamicFunction);
						// System.out.println("callerDynamicFunction: " + callerDynamicFunction);
						continue;
					}
					Function calledFunction = JavaStubDynamicExtractorUtil.findFunctionWithDynamic(calledDynamicFunction,
							calledFunctions);
					Function callerFunction = JavaStubDynamicExtractorUtil.findFunctionWithDynamic(callerDynamicFunction,
							callerFunctions);
					if (calledFunction == null || callerFunction == null) {
						// System.out.println("list is not null");
						// System.out.println("calledDynamicFunction: " + calledDynamicFunction);
						// System.out.println("callerDynamicFunction: " + callerDynamicFunction);
						continue;
					}
					FunctionDynamicCallFunction relation = new FunctionDynamicCallFunction(callerFunction,
							calledFunction);
					relation.setOrder(callerDynamicFunction.getOrder() + ":" + callerDynamicFunction.getDepth()
							+ " -> " + calledDynamicFunction.getOrder() + ":" + calledDynamicFunction.getDepth());
					addRelation(relation);
				}
			}
		}
	}

}
