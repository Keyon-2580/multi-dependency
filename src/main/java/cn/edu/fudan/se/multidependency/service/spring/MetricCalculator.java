package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.spring.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.spring.metric.PackageMetrics;
import cn.edu.fudan.se.multidependency.service.spring.metric.ProjectMetrics;

@Service
public class MetricCalculator {

	@Autowired
	private ProjectFileRepository fileRepository;
	
	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private PackageRepository packageRepository;
	
	private Collection<FileMetrics> fileMetricsCache = null;
	public Collection<FileMetrics> calculateFileMetrics() {
		if(fileMetricsCache != null) {
			return fileMetricsCache;
		}
		return fileMetricsCache = fileRepository.calculateFileMetrics();
	}

	private Collection<ProjectMetrics> projectMetricsCache = null;
	public Collection<ProjectMetrics> calculateProjectMetrics() {
		if(projectMetricsCache != null) {
			return projectMetricsCache;
		}
		return projectMetricsCache = projectRepository.calculateProjectMetrics();
	}
	
	private Collection<PackageMetrics> pckMetricsCache = null;
	public Collection<PackageMetrics> calculatePackageMetrics() {
		if(pckMetricsCache != null) {
			return pckMetricsCache;
		}
		return pckMetricsCache = packageRepository.calculatePackageMetrics();
	}
}
