package cn.edu.fudan.se.multidependency.model.relation;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PackageDependOnPackage implements Relation, RelationWithTimes {
	
	private static final long serialVersionUID = 6381791099417646137L;

    private Long id;
	
	private int times;
	
	private Package startPackage;
	
	private Package endPackage;
	
	public PackageDependOnPackage(Package startPackage, Package endPackage) {
		this.startPackage = startPackage;
		this.endPackage = endPackage;
	}

	@Override
	public void addTimes() {
		times++;
	}

	@Override
	public Node getStartNode() {
		return startPackage;
	}

	@Override
	public Node getEndNode() {
		return endPackage;
	}

	@Override
	public RelationType getRelationType() {
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
