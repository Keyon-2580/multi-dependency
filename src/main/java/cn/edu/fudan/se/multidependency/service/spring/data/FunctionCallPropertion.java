package cn.edu.fudan.se.multidependency.service.spring.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;

public class FunctionCallPropertion {
	
	public FunctionCallPropertion(Map<Function, List<FunctionCallFunction>> staticCalls, Map<Function, Map<Function, FunctionCallPropertionDetail>> dynamicCalls) {
		this.staticCalls = staticCalls;
		this.dynamicCalls = dynamicCalls;
	}
	
	/**
	 *  所有静态调用
	 */
	private Map<Function, List<FunctionCallFunction>> staticCalls;
	
	public Map<Function, List<FunctionCallFunction>> getStaticCalls() {
		return new HashMap<>(staticCalls);
	}
	
	/**
	 *  没有被动态调用的静态调用
	 */
	private Map<Function, Map<Function, FunctionCallPropertionDetail>> dynamicCalls;
	
	public Map<Function, Map<Function, FunctionCallPropertionDetail>> getDynamicCalls() {
		return new HashMap<>(dynamicCalls);
	}
	
	public JSONArray treeView() {
		JSONArray result = new JSONArray();
		JSONArray tags = new JSONArray();
		tags.add("function");
		JSONArray callTags = new JSONArray();
		callTags.add("call");
		callTags.add("function");
		for(Function caller : staticCalls.keySet()) {
			JSONObject functionJson = new JSONObject();
			functionJson.put("tags", tags);
			Map<Function, FunctionCallPropertionDetail> details = dynamicCalls.getOrDefault(caller, new HashMap<>());
			JSONArray callFunctionArray = new JSONArray();
			List<FunctionCallFunction> callFunctions = staticCalls.get(caller);
			functionJson.put("text", caller.getName() + caller.getParametersIdentifies() + " (" + details.size() + " / " + callFunctions.size() + ") ");
			for(FunctionCallFunction callFunction : callFunctions) {
				JSONObject callFunctionJson = new JSONObject();
				callFunctionJson.put("tags", callTags);
				callFunctionArray.add(callFunctionJson);
				FunctionCallPropertionDetail detail = details.get(callFunction.getCallFunction());
				StringBuilder builder = new StringBuilder();
				builder.append(callFunction.getCallFunction().getName())
					.append(callFunction.getCallFunction().getParametersIdentifies())
					.append(" (")
					.append(callFunction.getTimes())
					.append(") "); 
				if(detail != null) {
					callFunctionJson.put("backColor", "#00FF00");
					for(TestCase testCase : detail.getTestCaseCallTimes().keySet()) {
						builder.append(" (")
							.append(testCase.getName())
							.append(" : ")
							.append(detail.getTestCaseCallTimes().get(testCase))
							.append(")");
					}
				}
				callFunctionJson.put("text", builder.toString());
				
			}
			functionJson.put("nodes", callFunctionArray);
			result.add(functionJson);
		}
		return result;
	}
	
	/**
	 * 没有被动态调用的静态调用的占比
	 * @return
	 */
	public double coverageOfDynamicCalls() {
		return (sizeOfDynamicFunctionCallFunction() + 0.0) / sizeOfStaticFunctionCallFunction();
	}
	
	public double coverageOfNotDynamicCalls() {
		return 1 - (sizeOfDynamicFunctionCallFunction() + 0.0) / sizeOfStaticFunctionCallFunction();
	}
	
	public int sizeOfStaticFunctionCallFunction() {
		int count = 0;
		for(List<FunctionCallFunction> call : staticCalls.values()) {
			count += call.size();
		}
		
		return count;
	}
	
	public int sizeOfDynamicFunctionCallFunction() {
		int count = 0;
		for(Map<Function, FunctionCallPropertionDetail> call : dynamicCalls.values()) {
			count += call.size();
		}
		
		return count;
	}
}
