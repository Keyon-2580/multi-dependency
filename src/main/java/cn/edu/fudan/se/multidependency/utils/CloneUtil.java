package cn.edu.fudan.se.multidependency.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class CloneUtil {
	
	@Data
	public static class FilePathFromCsv {
		private String line;
		private int lineId;
		private String filePath;
		private int startLine;
		private int endLine;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class MethodNameForJavaFromCsv extends FilePathFromCsv {
		private String projectName;
		private String packageName;
		private String className;
		private String functionSimpleName;
		private List<String> parameterTypes = new ArrayList<>();
		public void addParameterType(String type) {
			this.parameterTypes.add(type);
		}
		private String getFunctionName() {
			StringBuilder builder = new StringBuilder();
			if(!StringUtils.isBlank(packageName)) {
				builder.append(packageName);
				builder.append(".");
			}
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
	
	public static Map<Integer, FilePathFromCsv> readJavaCloneCsvForFilePath(String filePath) throws Exception {
		Map<Integer, FilePathFromCsv> result = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				if(values.length != 4) {
					LOGGER.warn("克隆数据格式不正确：" + line);
					continue;
				}
				FilePathFromCsv file = new FilePathFromCsv();
				file.setLine(line);
				file.setLineId(Integer.parseInt(values[0]));
				file.setFilePath(values[1]);
				file.setStartLine(Integer.parseInt(values[2]));
				file.setEndLine(Integer.parseInt(values[3]));
				result.put(file.getLineId(), file);
			}
		}
		return result;
	}
	public static void main(String[] args) {
		String line = "18,ts-admin-route-service,D:\\projectPath1\\nostub\\removeDocument\\train-ticket\\ts-admin-route-service\\src\\main\\java\\adminroute\\config\\SecurityConfig.java,63,80,adminroute.config,SecurityConfig,configure,HttpSecurity#Map<String, String>#Map<String, Map<String,String>>#String#";
		String[] values = line.split(",");
		if(values.length < 9) {
			LOGGER.warn("克隆数据格式不正确：" + line);
			return;
		}
		MethodNameForJavaFromCsv method = new MethodNameForJavaFromCsv();
		int length = 0;
		method.setLine(line);
		method.setLineId(Integer.parseInt(values[0]));
		method.setProjectName(values[1]);
		method.setFilePath(values[2]);
		method.setStartLine(Integer.parseInt(values[3]));
		method.setEndLine(Integer.parseInt(values[4]));
		method.setPackageName(values[5]);
		method.setClassName(values[6]);
		method.setFunctionSimpleName(values[7]);
		for(int i = 0; i < 8; i++) {
			length += values[i].length() + 1;
		}
		String parametersStr = line.substring(length);
		String[] parameters = parametersStr.split("#");
		for(String parameter : parameters) {
			if(StringUtils.isBlank(parameter) || "None".equals(parameter)) {
				continue;
			}
			method.addParameterType(parameter);
		}
		System.out.println(method);;
	}
	public static Map<Integer, MethodNameForJavaFromCsv> readJavaCloneCsvForMethodName(String filePath) throws Exception {
		Map<Integer, MethodNameForJavaFromCsv> result = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				if(values.length < 9) {
					LOGGER.warn("克隆数据格式不正确：" + line);
					continue;
				}
				MethodNameForJavaFromCsv method = new MethodNameForJavaFromCsv();
				int length = 0;
				method.setLine(line);
				method.setLineId(Integer.parseInt(values[0]));
				method.setProjectName(values[1]);
				method.setFilePath(values[2]);
				method.setStartLine(Integer.parseInt(values[3]));
				method.setEndLine(Integer.parseInt(values[4]));
				method.setPackageName(values[5]);
				method.setClassName(values[6]);
				method.setFunctionSimpleName(values[7]);
				for(int i = 0; i < 8; i++) {
					length += values[i].length() + 1;
				}
				String parametersStr = line.substring(length);
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
	
	public static Collection<CloneResultFromCsv> readCloneResultCsv(String filePath) throws Exception {
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
	
	public static Collection<Collection<? extends Node>> groupCloneNodes(Iterable<? extends CloneRelation> relations) {
		List<Collection<? extends Node>> result = new ArrayList<>();
		Map<Node, Collection<Node>> nodeToCollection = new HashMap<>();
		for(CloneRelation relation : relations) {
			Node node1 = relation.getStartNode();
			Node node2 = relation.getEndNode();
			Collection<Node> collections1 = nodeToCollection.get(node1);
			Collection<Node> collections2 = nodeToCollection.get(node2);
			if(collections1 == null && collections2 == null) {
				collections1 = new ArrayList<>();
				collections1.add(node1);
				collections1.add(node2);
				result.add(collections1);
				nodeToCollection.put(node1, collections1);
				nodeToCollection.put(node2, collections1);
			} else if(collections1 != null && collections2 == null) {
				collections1.add(node2);
				nodeToCollection.put(node2, collections1);
			} else if(collections1 == null && collections2 != null) {
				collections2.add(node1);
				nodeToCollection.put(node1, collections2);
			} else {
				if(collections1 != collections2) {
					collections1.addAll(collections2);
					result.remove(collections2);
					for(Node node : collections2) {
						nodeToCollection.put(node, collections1);
					}
				}
			}
		}
		result.sort((collection1, collection2) -> {
			return collection2.size() - collection1.size();
		});
		return result;
	}
}
