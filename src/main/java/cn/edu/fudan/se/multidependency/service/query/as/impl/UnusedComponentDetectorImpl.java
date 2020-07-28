package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.as.UnusedComponentDetector;

@Service
public class UnusedComponentDetectorImpl implements UnusedComponentDetector {
	
	@Autowired
	private ASRepository asRepository;

	@Override
	public Collection<Package> unusedPackage() {
		return asRepository.unusedPackages();
	}

}
