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

import cn.edu.fudan.se.multidependency.model.node.NodeType;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.Commit;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Issue;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;

public class JavaDynamicUtil {
	
	public static Issue extractIssueFromMarkLine(String line) {
		if(!line.startsWith(NodeType.Issue.name())) {
			return null;
		}
		try {
			String[] strs = line.split(" ");
			Issue issue = new Issue();
			issue.setContent(strs[1]);
			return issue;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Commit extractCommitFromMarkLine(String line) {
		if(!line.startsWith(NodeType.Commit.name())) {
			return null;
		}
		try {
			String[] strs = line.split(" ");
			Commit commit = new Commit();
			commit.setCommitId(strs[1]);
//			String timeStr = strs[2] + " " + strs[3];
			commit.setMessage(strs[4]);
			return commit;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ProjectFile extractProjectFileFromMarkLine(String line) {
		if(!line.startsWith("File")) {
			return null;
		}
		try {
			String[] strs = line.split(" ");
			ProjectFile file = new ProjectFile();
			file.setPath(strs[1]);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static TestCase extractTestCaseFromMarkLine(String line) {
		if(!line.startsWith(NodeType.TestCase.name())) {
			return null;
		}
		try {
			String[] strs = line.split(" ");
			TestCase testCase = new TestCase();
			if(strs.length > 1) {
				testCase.setSuccess("success".equals(strs[1]));
				if(strs.length > 2) {
					testCase.setInputContent(strs[2]);
				}
			}
			return testCase;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Scenario extractScenarioFromMarkLine(String line) {
		if(!line.startsWith(NodeType.Scenario.name())) {
			return null;
		}
		try {
			String[] strs = line.split(" ");
			Scenario scenario = new Scenario();
			scenario.setScenarioName(strs[1]);
			return scenario;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Feature extractFeatureFromMarkLine(String line) {
		if(!line.startsWith(NodeType.Feature.name())) {
			return null;
		}
		try {
			String[] strs = line.split(" ");
			Feature feature = new Feature();
			feature.setFeatureName(strs[1]);
			return feature;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static DynamicFunctionExecutionFromKieker findCallerFunction(DynamicFunctionExecutionFromKieker called, List<DynamicFunctionExecutionFromKieker> sortedFunctions) {
		int midIndex = sortedFunctions.size() / 2;
		if(called.getBreadth() <= sortedFunctions.get(0).getBreadth()) {
			return null;
		}
		if(called.getBreadth() > sortedFunctions.get(sortedFunctions.size() - 1).getBreadth()) {
			return sortedFunctions.get(sortedFunctions.size() - 1);
		}
		int lessIndex = 0;
		int moreIndex = sortedFunctions.size() - 1;
		while(midIndex > lessIndex && midIndex < moreIndex) {
			if(sortedFunctions.get(midIndex).getBreadth() < called.getBreadth()) {
				lessIndex = midIndex;
			} else if(sortedFunctions.get(midIndex).getBreadth() > called.getBreadth()) {
				moreIndex = midIndex;
			}
			midIndex = (moreIndex + lessIndex) / 2;
		}
		return null;
	}
	
	public static int find(int num, List<Integer> list) {
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

	
	/**
	 * callId
	 * depth
	 * DynamicFunctionFromKieker
	 * @param file
	 * @return
	 */
	public static Map<String, Map<Integer, List<DynamicFunctionExecutionFromKieker>>> readKiekerExecutionFile(File... files) {
		Map<String, Map<Integer, List<DynamicFunctionExecutionFromKieker>>> result = new HashMap<>();
		for(File file : files) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					DynamicFunctionExecutionFromKieker dynamicFunction = splitFunctionExecution(line);
					if (dynamicFunction == null) {
						continue;
					}
					String callId = dynamicFunction.getCallId();
					Map<Integer, List<DynamicFunctionExecutionFromKieker>> groups = result.get(callId);
					groups = groups == null ? new HashMap<>() : groups;
					Integer depth = dynamicFunction.getDepth();
					List<DynamicFunctionExecutionFromKieker> functions = groups.get(depth);
					functions = functions == null ? new ArrayList<>() : functions;
					functions.add(dynamicFunction);
					groups.put(depth, functions);
					result.put(callId, groups);
				}
				for(Map<Integer, List<DynamicFunctionExecutionFromKieker>> groups : result.values()) {
					for(List<DynamicFunctionExecutionFromKieker> functions : groups.values()) {
						functions.sort(new Comparator<DynamicFunctionExecutionFromKieker>() {
							@Override
							public int compare(DynamicFunctionExecutionFromKieker o1, DynamicFunctionExecutionFromKieker o2) {
								return o1.getBreadth() - o2.getBreadth();
							}
						});
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

	public static Map<String, List<DynamicFunctionCallFromKieker>> readKiekerCallFile(File... files) {
		Map<String, List<DynamicFunctionCallFromKieker>> result = new HashMap<>();
		for(File file : files) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					DynamicFunctionCallFromKieker call = splitFunctionCall(line);
					if(call == null) {
						continue;
					}
					String callId = call.getCallId();
					List<DynamicFunctionCallFromKieker> groups = result.get(callId);
					groups = groups == null ? new ArrayList<>() : groups;
					groups.add(call);
				}
				for(List<DynamicFunctionCallFromKieker> groups : result.values()) {
					groups.sort(new Comparator<DynamicFunctionCallFromKieker>() {
						@Override
						public int compare(DynamicFunctionCallFromKieker o1, DynamicFunctionCallFromKieker o2) {
							return o1.getOrderId().compareTo(o2.getOrderId());
						}
					});
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static DynamicFunctionCallFromKieker splitFunctionCall(String callSentence) {
		try {
			String[] splits = callSentence.split(";");
			System.out.println(splits.length);
			if(splits.length != 9) {
				return null;
			}
			if(callSentence.contains(DynamicFunctionCallFromKieker.CLINIT)) {
				return null;
			}
			DynamicFunctionCallFromKieker functionCall = new DynamicFunctionCallFromKieker();
			String callerMethodStr = splits[5];
			String callerParametersStr = callerMethodStr.substring(
					callerMethodStr.lastIndexOf("(") + 1, callerMethodStr.length() - 1);
			if (!StringUtils.isBlank(callerParametersStr)) {
				String[] parameters = callerParametersStr.split(",");
				for (String parameter : parameters) {
					functionCall.addCallerParameter(parameter.trim());
				}
			}
			callerMethodStr = callerMethodStr.substring(0, callerMethodStr.lastIndexOf("("));
			String[] callerMethodsStr = callerMethodStr.split(" ");
			String callerFunctionName = callerMethodsStr[callerMethodsStr.length - 1];
			if (callerFunctionName.contains(DynamicFunctionCallFromKieker.INIT)) {
				callerFunctionName = callerFunctionName.replace("." + DynamicFunctionCallFromKieker.INIT, "");
				String[] temp = callerFunctionName.split("\\.");
				callerFunctionName = callerFunctionName + "." + temp[temp.length - 1];
				functionCall.setCallerFunctionName(callerFunctionName);
				functionCall.setCallerIsConstructor(true);
			} else {
				functionCall.setCallerFunctionName(callerFunctionName);
				functionCall.setCallerIsConstructor(false);
			}
			String calledMethodStr = splits[7];
			String calledParametersStr = calledMethodStr.substring(
					calledMethodStr.lastIndexOf("(") + 1, calledMethodStr.length() - 1);
			if (!StringUtils.isBlank(calledParametersStr)) {
				String[] parameters = calledParametersStr.split(",");
				for (String parameter : parameters) {
					functionCall.addCalledParameter(parameter.trim());
				}
			}
			calledMethodStr = calledMethodStr.substring(0, calledMethodStr.lastIndexOf("("));
			String[] calledMethodsStr = calledMethodStr.split(" ");
			String calledFunctionName = calledMethodsStr[calledMethodsStr.length - 1];
			if (calledFunctionName.contains(DynamicFunctionCallFromKieker.INIT)) {
				calledFunctionName = calledFunctionName.replace("." + DynamicFunctionCallFromKieker.INIT, "");
				String[] temp = calledFunctionName.split("\\.");
				calledFunctionName = calledFunctionName + "." + temp[temp.length - 1];
				functionCall.setCalledFunctionName(calledFunctionName);
				functionCall.setCalledIsConstructor(true);
			} else {
				functionCall.setCalledFunctionName(calledFunctionName);
				functionCall.setCalledIsConstructor(false);
			}
			functionCall.setCallId(splits[3]);
			functionCall.setOrderId(Integer.valueOf(splits[4]));
			return functionCall;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static DynamicFunctionExecutionFromKieker splitFunctionExecution(String callSentence) {
		try {
			String[] splits = callSentence.split(";");
			if (splits.length < 10) {
				return null;
			}
			DynamicFunctionExecutionFromKieker dynamicFunction = new DynamicFunctionExecutionFromKieker();
			dynamicFunction.setSentence(callSentence);
			dynamicFunction.setCallId(splits[4]);
			dynamicFunction.setDepth(Integer.valueOf(splits[9]));
			dynamicFunction.setBreadth(Integer.valueOf(splits[8]));
			String methodStr = splits[2];
			String parametersStr = methodStr.substring(methodStr.lastIndexOf("(") + 1, methodStr.length() - 1);
			if (!StringUtils.isBlank(parametersStr)) {
				String[] parameters = parametersStr.split(",");
				for (String parameter : parameters) {
					dynamicFunction.addParametersType(parameter.trim());
				}
			}
			methodStr = methodStr.substring(0, methodStr.lastIndexOf("("));
			String[] methodsStr = methodStr.split(" ");
			String functionName = methodsStr[methodsStr.length - 1];
			if (functionName.contains(DynamicFunctionExecutionFromKieker.INIT)) {
				dynamicFunction.setReturnType(DynamicFunctionExecutionFromKieker.INIT);
				functionName = functionName.replace("." + DynamicFunctionExecutionFromKieker.INIT, "");
				String[] temp = functionName.split("\\.");
				functionName = functionName + "." + temp[temp.length - 1];
				dynamicFunction.setFunctionName(functionName);
				dynamicFunction.setConstructor(true);
			} else {
				dynamicFunction.setReturnType(methodsStr[methodsStr.length - 2]);
				dynamicFunction.setFunctionName(functionName);
				dynamicFunction.setConstructor(false);
			}
			return dynamicFunction;
		} catch (Exception e) {
//			e.printStackTrace();
			return null;
		}
	}
	
	public static class DynamicFunctionCallFromKieker {
		public static final String INIT = DynamicFunctionExecutionFromKieker.INIT;
		public static final String CLINIT = "<clinit>";
		String sentence;
		String callId;
		Integer orderId;
		String callerFunctionName;
		List<String> callerParametersType = new ArrayList<>();
		boolean callerIsConstructor;
		String calledFunctionName;
		List<String> calledParametersType = new ArrayList<>();
		boolean calledIsConstructor;
		public void addCallerParameter(String str) {
			this.callerParametersType.add(str);
		}
		public void addCalledParameter(String str) {
			this.calledParametersType.add(str);
		}
		public String getSentence() {
			return sentence;
		}
		public void setSentence(String sentence) {
			this.sentence = sentence;
		}
		public String getCallId() {
			return callId;
		}
		public void setCallId(String callId) {
			this.callId = callId;
		}
		public Integer getOrderId() {
			return orderId;
		}
		public void setOrderId(Integer orderId) {
			this.orderId = orderId;
		}
		public String getCallerFunctionName() {
			return callerFunctionName;
		}
		public void setCallerFunctionName(String callerFunctionName) {
			this.callerFunctionName = callerFunctionName;
		}
		public List<String> getCallerParametersType() {
			return callerParametersType;
		}
		public void setCallerParametersType(List<String> callerParametersType) {
			this.callerParametersType = callerParametersType;
		}
		public boolean isCallerIsConstructor() {
			return callerIsConstructor;
		}
		public void setCallerIsConstructor(boolean callerIsConstructor) {
			this.callerIsConstructor = callerIsConstructor;
		}
		public String getCalledFunctionName() {
			return calledFunctionName;
		}
		public void setCalledFunctionName(String calledFunctionName) {
			this.calledFunctionName = calledFunctionName;
		}
		public List<String> getCalledParametersType() {
			return calledParametersType;
		}
		public void setCalledParametersType(List<String> calledParametersType) {
			this.calledParametersType = calledParametersType;
		}
		public boolean isCalledIsConstructor() {
			return calledIsConstructor;
		}
		public void setCalledIsConstructor(boolean calledIsConstructor) {
			this.calledIsConstructor = calledIsConstructor;
		}
		
	}

	/**
	 * 从kieker文件执行每一行读到的方法，包括该方法在调用链的顺序和层级
	 * @author fan
	 *
	 */
	public static class DynamicFunctionExecutionFromKieker { //读取到的方法，所具有的属性
		String sentence;
		String callId;
		Integer depth;
		Integer breadth;
		String returnType;
		String functionName;
		boolean isConstructor;
		List<String> parametersType = new ArrayList<>();
		public static final String INIT = "<init>";

		public String getSentence() {
			return sentence;
		}

		public void setSentence(String sentence) {
			this.sentence = sentence;
		}

		public String getCallId() {
			return callId;
		}

		public void setCallId(String callId) {
			this.callId = callId;
		}

		public Integer getDepth() {
			return depth;
		}

		public void setDepth(Integer depth) {
			this.depth = depth;
		}

		public Integer getBreadth() {
			return breadth;
		}

		public void setBreadth(Integer breadth) {
			this.breadth = breadth;
		}

		public String getReturnType() {
			return returnType;
		}

		public void setReturnType(String returnType) {
			this.returnType = returnType;
		}

		public String getFunctionName() {
			return functionName;
		}

		public void setFunctionName(String functionName) {
			this.functionName = functionName;
		}

		public List<String> getParametersType() {
			return parametersType;
		}

		public void setParametersType(List<String> parametersType) {
			this.parametersType = parametersType;
		}

		public void addParametersType(String parameterType) {
			this.parametersType.add(parameterType);
		}

		public boolean isConstructor() {
			return isConstructor;
		}

		public void setConstructor(boolean isConstructor) {
			this.isConstructor = isConstructor;
		}

		@Override
		public String toString() {
			return "DynamicFunctionFromKieker [sentence=" + sentence + ", callId=" + callId + ", depth=" + depth
					+ ", breadth=" + breadth + ", returnType=" + returnType + ", functionName=" + functionName
					+ ", isConstructor=" + isConstructor + ", parametersType=" + parametersType + "]";
		}

	}
	
	/**
	 * 对应dynamicFunction与Function
	 * @param dynamicFunction
	 * @param functions
	 * @return
	 */
	public static Function findFunctionWithDynamic(DynamicFunctionExecutionFromKieker dynamicFunction, List<Function> functions) {
		for(Function function : functions) {
			if(!dynamicFunction.getFunctionName().equals(function.getFunctionName())) {
				return null;
			}
			if(function.getParameters().size() != dynamicFunction.getParametersType().size()) {
				continue;
			}
			boolean flag = false;
			for(int i = 0; i < function.getParameters().size(); i++) {
				if(dynamicFunction.getParametersType().get(i).indexOf(function.getParameters().get(i)) < 0) {
					flag = true;
				}
			}
			if(flag) {
				continue;
			}
			return function;
		}
		return null;
	}

}
