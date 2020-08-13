package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.as.UnstableDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstablePackage;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculator;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class UnstableDependencyDetectorImpl implements UnstableDependencyDetector {

	private Map<Project, Integer> projectToFanInThreshold = new ConcurrentHashMap<>();
	private Map<Project, Integer> projectToCoChangeTimesThreshold = new ConcurrentHashMap<>();
	private Map<Project, Integer> projectToCoChangeFilesThreshold = new ConcurrentHashMap<>();
	
	private UnstableFileDetectStrategy unstableFileDetectStrategy;
	
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
			projectToFanInThreshold.put(project, 20);
		}
		return projectToFanInThreshold.get(project);
	}

	@Override
	public int getCoChangeTimesThreshold(Project project) {
		if(projectToCoChangeTimesThreshold.get(project) == null) {
			projectToCoChangeTimesThreshold.put(project, 4);
		}
		return projectToCoChangeTimesThreshold.get(project);
	}

	@Override
	public int getCoChangeFilesThreshold(Project project) {
		if(projectToCoChangeFilesThreshold.get(project) == null) {
			projectToCoChangeFilesThreshold.put(project, 10);
		}
		return projectToCoChangeFilesThreshold.get(project);
	}
	
	@Autowired
	private MetricCalculator metricCalculator;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ProjectFileRepository fileRepository;
	
	@Autowired
	private GitAnalyseService gitAnalyseService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private CacheService cache;
	
	@Override
	public Map<Long, List<UnstableFile>> unstableFiles() {
		String key = "unstableFiles";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<UnstableFile>> result = new HashMap<>();
		Collection<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			result.put(project.getId(), unstableFiles(project));
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	public List<UnstableFile> unstableFiles(Project project) {
		List<FileMetrics> fileMetrics = metricCalculator.calculateFileMetrics().get(project.getId());
		List<UnstableFile> unstableFiles = new ArrayList<>();
		for(FileMetrics metrics : fileMetrics) {
			UnstableFile unstableFile = isUnstableFile(project, metrics);
			if(unstableFile != null) {
				unstableFiles.add(unstableFile);
			}
		}
		return unstableFiles;
	}
	
	private UnstableFile isUnstableFile(Project project, FileMetrics metrics) {
//		unstableFileDetectStrategy = new UnstableFileDetectStrategyUsingInstability(metricCalculator, staticAnalyseService, unstablePackageThreshold);
		unstableFileDetectStrategy = new UnstableFileDetectStrategyUsingCoChange(getFanInThreshold(project), 
				getCoChangeTimesThreshold(project), getCoChangeFilesThreshold(project), fileRepository, gitAnalyseService);
		return unstableFileDetectStrategy.isUnstableFile(project, metrics);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, List<UnstablePackage>> unstablePackages() {
		String key = "unstablePackages";
		if(cache.get(getClass(), key) != null) {
			return (Map<Long, List<UnstablePackage>>) cache.get(getClass(), key);
		}
		Map<Long, List<UnstablePackage>> result = new HashMap<>();
		Collection<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			List<UnstablePackage> temp = unstablePackages(project);
			result.put(project.getId(), temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	private double unstablePackageThreshold = 0.3;
	
	public List<UnstablePackage> unstablePackages(Project project) {
		List<UnstablePackage> result = new ArrayList<>();
		if(project == null) {
			return result;
		}
		List<PackageMetrics> packageMetrics = metricCalculator.calculatePackageMetrics().get(project.getId());
		Map<Package, PackageMetrics> metricsMap = new HashMap<>();
		for(PackageMetrics packageMetric : packageMetrics) {
			metricsMap.put(packageMetric.getPck(), packageMetric);
		}
		Map<Package, List<DependsOn>> pckToDependsOns = staticAnalyseService.findPackageDependsOn(project);
		for(Map.Entry<Package, List<DependsOn>> entry : pckToDependsOns.entrySet()) {
			Package pck = entry.getKey();
			List<DependsOn> badDependencies = new ArrayList<>();
			List<DependsOn> totalDependencies = new ArrayList<>();
			List<DependsOn> dependsOns = entry.getValue();
			for(DependsOn dependsOn : dependsOns) {
				Package dependsOnPackage = (Package) dependsOn.getEndNode();
				if(metricsMap.get(pck).getInstability() < metricsMap.get(dependsOnPackage).getInstability()) {
					badDependencies.add(dependsOn);
				}
				totalDependencies.add(dependsOn);
			}
			if(totalDependencies.isEmpty()) {
				continue;
			}
			double doUD = badDependencies.size() / (totalDependencies.size() + 0.0);
			if(doUD >= unstablePackageThreshold) {
				UnstablePackage unstablePackage = new UnstablePackage();
				unstablePackage.setPck(pck);
				unstablePackage.setMetrics(metricsMap.get(pck));
				unstablePackage.addAllBadDependencies(badDependencies);
				unstablePackage.addAllTotalDependencies(totalDependencies);
				result.add(unstablePackage);
			}
		}
		
		return result;
	}

}
