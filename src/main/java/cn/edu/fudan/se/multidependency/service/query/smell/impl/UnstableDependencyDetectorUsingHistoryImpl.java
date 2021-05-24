package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableDependencyByHistory;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableDependencyDetectorUsingHistory;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class UnstableDependencyDetectorUsingHistoryImpl implements UnstableDependencyDetectorUsingHistory {

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

	private final Map<Long, Integer> projectToFanOutThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToCoChangeTimesThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Integer> projectToCoChangeFilesThresholdMap = new ConcurrentHashMap<>();
	private final Map<Long, Double> projectToMinRatioMap = new ConcurrentHashMap<>();

	public static final int DEFAULT_THRESHOLD_FAN_OUT = 10;
	public static final int DEFAULT_THRESHOLD_COCHANGE_TIMES = 3;
	public static final int DEFAULT_THRESHOLD_COCHANGE_FILES = 5;
	public static final double DEFAULT_MIN_RATIO = 0.5;

	@Override
	public void setFanOutThreshold(Long projectId, Integer minFanOut) {
		this.projectToFanOutThresholdMap.put(projectId, minFanOut);
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
	public Integer getFanOutThreshold(Long projectId) {
		if (!projectToFanOutThresholdMap.containsKey(projectId)) {
			Integer medFileFanOut = metricRepository.getMedFileFanInByProjectId(projectId);
			if (medFileFanOut != null) {
				projectToFanOutThresholdMap.put(projectId, medFileFanOut);
			}
			else {
				projectToFanOutThresholdMap.put(projectId, DEFAULT_THRESHOLD_FAN_OUT);
			}
//			if (projectToFanOutThresholdMap.get(projectId) < DEFAULT_THRESHOLD_FAN_OUT) {
//				projectToFanOutThresholdMap.put(projectId, DEFAULT_THRESHOLD_FAN_OUT);
//			}
		}
		return projectToFanOutThresholdMap.get(projectId);
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
	public Map<Long, List<UnstableDependencyByHistory>> queryUnstableDependency() {
		String key = "queryUnstableDependency";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<UnstableDependencyByHistory>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.UNSTABLE_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		for (Smell smell : smells) {
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			Iterator<Node> iterator = containedNodes.iterator();
			while (iterator.hasNext()) {
				ProjectFile component = (ProjectFile) iterator.next();
				Project project = containRelationService.findFileBelongToProject(component);
				UnstableDependencyByHistory unstableDependencyByHistory = new UnstableDependencyByHistory();
				unstableDependencyByHistory.setComponent(component);
				Collection<DependsOn> dependsOns = staticAnalyseService.findFileDependsOn(component);
				unstableDependencyByHistory.addAllFanOutDependencies(dependsOns);
				unstableDependencyByHistory.setFanOut(dependsOns.size());
				List<CoChange> allCoChanges = new ArrayList<>();
				for(DependsOn dependsOn : dependsOns) {
					ProjectFile fanOutFile = (ProjectFile) dependsOn.getEndNode();
					List<CoChange> coChanges = gitAnalyseService.findCoChangeBetweenTwoFilesWithoutDirection(component,fanOutFile);
					if(coChanges != null && !coChanges.isEmpty()) {
						Integer times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
//						if(times >= getCoChangeTimesThreshold(project.getId())){
//							allCoChanges.addAll(coChanges);
//						}
						if(times >= DEFAULT_THRESHOLD_COCHANGE_TIMES){
							allCoChanges.addAll(coChanges);
						}
					}
				}
				unstableDependencyByHistory.addAllCoChanges(allCoChanges);

				List<UnstableDependencyByHistory> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(unstableDependencyByHistory);
				result.put(project.getId(), temp);
			}
		}

		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnstableDependencyByHistory>> detectUnstableDependency() {
		Map<Long, List<UnstableDependencyByHistory>> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		if(projects != null && !projects.isEmpty()){
			for(Project project : projects) {
				List<UnstableDependencyByHistory> unstableDependencies = new ArrayList<>();
				List<ProjectFile> fileList = nodeService.queryAllFilesByProject(project.getId());
				for(ProjectFile file : fileList) {
					UnstableDependencyByHistory unstableInterfaceInFileLevel = isUnstableDependencyInFileLevel(project.getId(), file);
					if(unstableInterfaceInFileLevel != null) {
						unstableDependencies.add(unstableInterfaceInFileLevel);
					}
				}
				if(!unstableDependencies.isEmpty()){
					result.put(project.getId(), unstableDependencies);
				}
			}
		}
		return result;
	}

	private UnstableDependencyByHistory isUnstableDependencyInFileLevel(Long projectId, ProjectFile file) {
		Integer fanOutThreshold = getFanOutThreshold(projectId);
		Integer coChangeTimesThreshold = getCoChangeTimesThreshold(projectId);
		Integer coChangeFilesThreshold = getCoChangeFilesThreshold(projectId);
		Double minRatio = getProjectMinRatio(projectId);
		Collection<DependsOn> fanOutDependencies = staticAnalyseService.findFileDependsOn(file);
		if(fanOutDependencies.size() <= fanOutThreshold) {
			return null;
		}
		Integer coChangeFilesCount = 0;
		List<CoChange> allCoChanges = new ArrayList<>();
		if(fanOutDependencies != null){
			for(DependsOn dependedOn : fanOutDependencies) {
				// 遍历每个依赖File的文件，搜索协同修改次数
				ProjectFile fanInFile = (ProjectFile)dependedOn.getEndNode();
				List<CoChange> coChanges = gitAnalyseService.findCoChangeBetweenTwoFilesWithoutDirection(fanInFile,file);
				if(coChanges != null && !coChanges.isEmpty()) {
					Integer times = coChanges.stream().mapToInt(CoChange::getTimes).sum();
//					if(times >= getCoChangeTimesThreshold(project.getId())){
//							allCoChanges.addAll(coChanges);
//					}
					if(times >= DEFAULT_THRESHOLD_COCHANGE_TIMES){
						coChangeFilesCount++;
					}
					allCoChanges.addAll(coChanges);
				}
			}
		}
		UnstableDependencyByHistory result = null;
		if((coChangeFilesCount*1.0) / fanOutDependencies.size() >= minRatio) {
			result = new UnstableDependencyByHistory();
			result.setComponent(file);
			result.addAllFanOutDependencies(fanOutDependencies);
			result.setFanOut(fanOutDependencies.size() );
			result.addAllCoChanges(allCoChanges);
		}
		return result;
	}

}
