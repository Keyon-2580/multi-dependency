package cn.edu.fudan.se.multidependency.stub;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public class JavaStubDynamicExtractorUtil {
	
	
	public static DynamicFunctionExecutionFromStub extract(String sentence) {
		try {
			DynamicFunctionExecutionFromStub functionExecution = new DynamicFunctionExecutionFromStub();
			functionExecution.setSentence(sentence);
			String[] splitFilePath = sentence.split("\\|");
			String time = splitFilePath[0];
			functionExecution.setTime(time);
			String projectName = splitFilePath[1];
			functionExecution.setProjectName(projectName);
			String filePath = splitFilePath[2];
			functionExecution.setFilePath(filePath);
			String executionInfo = splitFilePath[3];
			String[] splitExecutionInfos = executionInfo.split("-");
			functionExecution.setLayer(Long.parseLong(splitExecutionInfos[3]));
			functionExecution.setOrder(Long.parseLong(splitExecutionInfos[2]));
			functionExecution.setFunctionName(splitExecutionInfos[0]);
			String parameterTypesStr = splitExecutionInfos[1].replace("(", "").replace(")", "");
			String[] parameterTypes = parameterTypesStr.split(",");
			for(String parameterType : parameterTypes) {
				functionExecution.addParameterType(parameterType.trim());
			}
			return functionExecution;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		String str = "2020/02/03-14:33:41|depends|D:\\multiple-dependency-project\\depends\\target\\generated-sources\\antlr4\\depends\\extractor\\java\\JavaParserBaseListener.java|depends.extractor.java.JavaParserBaseListener.exitEveryRule-(ParserRuleContext)-251-2";
		System.out.println(extract(str));
	}
	
	@Data
	public static class DynamicFunctionExecutionFromStub { //读取到的方法，所具有的属性
		String time;
		String projectName;
		String filePath;
		Long order;
		Long layer;
		String functionName;
		List<String> parameterTypes = new ArrayList<>();
		String sentence;
		public void addParameterType(String parameterType) {
			this.parameterTypes.add(parameterType);
		}
	}
}
