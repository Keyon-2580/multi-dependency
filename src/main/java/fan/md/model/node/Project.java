package fan.md.model.node;

import java.io.Serializable;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import fan.md.model.Language;

@NodeEntity
public class Project implements Serializable {
	private static final long serialVersionUID = 4058945695982024026L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	private String projectName;
	
	private String projectPath;
	
	private Language language;

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

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}
	
}
