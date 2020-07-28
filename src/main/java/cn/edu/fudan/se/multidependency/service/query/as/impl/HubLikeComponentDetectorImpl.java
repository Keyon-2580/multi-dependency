package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.service.query.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.metric.FanIOMetric;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class HubLikeComponentDetectorImpl implements HubLikeComponentDetector {
	
	public static final int MIN_FILE_FAN_IN = 20;
	public static final int MIN_FILE_FAN_OUT = 20;
	public static final int MIN_PACKAGE_FAN_IN = 5;
	public static final int MIN_PACKAGE_FAN_OUT = 5;
	
	@Autowired
	private PackageRepository packageRepository;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private ProjectFileRepository fileRepository;

	@Override
	public Collection<PackageMetrics> hubLikePackages() {
		List<PackageMetrics> result = new ArrayList<>();
		Map<Project, List<PackageMetrics>> projectToHubLikePackages = hubLikePackagesInDifferentProject();
		for(Map.Entry<Project, List<PackageMetrics>> entry : projectToHubLikePackages.entrySet()) {
			result.addAll(entry.getValue());
		}
		return result;
	}
	
	public Map<Project, List<PackageMetrics>> hubLikePackagesInDifferentProject() {
		Map<Project, List<PackageMetrics>> result = new HashMap<>();
		List<PackageMetrics> packageMetrics = packageRepository.calculatePackageMetrics();
		Map<Project, List<PackageMetrics>> projectToPackageMetrics = new HashMap<>();
		for(PackageMetrics metric : packageMetrics) {
			Package pck = metric.getPck();
			Project project = containRelationService.findPackageBelongToProject(pck);
			List<PackageMetrics> temp = projectToPackageMetrics.getOrDefault(project, new ArrayList<>());
			temp.add(metric);
			projectToPackageMetrics.put(project, temp);
		}
		
		for(Map.Entry<Project, List<PackageMetrics>> entry : projectToPackageMetrics.entrySet()) {
			Project project = entry.getKey();
			List<PackageMetrics> metrics = entry.getValue();
			int fanOutMedian = calculateFanOutMedian(metrics);
			int fanInMedian = calculateFanInMedian(metrics);
			List<PackageMetrics> temp = new ArrayList<>();
			for(PackageMetrics metric : metrics) {
				if(isHubLikeComponent(metric, fanOutMedian, fanInMedian, MIN_PACKAGE_FAN_IN, MIN_PACKAGE_FAN_OUT)) {
					temp.add(metric);
				}
			}
			result.put(project, temp);
		}
		
		return result;
	}

	@Override
	public Collection<FileMetrics> hubLikeFiles() {
		List<FileMetrics> result = new ArrayList<>();
		Map<Project, List<FileMetrics>> projectToHubLikeFiles = hubLikeFilesInDifferentProject();
		for(Map.Entry<Project, List<FileMetrics>> entry : projectToHubLikeFiles.entrySet()) {
			result.addAll(entry.getValue());
		}
		return result;
	}

	public Map<Project, List<FileMetrics>> hubLikeFilesInDifferentProject() {
		Map<Project, List<FileMetrics>> result = new HashMap<>();
		List<FileMetrics> packageMetrics = fileRepository.calculateFileMetrics();
		Map<Project, List<FileMetrics>> projectToFileMetrics = new HashMap<>();
		for(FileMetrics metric : packageMetrics) {
			ProjectFile file = metric.getFile();
			Project project = containRelationService.findFileBelongToProject(file);
			List<FileMetrics> temp = projectToFileMetrics.getOrDefault(project, new ArrayList<>());
			temp.add(metric);
			projectToFileMetrics.put(project, temp);
		}
		
		for(Map.Entry<Project, List<FileMetrics>> entry : projectToFileMetrics.entrySet()) {
			Project project = entry.getKey();
			List<FileMetrics> metrics = entry.getValue();
			int fanOutMedian = calculateFanOutMedian(metrics);
			int fanInMedian = calculateFanInMedian(metrics);
			List<FileMetrics> temp = new ArrayList<>();
			for(FileMetrics metric : metrics) {
				if(isHubLikeComponent(metric, fanOutMedian, fanInMedian, MIN_FILE_FAN_IN, MIN_FILE_FAN_OUT)) {
					temp.add(metric);
				}
			}
			result.put(project, temp);
		}
		
		return result;
	}
	
	private boolean isHubLikeComponent(FanIOMetric metric, int fanOutMedian, int fanInMedian, int minFanIn, int minFanOut) {
		return ((metric.getFanIn() >= minFanIn) && (metric.getFanOut() >= minFanOut)) || ((metric.fanIODValue() < (metric.allFanIO() / 4.0)) 
				&& (metric.getFanIn() > fanInMedian) 
				&& (metric.getFanOut() > fanOutMedian));
	}
	
	private int calculateFanInMedian(Collection<? extends FanIOMetric> metrics) {
		List<? extends FanIOMetric> list = new ArrayList<>(metrics);
		list.sort((m1, m2) -> {
			return m1.getFanIn() - m2.getFanIn();
		});
		int size = list.size();
		if(list.size() % 2 == 0) {
			return (list.get((size - 1) / 2).getFanIn() + list.get(size / 2).getFanIn()) / 2;
		} else {
			return list.get(size / 2).getFanIn();
		}
	}
	
	private int calculateFanOutMedian(Collection<? extends FanIOMetric> metrics) {
		List<? extends FanIOMetric> list = new ArrayList<>(metrics);
		list.sort((m1, m2) -> {
			return m1.getFanOut() - m2.getFanOut();
		});
		int size = list.size();
		if(list.size() % 2 == 0) {
			return (list.get((size - 1) / 2).getFanOut() + list.get(size / 2).getFanOut()) / 2;
		} else {
			return list.get(size / 2).getFanOut();
		}
	}
}
