package cn.edu.fudan.se.multidependency.model.node;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneRelationNode;
import cn.edu.fudan.se.multidependency.model.node.code.NodeWithLine;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class ProjectFile implements Node, CloneRelationNode, NodeWithLine {
	
	private static final long serialVersionUID = -8736926263545574636L;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
	private String name;
	
	private String path;
	
	private String suffix;
	
	private int endLine = -1;
	
	public int getStartLine() {
		return 1;
	}
	
	public static final String SUFFIX_JAVA = ".java";
	
	public ProjectFile(Long entityId, String name, String path, String suffix) {
		this.entityId = entityId;
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
		properties.put("endLine", getEndLine());
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

	@Override
	public CloneLevel getCloneLevel() {
		return CloneLevel.file;
	}

}
