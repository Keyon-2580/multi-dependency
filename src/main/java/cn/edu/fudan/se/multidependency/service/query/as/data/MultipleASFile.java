package cn.edu.fudan.se.multidependency.service.query.as.data;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;

@Data
public class MultipleASFile implements MultipleAS {
	
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
	
//	private boolean cyclicHierarchy;
	
//	private boolean god;
	
	private boolean unused;

	private static String toString(boolean b) {
		return b ? "T" : "";
	}
	
//	public String godToString() {
//		return toString(this.god);
//	}
	
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
	
//	public String cyclicHierarchyToString() {
//		return toString(this.cyclicHierarchy);
//	}
	
	public String unusedToString() {
		return toString(this.unused);
	}
	
	public boolean isSmellFile(MultipleAS smell) {
		if(smell.isCycle() && isCycle()) {
			return true;
		}
//		if(smell.isCyclicHierarchy() && isCyclicHierarchy()) {
//			return true;
//		}
//		if(smell.isGod() && isGod()) {
//			return true;
//		}
		if(smell.isHublike() && isHublike()) {
			return true;
		}
		if(smell.isLogicCoupling() && isLogicCoupling()) {
			return true;
		}
		if(smell.isSimilar() && isSimilar()) {
			return true;
		}
		if(smell.isUnstable() && isUnstable()) {
			return true;
		}
		if(smell.isUnused() && isUnused()) {
			return true;
		}
		return false;
	}

}
