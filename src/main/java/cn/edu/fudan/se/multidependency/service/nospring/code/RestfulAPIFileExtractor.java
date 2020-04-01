package cn.edu.fudan.se.multidependency.service.nospring.code;

import cn.edu.fudan.se.multidependency.model.node.RestfulAPI;

public interface RestfulAPIFileExtractor {
	
	Iterable<RestfulAPI> extract();
	
}
