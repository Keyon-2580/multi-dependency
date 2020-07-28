package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;

public interface ArchitectureSmellDetector {
	
	/**
	 * 包的循环依赖的检测
	 * @return
	 */
	Collection<Collection<DependsOn>> cyclePackages();
	
	Collection<Collection<DependsOn>> cycleFiles();
	
	Collection<Package> unusedPackages();
	
	Collection<PackageMetrics> hubLikePackages();
	
	Collection<FileMetrics> hubLikeFiles();
	
	Collection<CoChange> cochangesInDifferentModule(int minCochange);
}
