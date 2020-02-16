package cn.edu.fudan.se.multidependency.stub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.code.Function;

public class JavaStubDynamicExtractorUtil {
	
	/**
	 * remarks
	 * depth
	 * @param files
	 * @return
	 */
	public static Map<String, Map<Long, List<DynamicFunctionExecutionFromStub>>> readStubLogs(File... files) {
		Map<String, Map<Long, List<DynamicFunctionExecutionFromStub>>> result = new HashMap<>();
		for(File file : files) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = null;
				DynamicFunctionExecutionFromStub last = null;
				while ((line = reader.readLine()) != null) {
					DynamicFunctionExecutionFromStub dynamicFunction = extractByJson(line);
					if (dynamicFunction == null) {
						continue;
					}
					if(last != null && last.getOrder() >= dynamicFunction.getOrder()) {
						break;
					}
					String remarks = dynamicFunction.getRemarks().toString();
					Map<Long, List<DynamicFunctionExecutionFromStub>> groups = result.get(remarks);
					groups = groups == null ? new HashMap<>() : groups;
					Long depth = dynamicFunction.getDepth();
					List<DynamicFunctionExecutionFromStub> functions = groups.get(depth);
					functions = functions == null ? new ArrayList<>() : functions;
					functions.add(dynamicFunction);
					groups.put(depth, functions);
					result.put(remarks, groups);
				}
				for(Map<Long, List<DynamicFunctionExecutionFromStub>> groups : result.values()) {
					for(List<DynamicFunctionExecutionFromStub> functions : groups.values()) {
						functions.sort(new Comparator<DynamicFunctionExecutionFromStub>() {
							@Override
							public int compare(DynamicFunctionExecutionFromStub o1, DynamicFunctionExecutionFromStub o2) {
								return (int) (o1.getOrder() - o2.getOrder());
							}
						});
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private static void addDynamicFunctionExecutionForJaegerFromStub(
			Map<String, Map<String, Map<Long, List<DynamicFunctionExecutionForJaegerFromStub>>>> result, 
			DynamicFunctionExecutionForJaegerFromStub execution) {
		String traceId = execution.getTraceId();
		String spanId = execution.getSpanId();
		// 去掉没有traceId或没有spanId的函数执行
		if(StringUtils.isBlank(traceId) || StringUtils.isBlank(spanId)) {
			return;
		}
		Long depth = execution.getDepth();
		Map<String, Map<Long, List<DynamicFunctionExecutionForJaegerFromStub>>> spanResult = result.get(traceId);
		spanResult = spanResult == null ? new HashMap<>() : spanResult;
		Map<Long, List<DynamicFunctionExecutionForJaegerFromStub>> depthResult = spanResult.get(spanId);
		depthResult = depthResult == null ? new HashMap<>() : depthResult;
		List<DynamicFunctionExecutionForJaegerFromStub> executions = depthResult.get(depth);
		executions = executions == null ? new ArrayList<>() : executions;
		executions.add(execution);
		depthResult.put(depth, executions);
		spanResult.put(spanId, depthResult);
		result.put(traceId, spanResult);
	}

	
	public static Map<String, Map<String, Map<Long, List<DynamicFunctionExecutionForJaegerFromStub>>>> readStubJaegerLogs(File... files) {
		Map<String, Map<String, Map<Long, List<DynamicFunctionExecutionForJaegerFromStub>>>> result = new HashMap<>();
		for(File file : files) {
//			System.out.println("read log file: " + file.getAbsolutePath());
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					DynamicFunctionExecutionForJaegerFromStub dynamicFunction = extractByJaegerJson(line);
					if (dynamicFunction == null) {
						continue;
					}
					addDynamicFunctionExecutionForJaegerFromStub(result, dynamicFunction);
				}
				for(Map<String, Map<Long, List<DynamicFunctionExecutionForJaegerFromStub>>> spansResult : result.values()) {
					for(Map<Long, List<DynamicFunctionExecutionForJaegerFromStub>> groups : spansResult.values()) {
						for(List<DynamicFunctionExecutionForJaegerFromStub> functions : groups.values()) {
							functions.sort(new Comparator<DynamicFunctionExecutionForJaegerFromStub>() {
								@Override
								public int compare(DynamicFunctionExecutionForJaegerFromStub o1, DynamicFunctionExecutionForJaegerFromStub o2) {
									return (int) (o1.getOrder() - o2.getOrder());
								}
							});
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static DynamicFunctionExecutionForJaegerFromStub extractByJaegerJson(String sentence) {
		try {
			DynamicFunctionExecutionForJaegerFromStub functionExecution = new DynamicFunctionExecutionForJaegerFromStub();
			sentence = sentence.replace("\\", "\\\\");
			functionExecution.setSentence(sentence);
			JSONObject json = JSONObject.parseObject(sentence);
			functionExecution.setLanguage(json.getString("language"));
			functionExecution.setTime(json.getString("time"));
			functionExecution.setProject(json.getString("project"));
			functionExecution.setInFile(json.getString("inFile"));
			String function = json.getString("function");
			String functionName = function.substring(0, function.indexOf("("));
			functionExecution.setFunctionName(functionName);
			String parametersStr = function.substring(function.indexOf("(") + 1, function.length() - 1);
			if(!StringUtils.isBlank(parametersStr)) {
				String[] parameters = parametersStr.split(",");
				for(String parameter : parameters) {
					functionExecution.addParameter(parameter.trim());
				}
			}
			functionExecution.setOrder(Long.parseLong(json.getString("order")));
			functionExecution.setDepth(Long.parseLong(json.getString("depth")));
			functionExecution.setRemarks(json.getJSONObject("remarks"));
			functionExecution.setTraceId(json.getString("traceId"));
			functionExecution.setSpanId(json.getString("spanId"));
			return functionExecution;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static DynamicFunctionExecutionFromStub extractByJson(String sentence) {
		try {
			DynamicFunctionExecutionFromStub functionExecution = new DynamicFunctionExecutionFromStub();
			functionExecution.setSentence(sentence);
			JSONObject json = JSONObject.parseObject(sentence);
			functionExecution.setLanguage(json.getString("language"));
			functionExecution.setTime(json.getString("time"));
			functionExecution.setProject(json.getString("project"));
			functionExecution.setInFile(json.getString("inFile"));
			String function = json.getString("function");
			String functionName = function.substring(0, function.indexOf("("));
			functionExecution.setFunctionName(functionName);
			String parametersStr = function.substring(function.indexOf("(") + 1, function.length() - 1);
			if(!StringUtils.isBlank(parametersStr)) {
				String[] parameters = parametersStr.split(",");
				for(String parameter : parameters) {
					functionExecution.addParameter(parameter.trim());
				}
			}
			functionExecution.setOrder(Long.parseLong(json.getString("order")));
			functionExecution.setDepth(Long.parseLong(json.getString("depth")));
			functionExecution.setRemarks(json.getJSONObject("remarks"));
			return functionExecution;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		String str = "{\"language\" : \"java\", \"time\" : \"2020-02-05 14:34:06,450\", \"project\" : \"depends\", \"inFile\" : \"D:\\\\multiple-dependency-project\\\\depends\\\\src\\\\main\\\\java\\\\depends\\\\entity\\\\GenericName.java\", \"function\" : \"depends.entity.GenericName.getArguments()\", \"order\" : \"11\", \"depth\" : \"5\", \"remarks\" : {\"scenario\":\"eew\",\"featureId\":\"123\"}}";
		System.out.println(extractByJson(str));
		str = "{\"language\" : \"java\", \"time\" : \"2020-02-16 17:08:05,324\", \"project\" : \"ts-travel-service\", \"inFile\" : \"D:\\multiple-dependency-project\\train-ticket\\ts-travel-service\\src\\main\\java\\travel\\service\\TravelServiceImpl.java\", \"function\" : \"travel.service.TravelServiceImpl.getTripAllDetailInfo(TripAllDetailInfo, HttpHeaders)\", \"order\" : \"6\", \"depth\" : \"1\", \"traceId\" : \"af68737835b755c0\", \"spanId\" : \"c3de0841842f60ff\", \"remarks\" : {}}";
		str = str.replace("\\", "\\\\");
		System.out.println(str);
		System.out.println(extractByJaegerJson(str));
	}
	
	/**
	 * 从同名的Function中找到dynamicFunction对应参数的Function
	 * @param dynamicFunction
	 * @param functions
	 * @return
	 */
	public static Function findFunctionWithDynamic(DynamicFunctionExecutionFromStub dynamicFunction, List<Function> functions) {
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
			if(!dynamicFunction.getFunctionName().equals(function.getFunctionName())) {
				continue;
			}
			if(function.getParameters().size() != dynamicFunction.getParameters().size()) {
				continue;
			}
			if(functions.size() == 1) {
				return function;
			}
			boolean flag = false;
			for(int i = 0; i < function.getParameters().size(); i++) {
				if(dynamicFunction.getParameters().get(i).indexOf(function.getParameters().get(i)) < 0) {
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
	public static int find(Long num, List<Long> list) {
		int midIndex = list.size() / 2;
		if(num <= list.get(0)) {
			return -1;
		}
		if(num > list.get(list.size() - 1)) {
			return list.size() - 1;
		}
		int lessIndex = 0;
		int moreIndex = list.size() - 1;
		while(midIndex > lessIndex && midIndex < moreIndex) {
			if(list.get(midIndex) < num) {
				lessIndex = midIndex;
			} else if(list.get(midIndex) > num) {
				moreIndex = midIndex;
			}
			midIndex = (moreIndex + lessIndex) / 2;
		}
		return midIndex;
	}
}
