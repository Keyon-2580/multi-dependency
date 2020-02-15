package cn.edu.fudan.se.multidependency.service.spring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import lombok.Getter;

public class MSCallMS implements Serializable {
	private static final long serialVersionUID = -2020305373964102611L;
	
	@Getter
	private MicroService ms;
	
	@Getter
	private MicroService callMs;
	
	@Getter
	private Integer times;
	
	private List<SpanCallSpan> spanCallSpans = new ArrayList<>();
	
	public MSCallMS(MicroService ms, MicroService callMs) {
		this.ms = ms;
		this.callMs = callMs;
		this.times = 0;
	}
	
	public void addTimes(Integer times) {
		this.times += times;
	}
	
	public void addSpanCallSpan(SpanCallSpan spanCallSpan) {
		this.spanCallSpans.add(spanCallSpan);
	}
	
	public List<SpanCallSpan> getSpanCallSpans() {
		return new ArrayList<>(this.spanCallSpans);
	}
}
