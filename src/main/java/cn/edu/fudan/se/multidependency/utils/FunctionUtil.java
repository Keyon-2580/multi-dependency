package cn.edu.fudan.se.multidependency.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class FunctionUtil {
	
	public static void main(String[] args) throws Exception {
//		System.out.println(extractFunctionNameAndParameters("depends.format.json.JsonFormatDependencyDumper.toJson(JDepObject, String)"));
		String functionFullName = "depends.extractor.cpp.MacroEhcacheRepo.putMacros(List<String>, Map<String, List<String>>, String, Map<String, String>, IASTPreprocessorMacroDefinition[])";
		functionFullName = "depends.extractor.cpp.MacroEhcacheRepo.MacroEhcacheRepo(List<String>, Map<String, List<String>>, String, Map<String, String>, IASTPreprocessorMacroDefinition[])";
		while(functionFullName.contains("<")) {
			functionFullName = functionFullName.replaceAll("<[^<>]*>", "");
		}
		System.out.println(extractFunctionNameAndParameters(functionFullName));
	}
	
	public static List<String> extractFunctionNameAndParameters(String functionFullName) throws Exception {
		while(functionFullName.contains("<")) {
			functionFullName = functionFullName.replaceAll("<[^<>]*>", "");
		}
		List<String> result = new ArrayList<>();
		int index = functionFullName.indexOf("(");
		String functionName = functionFullName.substring(0, index);
		if(functionName.contains(".")) {
			String[] functionNameSplit = functionName.split("\\.");
			boolean isContrustor = false;
			if(functionNameSplit.length >= 2) {
				isContrustor = functionNameSplit[functionNameSplit.length - 1].equals(functionNameSplit[functionNameSplit.length - 2]);
			}
			if(isContrustor) {
				functionName = functionName.substring(0, functionName.lastIndexOf("."));
			}
		}
		result.add(functionName);
		String parametersStr = functionFullName.substring(index + 1, functionFullName.length() - 1);
		String[] parameters = parametersStr.split(",");
		for(String parameter : parameters) {
			if(!StringUtils.isBlank(parameter)) {
				result.add(parameter.trim());
			}
		}
		return result;
	}
	
}
