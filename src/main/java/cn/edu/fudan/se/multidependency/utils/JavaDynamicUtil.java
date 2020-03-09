package cn.edu.fudan.se.multidependency.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.exception.DynamicLogSentenceErrorException;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class JavaDynamicUtil {
	
	public static Map<String, List<JavaDynamicFunctionExecution>> readDynamicLogs(File... files) {
		Map<String, List<JavaDynamicFunctionExecution>> result = new HashMap<>();
		for(File file : files) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					JavaDynamicFunctionExecution dynamicFunction = null;
					try {
						dynamicFunction = extractJavaDynamicFunctionExecution(line);
					} catch (DynamicLogSentenceErrorException e) {
						e.printStackTrace();
						continue;
					}
					String project = dynamicFunction.getProject();
					List<JavaDynamicFunctionExecution> executions = result.get(project);
					executions = executions == null ? new ArrayList<>() : executions;
					executions.add(dynamicFunction);
					result.put(project, executions);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static JavaDynamicFunctionExecution extractJavaDynamicFunctionExecution(String sentence) throws DynamicLogSentenceErrorException {
		try {
//			LOGGER.info(sentence);
			JavaDynamicFunctionExecution functionExecution = new JavaDynamicFunctionExecution();
			functionExecution.setSentence(sentence);
			JSONObject json = JSONObject.parseObject(sentence);
			functionExecution.setLanguage(json.getString("language"));
			functionExecution.setTime(json.getString("time"));
			functionExecution.setProject(json.getString("project"));
			String file = json.getString("inFile");
			file = file == null ? json.getString("file") : file;
			functionExecution.setInFile(file);
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
			functionExecution.setParentSpanId(json.getString("parentSpanId"));
			functionExecution.setThreadId(json.getLong("currentThreadId"));
			functionExecution.setThreadName(json.getString("currentThreadName"));
			functionExecution.setCallMethod(json.getString("callMethod"));
			return functionExecution;
		} catch (Exception e) {
			throw new DynamicLogSentenceErrorException(sentence);
		}
	}
	
	/**
	 * 从一组数据中找出最大的小于num的数，并返回下标
	 * list为排序数组，否则后果自负
	 * num不在list中，否则返回-1
	 * @param num
	 * @param list
	 * @return
	 */
	public static int find(Long num, List<Long> list) {
		if(list == null || list.size() == 0 || list.contains(num) || num < list.get(0)) {
			return -1;
		}
		if(num > list.get(list.size() - 1)) {
			return list.size() - 1;
		}
		int lessIndex = 0;
		int moreIndex = list.size() - 1;
		while(lessIndex < moreIndex) {
			int midIndex = (moreIndex + lessIndex) / 2;
			if(list.get(midIndex) > num) {
				moreIndex = midIndex;
			} else {
				lessIndex = midIndex;
			}
			if(moreIndex - lessIndex == 1) {
				break;
			}
		}
		return lessIndex;
	}
	
	@Data
	@EqualsAndHashCode
	public static class JavaDynamicFunctionExecution {
		protected String language;
		protected String time;
		protected String project;
		protected String inFile;
		protected Long order;
		protected Long depth;
		protected String functionName;
		protected List<String> parameters = new ArrayList<>();
		public void addParameter(String parameter) {
			this.parameters.add(parameter);
		}
		protected JSONObject remarks;
		protected String sentence;
		protected String traceId;
		protected String spanId;
		protected String parentSpanId;
		protected Long threadId;
		protected String threadName;
		protected String callMethod;
		
		public static final String TRACE_START_PARENT_SPAN_ID = "-1";
	}
}
