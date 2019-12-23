package fan.md.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fan.md.model.node.code.Function;
import fan.md.model.node.dynamic.CallNode;
import fan.md.neo4j.repository.FunctionDynamicCallFunctionRepository;

@Service
public class DynamicCodeServiceImpl implements DynamicCodeService {

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
