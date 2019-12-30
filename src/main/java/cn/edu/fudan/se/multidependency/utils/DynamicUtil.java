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

import edu.emory.mathcs.backport.java.util.Arrays;

public class DynamicUtil {
	
	public static DynamicFunctionFromKieker findCallerFunction(DynamicFunctionFromKieker called, List<DynamicFunctionFromKieker> sortedFunctions) {
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

	public static void main(String[] args) {
//		Map m = readKiekerFile(new File("D:\\fan\\analysis\\depends-commits\\kieker\\kieker-before-change-PackageEntity-26501da0b6cc85630c535fa5b756b0d8e60975b6\\kieker-20191127-071809170-UTC-001.dat"));
//		System.out.println(m.size());
		System.out.println(split("$1;1577693392065836654;public depends.relations.Inferer.<init>(depends.entity.repo.EntityRepo, depends.relations.ImportLookupStrategy, depends.entity.repo.BuiltInType, boolean);<no-session-id>;3771483212946079748;1577693392065199674;1577693392065832878;DESKTOP-2SU7U6M;47;2"));
//		System.out.println(find(11, Arrays.asList(new Integer[]{1,2,4,5,10,12,14,15})));;
	}
	
	public static Map<String, Map<Integer, List<DynamicFunctionFromKieker>>> readKiekerFile(File file) {
		Map<String, Map<Integer, List<DynamicFunctionFromKieker>>> result = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				DynamicFunctionFromKieker dynamicFunction = split(line);
				if (dynamicFunction == null) {
					continue;
				}
				String callId = dynamicFunction.getCallId();
				Map<Integer, List<DynamicFunctionFromKieker>> groups = result.get(callId);
				groups = groups == null ? new HashMap<>() : groups;
				Integer depth = dynamicFunction.getDepth();
				List<DynamicFunctionFromKieker> functions = groups.get(depth);
				functions = functions == null ? new ArrayList<>() : functions;
				functions.add(dynamicFunction);
				groups.put(depth, functions);
				result.put(callId, groups);
			}
			for(Map<Integer, List<DynamicFunctionFromKieker>> groups : result.values()) {
				for(List<DynamicFunctionFromKieker> functions : groups.values()) {
					functions.sort(new Comparator<DynamicFunctionFromKieker>() {
						@Override
						public int compare(DynamicFunctionFromKieker o1, DynamicFunctionFromKieker o2) {
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
		return result;
	}

	public static DynamicFunctionFromKieker split(String callSentence) {

		try {
			String[] splits = callSentence.split(";");
			if (splits.length < 10) {
				return null;
			}
			DynamicFunctionFromKieker dynamicFunction = new DynamicFunctionFromKieker();
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
			if (functionName.contains(DynamicFunctionFromKieker.INIT)) {
				dynamicFunction.setReturnType(DynamicFunctionFromKieker.INIT);
				functionName = functionName.replace("." + DynamicFunctionFromKieker.INIT, "");
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

	public static class DynamicFunctionFromKieker {
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
}
