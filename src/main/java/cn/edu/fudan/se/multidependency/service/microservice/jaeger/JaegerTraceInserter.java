package cn.edu.fudan.se.multidependency.service.microservice.jaeger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;

public abstract class JaegerTraceInserter extends ExtractorForNodesAndRelationsImpl {
	
	protected Trace currentTrace;
	
	private Map<String, Span> spans = new HashMap<>();
	
	@Override
	public void addNodesAndRelations() {
		try {
			JSONObject json = extractTraceJSON();
			extract(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected abstract JSONObject extractTraceJSON() throws Exception;
	
	private void extract(JSONObject json) throws Exception {
		JSONArray array = json.getJSONArray("data");
		if(array.isEmpty()) {
			return ;
		}
		
		Map<String, String> isChildOfFromJson = new HashMap<>();
		
		for(int i = 0; i < array.size(); i++) {
			JSONObject data = array.getJSONObject(i);
			String traceId = data.getString("traceID");
			
			currentTrace = new Trace();
			currentTrace.setEntityId(repository.generateEntityId());
			currentTrace.setTraceId(traceId);
			addNode(currentTrace, null);

			JSONObject processes = data.getJSONObject("processes");
			JSONArray spansArray = data.getJSONArray("spans");
			for(int j = 0; j < spansArray.size(); j++) {
				Span span = new Span();
				JSONObject spanJson = spansArray.getJSONObject(j);
				String spanId = spanJson.getString("spanID");
				String operationName = spanJson.getString("operationName");
				String processId = spanJson.getString("processID");
				String serviceName = processes.getJSONObject(processId).getString("serviceName");
				JSONArray logs = spanJson.getJSONArray("logs");
				if(logs != null && logs.size() > 1) {
					JSONObject log = logs.getJSONObject(0);
					JSONArray fields = log.getJSONArray("fields");
					if(fields.size() > 2) {
						JSONObject field = fields.getJSONObject(1);
						span.setApiFunctionName(field.getString("value"));
					}
				}
				span.setTraceId(traceId);
				span.setSpanId(spanId);
				span.setOperationName(operationName);
				span.setServiceName(serviceName);
				span.setEntityId(generateEntityId());
				span.setTime(spanJson.getLong("startTime"));
				spans.put(span.getSpanId(), span);
				JSONArray references = spanJson.getJSONArray("references");
				if(references.size() > 1) {
					throw new Exception("error: references的数量大于1");
				}
				if(references.size() > 0) {
					JSONObject referenceChildOf = references.getJSONObject(0);
					isChildOfFromJson.put(spanId, referenceChildOf.getString("spanID"));
				}
			}
		}
		
		//去掉为http请求的span
		List<String> httpRequestMethodSpanId = new ArrayList<>();
		for(String calledSpanId : isChildOfFromJson.keySet()) {
			Span calledSpan = spans.get(calledSpanId);
			Span callerSpan = spans.get(isChildOfFromJson.get(calledSpanId));
			String calledSpanOperationName = calledSpan.getOperationName();
			String callerSpanOperationName = callerSpan.getOperationName();
			if(HttpRequestMethod.contains(calledSpanOperationName)
					&& !HttpRequestMethod.contains(callerSpanOperationName)) {
				Span realChild = null;
				for(String temp : isChildOfFromJson.keySet()) {
					if(calledSpanId.equals(isChildOfFromJson.get(temp))) {
						realChild = spans.get(temp);
						break;
					}
				}
				httpRequestMethodSpanId.add(calledSpanId);
				SpanCallSpan callSpan = new SpanCallSpan(callerSpan, realChild);
				callSpan.setHttpRequestMethod(calledSpanOperationName);
				callSpan.setRequestSpanId(calledSpan.getSpanId());
				addRelation(callSpan);
			} else if(!HttpRequestMethod.contains(calledSpanOperationName)
					&& !HttpRequestMethod.contains(callerSpanOperationName)) {
				SpanCallSpan callSpan = new SpanCallSpan(callerSpan, calledSpan);
				addRelation(callSpan);
			}
		}
		List<Span> sortedSpans = new ArrayList<>();
		for(String spanId : spans.keySet()) {
			if(!httpRequestMethodSpanId.contains(spanId)) {
				sortedSpans.add(spans.get(spanId));
			}
		}
		sortedSpans.sort(new Comparator<Span>() {
			@Override
			public int compare(Span o1, Span o2) {
				return o1.getTime().compareTo(o2.getTime());
			}
		});
		for(int i = 0; i < sortedSpans.size(); i++) {
			Span span = sortedSpans.get(i);
			span.setOrder(i);
			String serviceName = span.getServiceName();
			Project project = this.getNodes().findProjectByNameAndLanguage(serviceName, "java");
			MicroService microService = getNodes().findMicroServiceByName(serviceName);
			if(project == null || microService == null) {
				throw new Exception("error: span的serviceName不是一个项目 " + serviceName);
			}
			addNode(span, project);
			Contain traceContainSpan = new Contain(currentTrace, span);
			addRelation(traceContainSpan);
			MicroServiceCreateSpan projectCreateSpan = new MicroServiceCreateSpan(microService, span);
			addRelation(projectCreateSpan);
		}
		
	}

	enum HttpRequestMethod {
		GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE, CONNECT;
		
		public static boolean contains(String str) {
			for(HttpRequestMethod m : HttpRequestMethod.values()) {
				if(m.toString().equals(str)) {
					return true;
				}
			}
			return false;
		}
	}
}
