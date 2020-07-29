package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.as.ArchitectureSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.as.UnstableDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.UnusedComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikePackage;
import cn.edu.fudan.se.multidependency.service.query.as.data.LogicCoupling;
import cn.edu.fudan.se.multidependency.service.query.as.data.SimilarComponents;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;

@Service
public class ArchitectureSmellDetectorImpl implements ArchitectureSmellDetector {
	
	@Autowired
	private CyclicDependencyDetector cycleASDetector;

	@Autowired
	private UnusedComponentDetector unusedComponentDetector;
	
	@Autowired
	private HubLikeComponentDetector hubLikeComponentDetector;
	
	@Autowired
	private ImplicitCrossModuleDependencyDetector icdDependencyDetector;
	
	@Autowired
	private SimilarComponentsDetector similarComponentsDetector;
	
	@Autowired
	private UnstableDependencyDetector unstableDependencyDetector;
	
	@Override
	public Map<Project, List<Cycle<Package>>> cyclePackages(boolean withRelation) {
		return cycleASDetector.cyclePackages(withRelation);
	}
	
	@Override
	public Map<Project, List<Cycle<ProjectFile>>> cycleFiles(boolean withRelation) {
		return cycleASDetector.cycleFiles(withRelation);
	}

	@Override
	public Map<Project, List<Package>> unusedPackages() {
		return unusedComponentDetector.unusedPackage();
	}

	@Override
	public Map<Project, List<HubLikePackage>> hubLikePackages() {
		return hubLikeComponentDetector.hubLikePackages();
	}
	
	@Override
	public Map<Project, List<HubLikeFile>> hubLikeFiles() {
		return hubLikeComponentDetector.hubLikeFiles();
	}

	@Override
	public Collection<LogicCoupling> cochangesInDifferentModule(int minCochange) {
		return icdDependencyDetector.cochangesInDifferentModule(minCochange);
	}

	@Override
	public Map<Project, List<UnstableFile>> unstableFiles() {
		return unstableDependencyDetector.unstableFiles();
	}

	@Override
	public Collection<SimilarComponents<ProjectFile>> similarFiles() {
		return similarComponentsDetector.similarFiles();
	}

	@Override
	public Collection<SimilarComponents<Package>> similarPackages() {
		return similarComponentsDetector.similarPackages();
	}

}
