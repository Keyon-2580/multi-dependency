package cn.edu.fudan.se.multidependency.service.spring.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.service.spring.as.CycleComponentDetector;
import cn.edu.fudan.se.multidependency.service.spring.as.data.CycleComponents;

@Service
public class CycleComponentDetectorImpl implements CycleComponentDetector {

	@Autowired
	private ASRepository asRepository;
	
	@SuppressWarnings("unused")
	@Deprecated
	private Collection<DependsOn> findCyclePackageRelationsByIds(CycleComponents<Package> cycle) {
		long[] ids = new long[cycle.getComponents().size()];
		int i = 0;
		for(Package pck : cycle.getComponents()) {
			ids[i] = pck.getId();
		}
		return asRepository.cyclePackagesByIds(ids);
	}
	
	private Collection<DependsOn> findCyclePackageRelationsBySCC(CycleComponents<Package> cycle) {
		return asRepository.cyclePackagesBySCC(cycle.getPartition());
	}
	
	private Collection<DependsOn> findCycleFileRelationsBySCC(CycleComponents<ProjectFile> cycle) {
		return asRepository.cycleFilesBySCC(cycle.getPartition());
	}
	
	@Override
	public Collection<Collection<DependsOn>> cyclePackages() {
		Collection<CycleComponents<Package>> cyclePackages = asRepository.cyclePackages();
		List<Collection<DependsOn>> result = new ArrayList<>();
		for(CycleComponents<Package> cycle : cyclePackages) {
			result.add(findCyclePackageRelationsBySCC(cycle));
		}
		return result;
	}

	@Override
	public Collection<Collection<DependsOn>> cycleFiles() {
		Collection<CycleComponents<ProjectFile>> cycleFiles = asRepository.cycleFiles();
		List<Collection<DependsOn>> result = new ArrayList<>();
		for(CycleComponents<ProjectFile> cycle : cycleFiles) {
			result.add(findCycleFileRelationsBySCC(cycle));
		}
		return result;
	}
}
