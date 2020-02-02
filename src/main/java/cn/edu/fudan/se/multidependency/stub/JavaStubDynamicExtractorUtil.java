package cn.edu.fudan.se.multidependency.stub;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public class JavaStubDynamicExtractorUtil {
	
	
	public static DynamicFunctionExecutionFromStub extract(String sentence, String projectName) {
		try {
			DynamicFunctionExecutionFromStub functionExecution = new DynamicFunctionExecutionFromStub();
			functionExecution.setSentence(sentence);
			String[] splitFilePath = sentence.split("\\|");
			String filePath = splitFilePath[0];
			functionExecution.setFilePath(filePath);
			String executionInfo = splitFilePath[1];
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
			return null;
		}
	}
	
	public static void main(String[] args) {
		String str = "D:\\multiple-dependency-project\\depends\\src\\main\\java\\depends\\relations\\RelationCounter.java|depends.relations.RelationCounter.RelationCounter-(Collection<Entity>, Inferer, EntityRepo, boolean, AbstractLangProcessor)-939-3";
//		str = "D:\\multiple-dependency-project\\depends\\src\\main\\java\\depends\\relations\\RelationCounter.java|depends.relations.RelationCounter.RelationCounter-(Collection<Entity>)-939-3";
//		str = "D:\\multiple-dependency-project\\depends\\src\\main\\java\\depends\\relations\\RelationCounter.java|depends.relations.RelationCounter.RelationCounter-()-939-3";
		System.out.println(extract(str, "depends"));
	}
	
	@Data
	public static class DynamicFunctionExecutionFromStub { //读取到的方法，所具有的属性
		String sentence;
		String filePath;
		Long order;
		Long layer;
		String functionName;
		List<String> parameterTypes = new ArrayList<>();
		String time;
		String projectName;
		public void addParameterType(String parameterType) {
			this.parameterTypes.add(parameterType);
		}
	}
}
