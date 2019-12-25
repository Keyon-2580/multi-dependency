package cn.edu.fudan.se.multidependency.service;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.dynamic.CallNode;

public interface DynamicCodeService {
	
	
	public CallNode findCallTree(Function function, int depth);
	
}
