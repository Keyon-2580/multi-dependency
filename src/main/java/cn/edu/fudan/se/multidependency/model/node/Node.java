package cn.edu.fudan.se.multidependency.model.node;

import java.io.Serializable;
import java.util.Map;

public interface Node extends Serializable {

	Long getId();
	
	void setId(Long id);
	
	Long getEntityId();
	
	void setEntityId(Long entityId);

	Map<String, Object> getProperties();
	
	NodeType getNodeType();
	
}
