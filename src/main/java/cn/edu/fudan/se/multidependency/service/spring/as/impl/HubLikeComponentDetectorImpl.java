package cn.edu.fudan.se.multidependency.service.spring.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.spring.metric.FanIOMetric;
import cn.edu.fudan.se.multidependency.service.spring.metric.PackageMetrics;

@Service
public class HubLikeComponentDetectorImpl implements HubLikeComponentDetector {
	
	@Autowired
	private PackageRepository packageRepository;
	
	@Autowired
	private ContainRelationService containRelationService;

	@Override
	public Collection<Package> hubLikePackages() {
		List<Package> result = new ArrayList<>();
		/*List<PackageMetrics> packageMetrics = packageRepository.calculatePackageMetrics();
		int fanOutMedian = calculateFanOutMedian(packageMetrics);
		int fanInMedian = calculateFanInMedian(packageMetrics);
		for(PackageMetrics metric : packageMetrics) {
			if(isHubLikeComponent(metric, fanOutMedian, fanInMedian)) {
				result.add(metric.getPck());
			}
		}*/
		Map<Project, List<Package>> projectToHubLikePackages = hubLikePackagesInDifferentProject();
		for(Map.Entry<Project, List<Package>> entry : projectToHubLikePackages.entrySet()) {
			result.addAll(entry.getValue());
		}
		return result;
	}
	
	public Map<Project, List<Package>> hubLikePackagesInDifferentProject() {
		Map<Project, List<Package>> result = new HashMap<>();
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
			List<Package> temp = new ArrayList<>();
			for(PackageMetrics metric : metrics) {
				if(isHubLikeComponent(metric, fanOutMedian, fanInMedian)) {
					temp.add(metric.getPck());
				}
			}
			result.put(project, temp);
		}
		
		return result;
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
	
	private boolean isHubLikeComponent(FanIOMetric metric, int fanOutMedian, int fanInMedian) {
		return (metric.fanIODValue() < metric.allFanIO() / 4.0) 
				&& (metric.getFanIn() > fanInMedian) 
				&& (metric.getFanOut() > fanOutMedian);
	}

}
