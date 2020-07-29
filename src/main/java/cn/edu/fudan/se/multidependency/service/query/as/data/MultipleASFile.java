package cn.edu.fudan.se.multidependency.service.query.as.data;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;

@Data
public class MultipleASFile {
	
	public MultipleASFile(ProjectFile file) {
		this.file = file;
	}
	
	private Project project;

	private ProjectFile file;
	
	private boolean cycle;
	
	private boolean hublike;
	
	private boolean logicCoupling;
	
	private boolean similar;
	
	private boolean unstable;
	
	public int smellCount() {
		return (cycle ? 1 : 0) + (hublike ? 1 : 0) + (logicCoupling ? 1 : 0) + (similar ? 1 : 0) + (unstable ? 1 : 0);
	}
	
}
