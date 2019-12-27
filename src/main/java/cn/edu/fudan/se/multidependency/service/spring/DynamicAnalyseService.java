package cn.edu.fudan.se.multidependency.service.spring;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.dynamic.CallNode;

public interface DynamicAnalyseService {
	
	
	public CallNode findCallTree(Function function, int depth);
	
}
