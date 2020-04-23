package cn.edu.fudan.se.multidependency.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

public class CloneUtil {

	@Data
	public static class MethodNameForJava {
		private Integer lineId;
		private String projectName;
		private String filePath;
		private String startLine;
		private String endLine;
		private String packageName;
		private String className;
		private String functionName;
		private List<String> parameterTypes = new ArrayList<>();
		public void addParameterType(String type) {
			this.parameterTypes.add(type);
		}
	}
	
	public static Map<Integer, MethodNameForJava> readJavaCloneCsvForMethodName(String filePath) throws Exception {
		Map<Integer, MethodNameForJava> result = new HashMap<>();
		
		return result;
	}
	
	
	public static Map<Integer, Map<Integer, Double>> readCloneResultCsv(String filePath) throws Exception {
		Map<Integer, Map<Integer, Double>> result = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				if(values.length < 3) {
					continue;
				}
				
			}
		}
		return result;
	}
}
