package cn.edu.fudan.se.multidependency.service.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.dynamic.CallNode;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;

@Service
public class DynamicAnalyseServiceImpl implements DynamicAnalyseService {

	@Autowired
	FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;

	@Override
	public CallNode findCallTree(Function function, int depth) {
		CallNode root = new CallNode(function);
		root.setFunction(function);
		if(depth >= 1) {
			functionDynamicCallFunctionRepository.findCallFunctions(function.getId()).forEach(callFunction -> {
				root.addChild(findCallTree(callFunction, depth - 1));
			});
		}
		return root;
	}
	
}
