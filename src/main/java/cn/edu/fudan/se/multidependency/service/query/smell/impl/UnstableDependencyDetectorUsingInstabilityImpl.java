package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellDetectorService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.smell.UnstableASRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableDependencyDetectorUsingInstability;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableComponentByInstability;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class UnstableDependencyDetectorUsingInstabilityImpl implements UnstableDependencyDetectorUsingInstability {

	@Autowired
	private CacheService cache;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private UnstableASRepository asRepository;
	
	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private SmellDetectorService smellDetectorService;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private DependsOnRepository dependsOnRepository;

	@Autowired
	private ProjectFileRepository projectFileRepository;

	@Autowired
	private PackageRepository packageRepository;
	
	public static final int DEFAULT_THRESHOLD_FILE_FAN_OUT = 10;
	public static final int DEFAULT_THRESHOLD_PACKAGE_FAN_OUT = 10;
	public static final int DEFAULT_THRESHOLD_MODULE_FAN_OUT = 10;
	public static final double DEFAULT_THRESHOLD_RATIO = 0.3;
	
	private Map<Project, Integer> projectToFileFanOutThreshold = new ConcurrentHashMap<>();
	private Map<Project, Integer> projectToPackageFanOutThreshold = new ConcurrentHashMap<>();
	private Map<Project, Integer> projectToModuleFanOutThreshold = new ConcurrentHashMap<>();
	private Map<Project, Double> projectToRatioThreshold = new ConcurrentHashMap<>();

	@Override
	public Map<Long, List<UnstableComponentByInstability<ProjectFile>>> queryFileUnstableDependency() {
		String key = "fileUnstableDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<UnstableComponentByInstability<ProjectFile>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.UNSTABLE_DEPENDENCY));
		smellDetectorService.sortSmellByName(smells);
		List<UnstableComponentByInstability<ProjectFile>> fileUnstableDependencyList = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> contains = new HashSet<>(smellRepository.findSmellContains(smell.getId()));
			Iterator<Node> iterator = contains.iterator();
			if (iterator.hasNext()) {
				ProjectFile component = (ProjectFile) iterator.next();
				UnstableComponentByInstability<ProjectFile> fileUnstableDependency = new UnstableComponentByInstability<>();
				fileUnstableDependency.setComponent(component);
				List<DependsOn> dependsOns = dependsOnRepository.findFileDependsOn(component.getId());
				fileUnstableDependency.addAllTotalDependencies(dependsOns);
				for(DependsOn dependsOn : dependsOns) {
					ProjectFile dependsOnFile = (ProjectFile) dependsOn.getEndNode();
					int dependsOnFileFanOut = 0;
					Integer fanOut = projectFileRepository.getFileFanOutByFileId(dependsOnFile.getId());
					if (fanOut != null) {
						dependsOnFileFanOut = fanOut;
					}
					Project project = containRelationService.findFileBelongToProject(fileUnstableDependency.getComponent());
					if(component.getInstability() < dependsOnFile.getInstability() && dependsOnFileFanOut >= getFileFanOutThreshold(project)) {
						fileUnstableDependency.addBadDependency(dependsOn);
					}
				}
				fileUnstableDependency.setAllDependencies();
				fileUnstableDependency.setBadDependencies();
				fileUnstableDependencyList.add(fileUnstableDependency);
			}
		}
		for (UnstableComponentByInstability<ProjectFile> fileUnstableDependency : fileUnstableDependencyList) {
			Project project = containRelationService.findFileBelongToProject(fileUnstableDependency.getComponent());
			if (project != null) {
				List<UnstableComponentByInstability<ProjectFile>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileUnstableDependency);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableComponentByInstability<Package>>> queryPackageUnstableDependency() {
		String key = "packageUnstableDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<UnstableComponentByInstability<Package>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.PACKAGE, SmellType.UNSTABLE_DEPENDENCY));
		smellDetectorService.sortSmellByName(smells);
		List<UnstableComponentByInstability<Package>> fileUnstableDependencyList = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> contains = new HashSet<>(smellRepository.findSmellContains(smell.getId()));
			Iterator<Node> iterator = contains.iterator();
			if (iterator.hasNext()) {
				Package component = (Package) iterator.next();
				UnstableComponentByInstability<Package> packageUnstableDependency = new UnstableComponentByInstability<>();
				packageUnstableDependency.setComponent(component);
				List<DependsOn> dependsOns = dependsOnRepository.findPackageDependsOn(component.getId());
				packageUnstableDependency.addAllTotalDependencies(dependsOns);
				for(DependsOn dependsOn : dependsOns) {
					Package dependsOnPackage = (Package) dependsOn.getEndNode();
					int dependsOnPackageFanOut = 0;
					Integer fanOut = packageRepository.getPackageFanOutByFileId(dependsOnPackage.getId());
					if (fanOut != null) {
						dependsOnPackageFanOut = fanOut;
					}
					Project project = containRelationService.findPackageBelongToProject(packageUnstableDependency.getComponent());
					if(component.getInstability() < dependsOnPackage.getInstability() && dependsOnPackageFanOut >= getFileFanOutThreshold(project)) {
						packageUnstableDependency.addBadDependency(dependsOn);
					}
				}
				packageUnstableDependency.setAllDependencies();
				packageUnstableDependency.setBadDependencies();
				fileUnstableDependencyList.add(packageUnstableDependency);
			}
		}
		for (UnstableComponentByInstability<Package> fileUnstableDependency : fileUnstableDependencyList) {
			Project project = containRelationService.findPackageBelongToProject(fileUnstableDependency.getComponent());
			if (project != null) {
				List<UnstableComponentByInstability<Package>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileUnstableDependency);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableComponentByInstability<Module>>> queryModuleUnstableDependency() {
		String key = "moduleUnstableDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<UnstableComponentByInstability<Module>>> result = new HashMap<>();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableComponentByInstability<ProjectFile>>> detectFileUnstableDependency() {
		Map<Long, List<UnstableComponentByInstability<ProjectFile>>> result = new HashMap<>();
		Collection<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			List<UnstableComponentByInstability<ProjectFile>> temp = asRepository.unstableFilesByInstability(project.getId(), getFileFanOutThreshold(project), getRatioThreshold(project));
			result.put(project.getId(), temp);
		}
		return result;
	}

	@Override
	public Map<Long, List<UnstableComponentByInstability<Package>>> detectPackageUnstableDependency() {
		Map<Long, List<UnstableComponentByInstability<Package>>> result = new HashMap<>();
		Collection<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			List<UnstableComponentByInstability<Package>> temp = asRepository.unstablePackagesByInstability(project.getId(), getPackageFanOutThreshold(project), getRatioThreshold(project));
			result.put(project.getId(), temp);
		}
		return result;
	}

	
	@Override
	public Map<Long, List<UnstableComponentByInstability<Module>>> detectModuleUnstableDependency() {
		Map<Long, List<UnstableComponentByInstability<Module>>> result = new HashMap<>();
		Collection<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			List<UnstableComponentByInstability<Module>> temp = asRepository.unstableModulesByInstability(project.getId(), getModuleFanOutThreshold(project), getRatioThreshold(project));
			result.put(project.getId(), temp);
		}
		return result;
	}

	public void setRatio(Project project, double threshold) {
		this.projectToRatioThreshold.put(project, threshold);
		cache.remove(getClass());
	}
	
	public void setFileFanOutThreshold(Project project, int threshold) {
		this.projectToFileFanOutThreshold.put(project, threshold);
		cache.remove(getClass());
	}
	
	public void setPackageFanOutThreshold(Project project, int threshold) {
		this.projectToPackageFanOutThreshold.put(project, threshold);
		cache.remove(getClass());
	}

	public void setModuleFanOutThreshold(Project project, int threshold) {
		this.projectToModuleFanOutThreshold.put(project, threshold);
		cache.remove(getClass());
	}
	
	public int getFileFanOutThreshold(Project project) {
		if (!projectToFileFanOutThreshold.containsKey(project)) {
//			projectToFileFanOutThreshold.put(project, DEFAULT_THRESHOLD_FILE_FAN_OUT);
			projectToFileFanOutThreshold.put(project, metricRepository.getMedFileFanOutByProjectId(project.getId()));
		}
		return projectToFileFanOutThreshold.get(project);
	}
	
	public int getPackageFanOutThreshold(Project project) {
		if (!projectToPackageFanOutThreshold.containsKey(project)) {
//			projectToPackageFanOutThreshold.put(project, DEFAULT_THRESHOLD_PACKAGE_FAN_OUT);
			projectToPackageFanOutThreshold.put(project, metricRepository.getMedPackageFanOutByProjectId(project.getId()));
		}
		return projectToPackageFanOutThreshold.get(project);
	}

	public int getModuleFanOutThreshold(Project project) {
		if (!projectToModuleFanOutThreshold.containsKey(project)) {
			projectToModuleFanOutThreshold.put(project, DEFAULT_THRESHOLD_MODULE_FAN_OUT);
//			projectToPackageFanOutThreshold.put(project, metricRepository.getMedPackageFanOutByProjectId(project.getId()));
		}
		return projectToModuleFanOutThreshold.get(project);
	}
	
	public double getRatioThreshold(Project project) {
		if (!projectToRatioThreshold.containsKey(project)) {
			projectToRatioThreshold.put(project, DEFAULT_THRESHOLD_RATIO);
		}
		return projectToRatioThreshold.get(project);
	}
}
