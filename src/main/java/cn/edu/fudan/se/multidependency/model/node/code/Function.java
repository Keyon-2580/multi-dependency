package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Function implements Node {

	private static final long serialVersionUID = 6993550414163132668L;
	
	@Id
    @GeneratedValue
    private Long id;

    private Long entityId;

	private String functionName;

	private String returnTypeIdentify;

	private boolean fromDynamic = false;
	
	private boolean contrustor;
	
	private String inFilePath;

	/**
	 * 插入时使用这个，因为用BatchInserter的时候插入这个会转成字符串插入，用SDN读取时对应不到这个List
	 */
	@Transient
	private List<String> parameters = new ArrayList<>();
	
	/**
	 * 用SDN读取到这个
	 */
	private String parametersIdentifies;
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("functionName", getFunctionName() == null ? "" : getFunctionName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("returnTypeIdentify", getReturnTypeIdentify() == null ? "" : getReturnTypeIdentify());
		properties.put("parametersIdentifies", getParameters().toString().replace('[', '(').replace(']', ')'));
		properties.put("fromDynamic", isFromDynamic());
		properties.put("constructor", isContrustor());
		properties.put("inFilePath", getInFilePath() == null ? "" : getInFilePath());
		return properties;
	}
	
	@Override
	public NodeType getNodeType() {
		return NodeType.Function;
	}

	public List<String> getParameters() {
		if(parameters == null || parameters.size() == 0) {
			if(getParametersIdentifies() != null) {
				parameters = new ArrayList<>();
				String parametersStr = getParametersIdentifies().substring(getParametersIdentifies().lastIndexOf("(") + 1, getParametersIdentifies().length() - 1);
				if (!StringUtils.isBlank(parametersStr)) {
					String[] parameters = parametersStr.split(",");
					for (String parameter : parameters) {
						this.parameters.add(parameter);
					}
				}
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

	/**
	 * 在SDN中才能调用
	 * @return
	 */
	public String getParametersIdentifies() {
		return parametersIdentifies;
	}

}
