package cn.edu.fudan.se.multidependency.service.query.metric;

import cn.edu.fudan.se.multidependency.model.node.Node;

public interface FanIOMetric {
	
	Node getComponent();
	
	int getFanIn();
	
	int getFanOut();

	default int allFanIO() {
		return getFanIn() + getFanOut();
	}
	
	default int fanIODValue() {
		return Math.abs(getFanIn() - getFanOut());
	}
}
