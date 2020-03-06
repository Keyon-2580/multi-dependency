package cn.edu.fudan.se.multidependency.service.microservice.jaeger;

import java.io.File;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.utils.JSONUtil;

@Deprecated
public class JaegerTraceInserterFromJSONFile extends JaegerTraceInserter {
	
	private String jsonFilePath;
	
	public JaegerTraceInserterFromJSONFile(String jsonFilePath) {
		super();
		this.jsonFilePath = jsonFilePath;
	}

	@Override
	protected JSONObject extractTraceJSON() throws Exception {
		return JSONUtil.extractJson(new File(jsonFilePath));
	}

}
