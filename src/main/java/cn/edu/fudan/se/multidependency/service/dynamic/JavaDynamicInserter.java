package cn.edu.fudan.se.multidependency.service.dynamic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil.JavaDynamicFunctionExecution;

public class JavaDynamicInserter extends DynamicInserterForNeo4jService {
	
	protected Map<Project, Map<String, List<Function>>> projectToFunctions = new HashMap<>();
	
	@Override
	protected void extractNodesAndRelations() throws Exception {
		Map<String, Map<String, Map<Long, List<JavaDynamicFunctionExecution>>>> allDynamicFunctionFromJaegerStub 
			= JavaDynamicUtil.readJavaDynamicLogs(dynamicFunctionCallFiles);
		for (String traceId : allDynamicFunctionFromJaegerStub.keySet()) {
			Map<String, Map<Long, List<JavaDynamicFunctionExecution>>> spansResult = allDynamicFunctionFromJaegerStub.get(traceId);
			for (String spanId : spansResult.keySet()) {
				Map<Long, List<JavaDynamicFunctionExecution>> depthResult = spansResult.get(spanId);
				for (Long depth : depthResult.keySet()) {
					List<JavaDynamicFunctionExecution> executions = depthResult.get(depth);
					for (JavaDynamicFunctionExecution calledDynamicFunction : executions) {
						Project project = this.getNodes().findProject(calledDynamicFunction.getProject(),Language.java);
						
						Map<String, List<Function>> functions = projectToFunctions.get(project);
						if(functions == null) {
							functions = this.getNodes().findFunctionsInProject(project);
							projectToFunctions.put(project, functions);
						}
						
						if (calledDynamicFunction.getDepth() == 0) {
							if(calledDynamicFunction.getOrder() == 0) {
								// 是某段程序入口
								List<Function> calledFunctions = functions.get(calledDynamicFunction.getFunctionName());
								if (calledFunctions == null) {
									continue;
								}
								Function calledFunction = findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
								Span span = this.getNodes().findSpanBySpanId(calledDynamicFunction.getSpanId());
								if(span != null && calledFunction != null) {
									SpanStartWithFunction spanStartWithFunction = new SpanStartWithFunction(span,calledFunction);
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
						Function calledFunction = findFunctionWithDynamic(calledDynamicFunction, calledFunctions);
						Function callerFunction = findFunctionWithDynamic(callerDynamicFunction, callerFunctions);
						if (calledFunction == null || callerFunction == null) {
							// System.out.println("list is not null");
							// System.out.println("calledDynamicFunction: " + calledDynamicFunction);
							// System.out.println("callerDynamicFunction: " + callerDynamicFunction);
							continue;
						}
						addRelation(generateFunctionDynamicCall(callerFunction, calledFunction, callerDynamicFunction, calledDynamicFunction));
					}
				}
			}
		}
	}
	
	/**
	 * 从同名的Function中找到与dynamicFunction的参数对应的Function
	 * @param dynamicFunction
	 * @param functions
	 * @return
	 */
	private Function findFunctionWithDynamic(JavaDynamicFunctionExecution dynamicFunction, List<Function> functions) {
		// Function根据参数排序
		functions.sort(new Comparator<Function>() {
			@Override
			public int compare(Function o1, Function o2) {
				if(o1.getParameters() == null || o2.getParameters() == null) {
					return -1;
				}
				return o1.getParameters().size() - o2.getParameters().size();
			}
		});
		for(Function function : functions) {
			// 方法名是否相同
			if(!dynamicFunction.getFunctionName().equals(function.getFunctionName())) {
				continue;
			}
			// 方法参数数量是否相同
			if(function.getParameters().size() != dynamicFunction.getParameters().size()) {
				continue;
			}
			// 方法名相同且只有一个参数，直接返回此Function
			if(functions.size() == 1) {
				return function;
			}
			// 参数一一对应
			boolean flag = false;
			for(int i = 0; i < function.getParameters().size(); i++) {
				// 动态分析得到的参数的类型是完整的
				if(dynamicFunction.getParameters().get(i).indexOf(function.getParameters().get(i)) < 0) {
					// 参数没有对应
					flag = true;
					break;
				}
			}
			if(flag) {
				continue;
			}
			return function;
		}
		return null;
	}
	
	private FunctionDynamicCallFunction generateFunctionDynamicCall(Function callerFunction, Function calledFunction, JavaDynamicFunctionExecution callerDynamicFunction, JavaDynamicFunctionExecution calledDynamicFunction) {
		FunctionDynamicCallFunction relation = new FunctionDynamicCallFunction(callerFunction,calledFunction, calledDynamicFunction.getProject(), calledDynamicFunction.getLanguage());
		relation.setTraceId(callerDynamicFunction.getTraceId());
		relation.setSpanId(callerDynamicFunction.getSpanId());
		relation.setOrder(callerDynamicFunction.getOrder() + ":" + callerDynamicFunction.getDepth() + " -> " + calledDynamicFunction.getOrder() + ":" + calledDynamicFunction.getDepth());
		relation.setFromOrder(callerDynamicFunction.getOrder());
		relation.setToOrder(calledDynamicFunction.getOrder());
		relation.setFromDepth(callerDynamicFunction.getDepth());
		relation.setToDepth(calledDynamicFunction.getDepth());
		return relation;
	}
}
