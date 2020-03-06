package cn.edu.fudan.se.multidependency.service.microservice.jaeger;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.microservice.jaeger.JaegerUtil;

@Deprecated
public class JaegerTraceInserterFromHttp extends JaegerTraceInserter {

	public JaegerTraceInserterFromHttp(String traceId) {
		super();
		this.traceId = traceId;
	}
	
	private String traceId;

	protected JSONObject extractTraceJSON() throws Exception {
		return JaegerUtil.readJSON(traceId);
	}
}
