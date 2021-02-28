package cn.edu.fudan.se.multidependency.service.query.smell.data;

public interface MultipleAS {

	boolean isCycle();
	
	boolean isHublike();
	
	boolean isLogicCoupling();
	
	boolean isSimilar();
	
	boolean isUnstable();
	
//	boolean isCyclicHierarchy();
	
//	boolean isGod();
	
	boolean isUnused();
	
	boolean isUnutilized();
	
//	default int smellCount() {
//		return (isCycle() ? 1 : 0) + (isHublike() ? 1 : 0) + (isLogicCoupling() ? 1 : 0) 
//				+ (isSimilar() ? 1 : 0) + (isUnstable() ? 1 : 0) + (isCyclicHierarchy() ? 1 : 0)
//				+ (isGod() ? 1 : 0) + (isUnused() ? 1 : 0);
//	}
	default int getSmellCount() {
		return (isCycle() ? 1 : 0) + (isHublike() ? 1 : 0) + (isLogicCoupling() ? 1 : 0) 
				+ (isSimilar() ? 1 : 0) + (isUnstable() ? 1 : 0)
				+ (isUnused() ? 1 : 0) + (isUnutilized() ? 1 : 0);
	}
	
}
