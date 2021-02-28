package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.smell.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeModule;
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
	private GitAnalyseService gitAnalyseService;

	Map<Project, int[]> projectMinFileFanIOs = new ConcurrentHashMap<>();
	Map<Project, int[]> projectMinModuleFanIOs = new ConcurrentHashMap<>();

	private Map<Project, int[]> projectMinFileCoChangeFilesAndTimesThresholds = new ConcurrentHashMap<>();
	private Map<Project, int[]> projectMinModuleCoChangeFilesAndTimesThresholds = new ConcurrentHashMap<>();

	public static final int DEFAULT_THRESHOLD_FAN_IN = 10;
	public static final int DEFAULT_THRESHOLD_FAN_OUT = 10;
	public static final int DEFAULT_THRESHOLD_COCHANGE_TIMES = 3;
	public static final int DEFAULT_THRESHOLD_COCHANGE_FILES = 5;

	@Override
	public Map<Long, List<HubLikeFile>> hubLikeFiles() {
		String key = "hubLikeFiles";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, List<HubLikeFile>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), hubLikeFiles(project));
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<HubLikeModule>> hubLikeModules() {
		String key = "hubLikeModules";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, List<HubLikeModule>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), hubLikeModules(project));
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	public List<HubLikeFile> hubLikeFiles(Project project) {
		return hubLikeFiles(project, getProjectMinFileFanIO(project)[0], getProjectMinFileFanIO(project)[1],
				getProjectMinFileCoChangeFilesAndTimesThreshold(project)[0], getProjectMinFileCoChangeFilesAndTimesThreshold(project)[1]);
	}

	public List<HubLikeModule> hubLikeModules(Project project) {
		return hubLikeModules(project, getProjectMinModuleFanIO(project)[0], getProjectMinModuleFanIO(project)[1],
				getProjectMinModuleCoChangeFilesAndTimesThreshold(project)[0], getProjectMinModuleCoChangeFilesAndTimesThreshold(project)[1]);
	}

	public List<HubLikeFile> hubLikeFiles(Project project, int minFanIn, int minFanOut, int coChangeFilesThreshold, int coChangeTimesThreshold) {
		List<HubLikeFile> result = new ArrayList<>();
		if(project == null) {
			return result;
		}
		List<FileMetrics> fileMetrics = metricCalculatorService.calculateProjectFileMetrics().get(project.getId());
		for(FileMetrics metric : fileMetrics) {
			if(isHubLikeComponent(metric, minFanIn, minFanOut)) {
				HubLikeFile hubLikeFile = new HubLikeFile(metric.getFile(), metric.getFanIn(), metric.getFanOut());
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
				hubLikeFile.addAllCoChangesWithFanIn(inCoChanges);

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
				hubLikeFile.addAllCoChangesWithFanOut(outCoChanges);

				if((inCoChangeFilesCount + outCoChangeFilesCount) >= coChangeFilesThreshold){
					result.add(hubLikeFile);
				}
			}
		}
		result.sort((f1, f2) -> {
			return (f2.getFanIn() + f2.getFanOut()) - (f1.getFanIn() + f1.getFanOut());
		});
		return result;
		/*return asRepository.findHubLikeFiles(project.getId(), minFanIn, minFanOut);*/
	}
	
	public List<HubLikeModule> hubLikeModules(Project project, int minFanIn, int minFanOut, int coChangeFilesThreshold, int coChangeTimesThreshold) {
		if(project == null) {
			return new ArrayList<>();
		}
		List<HubLikeModule> result = asRepository.findHubLikeModules(project.getId(), minFanIn, minFanOut);
		result.sort((p1, p2) -> {
			return (int) ((p2.getFanIn() + p2.getFanOut()) - (p1.getFanIn() + p1.getFanOut()));
		});
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
			result[0] = (int) defaultFileMinFanIn(project);
			result[1] = (int) defaultFileMinFanOut(project);
			projectMinFileFanIOs.put(project, result);
		}
		return result;
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
	public void setProjectMinFileFanIO(Project project, int minFanIn, int minFanOut) {
		int[] result = new int[2];
		result[0] = minFanIn;
		result[1] = minFanOut;
		projectMinFileFanIOs.put(project, result);
		cache.remove(getClass());
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

	private double defaultModuleMinFanIn(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
//		return FanIOMetric.calculateFanInUpperQuartile(metricCalculator.calculatePackageMetrics().get(project.getId()));
//		return metricCalculator.calculateProjectMetrics(false).get(project).getNop() / 5;
		return DEFAULT_THRESHOLD_FAN_IN/2.0;
	}

	private double defaultModuleMinFanOut(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
//		return FanIOMetric.calculateFanOutUpperQuartile(metricCalculator.calculatePackageMetrics().get(project.getId()));
//		return metricCalculator.calculateProjectMetrics(false).get(project).getNop() / 5;
		return DEFAULT_THRESHOLD_FAN_OUT/2.0;
	}

	private double defaultFileMinFanIn(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
//		return FanIOMetric.calculateFanInUpperQuartile(metricCalculator.calculateFileMetrics().get(project.getId()));
//		return metricCalculator.calculateProjectMetrics(false).get(project.getId()).getNof() / 5;
		return DEFAULT_THRESHOLD_FAN_IN;
	}

	private double defaultFileMinFanOut(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
//		return FanIOMetric.calculateFanOutUpperQuartile(metricCalculator.calculateFileMetrics().get(project.getId()));
//		return metricCalculator.calculateProjectMetrics(false).get(project.getId()).getNof() / 5;
		return DEFAULT_THRESHOLD_FAN_OUT;
	}
}
