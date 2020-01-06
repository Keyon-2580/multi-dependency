package cn.edu.fudan.se.multidependency.service.dynamic;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil.DynamicFunctionCallFromKieker;

public class KiekerDynamicCallInserterForNeo4jService extends KiekerDynamicInserterForNeo4jService {

	@Override
	protected void extractNodesAndRelations() throws Exception {
		Map<String, List<Function>> functions = this.getNodes().allFunctionsByFunctionName();
		Map<String, List<DynamicFunctionCallFromKieker>> allDynamicFunctionFromKiekers = JavaDynamicUtil.readKiekerCallFile(dynamicFunctionCallFiles);
		TestCase currentTestCase = null;
		for(String callId : allDynamicFunctionFromKiekers.keySet()) {
			List<DynamicFunctionCallFromKieker> calls = allDynamicFunctionFromKiekers.get(callId);
			for(DynamicFunctionCallFromKieker call : calls) {
				
			}
		}
	}

}
