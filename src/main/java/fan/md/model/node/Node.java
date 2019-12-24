package fan.md.model.node;

import java.io.Serializable;
import java.util.Map;

public interface Node extends Serializable {

	Long getId();
	
	void setId(Long id);

	Map<String, Object> getProperties();
	
	NodeType getNodeType();
	
}
