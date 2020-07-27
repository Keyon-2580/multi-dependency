package cn.edu.fudan.se.multidependency.service.spring.as.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.service.spring.as.ArchitectureSmellDetector;
import cn.edu.fudan.se.multidependency.service.spring.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.spring.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.spring.as.UnusedComponentDetector;
import cn.edu.fudan.se.multidependency.service.spring.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.spring.metric.PackageMetrics;

@Service
public class ArchitectureSmellDetectorImpl implements ArchitectureSmellDetector {
	
	@Autowired
	private CyclicDependencyDetector cycleASDetector;

	@Autowired
	private UnusedComponentDetector unusedComponentDetector;
	
	@Autowired
	private HubLikeComponentDetector hubLikeComponentDetector;
	
	@Override
	public Collection<Collection<DependsOn>> cyclePackages() {
		return cycleASDetector.cyclePackages();
	}
	
	@Override
	public Collection<Collection<DependsOn>> cycleFiles() {
		return cycleASDetector.cycleFiles();
	}

	@Override
	public Collection<Package> unusedPackages() {
		return unusedComponentDetector.unusedPackage();
	}

	@Override
	public Collection<PackageMetrics> hubLikePackages() {
		return hubLikeComponentDetector.hubLikePackages();
	}
	
	@Override
	public Collection<FileMetrics> hubLikeFiles() {
		return hubLikeComponentDetector.hubLikeFiles();
	}

}
