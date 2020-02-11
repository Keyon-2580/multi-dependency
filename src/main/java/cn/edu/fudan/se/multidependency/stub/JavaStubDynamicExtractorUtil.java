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
import lombok.Data;

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
	}
	
	@Data
	public static class DynamicFunctionExecutionFromStub { //读取到的方法，所具有的属性
		String language;
		String time;
		String project;
		String inFile;
		Long order;
		Long depth;
		String functionName;
		List<String> parameters = new ArrayList<>();
		public void addParameter(String parameter) {
			this.parameters.add(parameter);
		}
		JSONObject remarks;
		String sentence;
	}
	
	public static Function findFunctionWithDynamic(DynamicFunctionExecutionFromStub dynamicFunction, List<Function> functions) {
		System.out.println("findFunction " + functions.size() + " " + dynamicFunction.getFunctionName());
		for(Function function : functions) {
			System.out.println("findFunction " + function.getFunctionName());
			if(!dynamicFunction.getFunctionName().equals(function.getFunctionName())) {
				return null;
			}
			System.out.println(function.getParameters().size() + " " + dynamicFunction.getParameters().size());
			if(function.getParameters().size() != dynamicFunction.getParameters().size()) {
				continue;
			}
			boolean flag = false;
			for(int i = 0; i < function.getParameters().size(); i++) {
				if(dynamicFunction.getParameters().get(i).indexOf(function.getParameters().get(i)) < 0) {
					flag = true;
				}
			}
			System.out.println(flag);
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
