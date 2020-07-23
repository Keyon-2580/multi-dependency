package cn.edu.fudan.se.multidependency.service.spring.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependOn;
import cn.edu.fudan.se.multidependency.service.spring.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.spring.metric.PackageMetrics;

public interface ArchitectureSmellDetector {
	
	/**
	 * 包的循环依赖的检测
	 * @return
	 */
	Collection<Collection<DependOn>> cyclePackages();
	
	Collection<Collection<DependOn>> cycleFiles();
	
	Collection<Package> unusedPackages();
	
	Collection<PackageMetrics> hubLikePackages();
	
	Collection<FileMetrics> hubLikeFiles();
}
