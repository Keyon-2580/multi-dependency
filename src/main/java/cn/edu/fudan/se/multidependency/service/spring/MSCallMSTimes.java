package cn.edu.fudan.se.multidependency.service.spring;

import java.io.Serializable;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class MSCallMSTimes implements Serializable {
	
	private static final long serialVersionUID = -2020305373964102611L;
	
	private MicroService ms;
	private MicroService callMs;
	
	private Integer times;
	
	public MSCallMSTimes(MicroService ms, MicroService callMs) {
		this.ms = ms;
		this.callMs = callMs;
		this.times = 0;
	}
	
	public void addTimes(Integer times) {
		this.times += times;
	}
}
