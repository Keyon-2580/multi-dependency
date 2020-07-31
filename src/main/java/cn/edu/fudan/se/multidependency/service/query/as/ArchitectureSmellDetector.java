package cn.edu.fudan.se.multidependency.service.query.as;

public interface ArchitectureSmellDetector {
	
	void setCyclePackagesWithRelation(boolean withRelation);

	void setCycleFilesWithRelation(boolean withRelation);	
	
	/**
	 * 包的循环依赖的检测
	 * @return
	 */
	Object cyclePackages();
	
	Object cycleFiles();
	
	Object unusedPackages();
	
	Object hubLikePackages();
	
	Object hubLikeFiles();
	
	Object cochangesInDifferentModule(int minCochange);
	
	Object unstableFiles(int minFanIn, int cochangeTimesThreshold, int cochangeFilesThreshold);
	Object unstableFiles();
	
	Object similarFiles();
	
	Object similarPackages();
	
	Object multipleASFiles(int minCoChangeSInLogicCouplingFiles);
}
