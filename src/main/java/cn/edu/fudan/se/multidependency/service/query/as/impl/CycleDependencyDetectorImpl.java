package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.as.data.CycleComponents;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class CycleDependencyDetectorImpl implements CyclicDependencyDetector {

	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private ContainRelationService containRelationService;	
	
	private Collection<DependsOn> findCyclePackageRelationsBySCC(CycleComponents<Package> cycle) {
		return asRepository.cyclePackagesBySCC(cycle.getPartition());
	}
	
	private Collection<DependsOn> findCycleFileRelationsBySCC(CycleComponents<ProjectFile> cycle) {
		return asRepository.cycleFilesBySCC(cycle.getPartition());
	}
	
	@Override
	public Map<Long, List<Cycle<Package>>> cyclePackages() {
		String key = "cyclePackages";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<Package>> cycles = asRepository.packageCycles();
		Map<Long, List<Cycle<Package>>> result = new HashMap<>();
		for(CycleComponents<Package> cycle : cycles) {
			Cycle<Package> cyclePackage = new Cycle<Package>(cycle);
			cyclePackage.addAll(findCyclePackageRelationsBySCC(cycle));
			for(Package pck : cycle.getComponents()) {
				Project project = containRelationService.findPackageBelongToProject(pck);
				List<Cycle<Package>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(cyclePackage);
				result.put(project.getId(), temp);
				break;
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<Cycle<ProjectFile>>> cycleFiles() {
		String key = "cycleFiles";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<ProjectFile>> cycles = asRepository.fileCycles();
		Map<Long, List<Cycle<ProjectFile>>> result = new HashMap<>();
		for(CycleComponents<ProjectFile> cycle : cycles) {
			Cycle<ProjectFile> cycleFile = new Cycle<ProjectFile>(cycle);
			cycleFile.addAll(findCycleFileRelationsBySCC(cycle));
			for(ProjectFile file : cycle.getComponents()) {
				Project project = containRelationService.findFileBelongToProject(file);
				List<Cycle<ProjectFile>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(cycleFile);
				result.put(project.getId(), temp);
				break;
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}
}
