package cn.edu.fudan.se.multidependency.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CppDynamicUtil {
	
	public static final String REGEX_DOT = "([^-]+)->([^\\[]+).*";
	public static final Pattern PATTERN_DOT = Pattern.compile(REGEX_DOT);
	
	public static Map<String, List<String>> extractFunctionCall(File dotFile) {
		Map<String, List<String>> functionCallFunctions = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(dotFile))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				if(!line.contains("->")) {
					continue;
				}
				Matcher matcher = PATTERN_DOT.matcher(line);
				if(matcher.find()) {
					String start = matcher.group(1).trim().replace("\"", "");
					String end = matcher.group(2).trim().replace("\"", "");
					List<String> calls = functionCallFunctions.get(start);
					calls = calls == null ? new ArrayList<>() : calls;
					calls.add(end);
					functionCallFunctions.put(start, calls);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return functionCallFunctions;
	}
	
	public static void main(String[] args) {
//		test(new File("D:\\multiple-dependency-project\\callgrind.out.bash"));
		extractFunctionCall(new File("D:\\multiple-dependency-project\\bash-w.dot"));
	}
	
	public static List<String> extractFile(File callgrindFile, String projectPath) {
		List<String> result = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(callgrindFile))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				if(line.startsWith(CallGrindIdentify.fi.name()) || line.startsWith(CallGrindIdentify.cfi.name()) || line.startsWith(CallGrindIdentify.fl.name())) {
					String[] splits = line.split(" ");
					if(splits.length < 2) {
						continue;
					}
					String filePath = splits[1];
					if(filePath.lastIndexOf(projectPath + "/") >= 0) {
						result.add(filePath);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void test(File callgrindFile) {
		Map<Integer, CallGrind> idToCallGrind = new HashMap<>();
		Map<Integer, List<CallGrind>> fileHasFn = new HashMap<>();
		Map<Integer, List<CallGrind>> fnCallFn = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(callgrindFile))) {
			String line = null;
			Integer currentFnId = null;
			Integer lastFileId = null;
			while((line = reader.readLine()) != null) {
				if(line.startsWith(CallGrindIdentify.fi.name())) {
					String[] splits = line.split(" ");
					Integer id = Integer.valueOf(splits[0].substring("fi=(".length(), splits[0].length() - 1));
					CallGrind currentFn = idToCallGrind.get(currentFnId);
					if(currentFn == null) {
						currentFn = new CallGrind();
						currentFn.setId(currentFnId);
						currentFn.setIdentify(CallGrindIdentify.fn);
						idToCallGrind.put(currentFnId, currentFn);
					}
					if(splits.length > 1) {
						currentFn.setName(splits[1]);
					}
				} else if(line.startsWith(CallGrindIdentify.cfi.name())) {
					
				} else if(line.startsWith(CallGrindIdentify.fl.name())) {
					
				} else if(line.startsWith(CallGrindIdentify.fn.name())) {
					String[] splits = line.split(" ");
					currentFnId = Integer.valueOf(splits[0].substring("fn=(".length(), splits[0].length() - 1));
					CallGrind currentFn = idToCallGrind.get(currentFnId);
					if(currentFn == null) {
						currentFn = new CallGrind();
						currentFn.setId(currentFnId);
						currentFn.setIdentify(CallGrindIdentify.fn);
						idToCallGrind.put(currentFnId, currentFn);
					}
					if(splits.length > 1) {
						currentFn.setName(splits[1]);
					}
					if(lastFileId != null) {
						List<CallGrind> has = fileHasFn.get(lastFileId);
						has = has == null ? new ArrayList<>() : has;
						has.add(currentFn);
						fileHasFn.put(lastFileId, has);
					}
					lastFileId = null;
				} else if(line.startsWith(CallGrindIdentify.cfn.name())) {
					String[] splits = line.split(" ");
					Integer cfnId = Integer.valueOf(splits[0].substring("cfn=(".length(), splits[0].length() - 1));
					CallGrind cfn = idToCallGrind.get(cfnId);
					if(cfn == null) {
						cfn = new CallGrind();
						cfn.setId(currentFnId);
						cfn.setIdentify(CallGrindIdentify.fn);
						idToCallGrind.put(cfnId, cfn);
					}
					if(splits.length > 1) {
						cfn.setName(splits[1]);
					}
					List<CallGrind> callGrinds = fnCallFn.get(currentFnId);
					callGrinds = callGrinds == null ? new ArrayList<>() : callGrinds;
					callGrinds.add(cfn);
					fnCallFn.put(currentFnId, callGrinds);
					if(lastFileId != null) {
						List<CallGrind> has = fileHasFn.get(lastFileId);
						has = has == null ? new ArrayList<>() : has;
						has.add(cfn);
						fileHasFn.put(lastFileId, has);
					}
					lastFileId = null;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static enum CallGrindIdentify {
		fn, fi, cfn, cfi, fl;
	}
	
	public static class CallGrind implements Serializable {
		private static final long serialVersionUID = -1880192207383355081L;
		Integer id;
		String name;
		CallGrindIdentify identify;
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public CallGrindIdentify getIdentify() {
			return identify;
		}
		public void setIdentify(CallGrindIdentify identify) {
			this.identify = identify;
		}
		
	}
	
}
