package cn.edu.fudan.se.multidependency.model.relation.clone;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

public interface CloneRelation extends Relation {

	double getValue();
	
	int getNode1StartLine();
	
	int getNode1EndLine();
	
	int getNode2StartLine();
	
	int getNode2EndLine();
	
	int getNode1Index();

	int getNode2Index();
	
	void setCloneType(String cloneType);
	
	String getCloneType();
	
	default Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("value", getValue());
		properties.put("node1Index", getNode1Index());
		properties.put("node2Index", getNode2Index());
		properties.put("node1StartLine", getNode1StartLine());
		properties.put("node1EndLine", getNode1EndLine());
		properties.put("node2StartLine", getNode2StartLine());
		properties.put("node2EndLine", getNode2EndLine());
		properties.put("cloneType", getCloneType() == null ? "" : getCloneType());
		return properties;
	}
}
