package cn.edu.fudan.se.multidependency.model.node;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class ProjectFile implements Node {
	
	private static final long serialVersionUID = -8736926263545574636L;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
	private String name;
	
	private String path;
	
	private String suffix;
	
	public ProjectFile(String name, String path, String suffix) {
		super();
		this.name = name;
		this.path = path;
		this.suffix = suffix;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("path", getPath() == null ? "" : getPath());
		properties.put("suffix", getSuffix() == null ? "" : getSuffix());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.ProjectFile;
	}

	public static final String LABEL_INDEX = "path";
	@Override
	public String indexName() {
		return LABEL_INDEX;
	}

}
