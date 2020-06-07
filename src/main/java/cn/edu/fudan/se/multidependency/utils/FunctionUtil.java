package cn.edu.fudan.se.multidependency.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.edu.fudan.se.multidependency.model.node.code.Function;

public class FunctionUtil {
	
	public static void main(String[] args) throws Exception {
//		System.out.println(extractFunctionNameAndParameters("depends.format.json.JsonFormatDependencyDumper.toJson(JDepObject, String)"));
		String functionFullName = "depends.extractor.cpp.MacroEhcacheRepo.putMacros(List<String>, Map<String, List<String>>, String, Map<String, String>, IASTPreprocessorMacroDefinition[])";
		functionFullName = "depends.extractor.cpp.MacroEhcacheRepo.MacroEhcacheRepo(List<String>, Map<String, List<String>>, String, Map<String, String>, IASTPreprocessorMacroDefinition[])";
		functionFullName = "com.google.common.cache.CacheTesting.checkRecency(LoadingCache<Integer, Integer>,int,Receiver<ReferenceEntry<Integer, Integer)";
//		while(functionFullName.contains("<")) {
//			functionFullName = functionFullName.replaceAll("<[^<>]*>", "");
//		}
		System.out.println(extractFunctionNameAndParameters(functionFullName));
		
	/*	StringBuilder builder = new StringBuilder();
		builder.append("configure");
		builder.append("(");
		for(int i = 0; i < 5; i++) {
			if(i != 0) {
				builder.append(",");
			}
			builder.append("Map<String, Map<String,String>>");
		}
		builder.append(")");
		functionFullName = builder.toString();
		System.out.println(extractFunctionNameAndParameters(functionFullName));*/
	}
	
	/**
	 * 
	 * @param function
	 * @param functionFullName packageName + className + functionName + parameters
	 * @return
	 * @throws Exception
	 */
	public static boolean isSameJavaFunction(Function function, String functionFullName) throws Exception {
		List<String> functionNameAndParameters = extractFunctionNameAndParameters(functionFullName);
		String functionName = functionNameAndParameters.get(0);
		if(!function.getName().equals(functionName)) {
			return false;
		}
		if(function.getParameters().size() != (functionNameAndParameters.size() - 1)) {
			return false;
		}
		/// FIXME
		return true;
	}
	
	public static List<String> extractFunctionNameAndParameters(String functionFullName) throws Exception {
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
		int maxTimes = 0;
		while(parametersStr.contains("<") && parametersStr.contains(">")) {
			parametersStr = parametersStr.replaceAll("<[^<>]*>", "");
			if(++maxTimes > 20) {
				break;
			}
		}
		String[] parameters = parametersStr.split(",");
		for(String parameter : parameters) {
			maxTimes = 0;
			while(parameter.contains("<") && !parameter.contains(">")) {
				parameter = parameter + ">";
				parameter = parameter.replaceAll("<[^<>]*>", "");
				if(++maxTimes > 20) {
					break;
				}
			}
			parameter.replace(">", "");
			parameter.replace("<", "");
			if(!StringUtils.isBlank(parameter)) {
				result.add(parameter.trim());
			}
		}
		return result;
	}
	
}
