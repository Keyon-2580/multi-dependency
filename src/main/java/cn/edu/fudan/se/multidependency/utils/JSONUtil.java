package cn.edu.fudan.se.multidependency.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JSONUtil {
	
	public static JSONObject extractJSONObject(File file) throws Exception {
		return JSONObject.parseObject(extractJsonString(file));
	}
	
	public static JSONArray extractJSONArray(File file) throws Exception {
		return JSONObject.parseArray(extractJsonString(file));
	}
	
	public static String extractJsonString(File file) throws Exception {
		StringBuilder builder = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} 
		return builder.toString();
	}
}
