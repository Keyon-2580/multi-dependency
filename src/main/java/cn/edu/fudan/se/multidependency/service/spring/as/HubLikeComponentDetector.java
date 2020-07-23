package cn.edu.fudan.se.multidependency.service.spring.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.service.spring.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.spring.metric.PackageMetrics;

public interface HubLikeComponentDetector {

	Collection<PackageMetrics> hubLikePackages();
	
	Collection<FileMetrics> hubLikeFiles();
}
