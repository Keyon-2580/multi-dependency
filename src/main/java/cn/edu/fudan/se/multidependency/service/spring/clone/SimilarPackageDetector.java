package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.service.spring.clone.data.SimilarPackage;

public interface SimilarPackageDetector {

	Collection<SimilarPackage> detectSimilarPackages(int threshold, double percentage);
	
	
}
