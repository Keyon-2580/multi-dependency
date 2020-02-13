package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity(RelationType.str_NODE_IS_TESTCASE)
@Data
@NoArgsConstructor
public class NodeIsTestCase implements Relation {

	private static final long serialVersionUID = 7393884849758562237L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Node node;
	
	@EndNode
	private TestCase testCase;

	@Override
	public Long getStartNodeGraphId() {
		return node.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return testCase.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.NODE_IS_TESTCASE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
