package cn.edu.fudan.se.multidependency.utils;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode()
public class JavaDynamicFunctionExecution {
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
}
