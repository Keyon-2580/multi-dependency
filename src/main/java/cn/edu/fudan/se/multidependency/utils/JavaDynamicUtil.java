package cn.edu.fudan.se.multidependency.utils;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.exception.DynamicLogSentenceErrorException;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class JavaDynamicUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaDynamicUtil.class);
	
	/**
	 * traceId spanId depth 
	 * @param files
	 * @return
	 */
	private static void addDynamicFunctionExecution(
			Map<String, Map<String, Map<Long, List<JavaDynamicFunctionExecution>>>> result, 
			JavaDynamicFunctionExecution execution) {
		String traceId = execution.getTraceId();
		String spanId = execution.getSpanId();
		if(StringUtils.isBlank(traceId) || StringUtils.isBlank(spanId)) {
			return;
//			traceId = execution.getThreadId() + "_" + execution.getThreadName();
//			spanId = traceId;
		}
		Long depth = execution.getDepth();
		Map<String, Map<Long, List<JavaDynamicFunctionExecution>>> spanResult = result.get(traceId);
		spanResult = spanResult == null ? new HashMap<>() : spanResult;
		Map<Long, List<JavaDynamicFunctionExecution>> depthResult = spanResult.get(spanId);
		depthResult = depthResult == null ? new HashMap<>() : depthResult;
		List<JavaDynamicFunctionExecution> executions = depthResult.get(depth);
		executions = executions == null ? new ArrayList<>() : executions;
		executions.add(execution);
		depthResult.put(depth, executions);
		spanResult.put(spanId, depthResult);
		result.put(traceId, spanResult);
	}
	
	public static Map<String, Map<String, Map<Long, List<JavaDynamicFunctionExecution>>>> readJavaDynamicLogs(File... files) {
		Map<String, Map<String, Map<Long, List<JavaDynamicFunctionExecution>>>> result = new HashMap<>();
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
					addDynamicFunctionExecution(result, dynamicFunction);
				}
				for(Map<String, Map<Long, List<JavaDynamicFunctionExecution>>> spansResult : result.values()) {
					for(Map<Long, List<JavaDynamicFunctionExecution>> groups : spansResult.values()) {
						for(List<JavaDynamicFunctionExecution> functions : groups.values()) {
							functions.sort(new Comparator<JavaDynamicFunctionExecution>() {
								@Override
								public int compare(JavaDynamicFunctionExecution o1, JavaDynamicFunctionExecution o2) {
									return (int) (o1.getOrder() - o2.getOrder());
								}
							});
						}
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
			return functionExecution;
		} catch (Exception e) {
			throw new DynamicLogSentenceErrorException(sentence);
		}
	}
	
	/**
	 * 从一组数据中找出最大的小于num的数，并返回下标
	 * @param num
	 * @param list
	 * @return
	 */
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
	
	@Data
	@EqualsAndHashCode()
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
		
		public static final String TRACE_START_PARENT_SPAN_ID = "-1";
	}
}
