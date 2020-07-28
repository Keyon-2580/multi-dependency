package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.service.query.clone.data.SimilarPackage;

public interface SimilarPackageDetector {

	Collection<SimilarPackage> detectSimilarPackages(int threshold, double percentage);
	
	
}
