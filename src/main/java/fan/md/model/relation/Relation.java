package fan.md.model.relation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public interface Relation extends Serializable {
	
	Long getId();
	
	void setId(Long id);

	Long getStartNodeGraphId();
	
	Long getEndNodeGraphId();
	
	RelationType getRelationType();
	
	default Map<String, Object> getProperties() {
		return new HashMap<>();
	}
	
}
