package cn.edu.fudan.se.multidependency.service.spring.data;

import java.util.HashSet;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import lombok.Data;

@Data
public class FileCloneGroup implements IsCloneGroup {
	
	public FileCloneGroup(CloneGroup group) {
		this.group = group;
	}

	private CloneGroup group;
	
	private Set<ProjectFile> nodes = new HashSet<>();
	
	private Set<FileCloneFile> relations = new HashSet<>();

	/*private Map<ProjectFile, Project> fileBelongToProject = new HashMap<>();
	
	private Map<ProjectFile, MicroService> fileBelongToMSs = new HashMap<>();
	
	public void addFileBelongToProject(ProjectFile file, Project project) {
		this.fileBelongToProject.put(file, project);
	}
	
	public void addFileBelongToMicroService(ProjectFile file, MicroService ms) {
		this.fileBelongToMSs.put(file, ms);
	}
	
	public Collection<MicroService> relatedMSs() {
		Set<MicroService> result = new HashSet<>();
		result.addAll(fileBelongToMSs.values());
		return result;
	}
	
	public Collection<Project> relatedProjects() {
		Set<Project> result = new HashSet<>();
		result.addAll(fileBelongToProject.values());
		return result;
	}*/
	public void addFile(ProjectFile file) {
		this.nodes.add(file);
	}
	
	public void addRelation(FileCloneFile relation) {
		this.relations.add(relation);
	}
	
}
