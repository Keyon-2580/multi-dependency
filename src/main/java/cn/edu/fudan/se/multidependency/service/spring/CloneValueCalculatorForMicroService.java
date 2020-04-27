package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.Clone;
import cn.edu.fudan.se.multidependency.model.relation.Clone.CloneValueCalculator;

public class CloneValueCalculatorForMicroService implements CloneValueCalculator {
	
	private Map<MicroService, Collection<Function>> msToFunctions = new HashMap<>();
	
	public void addFunctions(Collection<Function> functions, MicroService ms) {
		Collection<Function> temp = msToFunctions.getOrDefault(ms, new ArrayList<>());
		temp.addAll(functions);
		msToFunctions.put(ms, temp);
	}

	@Override
	public String calculate(Clone clone) {
		if(clone.getLevel() != NodeLabelType.MicroService) {
			return clone.getValue() + "";
		}
		StringBuilder builder = new StringBuilder();
		
		builder.append("(");

		List<Clone> functionClones = clone.getChildren();
		
		builder.append(", ");

		builder.append(")");
		return builder.toString();
	}

}
