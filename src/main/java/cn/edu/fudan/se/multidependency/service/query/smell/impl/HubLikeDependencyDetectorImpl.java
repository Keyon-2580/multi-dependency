package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellDetectorService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.smell.data.*;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.smell.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.HubLikeDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.metric.FanIOMetric;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class HubLikeDependencyDetectorImpl implements HubLikeDependencyDetector {

	@Autowired
	private MetricCalculatorService metricCalculatorService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private CacheService cache;

	@Autowired
	private ASRepository asRepository;

	@Autowired
	private ProjectFileRepository fileRepository;

	@Autowired
	private PackageRepository packageRepository;

	@Autowired
	private GitAnalyseService gitAnalyseService;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private SmellDetectorService smellDetectorService;

	public static final int DEFAULT_MIN_FILE_FAN_IN = 10;
	public static final int DEFAULT_MIN_FILE_FAN_OUT = 10;
	public static final int DEFAULT_MIN_PACKAGE_FAN_IN = 10;
	public static final int DEFAULT_MIN_PACKAGE_FAN_OUT = 10;
	public static final int DEFAULT_MIN_MODULE_FAN_IN = 10;
	public static final int DEFAULT_MIN_MODULE_FAN_OUT = 10;

	private final Map<Long, Integer[]> projectToMinFileFanIOMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer[]> projectToMinPackageFanIOMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer[]> projectToMinModuleFanIOMap = new ConcurrentHashMap<>();

	@Override
	public Map<Long, List<FileHubLike>> queryFileHubLikeDependency() {
		String key = "fileHubLikeDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<FileHubLike>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.HUBLIKE_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		List<FileHubLike> fileHubLikes = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			while (iterator.hasNext()) {
				ProjectFile component = (ProjectFile) iterator.next();
				FileHubLike fileHubLike = new FileHubLike(component, fileRepository.getFanIn(component.getId()), fileRepository.getFanOut(component.getId()));
//				Collection<ProjectFile> fanInFiles = fileRepository.calculateFanIn(component.getId());
//				List<CoChange> inCoChanges = new ArrayList<>();
//				if(fanInFiles != null && !fanInFiles.isEmpty()){
//					for(ProjectFile dependedByFile : fanInFiles) {
//						// 遍历每个依赖File的文件，搜索协同修改次数
//						CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(component, dependedByFile);
//						if(cochange != null) {
//							inCoChanges.add(cochange);
//						}
//					}
//				}
//				fileHubLike.addAllCoChangesWithFanIn(inCoChanges);
//
//				Collection<ProjectFile> fanOutFiles = fileRepository.calculateFanOut(component.getId());
//				List<CoChange> outCoChanges = new ArrayList<>();
//				if(fanOutFiles != null && !fanOutFiles.isEmpty()){
//					for(ProjectFile dependedOnFile : fanOutFiles) {
//						// 遍历每个依赖File的文件，搜索协同修改次数
//						CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(component, dependedOnFile);
//						if(cochange != null) {
//							outCoChanges.add(cochange);
//						}
//					}
//				}
//				fileHubLike.addAllCoChangesWithFanOut(outCoChanges);
				fileHubLikes.add(fileHubLike);
			}
		}
		for (FileHubLike fileHubLike : fileHubLikes) {
			Project project = containRelationService.findFileBelongToProject(fileHubLike.getFile());
			if (project != null) {
				List<FileHubLike> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileHubLike);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<PackageHubLike>> queryPackageHubLikeDependency() {
		String key = "packageHubLikeDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<PackageHubLike>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.PACKAGE, SmellType.HUBLIKE_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		List<PackageHubLike> packageHubLikes = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			while (iterator.hasNext()) {
				Package component = (Package) iterator.next();
				PackageHubLike packageHubLike = new PackageHubLike(component, packageRepository.getFanIn(component.getId()), packageRepository.getFanOut(component.getId()));
				packageHubLikes.add(packageHubLike);
			}
		}
		for (PackageHubLike packageHubLike : packageHubLikes) {
			Project project = containRelationService.findPackageBelongToProject(packageHubLike.getPck());
			if (project != null) {
				List<PackageHubLike> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(packageHubLike);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<ModuleHubLike>> queryModuleHubLikeDependency() {
		String key = "moduleHubLikeDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<ModuleHubLike>> result = new HashMap<>();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<FileHubLike>> detectFileHubLikeDependency() {
		List<Project> projects = nodeService.allProjects();
		Map<Long, List<FileHubLike>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), fileHubLikes(project));
		}
		return result;
	}

	@Override
	public Map<Long, List<PackageHubLike>> detectPackageHubLikeDependency() {
		List<Project> projects = nodeService.allProjects();
		Map<Long, List<PackageHubLike>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), packageHubLikes(project));
		}
		return result;
	}

	@Override
	public Map<Long, List<ModuleHubLike>> detectModuleHubLikeDependency() {
		List<Project> projects = nodeService.allProjects();
		Map<Long, List<ModuleHubLike>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), moduleHubLikes(project));
		}
		return result;
	}

	public List<FileHubLike> fileHubLikes(Project project) {
		return fileHubLikes(project, getProjectMinFileFanIO(project.getId())[0], getProjectMinFileFanIO(project.getId())[1]);
	}

	public List<PackageHubLike> packageHubLikes(Project project) {
		return packageHubLikes(project, getProjectMinPackageFanIO(project.getId())[0], getProjectMinPackageFanIO(project.getId())[1]);
	}

	public List<ModuleHubLike> moduleHubLikes(Project project) {
		return moduleHubLikes(project, getProjectMinModuleFanIO(project.getId())[0], getProjectMinModuleFanIO(project.getId())[1]);
	}

	public List<FileHubLike> fileHubLikes(Project project, int minFanIn, int minFanOut) {
		if(project == null) {
			return new ArrayList<>();
		}
		return asRepository.findFileHubLikes(project.getId(), minFanIn, minFanOut);
//		List<FileHubLike> result = new ArrayList<>();
//		if(project == null) {
//			return result;
//		}
//		List<FileMetrics> fileMetrics = metricCalculatorService.calculateProjectFileMetrics().get(project.getId());
//		for(FileMetrics metric : fileMetrics) {
//			if(isHubLikeComponent(metric, minFanIn, minFanOut)) {
//				FileHubLike fileHubLike = new FileHubLike(metric.getFile(), metric.getFanIn(), metric.getFanOut());
//				Collection<ProjectFile> fanInFiles = fileRepository.calculateFanIn(metric.getFile().getId());
//				int inCoChangeFilesCount = 0;
//				List<CoChange> inCoChanges = new ArrayList<>();
//				if(fanInFiles != null && !fanInFiles.isEmpty()){
//					for(ProjectFile dependedByFile : fanInFiles) {
//						// 遍历每个依赖File的文件，搜索协同修改次数
//						CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(metric.getFile(), dependedByFile);
//						if(cochange != null && cochange.getTimes() >= coChangeTimesThreshold) {
//							inCoChangeFilesCount++;
//							inCoChanges.add(cochange);
//						}
//					}
//				}
//				fileHubLike.addAllCoChangesWithFanIn(inCoChanges);
//
//				Collection<ProjectFile> fanOutFiles = fileRepository.calculateFanOut(metric.getFile().getId());
//				int outCoChangeFilesCount = 0;
//				List<CoChange> outCoChanges = new ArrayList<>();
//				if(fanOutFiles != null && !fanOutFiles.isEmpty()){
//					for(ProjectFile dependedOnFile : fanOutFiles) {
//						// 遍历每个依赖File的文件，搜索协同修改次数
//						CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(metric.getFile(), dependedOnFile);
//						if(cochange != null && cochange.getTimes() >= coChangeTimesThreshold) {
//							outCoChangeFilesCount++;
//							outCoChanges.add(cochange);
//						}
//					}
//				}
//				fileHubLike.addAllCoChangesWithFanOut(outCoChanges);
//
//				if((inCoChangeFilesCount + outCoChangeFilesCount) >= coChangeFilesThreshold){
//					result.add(fileHubLike);
//				}
//			}
//		}
//		return result;
	}

	public List<PackageHubLike> packageHubLikes(Project project, int minFanIn, int minFanOut) {
		if(project == null) {
			return new ArrayList<>();
		}
		return asRepository.findPackageHubLikes(project.getId(), minFanIn, minFanOut);
	}

	public List<ModuleHubLike> moduleHubLikes(Project project, int minFanIn, int minFanOut) {
		if(project == null) {
			return new ArrayList<>();
		}
		List<ModuleHubLike> result = asRepository.findModuleHubLikes(project.getId(), minFanIn, minFanOut);
		result.sort((p1, p2) -> (int) ((p2.getFanIn() + p2.getFanOut()) - (p1.getFanIn() + p1.getFanOut())));
		return result;
	}
	
	private boolean isHubLikeComponent(FanIOMetric metric, int minFanIn, int minFanOut) {
		return metric.getFanIn() > minFanIn && metric.getFanOut() > minFanOut;
	}

	@Override
	public void setProjectMinFileFanIO(Long projectId, Integer minFileFanIn, Integer minFileFanOut) {
		Integer[] result = new Integer[2];
		result[0] = minFileFanIn;
		result[1] = minFileFanOut;
		projectToMinFileFanIOMap.put(projectId, result);
		cache.remove(getClass());
	}

	@Override
	public void setProjectMinPackageFanIO(Long projectId, Integer minPackageFanIn, Integer minPackageFanOut) {
		Integer[] result = new Integer[2];
		result[0] = minPackageFanIn;
		result[1] = minPackageFanOut;
		projectToMinPackageFanIOMap.put(projectId, result);
		cache.remove(getClass());
	}

	@Override
	public void setProjectMinModuleFanIO(Long projectId, Integer minModuleFanIn, Integer minModuleFanOut) {
		Integer[] result = new Integer[2];
		result[0] = minModuleFanIn;
		result[1] = minModuleFanOut;
		projectToMinModuleFanIOMap.put(projectId, result);
		cache.remove(getClass());
	}

	@Override
	public Integer[] getProjectMinFileFanIO(Long projectId) {
		if (!projectToMinFileFanIOMap.containsKey(projectId)) {
			Integer[] result = new Integer[2];
			Integer medFileFanIn = metricRepository.getMedFileFanInByProjectId(projectId);
			Integer medFileFanOut = metricRepository.getMedFileFanOutByProjectId(projectId);
			if(medFileFanIn != null && medFileFanOut != null) {
				result[0] = medFileFanIn;
				result[1] = medFileFanOut;
			}
			else {
				result[0] = DEFAULT_MIN_FILE_FAN_IN;
				result[1] = DEFAULT_MIN_FILE_FAN_OUT;
			}
//			if (result[0] < DEFAULT_MIN_FILE_FAN_IN) {
//				result[0] = DEFAULT_MIN_FILE_FAN_IN;
//			}
//			if (result[1] < DEFAULT_MIN_FILE_FAN_OUT) {
//				result[1] = DEFAULT_MIN_FILE_FAN_OUT;
//			}
			projectToMinFileFanIOMap.put(projectId, result);
		}
		return projectToMinFileFanIOMap.get(projectId);
	}

	@Override
	public Integer[] getProjectMinPackageFanIO(Long projectId) {
		if (!projectToMinPackageFanIOMap.containsKey(projectId)) {
			Integer[] result = new Integer[2];
			Integer medPackageFanIn = metricRepository.getMedPackageFanInByProjectId(projectId);
			Integer medPackageFanOut = metricRepository.getMedPackageFanOutByProjectId(projectId);
			if(medPackageFanIn != null && medPackageFanOut != null) {
				result[0] = medPackageFanIn;
				result[1] = medPackageFanOut;
			}
			else {
				result[0] = DEFAULT_MIN_PACKAGE_FAN_IN;
				result[1] = DEFAULT_MIN_PACKAGE_FAN_OUT;
			}
//			if (result[0] < DEFAULT_MIN_PACKAGE_FAN_IN) {
//				result[0] = DEFAULT_MIN_PACKAGE_FAN_IN;
//			}
//			if (result[1] < DEFAULT_MIN_PACKAGE_FAN_OUT) {
//				result[1] = DEFAULT_MIN_PACKAGE_FAN_OUT;
//			}
			projectToMinPackageFanIOMap.put(projectId, result);
		}
		return projectToMinPackageFanIOMap.get(projectId);
	}

	@Override
	public Integer[] getProjectMinModuleFanIO(Long projectId) {
		if (!projectToMinModuleFanIOMap.containsKey(projectId)) {
			Integer[] result = new Integer[2];
			Integer medPackageFanIn = metricRepository.getMedPackageFanInByProjectId(projectId);
			Integer medPackageFanOut = metricRepository.getMedPackageFanOutByProjectId(projectId);
			if(medPackageFanIn != null && medPackageFanOut != null) {
				result[0] = medPackageFanIn;
				result[1] = medPackageFanOut;
			}
			else {
				result[0] = DEFAULT_MIN_MODULE_FAN_IN;
				result[1] = DEFAULT_MIN_MODULE_FAN_OUT;
			}
			projectToMinModuleFanIOMap.put(projectId, result);
		}
		return projectToMinModuleFanIOMap.get(projectId);
	}
}
