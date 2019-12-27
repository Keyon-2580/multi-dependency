package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;

@NodeEntity
public class Function implements Node {

	private static final long serialVersionUID = 6993550414163132668L;
	
	@Id
    @GeneratedValue
    private Long id;
	
    private Integer entityId;

	private String functionName;
	
	private String returnTypeIdentify;

	@Transient
	private List<String> parameters = new ArrayList<>();
	
	private String parametersIdentifies;
	
	public String getFunctionName() {
		return functionName;
	}
	
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("functionName", getFunctionName() == null ? "" : getFunctionName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("returnTypeIdentify", getReturnTypeIdentify() == null ? "" : getReturnTypeIdentify());
		properties.put("parametersIdentifies", getParameters().toString().replace('[', '(').replace(']', ')'));
		return properties;
	}
	
	@Override
	public NodeType getNodeType() {
		return NodeType.Function;
	}

	public String getReturnTypeIdentify() {
		return returnTypeIdentify;
	}

	public void setReturnTypeIdentify(String returnTypeIdentify) {
		this.returnTypeIdentify = returnTypeIdentify;
	}
	
	public List<String> getParameters() {
		if(parameters == null || parameters.size() == 0) {
			if(getParametersIdentifies() != null) {
//				String temp = parametersIdentifies.
			} else {
				return new ArrayList<>();
			}
		}
		return parameters;
	}
	
	public void cleanParameters() {
		this.parameters.clear();
	}
	
	public void addParameterIdentifies(String... parameters) {
		for(String parameter : parameters) {
			this.parameters.add(parameter);
		}
	}

	public String getParametersIdentifies() {
		return parametersIdentifies;
	}

}
