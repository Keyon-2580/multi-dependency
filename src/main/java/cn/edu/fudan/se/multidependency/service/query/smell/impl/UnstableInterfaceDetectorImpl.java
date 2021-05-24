package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableInterfaceDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableInterface;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UnstableInterfaceDetectorImpl implements UnstableInterfaceDetector {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private StaticAnalyseService staticAnalyseService;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private GitAnalyseService gitAnalyseService;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private CacheService cache;

	private final Map<Long, Integer> projectToFanInThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToCoChangeTimesThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToCoChangeFilesThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Double> projectToMinRatioMap = new ConcurrentHashMap<>();
	
	public static final int DEFAULT_THRESHOLD_FAN_IN = 10;
	public static final int DEFAULT_THRESHOLD_COCHANGE_TIMES = 3;
	public static final int DEFAULT_THRESHOLD_COCHANGE_FILES = 5;
	public static final double DEFAULT_MIN_RATIO = 0.5;

	@Override
	public void setFanInThreshold(Long projectId, Integer minFanIn) {
		this.projectToFanInThresholdMap.put(projectId, minFanIn);
		cache.remove(getClass());
	}

	@Override
	public void setCoChangeTimesThreshold(Long projectId, Integer cochangeTimesThreshold) {
		this.projectToCoChangeTimesThresholdMap.put(projectId, cochangeTimesThreshold);
		cache.remove(getClass());
	}

	@Override
	public void setCoChangeFilesThreshold(Long projectId, Integer cochangeFilesThreshold) {
		this.projectToCoChangeFilesThresholdMap.put(projectId, cochangeFilesThreshold);
		cache.remove(getClass());
	}

	@Override
	public void setProjectMinRatio(Long projectId, Double minRatio) {
		this.projectToMinRatioMap.put(projectId, minRatio);
		cache.remove(getClass());
	}

	@Override
	public Integer getFanInThreshold(Long projectId) {
		if (!projectToFanInThresholdMap.containsKey(projectId)) {
			Integer medFileFanIn = metricRepository.getMedFileFanInByProjectId(projectId);
			if (medFileFanIn != null) {
				projectToFanInThresholdMap.put(projectId, medFileFanIn);
			}
			else {
				projectToFanInThresholdMap.put(projectId, DEFAULT_THRESHOLD_FAN_IN);
			}
//			if (projectToFanInThresholdMap.get(projectId) < DEFAULT_THRESHOLD_FAN_IN) {
//				projectToFanInThresholdMap.put(projectId, DEFAULT_THRESHOLD_FAN_IN);
//			}
		}
		return projectToFanInThresholdMap.get(projectId);
	}

	@Override
	public Integer getCoChangeTimesThreshold(Long projectId) {
		if (!projectToCoChangeTimesThresholdMap.containsKey(projectId)) {
			Integer medFileCoChangeTimes = metricRepository.getMedFileCoChangeByProjectId(projectId);
			if (medFileCoChangeTimes != null) {
				projectToCoChangeTimesThresholdMap.put(projectId, medFileCoChangeTimes);
			}
			else {
				projectToCoChangeTimesThresholdMap.put(projectId, DEFAULT_THRESHOLD_COCHANGE_TIMES);
			}
//			if (projectToCoChangeTimesThresholdMap.get(projectId) < DEFAULT_THRESHOLD_COCHANGE_TIMES) {
//				projectToCoChangeTimesThresholdMap.put(projectId, DEFAULT_THRESHOLD_COCHANGE_TIMES);
//			}
		}
		return projectToCoChangeTimesThresholdMap.get(projectId);
	}

	@Override
	public Integer getCoChangeFilesThreshold(Long projectId) {
		if(projectToCoChangeFilesThresholdMap.get(projectId) == null) {
			projectToCoChangeFilesThresholdMap.put(projectId, DEFAULT_THRESHOLD_COCHANGE_FILES);
		}
		return projectToCoChangeFilesThresholdMap.get(projectId);
	}

	@Override
	public Double getProjectMinRatio(Long projectId) {
		if (!projectToMinRatioMap.containsKey(projectId)) {
			projectToMinRatioMap.put(projectId, DEFAULT_MIN_RATIO);
		}
		return projectToMinRatioMap.get(projectId);
	}

	@Override
	public Map<Long, List<UnstableInterface>> queryUnstableInterface() {
		String key = "queryUnstableInterface";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<UnstableInterface>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.UNSTABLE_INTERFACE));
		SmellUtils.sortSmellByName(smells);
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			while (iterator.hasNext()) {
				ProjectFile component = (ProjectFile) iterator.next();
				Project project = containRelationService.findFileBelongToProject(component);
				UnstableInterface unstableInterface = new UnstableInterface();
				unstableInterface.setComponent(component);
				Collection<DependsOn> dependsOnBys = staticAnalyseService.findFileDependedOnBy(component);
				unstableInterface.addAllFanInDependencies(dependsOnBys);
				unstableInterface.setFanIn(dependsOnBys.size());
				List<CoChange> allCoChanges = new ArrayList<>();
				for(DependsOn dependsOn : dependsOnBys) {
					ProjectFile fanInFile = (ProjectFile) dependsOn.getStartNode();
					List<CoChange> coChanges = gitAnalyseService.findCoChangeBetweenTwoFilesWithoutDirection(fanInFile,component);
					if(coChanges != null && !coChanges.isEmpty()) {
						Integer times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
						if(times >= getCoChangeTimesThreshold(project.getId())){
							allCoChanges.addAll(coChanges);
						}
					}
				}
				unstableInterface.addAllCoChanges(allCoChanges);

				List<UnstableInterface> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(unstableInterface);
				result.put(project.getId(), temp);
			}
		}

		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableInterface>> detectUnstableInterface() {
		Map<Long, List<UnstableInterface>> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		if(projects != null && !projects.isEmpty()){
			for(Project project : projects) {
				List<UnstableInterface> unstableInterfaces = new ArrayList<>();
				List<ProjectFile> fileList = nodeService.queryAllFilesByProject(project.getId());
				for(ProjectFile file : fileList) {
					UnstableInterface unstableFile = isUnstableInterfaceInFileLevel(project.getId(), file);
					if(unstableFile != null) {
						unstableInterfaces.add(unstableFile);
					}
				}
				if(!unstableInterfaces.isEmpty()){
					result.put(project.getId(), unstableInterfaces);
				}
			}
		}
		return result;
	}
	
	private UnstableInterface isUnstableInterfaceInFileLevel(Long projectId, ProjectFile file) {
		Integer fanInThreshold = getFanInThreshold(projectId);
		Integer coChangeTimesThreshold = getCoChangeTimesThreshold(projectId);
		Integer coChangeFilesThreshold = getCoChangeFilesThreshold(projectId);
		Double minRatio = getProjectMinRatio(projectId);
		Collection<DependsOn> fanInDependencies = staticAnalyseService.findFileDependedOnBy(file);
		if(fanInDependencies.size() <= fanInThreshold) {
			return null;
		}
		Integer coChangeFilesCount = 0;
		List<CoChange> allCoChanges = new ArrayList<>();
		if(fanInDependencies != null){
			for(DependsOn dependedOnBy : fanInDependencies) {
				// 遍历每个依赖File的文件，搜索协同修改次数
				ProjectFile fanInFile = (ProjectFile)dependedOnBy.getStartNode();
				List<CoChange> coChanges = gitAnalyseService.findCoChangeBetweenTwoFilesWithoutDirection(fanInFile,file);
				if(coChanges != null && !coChanges.isEmpty()) {
					Integer times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
					if(times >= DEFAULT_THRESHOLD_COCHANGE_TIMES){
						coChangeFilesCount++;
					}
					allCoChanges.addAll(coChanges);
				}
			}
		}
		UnstableInterface result = null;
		if((coChangeFilesCount*1.0) / fanInDependencies.size() >= minRatio) {
			result = new UnstableInterface();
			result.setComponent(file);
			result.addAllFanInDependencies(fanInDependencies);
			result.setFanIn(fanInDependencies.size() );
			result.addAllCoChanges(allCoChanges);
		}
		return result;
	}

}
