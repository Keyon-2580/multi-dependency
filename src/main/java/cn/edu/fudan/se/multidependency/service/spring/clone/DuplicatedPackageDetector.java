package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.service.spring.clone.data.DuplicatedPackage;

public interface DuplicatedPackageDetector {

	Collection<DuplicatedPackage> detectDuplicatedPackages(int threshold, double percentage);
	
	
}
