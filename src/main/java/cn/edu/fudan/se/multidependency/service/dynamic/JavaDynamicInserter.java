package cn.edu.fudan.se.multidependency.service.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicFunctionExecution;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil;

public class JavaDynamicInserter extends DynamicInserterForNeo4jService {
	
	@Override
	protected void extractNodesAndRelations() throws Exception {
		Map<String, Map<String, Map<Long, List<JavaDynamicFunctionExecution>>>> allDynamicFunctionFromJaegerStub 
			= JavaDynamicUtil.readJavaDynamicLogs(dynamicFunctionCallFiles);
		Map<String, List<Function>> functions = null;// 获取到所分析项目的所有方法，由于涉及到方法重载，所以一个方法名，可能对应几个方法
		for (String traceId : allDynamicFunctionFromJaegerStub.keySet()) {
			Map<String, Map<Long, List<JavaDynamicFunctionExecution>>> spansResult = allDynamicFunctionFromJaegerStub.get(traceId);
			for (String spanId : spansResult.keySet()) {
				Map<Long, List<JavaDynamicFunctionExecution>> depthResult = spansResult.get(spanId);
				for (Long depth : depthResult.keySet()) {
					List<JavaDynamicFunctionExecution> executions = depthResult.get(depth);
					for (JavaDynamicFunctionExecution calledDynamicFunction : executions) {
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
								Function calledFunction = JavaDynamicUtil
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
						// 根据order和depth找到调用calledDynamicFunction的函数
						List<Long> list = new ArrayList<>();
						for (JavaDynamicFunctionExecution test : depthResult.get(calledDynamicFunction.getDepth() - 1)) {
							list.add(test.getOrder());
						}
						JavaDynamicFunctionExecution callerDynamicFunction = null;
						int callerIndex = JavaDynamicUtil.find(calledDynamicFunction.getOrder(), list);
						if (callerIndex != -1) {
							callerDynamicFunction = depthResult.get(calledDynamicFunction.getDepth() - 1).get(callerIndex);
						} else {
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
						Function calledFunction = JavaDynamicUtil
								.findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
						Function callerFunction = JavaDynamicUtil
								.findFunctionWithDynamic(callerDynamicFunction, callerFunctions);
						if (calledFunction == null || callerFunction == null) {
							// System.out.println("list is not null");
							// System.out.println("calledDynamicFunction: " + calledDynamicFunction);
							// System.out.println("callerDynamicFunction: " + callerDynamicFunction);
							continue;
						}
						FunctionDynamicCallFunction relation = new FunctionDynamicCallFunction(callerFunction,
								calledFunction, calledDynamicFunction.getProject(), calledDynamicFunction.getLanguage());
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
