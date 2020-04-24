package cn.edu.fudan.se.multidependency.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Data;

public class CloneUtil {

	@Data
	public static class MethodNameForJavaFromCsv {
		private String line;
		private Integer lineId;
		private String projectName;
		private String filePath;
		private int startLine;
		private int endLine;
		private String packageName;
		private String className;
		private String functionSimpleName;
		private List<String> parameterTypes = new ArrayList<>();
		public void addParameterType(String type) {
			this.parameterTypes.add(type);
		}
		private String getFunctionName() {
			StringBuilder builder = new StringBuilder();
			builder.append((StringUtils.isBlank(packageName) ? "default" : packageName));
			builder.append(".");
			builder.append(className);
			builder.append(".");
			builder.append(functionSimpleName);
			return builder.toString();
		}
		public int countOfParameterTypes() {
			return parameterTypes.size();
		}
		public String getFunctionFullName() {
			StringBuilder builder = new StringBuilder();
			builder.append(getFunctionName());
			builder.append("(");
			for(int i = 0; i < this.parameterTypes.size(); i++) {
				if(i != 0) {
					builder.append(",");
				}
				builder.append(parameterTypes.get(i));
			}
			builder.append(")");
			return builder.toString();
		}
	}
	
	public static Map<Integer, MethodNameForJavaFromCsv> readJavaCloneCsvForMethodName(String filePath) throws Exception {
		Map<Integer, MethodNameForJavaFromCsv> result = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				if(values.length != 9) {
					LOGGER.warn("克隆数据格式不正确：" + line);
					continue;
				}
				MethodNameForJavaFromCsv method = new MethodNameForJavaFromCsv();
				method.setLine(line);
				method.setLineId(Integer.parseInt(values[0]));
				method.setProjectName(values[1]);
				method.setFilePath(values[2]);
				method.setStartLine(Integer.parseInt(values[3]));
				method.setEndLine(Integer.parseInt(values[4]));
				method.setPackageName(values[5]);
				method.setClassName(values[6]);
				method.setFunctionSimpleName(values[7]);
				String parametersStr = values[8];
				String[] parameters = parametersStr.split("#");
				for(String parameter : parameters) {
					if(StringUtils.isBlank(parameter) || "None".equals(parameter)) {
						continue;
					}
					method.addParameterType(parameter);
				}
				result.put(method.getLineId(), method);
			}
		}
		return result;
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneUtil.class);
	
	
	@Data
	@AllArgsConstructor
	public static class CloneResultFromCsv {
		private int start;
		private int end;
		private double value;
	}
	
	static int count = 0;
	public static void main(String[] args) throws Exception {
		Iterable<CloneResultFromCsv> result = readCloneResultCsv("src/main/resources/clone/type123_method_result.csv");
		Map<Integer, MethodNameForJavaFromCsv> names = readJavaCloneCsvForMethodName("src/main/resources/clone/MethodNameTable.csv");
		result.forEach(clone -> {
			count++;
			System.out.println(clone);
			System.out.println(names.get(clone.getStart()));
			System.out.println(names.get(clone.end));
		});
		System.out.println(count);
	}
	
	public static Iterable<CloneResultFromCsv> readCloneResultCsv(String filePath) throws Exception {
		List<CloneResultFromCsv> result = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				if(values.length != 3) {
					LOGGER.warn("克隆数据格式不正确：" + line);
					continue;
				}
				int start = Integer.parseInt(values[0]);
				int end = Integer.parseInt(values[1]);
				double value = Double.parseDouble(values[2]);
				result.add(new CloneResultFromCsv(start, end, value));
			}
		}
		return result;
	}
}
