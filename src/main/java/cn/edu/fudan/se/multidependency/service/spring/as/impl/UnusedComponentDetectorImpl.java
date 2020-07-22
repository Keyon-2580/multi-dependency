package cn.edu.fudan.se.multidependency.service.spring.as.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.service.spring.as.UnusedComponentDetector;

@Service
public class UnusedComponentDetectorImpl implements UnusedComponentDetector {
	
	@Autowired
	private PackageRepository packageRepository;

	@Override
	public Collection<Package> unusedPackage() {
		return packageRepository.unusedPackages();
	}

}