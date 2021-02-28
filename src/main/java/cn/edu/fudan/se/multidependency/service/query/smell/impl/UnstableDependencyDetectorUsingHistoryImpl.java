package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableDependencyDetectorUsingHistory;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableFileInHistory;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class UnstableDependencyDetectorUsingHistoryImpl implements UnstableDependencyDetectorUsingHistory {

	private Map<Project, Integer> projectToFanInThreshold = new ConcurrentHashMap<>();
	private Map<Project, Integer> projectToCoChangeTimesThreshold = new ConcurrentHashMap<>();
	private Map<Project, Integer> projectToCoChangeFilesThreshold = new ConcurrentHashMap<>();
	
	public static final int DEFAULT_THRESHOLD_FAN_IN = 10;
	public static final int DEFAULT_THRESHOLD_COCHANGE_TIMES = 3;
	public static final int DEFAULT_THRESHOLD_COCHANGE_FILES = 5;
	
	public void setFanInThreshold(Project project, int threshold) {
		this.projectToFanInThreshold.put(project, threshold);
		cache.remove(getClass());
	}

	@Override
	public void setCoChangeTimesThreshold(Project project, int cochangeTimesThreshold) {
		this.projectToCoChangeTimesThreshold.put(project, cochangeTimesThreshold);
		cache.remove(getClass());
	}

	@Override
	public void setCoChangeFilesThreshold(Project project, int cochangeFilesThreshold) {
		this.projectToCoChangeFilesThreshold.put(project, cochangeFilesThreshold);
		cache.remove(getClass());
	}

	@Override
	public int getFanInThreshold(Project project) {
		if(projectToFanInThreshold.get(project) == null) {
			projectToFanInThreshold.put(project, DEFAULT_THRESHOLD_FAN_IN);
		}
		return projectToFanInThreshold.get(project);
	}

	@Override
	public int getCoChangeTimesThreshold(Project project) {
		if(projectToCoChangeTimesThreshold.get(project) == null) {
			projectToCoChangeTimesThreshold.put(project, DEFAULT_THRESHOLD_COCHANGE_TIMES);
		}
		return projectToCoChangeTimesThreshold.get(project);
	}

	@Override
	public int getCoChangeFilesThreshold(Project project) {
		if(projectToCoChangeFilesThreshold.get(project) == null) {
			projectToCoChangeFilesThreshold.put(project, DEFAULT_THRESHOLD_COCHANGE_FILES);
		}
		return projectToCoChangeFilesThreshold.get(project);
	}
	
	@Autowired
	private MetricCalculatorService metricCalculatorService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ProjectFileRepository fileRepository;
	
	@Autowired
	private GitAnalyseService gitAnalyseService;
	
	@Autowired
	private CacheService cache;
	
	@Override
	public Map<Long, List<UnstableFileInHistory>> unstableFiles() {
		String key = "unstableFiles";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<UnstableFileInHistory>> result = new HashMap<>();
		Collection<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			result.put(project.getId(), unstableFiles(project));
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	public List<UnstableFileInHistory> unstableFiles(Project project) {
		List<FileMetrics> fileMetrics = metricCalculatorService.calculateProjectFileMetrics().get(project.getId());
		List<UnstableFileInHistory> result = new ArrayList<>();
		for(FileMetrics metrics : fileMetrics) {
			UnstableFileInHistory unstableFile = isUnstableFile(project, metrics);
			if(unstableFile != null) {
				result.add(unstableFile);
			}
		}
		return result;
	}
	
	private UnstableFileInHistory isUnstableFile(Project project, FileMetrics metrics) {
		int fanInThreshold = getFanInThreshold(project);
		int coChangeTimesThreshold = getCoChangeTimesThreshold(project);
		int coChangeFilesThreshold = getCoChangeFilesThreshold(project);
		if(metrics.getFanIn() < fanInThreshold) {
			return null;
		}
		int coChangeFilesCount = 0;
		ProjectFile file = metrics.getFile();
		Collection<ProjectFile> fanInFiles = fileRepository.calculateFanIn(file.getId());
		List<CoChange> cochanges = new ArrayList<>();
		if(fanInFiles != null){
			for(ProjectFile dependedOnFile : fanInFiles) {
				// 遍历每个依赖File的文件，搜索协同修改次数
				CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(file, dependedOnFile);
				if(cochange != null && cochange.getTimes() >= coChangeTimesThreshold) {
					coChangeFilesCount++;
					cochanges.add(cochange);
				}
			}
		}
		UnstableFileInHistory result = null;
		if(coChangeFilesCount >= coChangeFilesThreshold) {
			result = new UnstableFileInHistory();
			result.setComponent(file);
			result.setFanIn(metrics.getFanIn());
			result.addAllCoChanges(cochanges);
		}
		return result;
	}

}
