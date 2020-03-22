package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FunctionCallPropertion {
	
	// 所有静态调用
	private Map<Function, List<FunctionCallFunction>> staticCalls;
	
	// 没有被动态调用的静态调用
	private Map<Function, List<FunctionCallFunction>> notDynamicCalls;
	
	/**
	 * 没有被动态调用的静态调用的占比
	 * @return
	 */
	public double propertionOfNotDynamicCalls() {
		return (sizeOfNotDynamicFunctionCallFunction() + 0.0) / sizeOfStaticFunctionCallFunction();
	}
	
	public int sizeOfStaticFunctionCallFunction() {
		return sizeOfFunctionCallFunction(staticCalls);
	}
	
	public int sizeOfNotDynamicFunctionCallFunction() {
		return sizeOfFunctionCallFunction(notDynamicCalls);
	}
	
	public static int sizeOfFunctionCallFunction(Map<Function, List<FunctionCallFunction>> calls) {
		int count = 0;
		for(List<FunctionCallFunction> call : calls.values()) {
			count += call.size();
		}
		
		return count;
	}
}
