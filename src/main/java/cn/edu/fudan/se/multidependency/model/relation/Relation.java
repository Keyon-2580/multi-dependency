package cn.edu.fudan.se.multidependency.model.relation;

import java.io.Serializable;
import java.util.Map;

public interface Relation extends Serializable {
	
	Long getId();
	
	void setId(Long id);

	Long getStartNodeGraphId();
	
	Long getEndNodeGraphId();
	
	RelationType getRelationType();
	
	Map<String, Object> getProperties();
	
}
