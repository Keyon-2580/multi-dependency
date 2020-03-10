package cn.edu.fudan.se.multidependency.model;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public abstract class DynamicFunctionExecution {
	protected String sentence;
	protected String time;
	protected String project;
	protected String inFile;
	protected String functionName;
	protected String traceId;
	protected String spanId;
	protected String parentSpanId;
	protected String callMethod;
	protected JSONObject remarks;
	
	public static final String TRACE_START_PARENT_SPAN_ID = "-1";
	
	public abstract Language getLanguage();
}
