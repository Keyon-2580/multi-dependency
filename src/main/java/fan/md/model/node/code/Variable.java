package fan.md.model.node.code;

import java.util.Map;

import fan.md.model.node.Node;
import fan.md.model.node.NodeType;

public class Variable implements Node {

	private static final long serialVersionUID = 7656480620809763012L;

	private Long parentId;
	
	@Override
	public Long getId() {
		return null;
	}

	@Override
	public void setId(Long id) {
		
	}

	@Override
	public Map<String, Object> getProperties() {
		return null;
	}

	@Override
	public NodeType getNodeType() {
		return null;
	}

	@Override
	public Long getParentId() {
		return parentId;
	}
	
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
}
