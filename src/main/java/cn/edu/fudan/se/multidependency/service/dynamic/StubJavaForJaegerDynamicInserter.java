package cn.edu.fudan.se.multidependency.service.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.stub.DynamicFunctionExecutionFromStub;
import cn.edu.fudan.se.multidependency.stub.JavaStubDynamicExtractorUtil;

public class StubJavaForJaegerDynamicInserter extends DynamicInserterForNeo4jService {

	@Override
	protected void extractScenarioAndTestCaseAndFeatures() {
		// do nothing
	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		Map<String, Map<String, Map<Long, List<DynamicFunctionExecutionFromStub>>>> allDynamicFunctionFromJaegerStub = JavaStubDynamicExtractorUtil
				.readStubJaegerLogs(dynamicFunctionCallFiles);
		Map<String, List<Function>> functions = null;// 获取到所分析项目的所有方法，由于涉及到方法重载，所以一个方法名，可能对应几个方法
		for (String traceId : allDynamicFunctionFromJaegerStub.keySet()) {
			Map<String, Map<Long, List<DynamicFunctionExecutionFromStub>>> spansResult = allDynamicFunctionFromJaegerStub.get(traceId);
			for (String spanId : spansResult.keySet()) {
				Map<Long, List<DynamicFunctionExecutionFromStub>> depthResult = spansResult.get(spanId);
				for (Long depth : depthResult.keySet()) {
					List<DynamicFunctionExecutionFromStub> executions = depthResult.get(depth);
					for (DynamicFunctionExecutionFromStub calledDynamicFunction : executions) {
						functions = this.getNodes().findFunctionsInProject(
								this.getNodes().findProjectByNameAndLanguage(calledDynamicFunction.getProject(),
										Language.java.toString()));
						if (calledDynamicFunction.getDepth() == 0) {
							if(calledDynamicFunction.getOrder() == 0) {
								// 是某段程序入口
								List<Function> calledFunctions = functions.get(calledDynamicFunction.getFunctionName());
								if (calledFunctions == null) {
									continue;
								}
								Function calledFunction = JavaStubDynamicExtractorUtil
										.findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
								Span span = this.getNodes().findSpanBySpanId(calledDynamicFunction.getSpanId());
								if(span != null && calledFunction != null) {
									SpanStartWithFunction spanStartWithFunction = new SpanStartWithFunction(span,
											calledFunction);
									addRelation(spanStartWithFunction);
								}
							}
							continue;
						}
						List<Long> list = new ArrayList<>();
						for (DynamicFunctionExecutionFromStub test : depthResult
								.get(calledDynamicFunction.getDepth() - 1)) {
							list.add(test.getOrder());
						}
						DynamicFunctionExecutionFromStub callerDynamicFunction = null;
						if (JavaStubDynamicExtractorUtil.find(calledDynamicFunction.getOrder(), list) != -1) {
							callerDynamicFunction = depthResult.get(calledDynamicFunction.getDepth() - 1)
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
						Function calledFunction = JavaStubDynamicExtractorUtil
								.findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
						Function callerFunction = JavaStubDynamicExtractorUtil
								.findFunctionWithDynamic(callerDynamicFunction, callerFunctions);
						if (calledFunction == null || callerFunction == null) {
							// System.out.println("list is not null");
							// System.out.println("calledDynamicFunction: " + calledDynamicFunction);
							// System.out.println("callerDynamicFunction: " + callerDynamicFunction);
							continue;
						}
						FunctionDynamicCallFunction relation = new FunctionDynamicCallFunction(callerFunction,
								calledFunction, calledDynamicFunction.getProject());
						relation.setTraceId(callerDynamicFunction.getTraceId());
						relation.setSpanId(callerDynamicFunction.getSpanId());
						relation.setOrder(callerDynamicFunction.getOrder() + ":" + callerDynamicFunction.getDepth()
								+ " -> " + calledDynamicFunction.getOrder() + ":" + calledDynamicFunction.getDepth());
						relation.setFromOrder(callerDynamicFunction.getOrder());
						relation.setToOrder(calledDynamicFunction.getOrder());
						relation.setFromDepth(callerDynamicFunction.getDepth());
						relation.setToDepth(calledDynamicFunction.getDepth());
						addRelation(relation);
					}
				}
			}
		}
	}

}
