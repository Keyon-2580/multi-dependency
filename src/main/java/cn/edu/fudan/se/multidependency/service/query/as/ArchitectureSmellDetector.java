package cn.edu.fudan.se.multidependency.service.query.as;

public interface ArchitectureSmellDetector {
	
	/**
	 * 包的循环依赖的检测
	 * @return
	 */
	Object cyclePackages(boolean withRelation);
	
	Object cycleFiles(boolean withRelation);
	
	Object unusedPackages();
	
	Object hubLikePackages();
	
	Object hubLikeFiles();
	
	Object cochangesInDifferentModule(int minCochange);
	
	Object unstableFiles();
	
	Object similarFiles();
	
	Object similarPackages();
	
	Object multipleASFiles(int minCoChangeSInLogicCouplingFiles);
}
