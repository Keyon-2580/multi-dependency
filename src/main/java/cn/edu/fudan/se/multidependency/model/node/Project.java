package cn.edu.fudan.se.multidependency.model.node;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

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
	
	private String projectName;

	private String projectPath;

	private String language;

	private Long entityId;
	
	public Project(String projectName, String projectPath, Language language) {
		super();
		this.projectName = projectName;
		this.projectPath = projectPath;
		this.language = language.toString();
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("projectName", getProjectName() == null ? "" : getProjectName());
		properties.put("projectPath", getProjectPath() == null ? "" : getProjectPath());
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
