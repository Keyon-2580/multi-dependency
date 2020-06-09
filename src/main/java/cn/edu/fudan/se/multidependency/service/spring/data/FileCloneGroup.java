package cn.edu.fudan.se.multidependency.service.spring.data;

import java.util.HashSet;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import lombok.Data;

@Data
public class FileCloneGroup {
	
	public FileCloneGroup(CloneGroup group) {
		this.group = group;
	}

	private CloneGroup group;
	
	private Set<ProjectFile> files = new HashSet<>();
	
	private Set<FileCloneFile> relations = new HashSet<>();
	
	public void addFile(ProjectFile file) {
		this.files.add(file);
	}
	
	public void addRelation(FileCloneFile relation) {
		this.relations.add(relation);
	}
	
	public int sizeOfFiles() {
		return files.size();
	}
	
	public int sizeOfRelations() {
		return relations.size();
	}
	
	
}
