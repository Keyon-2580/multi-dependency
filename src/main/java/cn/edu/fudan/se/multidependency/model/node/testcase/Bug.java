package cn.edu.fudan.se.multidependency.model.node.testcase;

import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;

public class Bug implements Node {

	private static final long serialVersionUID = 4123992909482501778L;

	@Override
	public Long getId() {
		return null;
	}

	@Override
	public void setId(Long id) {
		
	}

	@Override
	public Long getEntityId() {
		return null;
	}

	@Override
	public void setEntityId(Long entityId) {
		
	}

	@Override
	public Map<String, Object> getProperties() {
		return null;
	}

	@Override
	public NodeType getNodeType() {
		return null;
	}

}
