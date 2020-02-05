package cn.edu.fudan.se.multidependency.stub;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

public class JavaStubDynamicExtractorUtil {
	
	public static DynamicFunctionExecutionFromStub extractByJson(String sentence) {
		try {
			DynamicFunctionExecutionFromStub functionExecution = new DynamicFunctionExecutionFromStub();
			functionExecution.setSentence(sentence);
			JSONObject json = JSONObject.parseObject(sentence);
			functionExecution.setLanguage(json.getString("language"));
			functionExecution.setTime(json.getString("time"));
			functionExecution.setProject(json.getString("project"));
			functionExecution.setInFile(json.getString("inFile"));
			functionExecution.setFunction(json.getString("function"));
			functionExecution.setOrder(Long.parseLong(json.getString("order")));
			functionExecution.setLayer(Long.parseLong(json.getString("layer")));
			functionExecution.setRemarks(json.getJSONObject("remarks"));
			return functionExecution;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		String str = "{\"language\" : \"java\", \"time\" : \"2020-02-05 14:34:06,450\", \"project\" : \"depends\", \"inFile\" : \"D:\\\\multiple-dependency-project\\\\depends\\\\src\\\\main\\\\java\\\\depends\\\\entity\\\\GenericName.java\", \"function\" : \"depends.entity.GenericName.getArguments()\", \"order\" : \"11\", \"layer\" : \"5\", \"remarks\" : {\"scenario\":\"eew\",\"featureId\":\"123\"}}";
		System.out.println(extractByJson(str));
	}
	
	@Data
	public static class DynamicFunctionExecutionFromStub { //读取到的方法，所具有的属性
		String language;
		String time;
		String project;
		String inFile;
		Long order;
		Long layer;
		String function;
		JSONObject remarks;
		String sentence;
	}
}
