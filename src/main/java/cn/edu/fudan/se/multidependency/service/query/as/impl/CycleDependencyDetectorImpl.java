package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.as.CycleASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.ModuleService;
import cn.edu.fudan.se.multidependency.service.query.as.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.as.data.CycleComponents;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class CycleDependencyDetectorImpl implements CyclicDependencyDetector {

	@Autowired
	private CycleASRepository asRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private ContainRelationService containRelationService;	
	
	@Autowired
	private ModuleService moduleService;
	
	private Collection<DependsOn> findCyclePackageRelationsBySCC(CycleComponents<Package> cycle) {
		return asRepository.cyclePackagesBySCC(cycle.getPartition());
	}
	
	private Collection<DependsOn> findCycleFileRelationsBySCC(CycleComponents<ProjectFile> cycle) {
		return asRepository.cycleFilesBySCC(cycle.getPartition());
	}

	private Collection<DependsOn> findCycleTypeRelationsBySCC(CycleComponents<Type> cycle) {
		return asRepository.cycleTypesBySCC(cycle.getPartition());
	}
	
	private Collection<DependsOn> findCycleModuleRelationsBySCC(CycleComponents<Module> cycle) {
		return asRepository.cycleModulesBySCC(cycle.getPartition());
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Type>>> cycleTypes() {
		String key = "cycleTypes";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<Type>> cycles = asRepository.typeCycles();
		Map<Long, Map<Integer, Cycle<Type>>> result = new HashMap<>();
		for(CycleComponents<Type> cycle : cycles) {
			Cycle<Type> cycleType = new Cycle<Type>(cycle);
			cycleType.addAll(findCycleTypeRelationsBySCC(cycle));
			boolean flag = false;
			Project project = null;
			for(Type type : cycle.getComponents()) {
				if(!flag) {
					project = containRelationService.findTypeBelongToProject(type);
					flag = true;
				}
			}
			if(project != null) {
				Map<Integer, Cycle<Type>> temp = result.getOrDefault(project.getId(), new HashMap<>());
				temp.put(cycleType.getPartition(), cycleType);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<ProjectFile>>> cycleFiles() {
		String key = "cycleFiles";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<ProjectFile>> cycles = asRepository.fileCycles();
		Map<Long, Map<Integer, Cycle<ProjectFile>>> result = new HashMap<>();
		for(CycleComponents<ProjectFile> cycle : cycles) {
			Cycle<ProjectFile> cycleFile = new Cycle<ProjectFile>(cycle);
			cycleFile.addAll(findCycleFileRelationsBySCC(cycle));
			boolean flag = false;
			Project project = null;
			Module lastModule = null;
			for(ProjectFile file : cycle.getComponents()) {
				Module fileBelongToModule = moduleService.findFileBelongToModule(file);
				if(lastModule == null) {
					lastModule = fileBelongToModule;
				}
				cycleFile.putComponentBelongToGroup(file, fileBelongToModule);
				if(!flag) {
					project = containRelationService.findFileBelongToProject(file);
					flag = true;
				}
			}
			Map<Integer, Cycle<ProjectFile>> temp = result.getOrDefault(project.getId(), new HashMap<>());
			temp.put(cycleFile.getPartition(), cycleFile);
			result.put(project.getId(), temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Module>>> cycleModules() {
		String key = "cycleModules";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<Module>> cycles = asRepository.moduleCycles();
		Map<Long, Map<Integer, Cycle<Module>>> result = new HashMap<>();
		for(CycleComponents<Module> cycle : cycles) {
			Cycle<Module> cyclePackage = new Cycle<Module>(cycle);
			cyclePackage.addAll(findCycleModuleRelationsBySCC(cycle));
			boolean flag = false;
			for(Module pck : cycle.getComponents()) {
				cyclePackage.putComponentBelongToGroup(pck, pck);
				if(!flag) {
					Project project = moduleService.findModuleBelongToProject(pck);
					Map<Integer, Cycle<Module>> temp = result.getOrDefault(project.getId(), new HashMap<>());
					temp.put(cyclePackage.getPartition(), cyclePackage);
					result.put(project.getId(), temp);
					flag = true;
				}
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Package>>> cyclePackages() {
		String key = "cyclePackages";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<Package>> cycles = asRepository.packageCycles();
		Map<Long, Map<Integer, Cycle<Package>>> result = new HashMap<>();
		for(CycleComponents<Package> cycle : cycles) {
			Cycle<Package> cyclePackage = new Cycle<Package>(cycle);
			cyclePackage.addAll(findCyclePackageRelationsBySCC(cycle));
			boolean flag = false;
			for(Package pck : cycle.getComponents()) {
				cyclePackage.putComponentBelongToGroup(pck, pck);
				if(!flag) {
					Project project = containRelationService.findPackageBelongToProject(pck);
					Map<Integer, Cycle<Package>> temp = result.getOrDefault(project.getId(), new HashMap<>());
					temp.put(cyclePackage.getPartition(), cyclePackage);
					result.put(project.getId(), temp);
					flag = true;
				}
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}
}
