package cn.edu.fudan.se.multidependency.service.spring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;
import lombok.Getter;

public class SpanWithFunctions implements Serializable {

	private static final long serialVersionUID = 1191449433118411194L;
	
	@Getter
	private Span span;
	
	@Getter
	private Function startFunction;
	
	@Getter
	private Map<Function, List<FunctionDynamicCallFunction>> functionToCalls = new HashMap<>();
	
	public SpanWithFunctions(SpanStartWithFunction spanStartWithFunction, List<FunctionDynamicCallFunction> functionCalls) {
		init(spanStartWithFunction, functionCalls);
	}
	
	private void init(SpanStartWithFunction spanStartWithFunction, List<FunctionDynamicCallFunction> functionCalls) {
		this.span = spanStartWithFunction.getSpan();
		this.startFunction = spanStartWithFunction.getFunction();
		for(FunctionDynamicCallFunction call : functionCalls) {
			if(!span.getSpanId().equals(call.getSpanId()) || !span.getTraceId().equals(call.getTraceId())) {
				continue;
			}
			Function caller = call.getFunction();
			List<FunctionDynamicCallFunction> callFunctions = functionToCalls.get(caller);
			callFunctions = callFunctions == null ? new ArrayList<>() : callFunctions;
			callFunctions.add(call);
			functionToCalls.put(caller, callFunctions);
		}
	}

}
