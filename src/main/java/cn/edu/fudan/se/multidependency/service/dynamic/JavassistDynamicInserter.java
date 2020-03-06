package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;

public class JavassistDynamicInserter extends StubJavaForJaegerDynamicInserter {

	@Override
	protected void extractNodesAndRelations() throws Exception {
		
		extractMicroServiceCall();
		
		super.extractNodesAndRelations();
	}
	
	private Map<Trace, List<Span>> traceToSpans = new HashMap<>();
	
	private void addSpanToTrace(Trace trace, Span span) {
		List<Span> spans = traceToSpans.get(trace);
		spans = spans == null ? new ArrayList<>() : spans;
		if(!spans.contains(span)) {
			spans.add(span);
		}
		traceToSpans.put(trace, spans);
	}
	
	private Map<Span, String> spanToCallMethod = new HashMap<>();
	
	private Map<String, String> spanIdToParentSpanId = new HashMap<>();
	
	public static Long changeTimeStrToLong(String time) {
		SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		Long result = -1L;
		try {
			result = sim.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}		
		return result;
	}
	
	private void extractMicroServiceCall() throws Exception {
		for(File file : dynamicFunctionCallFiles) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					JSONObject json = JSONObject.parseObject(line);
					String traceId = json.getString("traceId");
					String spanId = json.getString("spanId");
					String parentSpanId = json.getString("parentSpanId");
					Integer order = json.getInteger("order");
					Integer depth = json.getInteger("depth");
					if(StringUtils.isBlank(traceId) || StringUtils.isBlank(spanId) || StringUtils.isBlank(parentSpanId) || order != 0 || depth != 0) {
						continue;
					}
					Trace trace = this.getNodes().findTraces().get(traceId);
					if(trace == null) {
						trace = new Trace();
						trace.setEntityId(generateEntityId());
						trace.setTraceId(traceId);
						addNode(trace, null);
					}
					Span span = this.getNodes().findSpanBySpanId(spanId);
					if(span == null) {
						span = new Span();
						span.setSpanId(spanId);
						span.setTraceId(traceId);
						span.setEntityId(generateEntityId());
						span.setOperationName(json.getString("function"));
						span.setApiFunctionName(json.getString("function"));
						String serviceName = json.getString("project");
						span.setServiceName(serviceName);
						span.setTime(changeTimeStrToLong(json.getString("time")));
						spanToCallMethod.put(span, json.getString("callMethod"));
						
						Project project = this.getNodes().findProjectByNameAndLanguage(serviceName, "java");
						MicroService microService = getNodes().findMicroServiceByName(serviceName);
						if(project == null || microService == null) {
							throw new Exception("error: span的serviceName不是一个项目 " + serviceName);
						}
						addNode(span, project);
						
						Contain contain = new Contain(trace, span);
						addRelation(contain);
						addSpanToTrace(trace, span);
						
						MicroServiceCreateSpan projectCreateSpan = new MicroServiceCreateSpan(microService, span);
						addRelation(projectCreateSpan);
					}
					spanIdToParentSpanId.put(spanId, parentSpanId);
				}
			}
		}
		for(String spanId : spanIdToParentSpanId.keySet()) {
			String parentSpanId = spanIdToParentSpanId.get(spanId);
			if("-1".equals(parentSpanId)) {
				continue;
			}
			Span parentSpan = this.getNodes().findSpanBySpanId(parentSpanId);
			if(parentSpan == null) {
				throw new Exception("SpanId为 " + parentSpanId + " 的span不存在");
			}
			Span span = this.getNodes().findSpanBySpanId(spanId);
			SpanCallSpan spanCallSpan = new SpanCallSpan(parentSpan, span);
			spanCallSpan.setHttpRequestMethod(spanToCallMethod.get(span));
			addRelation(spanCallSpan);
		}
		
		for(Trace trace : traceToSpans.keySet()) {
			List<Span> sortedSpans = traceToSpans.get(trace);
			sortedSpans.sort(new Comparator<Span>() {
				@Override
				public int compare(Span o1, Span o2) {
					return o1.getTime().compareTo(o2.getTime());
				}
			});
			for(int i = 0; i < sortedSpans.size(); i++) {
				Span span = sortedSpans.get(i);
				span.setOrder(i);
			}
		}
	}

}
