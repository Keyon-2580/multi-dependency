package cn.edu.fudan.se.multidependency.service.spring.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import lombok.Data;

@Data
public class CloneLineValue<P> implements Serializable {
	
	private static final long serialVersionUID = -5684168202304114767L;

	public CloneLineValue(P project) {
		this.project = project;
	}
	
	private P project;

	private Set<ProjectFile> allFiles = new HashSet<>();
	
	private Set<ProjectFile> cloneFiles = new HashSet<>();
	
	private Set<Function> cloneFunctions = new HashSet<>();
	
	private long allFilesLines = 0;
	
	private long allCloneFilesLines = 0;
	
	private long allCloneFunctionsLines = 0;
	
	public void addAllFiles(Collection<ProjectFile> files) {
		this.allFiles.addAll(files);
		for(ProjectFile file : files) {
			this.allFilesLines += file.getLine();
		}
	}
	
	public void addAllCloneFiles(Collection<ProjectFile> files) {
		this.cloneFiles.addAll(files);
		for(ProjectFile file : files) {
			this.allCloneFilesLines += file.getLine();
		}
	}
	
	public void addCloneFile(ProjectFile file) {
		this.cloneFiles.add(file);
		this.allCloneFilesLines += file.getLine();
	}
	
	public void addAllCloneFunctions(Collection<Function> functions) {
		this.cloneFunctions.addAll(functions);
		for(Function function : functions) {
			this.allCloneFunctionsLines += function.getLines();
		}
	}
	
	public void addCloneFunction(Function function) {
		this.cloneFunctions.add(function);
		this.allCloneFunctionsLines += function.getLines();
	}
	
}
