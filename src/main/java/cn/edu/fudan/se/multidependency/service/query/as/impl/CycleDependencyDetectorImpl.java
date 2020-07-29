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
import cn.edu.fudan.se.multidependency.service.query.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.as.data.CycleComponents;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class CycleDependencyDetectorImpl implements CyclicDependencyDetector {

	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private ContainRelationService containRelationService;	
	
	private Collection<DependsOn> findCyclePackageRelationsBySCC(CycleComponents<Package> cycle) {
		return asRepository.cyclePackagesBySCC(cycle.getPartition());
	}
	
	private Collection<DependsOn> findCycleFileRelationsBySCC(CycleComponents<ProjectFile> cycle) {
		return asRepository.cycleFilesBySCC(cycle.getPartition());
	}
	
	@Override
	public Map<Project, List<Cycle<Package>>> cyclePackages(boolean withRelation) {
		Collection<CycleComponents<Package>> cycles = asRepository.packageCycles();
		Map<Project, List<Cycle<Package>>> result = new HashMap<>();
		for(CycleComponents<Package> cycle : cycles) {
			Cycle<Package> cyclePackage = new Cycle<Package>(cycle);
			if(withRelation) {
				cyclePackage.addAll(findCyclePackageRelationsBySCC(cycle));
			}
			for(Package pck : cycle.getComponents()) {
				Project project = containRelationService.findPackageBelongToProject(pck);
				List<Cycle<Package>> temp = result.getOrDefault(project, new ArrayList<>());
				temp.add(cyclePackage);
				result.put(project, temp);
				break;
			}
		}
		
		return result;
	}

	@Override
	public Map<Project, List<Cycle<ProjectFile>>> cycleFiles(boolean withRelation) {
		Collection<CycleComponents<ProjectFile>> cycles = asRepository.fileCycles();
		Map<Project, List<Cycle<ProjectFile>>> result = new HashMap<>();
		for(CycleComponents<ProjectFile> cycle : cycles) {
			Cycle<ProjectFile> cycleFile = new Cycle<ProjectFile>(cycle);
			if(withRelation) {
				cycleFile.addAll(findCycleFileRelationsBySCC(cycle));
			}
			for(ProjectFile file : cycle.getComponents()) {
				Project project = containRelationService.findFileBelongToProject(file);
				List<Cycle<ProjectFile>> temp = result.getOrDefault(project, new ArrayList<>());
				temp.add(cycleFile);
				result.put(project, temp);
				break;
			}
		}
		return result;
	}
}
