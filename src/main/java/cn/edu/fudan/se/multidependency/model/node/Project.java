package cn.edu.fudan.se.multidependency.model.node;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

import cn.edu.fudan.se.multidependency.model.Language;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Project implements Node {
	
	private static final long serialVersionUID = 4058945695982024026L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	private String name;

	private String path;

	private String language;
	
	@Transient
	private String microserviceName;

	private Long entityId;
	
	public Project(String name, String path, Language language) {
		super();
		this.name = name;
		this.path = path;
		this.language = language.toString();
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("path", getPath() == null ? "" : getPath());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Project;
	}

	
	public static final String LABEL_INDEX = "projectName";
	@Override
	public String indexName() {
		return LABEL_INDEX;
	}

}
