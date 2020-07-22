package cn.edu.fudan.se.multidependency.service.spring.as.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependOn;
import cn.edu.fudan.se.multidependency.service.spring.as.ArchitectureSmellDetector;
import cn.edu.fudan.se.multidependency.service.spring.as.CycleASDetector;
import cn.edu.fudan.se.multidependency.service.spring.as.UnusedComponentDetector;

@Service
public class ArchitectureSmellDetectorImpl implements ArchitectureSmellDetector {
	
	@Autowired
	private CycleASDetector cycleASDetector;

	@Autowired
	private UnusedComponentDetector unusedComponentDetector;
	
	@Override
	public Collection<Collection<DependOn>> findCyclePackages() {
		return cycleASDetector.findCyclePackages();
	}

	@Override
	public Collection<Package> unusedPackages() {
		return unusedComponentDetector.unusedPackage();
	}

	
}
