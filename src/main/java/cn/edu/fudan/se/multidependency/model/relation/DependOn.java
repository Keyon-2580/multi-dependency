package cn.edu.fudan.se.multidependency.model.relation;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DependOn<N extends Node> implements Relation, RelationWithTimes {
	
	private static final long serialVersionUID = 6381791099417646137L;

    private Long id;
	
	private int times;
	
	private N startNode;
	
	private N endNode;

	public DependOn(N startNode, N endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}
	
	@Override
	public void addTimes() {
		times++;
	}

	@Override
	public Node getStartNode() {
		return startNode;
	}

	@Override
	public Node getEndNode() {
		return endNode;
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
