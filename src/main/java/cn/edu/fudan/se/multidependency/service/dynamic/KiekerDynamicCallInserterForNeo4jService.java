package cn.edu.fudan.se.multidependency.service.dynamic;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;

public class KiekerDynamicCallInserterForNeo4jService extends KiekerDynamicInserterForNeo4jService {

	@Override
	protected void extractNodesAndRelations() throws Exception {
		Map<String, List<Function>> functions = this.getNodes().allFunctionsByFunctionName();
		
	}

}
