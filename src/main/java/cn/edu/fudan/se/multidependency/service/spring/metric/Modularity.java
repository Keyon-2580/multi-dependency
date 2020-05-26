package cn.edu.fudan.se.multidependency.service.spring.metric;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;

@Data
public class Modularity {

	private double value;
	
	private NodeLabelType modularityType;
	
}
