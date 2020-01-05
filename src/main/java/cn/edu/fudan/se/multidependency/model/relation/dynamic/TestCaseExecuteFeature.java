package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_TESTCASE_EXECUTE_FEATURE)
@Deprecated
public class TestCaseExecuteFeature implements Relation {

	private static final long serialVersionUID = 3879809639261277046L;

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	@StartNode
	private TestCase testCase;
	
	@EndNode
	private Feature feature;

	@Override
	public Long getStartNodeGraphId() {
		return testCase.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return feature.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TESTCASE_EXECUTE_FEATURE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}

}
