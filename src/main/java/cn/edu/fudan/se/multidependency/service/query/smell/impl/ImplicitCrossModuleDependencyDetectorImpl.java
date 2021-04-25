package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellDetectorService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.ModuleService;
import cn.edu.fudan.se.multidependency.service.query.smell.data.LogicCouplingComponents;

@Service
public class ImplicitCrossModuleDependencyDetectorImpl implements ImplicitCrossModuleDependencyDetector {
	
	@Autowired
	private CoChangeRepository cochangeRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private ModuleService moduleService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private PackageRepository packageRepository;

	@Autowired
	private ProjectFileRepository projectFileRepository;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private SmellDetectorService smellDetectorService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private MetricRepository metricRepository;

	private static final int MIN_FILE_CO_CHANGE = 10;
	private static final int MIN_PACKAGE_CO_CHANGE = 10;

	private Map<Long, Integer> projectMinFileCoChange = new ConcurrentHashMap<>();
	private Map<Long, Integer> projectMinPackageCoChange = new ConcurrentHashMap<>();

	@Override
	public Map<Long, List<LogicCouplingComponents<ProjectFile>>> queryFileImplicitCrossModuleDependency() {
		String key = "fileImplicitCrossModuleDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<LogicCouplingComponents<ProjectFile>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY));
		smellDetectorService.sortSmellByName(smells);
		List<LogicCouplingComponents<ProjectFile>> fileImplicitCrossModuleDependencyList = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> contains = new HashSet<>(smellRepository.findSmellContains(smell.getId()));
			Iterator<Node> iterator = contains.iterator();
			ProjectFile file1 = null;
			ProjectFile file2 = null;
			if (iterator.hasNext()) {
				file1 = (ProjectFile) iterator.next();
			}
			if (iterator.hasNext()) {
				file2 = (ProjectFile) iterator.next();
			}
			if (file1 != null && file2 != null) {
				CoChange coChange = cochangeRepository.findCoChangesBetweenTwoFilesWithoutDirection(file1.getId(), file2.getId());
				if (coChange != null) {
					LogicCouplingComponents<ProjectFile> fileImplicitCrossModuleDependency = new LogicCouplingComponents<>(file1, file2, coChange.getTimes());
					fileImplicitCrossModuleDependencyList.add(fileImplicitCrossModuleDependency);
				}
			}
		}
		for (LogicCouplingComponents<ProjectFile> fileImplicitCrossModuleDependency : fileImplicitCrossModuleDependencyList) {
			Project project1 = containRelationService.findFileBelongToProject(fileImplicitCrossModuleDependency.getNode1());
			Project project2 = containRelationService.findFileBelongToProject(fileImplicitCrossModuleDependency.getNode2());
			if (project1 != null && project2 != null && project1.getId().equals(project2.getId())) {
				List<LogicCouplingComponents<ProjectFile>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
				temp.add(fileImplicitCrossModuleDependency);
				result.put(project1.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<LogicCouplingComponents<Package>>> queryPackageImplicitCrossModuleDependency() {
		String key = "packageImplicitCrossModuleDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<LogicCouplingComponents<Package>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.PACKAGE, SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY));
		smellDetectorService.sortSmellByName(smells);
		List<LogicCouplingComponents<Package>> packageImplicitCrossModuleDependencyList = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> contains = new HashSet<>(smellRepository.findSmellContains(smell.getId()));
			Iterator<Node> iterator = contains.iterator();
			Package package1 = null;
			Package package2 = null;
			if (iterator.hasNext()) {
				package1 = (Package) iterator.next();
			}
			if (iterator.hasNext()) {
				package2 = (Package) iterator.next();
			}
			if (package1 != null && package2 != null) {
				CoChange coChange = cochangeRepository.findCoChangesBetweenTwoPackagesWithoutDirection(package1.getId(), package2.getId());
				if (coChange != null) {
					LogicCouplingComponents<Package> fileImplicitCrossModuleDependency = new LogicCouplingComponents<>(package1, package2, coChange.getTimes());
					packageImplicitCrossModuleDependencyList.add(fileImplicitCrossModuleDependency);
				}
			}
		}
		for (LogicCouplingComponents<Package> fileImplicitCrossModuleDependency : packageImplicitCrossModuleDependencyList) {
			Project project1 = containRelationService.findPackageBelongToProject(fileImplicitCrossModuleDependency.getNode1());
			Project project2 = containRelationService.findPackageBelongToProject(fileImplicitCrossModuleDependency.getNode2());
			if (project1 != null && project2 != null && project1.getId().equals(project2.getId())) {
				List<LogicCouplingComponents<Package>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
				temp.add(fileImplicitCrossModuleDependency);
				result.put(project1.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<LogicCouplingComponents<ProjectFile>>> detectFileImplicitCrossModuleDependency() {
		Map<Long, List<LogicCouplingComponents<ProjectFile>>> result = new HashMap<>();
		List<LogicCouplingComponents<ProjectFile>> fileImplicitCrossModuleDependencyList = new ArrayList<>();
		Collection<Project> projects = nodeService.allProjects();
		for (Project project : projects) {
			Collection<CoChange> cochangesWithOutDependsOn = cochangeRepository.findProjectGreaterThanCountCoChanges(project.getId(), getFileMinCoChange(project.getId()));
			for(CoChange cochange : cochangesWithOutDependsOn) {
				// 两个文件在不同的模块，并且两个文件之间没有依赖关系
				if(moduleService.isInDependence((ProjectFile) cochange.getNode1(), (ProjectFile) cochange.getNode2())) {
					fileImplicitCrossModuleDependencyList.add(new LogicCouplingComponents<>((ProjectFile) cochange.getNode1(), (ProjectFile) cochange.getNode2(), cochange.getTimes()));
				}
			}
		}
		for (LogicCouplingComponents<ProjectFile> fileImplicitCrossModuleDependency : fileImplicitCrossModuleDependencyList) {
			Project project1 = containRelationService.findFileBelongToProject(fileImplicitCrossModuleDependency.getNode1());
			Project project2 = containRelationService.findFileBelongToProject(fileImplicitCrossModuleDependency.getNode2());
			if (project1 != null && project2 != null && project1.getId().equals(project2.getId())) {
				List<LogicCouplingComponents<ProjectFile>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
				temp.add(fileImplicitCrossModuleDependency);
				result.put(project1.getId(), temp);
			}
		}
		return result;
	}

	@Override
	public Map<Long, List<LogicCouplingComponents<Package>>> detectPackageImplicitCrossModuleDependency() {
		Map<Long, List<LogicCouplingComponents<Package>>> result = new HashMap<>();
		List<LogicCouplingComponents<Package>> packageImplicitCrossModuleDependencyList = new ArrayList<>();
		List<Project> projects = projectRepository.queryAllProjects();
		for (Project project : projects) {
			List<Package> packages = packageRepository.getPackageByProjectId(project.getId());
			int length = packages.size();
			for (int i = 0; i < length; i ++) {
				long pckId1 = packages.get(i).getId();
				int fileNumber1 = projectFileRepository.getFilesNumberByPackageId(pckId1);
				for (int j = i + 1; j < length; j ++) {
					long pckId2 = packages.get(j).getId();
					int fileNumber2 = projectFileRepository.getFilesNumberByPackageId(pckId2);
					int fileCoChangeNumber = packageRepository.getCoChangeFileNumberByPackagesId(pckId1, pckId2);
					int fileDependOnNumber = packageRepository.getDependOnFileNumberByPackagesId(pckId1, pckId2);
					CoChange coChange = cochangeRepository.findPackageCoChangeByPackageId(pckId1, pckId2);
					if (coChange != null && coChange.getTimes() >= getPackageMinCoChange(project.getId()) && (fileCoChangeNumber) / (fileNumber1 + fileNumber2 + 0.0) > 0.5 && (coChange.getTimes() / (fileNumber1 + fileNumber2 + 0.0)) >= 1.5 && fileDependOnNumber == 0) {
						packageImplicitCrossModuleDependencyList.add(new LogicCouplingComponents<>(packages.get(i), packages.get(j), coChange.getTimes()));
					}
				}
			}
		}
		for (LogicCouplingComponents<Package> packageImplicitCrossModuleDependency : packageImplicitCrossModuleDependencyList) {
			Project project1 = containRelationService.findPackageBelongToProject(packageImplicitCrossModuleDependency.getNode1());
			Project project2 = containRelationService.findPackageBelongToProject(packageImplicitCrossModuleDependency.getNode2());
			if (project1 != null && project2 != null && project1.getId().equals(project2.getId())) {
				List<LogicCouplingComponents<Package>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
				temp.add(packageImplicitCrossModuleDependency);
				result.put(project1.getId(), temp);
			}
		}
		return result;
	}

	@Override
	public void setProjectFileMinCoChange(Long projectId, int minFileCoChange) {
		projectMinFileCoChange.put(projectId, minFileCoChange);
	}
	
	@Override
	public Integer getFileMinCoChange(Long projectId) {
		if (!projectMinFileCoChange.containsKey(projectId)) {
			Integer medFileCoChange = metricRepository.getMedFileCoChangeByProjectId(projectId);
			if (medFileCoChange != null) {
				projectMinFileCoChange.put(projectId, medFileCoChange);
			}
			else {
				projectMinFileCoChange.put(projectId, MIN_FILE_CO_CHANGE);
			}
		}
		return projectMinFileCoChange.get(projectId);
	}

	@Override
	public void setProjectPackageMinCoChange(Long projectId, int minPackageCoChange) {
		projectMinPackageCoChange.put(projectId, minPackageCoChange);
	}

	@Override
	public Integer getPackageMinCoChange(Long projectId) {
		if (!projectMinPackageCoChange.containsKey(projectId)) {
			Integer medPackageCoChange = metricRepository.getMedPackageCoChangeByProjectId(projectId);
			if (medPackageCoChange != null) {
				projectMinPackageCoChange.put(projectId, medPackageCoChange);
			}
			else {
				projectMinPackageCoChange.put(projectId, MIN_PACKAGE_CO_CHANGE);
			}
		}
		return projectMinPackageCoChange.get(projectId);
	}
}
