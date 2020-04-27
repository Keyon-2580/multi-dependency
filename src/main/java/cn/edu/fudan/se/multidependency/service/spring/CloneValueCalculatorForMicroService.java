package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone.CloneValueCalculator;

public class CloneValueCalculatorForMicroService implements CloneValueCalculator {
	
	/**
	 * 微服务内的方法数
	 */
	private Map<MicroService, Collection<Function>> msToFunctions = new HashMap<>();
	
	public void addFunctions(Collection<Function> functions, MicroService ms) {
		Collection<Function> temp = msToFunctions.getOrDefault(ms, new ArrayList<>());
		temp.addAll(functions);
		msToFunctions.put(ms, temp);
	}

	@Override
	public String calculate(Clone<? extends Node> clone) {
		assert(clone.getNode1() instanceof MicroService && clone.getNode2() instanceof MicroService);
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		List<FunctionCloneFunction> functionClones = clone.getChildren();
		MicroService ms1 = (MicroService) clone.getNode1();
		MicroService ms2 = (MicroService) clone.getNode2();
		int functionSizeOfMs1 = msToFunctions.get(ms1).size();
		int functionSizeOfMs2 = msToFunctions.get(ms2).size();
		double a = functionClones.size() / (functionSizeOfMs1 + functionSizeOfMs2 + 0.0);
		builder.append(a);
		builder.append(", ");
		double b = clone.getValue() / (functionClones.size() + 0.0);
		builder.append(b);
		builder.append(")");
		return builder.toString();
	}

}
