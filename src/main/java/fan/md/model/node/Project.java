package fan.md.model.node;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import fan.md.model.Language;

@NodeEntity("Project")
public class Project implements Node {
	private static final long serialVersionUID = 4058945695982024026L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	private String projectName;
	
	private String projectPath;
	
	private String language;
	
	public Project(String projectName, String projectPath, Language language) {
		super();
		this.projectName = projectName;
		this.projectPath = projectPath;
		this.language = language.toString();
	}
	
	public Project() {
		super();
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language.toString();
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("language", language.toString());
		properties.put("projectName", getProjectName() == null ? "" : getProjectName());
		properties.put("projectPath", getProjectPath() == null ? "" : getProjectPath());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Project;
	}

}
