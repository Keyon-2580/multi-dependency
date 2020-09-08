package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikePackage;
import cn.edu.fudan.se.multidependency.service.query.metric.FanIOMetric;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculator;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class HubLikeComponentDetectorImpl implements HubLikeComponentDetector {
	
	@Autowired
	private MetricCalculator metricCalculator;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private CacheService cache;
	
	@SuppressWarnings("unused")
	@Autowired
	private ASRepository asRepository;

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
	public Map<Long, List<HubLikePackage>> hubLikePackages() {
		String key = "hubLikePackages";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, List<HubLikePackage>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), hubLikePackages(project));
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	public List<HubLikeFile> hubLikeFiles(Project project) {
		return hubLikeFiles(project, getProjectMinFileFanIO(project)[0], getProjectMinFileFanIO(project)[1]);
	}

	public List<HubLikePackage> hubLikePackages(Project project) {
		return hubLikePackages(project, getProjectMinFileFanIO(project)[0], getProjectMinFileFanIO(project)[1]);
	}

	public List<HubLikeFile> hubLikeFiles(Project project, int minFanIn, int minFanOut) {
		List<HubLikeFile> result = new ArrayList<>();
		if(project == null) {
			return result;
		}
		List<FileMetrics> fileMetrics = metricCalculator.calculateFileMetrics().get(project.getId());
		for(FileMetrics metric : fileMetrics) {
			if(isHubLikeComponent(metric, minFanIn, minFanOut)) {
				result.add(new HubLikeFile(metric.getFile(), metric.getFanOut(), metric.getFanIn()));
			}
		}
		result.sort((f1, f2) -> {
			return (f2.getFanIn() + f2.getFanOut()) - (f1.getFanIn() + f1.getFanOut());
		});
		return result;
		/*return asRepository.findHubLikeFiles(project.getId(), minFanIn, minFanOut);*/
	}
	
	public List<HubLikePackage> hubLikePackages(Project project, int minFanIn, int minFanOut) {
		List<HubLikePackage> result = new ArrayList<>();
		if(project == null) {
			return result;
		}
		List<PackageMetrics> packageMetrics = metricCalculator.calculatePackageMetrics().get(project.getId());
		for(PackageMetrics metric : packageMetrics) {
			if(isHubLikeComponent(metric, minFanIn, minFanOut)) {
				result.add(new HubLikePackage(metric.getPck(), metric.getFanOut(), metric.getFanIn(), metric.getLoc()));
			}
		}
		result.sort((p1, p2) -> {
			return (p2.getFanIn() + p2.getFanOut()) - (p1.getFanIn() + p1.getFanOut());
		});
		return result;
	}
	
	private boolean isHubLikeComponent(FanIOMetric metric, int minFanIn, int minFanOut) {
		return metric.getFanIn() >= minFanIn && metric.getFanOut() >= minFanOut;
	}
	
	Map<Project, int[]> projectMinFileFanIOs = new ConcurrentHashMap<>();
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

	Map<Project, int[]> projectMinPackageFanIOs = new ConcurrentHashMap<>();
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
	public void setProjectMinFileFanIO(Project project, int minFanIn, int minFanOut) {
		int[] result = new int[2];
		result[0] = minFanIn;
		result[1] = minFanOut;
		projectMinFileFanIOs.put(project, result);
		cache.remove(getClass());
	}

	@Override
	public void setProjectMinPackageFanIO(Project project, int minFanIn, int minFanOut) {
		int[] result = new int[2];
		result[0] = minFanIn;
		result[1] = minFanOut;
		projectMinPackageFanIOs.put(project, result);
		cache.remove(getClass());
	}

	private double defaultPackageMinFanIn(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
//		return FanIOMetric.calculateFanInUpperQuartile(metricCalculator.calculatePackageMetrics().get(project.getId()));
//		return metricCalculator.calculateProjectMetrics(false).get(project).getNop() / 5;
		return 5;
	}

	private double defaultPackageMinFanOut(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
//		return FanIOMetric.calculateFanOutUpperQuartile(metricCalculator.calculatePackageMetrics().get(project.getId()));
//		return metricCalculator.calculateProjectMetrics(false).get(project).getNop() / 5;
		return 5;
	}

	private double defaultFileMinFanIn(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
//		return FanIOMetric.calculateFanInUpperQuartile(metricCalculator.calculateFileMetrics().get(project.getId()));
//		return metricCalculator.calculateProjectMetrics(false).get(project.getId()).getNof() / 5;
		return 20;
	}

	private double defaultFileMinFanOut(Project project) {
		if(project == null) {
			return Integer.MAX_VALUE;
		}
//		return FanIOMetric.calculateFanOutUpperQuartile(metricCalculator.calculateFileMetrics().get(project.getId()));
//		return metricCalculator.calculateProjectMetrics(false).get(project.getId()).getNof() / 5;
		return 20;
	}
}
