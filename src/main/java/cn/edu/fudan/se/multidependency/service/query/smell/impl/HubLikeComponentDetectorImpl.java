package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.smell.data.*;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.smell.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.metric.FanIOMetric;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class HubLikeComponentDetectorImpl implements HubLikeComponentDetector {
	
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

	Map<Project, int[]> projectMinFileFanIOs = new ConcurrentHashMap<>();
	Map<Project, int[]> projectMinPackageFanIOs = new ConcurrentHashMap<>();
	Map<Project, int[]> projectMinModuleFanIOs = new ConcurrentHashMap<>();

	Map<Project, int[]> projectMinFileCoChangeFilesAndTimesThresholds = new ConcurrentHashMap<>();
	Map<Project, int[]> projectMinPackageCoChangeFilesAndTimesThresholds = new ConcurrentHashMap<>();
	Map<Project, int[]> projectMinModuleCoChangeFilesAndTimesThresholds = new ConcurrentHashMap<>();

	public static final int DEFAULT_THRESHOLD_FAN_IN = 10;
	public static final int DEFAULT_THRESHOLD_FAN_OUT = 10;
	public static final int DEFAULT_THRESHOLD_COCHANGE_TIMES = 3;
	public static final int DEFAULT_THRESHOLD_COCHANGE_FILES = 5;

	@Override
	public Map<Long, List<FileHubLike>> queryFileHubLike() {
		String key = "fileHubLike";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<FileHubLike>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.HUBLIKE_DEPENDENCY));
		smells.sort((smell1, smell2) -> {
			List<String> namePart1 = Arrays.asList(smell1.getName().split("_"));
			List<String> namePart2 = Arrays.asList(smell2.getName().split("_"));
			int partition1 = Integer.parseInt(namePart1.get(namePart1.size() - 1));
			int partition2 = Integer.parseInt(namePart2.get(namePart2.size() - 1));
			return Integer.compare(partition1, partition2);
		});
		List<FileHubLike> fileHubLikes = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> contains = new HashSet<>(smellRepository.findSmellContains(smell.getId()));
			if (contains.iterator().hasNext()) {
				ProjectFile component = (ProjectFile) contains.iterator().next();
				FileHubLike fileHubLike = new FileHubLike(component, fileRepository.getFanIn(component.getId()), fileRepository.getFanOut(component.getId()));
				Collection<ProjectFile> fanInFiles = fileRepository.calculateFanIn(component.getId());
				List<CoChange> inCoChanges = new ArrayList<>();
				if(fanInFiles != null && !fanInFiles.isEmpty()){
					for(ProjectFile dependedByFile : fanInFiles) {
						// 遍历每个依赖File的文件，搜索协同修改次数
						CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(component, dependedByFile);
						if(cochange != null) {
							inCoChanges.add(cochange);
						}
					}
				}
				fileHubLike.addAllCoChangesWithFanIn(inCoChanges);

				Collection<ProjectFile> fanOutFiles = fileRepository.calculateFanOut(component.getId());
				List<CoChange> outCoChanges = new ArrayList<>();
				if(fanOutFiles != null && !fanOutFiles.isEmpty()){
					for(ProjectFile dependedOnFile : fanOutFiles) {
						// 遍历每个依赖File的文件，搜索协同修改次数
						CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(component, dependedOnFile);
						if(cochange != null) {
							outCoChanges.add(cochange);
						}
					}
				}
				fileHubLike.addAllCoChangesWithFanOut(outCoChanges);
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
	public Map<Long, List<PackageHubLike>> queryPackageHubLike() {
		String key = "packageHubLike";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<PackageHubLike>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.PACKAGE, SmellType.HUBLIKE_DEPENDENCY));
		smells.sort((smell1, smell2) -> {
			List<String> namePart1 = Arrays.asList(smell1.getName().split("_"));
			List<String> namePart2 = Arrays.asList(smell2.getName().split("_"));
			int partition1 = Integer.parseInt(namePart1.get(namePart1.size() - 1));
			int partition2 = Integer.parseInt(namePart2.get(namePart2.size() - 1));
			return Integer.compare(partition1, partition2);
		});
		List<PackageHubLike> packageHubLikes = new ArrayList<>();
		for (Smell smell : smells) {
			Set<Node> contains = new HashSet<>(smellRepository.findSmellContains(smell.getId()));
			if (contains.iterator().hasNext()) {
				Package component = (Package) contains.iterator().next();
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
	public Map<Long, List<ModuleHubLike>> queryModuleHubLike() {
		String key = "moduleHubLike";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<ModuleHubLike>> result = new HashMap<>();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<FileHubLike>> detectFileHubLike() {
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, List<FileHubLike>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), fileHubLikes(project));
		}
		return result;
	}

	@Override
	public Map<Long, List<PackageHubLike>> detectPackageHubLike() {
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, List<PackageHubLike>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), packageHubLikes(project));
		}
		return result;
	}

	@Override
	public Map<Long, List<ModuleHubLike>> detectModuleHubLike() {
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, List<ModuleHubLike>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), moduleHubLikes(project));
		}
		return result;
	}

	public List<FileHubLike> fileHubLikes(Project project) {
		return fileHubLikes(project, getProjectMinFileFanIO(project)[0], getProjectMinFileFanIO(project)[1],
				getProjectMinFileCoChangeFilesAndTimesThreshold(project)[0], getProjectMinFileCoChangeFilesAndTimesThreshold(project)[1]);
	}

	public List<PackageHubLike> packageHubLikes(Project project) {
		return packageHubLikes(project, getProjectMinPackageFanIO(project)[0], getProjectMinPackageFanIO(project)[1],
				getProjectMinPackageCoChangeFilesAndTimesThreshold(project)[0], getProjectMinPackageCoChangeFilesAndTimesThreshold(project)[1]);
	}

	public List<ModuleHubLike> moduleHubLikes(Project project) {
		return moduleHubLikes(project, getProjectMinModuleFanIO(project)[0], getProjectMinModuleFanIO(project)[1],
				getProjectMinModuleCoChangeFilesAndTimesThreshold(project)[0], getProjectMinModuleCoChangeFilesAndTimesThreshold(project)[1]);
	}

	public List<FileHubLike> fileHubLikes(Project project, int minFanIn, int minFanOut, int coChangeFilesThreshold, int coChangeTimesThreshold) {
		List<FileHubLike> result = new ArrayList<>();
		if(project == null) {
			return result;
		}
		List<FileMetrics> fileMetrics = metricCalculatorService.calculateProjectFileMetrics().get(project.getId());
		for(FileMetrics metric : fileMetrics) {
			if(isHubLikeComponent(metric, minFanIn, minFanOut)) {
				FileHubLike fileHubLike = new FileHubLike(metric.getFile(), metric.getFanIn(), metric.getFanOut());
				Collection<ProjectFile> fanInFiles = fileRepository.calculateFanIn(metric.getFile().getId());
				int inCoChangeFilesCount = 0;
				List<CoChange> inCoChanges = new ArrayList<>();
				if(fanInFiles != null && !fanInFiles.isEmpty()){
					for(ProjectFile dependedByFile : fanInFiles) {
						// 遍历每个依赖File的文件，搜索协同修改次数
						CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(metric.getFile(), dependedByFile);
						if(cochange != null && cochange.getTimes() >= coChangeTimesThreshold) {
							inCoChangeFilesCount++;
							inCoChanges.add(cochange);
						}
					}
				}
				fileHubLike.addAllCoChangesWithFanIn(inCoChanges);

				Collection<ProjectFile> fanOutFiles = fileRepository.calculateFanOut(metric.getFile().getId());
				int outCoChangeFilesCount = 0;
				List<CoChange> outCoChanges = new ArrayList<>();
				if(fanOutFiles != null && !fanOutFiles.isEmpty()){
					for(ProjectFile dependedOnFile : fanOutFiles) {
						// 遍历每个依赖File的文件，搜索协同修改次数
						CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(metric.getFile(), dependedOnFile);
						if(cochange != null && cochange.getTimes() >= coChangeTimesThreshold) {
							outCoChangeFilesCount++;
							outCoChanges.add(cochange);
						}
					}
				}
				fileHubLike.addAllCoChangesWithFanOut(outCoChanges);

				if((inCoChangeFilesCount + outCoChangeFilesCount) >= coChangeFilesThreshold){
					result.add(fileHubLike);
				}
			}
		}
		result.sort((f1, f2) -> (f2.getFanIn() + f2.getFanOut()) - (f1.getFanIn() + f1.getFanOut()));
		return result;
	}

	public List<PackageHubLike> packageHubLikes(Project project, int minFanIn, int minFanOut, int coChangeFilesThreshold, int coChangeTimesThreshold) {
		if(project == null) {
			return new ArrayList<>();
		}
		List<PackageHubLike> result = asRepository.findPackageHubLikes(project.getId(), minFanIn, minFanOut);
		result.sort((p1, p2) -> (int) ((p2.getFanIn() + p2.getFanOut()) - (p1.getFanIn() + p1.getFanOut())));
		return result;
	}

	public List<ModuleHubLike> moduleHubLikes(Project project, int minFanIn, int minFanOut, int coChangeFilesThreshold, int coChangeTimesThreshold) {
		if(project == null) {
			return new ArrayList<>();
		}
		List<ModuleHubLike> result = asRepository.findModuleHubLikes(project.getId(), minFanIn, minFanOut);
		result.sort((p1, p2) -> (int) ((p2.getFanIn() + p2.getFanOut()) - (p1.getFanIn() + p1.getFanOut())));
		return result;
	}
	
	private boolean isHubLikeComponent(FanIOMetric metric, int minFanIn, int minFanOut) {
		return metric.getFanIn() >= minFanIn && metric.getFanOut() >= minFanOut;
	}
	

	@Override
	public int[] getProjectMinFileFanIO(Project project) {
		int[] result = projectMinFileFanIOs.get(project);
		if(result == null) {
			result = new int[2];
//			result[0] = (int) defaultFileMinFanIn(project);
//			result[1] = (int) defaultFileMinFanOut(project);
			result[0] = metricRepository.getProjectMinFanInByProjectId(project.getId());
			result[1] = metricRepository.getProjectMinFanOutByProjectId(project.getId());
			projectMinFileFanIOs.put(project, result);
		}
		return result;
	}

	@Override
	public void setProjectMinFileFanIO(Project project, int minFanIn, int minFanOut) {
		int[] result = new int[2];
		result[0] = minFanIn;
		result[1] = minFanOut;
		projectMinFileFanIOs.put(project, result);
		cache.remove(getClass());
	}

	@Override
	public int[] getProjectMinPackageFanIO(Project project) {
		int[] result = projectMinPackageFanIOs.get(project);
		if(result == null) {
			result = new int[2];
			result[0] = (int) defaultPackageMinFanIn(project);
			result[1] = (int) defaultPackageMinFanOut(project);
			projectMinPackageFanIOs.put(project, result);
		}
		return result;
	}

	@Override
	public void setProjectMinPackageFanIO(Project project, int minFanIn, int minFanOut) {
		int[] result = new int[2];
		result[0] = minFanIn;
		result[1] = minFanOut;
		projectMinPackageFanIOs.put(project, result);
		cache.remove(getClass());
	}

	@Override
	public int[] getProjectMinModuleFanIO(Project project) {
		int[] result = projectMinModuleFanIOs.get(project);
		if(result == null) {
			result = new int[2];
			result[0] = (int) defaultModuleMinFanIn(project);
			result[1] = (int) defaultModuleMinFanOut(project);
			projectMinModuleFanIOs.put(project, result);
		}
		return result;
	}

	@Override
	public void setProjectMinModuleFanIO(Project project, int minFanIn, int minFanOut) {
		int[] result = new int[2];
		result[0] = minFanIn;
		result[1] = minFanOut;
		projectMinModuleFanIOs.put(project, result);
		cache.remove(getClass());
	}

	@Override
	public int[] getProjectMinFileCoChangeFilesAndTimesThreshold(Project project) {
		int[] result = projectMinFileCoChangeFilesAndTimesThresholds.get(project);
		if(result == null) {
			result = new int[2];
			result[0] = DEFAULT_THRESHOLD_COCHANGE_FILES;
			result[1] = DEFAULT_THRESHOLD_COCHANGE_TIMES;
			projectMinFileCoChangeFilesAndTimesThresholds.put(project, result);
		}
		return result;
	}

	@Override
	public void setProjectMinFileCoChangeFilesAndTimesThreshold(Project project, int coChangeFilesThreshold, int coChangeTimesThreshold) {
		int[] result = new int[2];
		result[0] = coChangeFilesThreshold;
		result[1] = coChangeTimesThreshold;
		projectMinFileCoChangeFilesAndTimesThresholds.put(project, result);
		cache.remove(getClass());
	}

	@Override
	public int[] getProjectMinPackageCoChangeFilesAndTimesThreshold(Project project) {
		int[] result = projectMinPackageCoChangeFilesAndTimesThresholds.get(project);
		if(result == null) {
			result = new int[2];
			result[0] = DEFAULT_THRESHOLD_COCHANGE_FILES;
			result[1] = DEFAULT_THRESHOLD_COCHANGE_TIMES;
			projectMinPackageCoChangeFilesAndTimesThresholds.put(project, result);
		}
		return result;
	}

	@Override
	public void setProjectMinPackageCoChangeFilesAndTimesThreshold(Project project, int coChangeFilesThreshold, int coChangeTimesThreshold) {
		int[] result = new int[2];
		result[0] = coChangeFilesThreshold;
		result[1] = coChangeTimesThreshold;
		projectMinPackageCoChangeFilesAndTimesThresholds.put(project, result);
		cache.remove(getClass());
	}

	@Override
	public int[] getProjectMinModuleCoChangeFilesAndTimesThreshold(Project project) {
		int[] result = projectMinModuleCoChangeFilesAndTimesThresholds.get(project);
		if(result == null) {
			result = new int[2];
			result[0] = DEFAULT_THRESHOLD_COCHANGE_FILES;
			result[1] = DEFAULT_THRESHOLD_COCHANGE_TIMES;
			projectMinModuleCoChangeFilesAndTimesThresholds.put(project, result);
		}
		return result;
	}

	@Override
	public void setProjectMinModuleCoChangeFilesAndTimesThreshold(Project project, int coChangeFilesThreshold, int coChangeTimesThreshold) {
		int[] result = new int[2];
		result[0] = coChangeFilesThreshold;
		result[1] = coChangeTimesThreshold;
		projectMinModuleCoChangeFilesAndTimesThresholds.put(project, result);
		cache.remove(getClass());
	}

	private double defaultFileMinFanIn(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
		return DEFAULT_THRESHOLD_FAN_IN;
	}

	private double defaultFileMinFanOut(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
		return DEFAULT_THRESHOLD_FAN_OUT;
	}

	private double defaultPackageMinFanIn(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
		return DEFAULT_THRESHOLD_FAN_IN;
	}

	private double defaultPackageMinFanOut(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
		return DEFAULT_THRESHOLD_FAN_OUT;
	}

	private double defaultModuleMinFanIn(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
		return DEFAULT_THRESHOLD_FAN_IN/2.0;
	}

	private double defaultModuleMinFanOut(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
		return DEFAULT_THRESHOLD_FAN_OUT/2.0;
	}
}
