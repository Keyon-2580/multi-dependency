package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;

public interface HubLikeComponentDetector {

	Collection<PackageMetrics> hubLikePackages();
	
	Collection<FileMetrics> hubLikeFiles();
}
