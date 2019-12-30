package cn.edu.fudan.se.multidependency.service.spring;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.StaticCodeNodes;
import cn.edu.fudan.se.multidependency.model.node.dynamic.CallNode;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;

@Service
public class DynamicAnalyseServiceImpl implements DynamicAnalyseService {
	
	protected StaticCodeNodes codeNodes;

	protected Nodes nodes = new Nodes();

	protected Relations relations = new Relations();
	
	@Autowired
	private FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;

	public void insertToNeo4jDataBase(String scenarioName, List<String> featureName, String testcaseName,
			File executeFile) throws Exception {
		
		
		insertRelations();
	}
	
	private void insertRelations() {
		relations.getAllRelations().forEach((relationType, rs) -> {
			rs.forEach(relation -> {
				if(relation.getRelationType() == RelationType.DEPENDENCY_DYNAMIC_FUNCTION_CALL_FUNCTION) {
					functionDynamicCallFunctionRepository.save((FunctionDynamicCallFunction) relation);
				}
			});
		});
	}
	
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

	public StaticCodeNodes getCodeNodes() {
		return codeNodes;
	}

	public void setCodeNodes(StaticCodeNodes codeNodes) {
		this.codeNodes = codeNodes;
	}
	
}
