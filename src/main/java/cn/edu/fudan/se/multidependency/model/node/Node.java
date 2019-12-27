package cn.edu.fudan.se.multidependency.model.node;

import java.io.Serializable;
import java.util.Map;

public interface Node extends Serializable {

	Long getId();
	
	void setId(Long id);
	
	Integer getEntityId();
	
	void setEntityId(Integer entityId);

	Map<String, Object> getProperties();
	
	NodeType getNodeType();
	
}
