package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.utils.CppDynamicUtil;

public class CppDynamicInserterForNeo4jService extends DynamicInserterForNeo4jService {

	@Override
	protected void extractScenarioAndTestCaseAndFeatures() {
		
	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		Map<String, List<Function>> functions = this.getNodes().allFunctionsByFunctionName();
		for(File file : executeFiles) {
			Map<String, List<String>> result = CppDynamicUtil.extract(file);
			for(String start : result.keySet()) {
				List<String> ends = result.get(start);
				List<Function> startFunctions = functions.get(start);
				Function startFunction = null;
				if(startFunctions != null && startFunctions.size() != 0) {
					if(startFunctions.size() == 1) {
						startFunction = startFunctions.get(0);
					}
				}
				if(startFunction == null) {
					continue;
				}
				for(String end : ends) {
					List<Function> endFunctions = functions.get(end);
					if(endFunctions != null && endFunctions.size() != 0) {
						if(endFunctions.size() == 1) {
							Function endFunction = endFunctions.get(0);
							FunctionDynamicCallFunction dynamicCall = new FunctionDynamicCallFunction();
							dynamicCall.setFunction(startFunction);
							dynamicCall.setCallFunction(endFunction);
							addRelation(dynamicCall);
						}
					} else {
						continue;
					}
				}
			}
		}
	}

}
