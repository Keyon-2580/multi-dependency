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
	
	private boolean cyclicHierarchy;
	
	private boolean god;
	
	public int smellCount() {
		return (cycle ? 1 : 0) + (hublike ? 1 : 0) + (logicCoupling ? 1 : 0) 
				+ (similar ? 1 : 0) + (unstable ? 1 : 0) + (cyclicHierarchy ? 1 : 0)
				+ (god ? 1 : 0);
	}
	
	private static String toString(boolean b) {
		return b ? "T" : "";
	}
	
	public String godToString() {
		return toString(this.god);
	}
	
	public String cycleToString() {
		return toString(this.cycle);
	}
	
	public String hubLikeToString() {
		return toString(this.hublike);
	}
	
	public String unstableToString() {
		return toString(this.unstable);
	}
	
	public String logicCouplingToString() {
		return toString(this.logicCoupling);
	}
	
	public String similarToString() {
		return toString(this.similar);
	}
	
	public String cyclicHierarchyToString() {
		return toString(this.cyclicHierarchy);
	}
	
}
