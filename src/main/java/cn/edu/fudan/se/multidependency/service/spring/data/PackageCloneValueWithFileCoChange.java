package cn.edu.fudan.se.multidependency.service.spring.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;

@Data
public class PackageCloneValueWithFileCoChange implements Serializable {

	private static final long serialVersionUID = 1287543734251562271L;
	
	private Package pck1;
	
	private Package pck2;
	
	// 两个克隆节点内部的克隆关系
	protected List<FileCloneWithCoChange> children = new ArrayList<>();
	
	private Set<ProjectFile> cloneFiles1 = new HashSet<>();
	
	private Set<ProjectFile> cloneFiles2 = new HashSet<>();
	
	private Set<ProjectFile> allFiles1 = new HashSet<>();
	
	private Set<ProjectFile> allFiles2 = new HashSet<>();
	
	public void addCloneFile1(ProjectFile file) {
		this.cloneFiles1.add(file);
	}
	
	public void addCloneFile2(ProjectFile file) {
		this.cloneFiles2.add(file);
	}
	
	public void addFile1(ProjectFile file) {
		this.allFiles1.add(file);
	}
	
	public void addFile2(ProjectFile file) {
		this.allFiles2.add(file);
	}
	
	public void addFile1(Collection<ProjectFile> files) {
		this.allFiles1.addAll(files);
	}

	public void addFile2(Collection<ProjectFile> files) {
		this.allFiles2.addAll(files);
	}
	
	public void sortChildren() {
		children.sort((clone1, clone2) -> {
			return clone2.getCochangeTimes() - clone1.getCochangeTimes();
		});
	}
	
	public void addChild(FileCloneWithCoChange child) {
		this.children.add(child);
	}
	
}
