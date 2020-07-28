package cn.edu.fudan.se.multidependency.service.query;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.ProjectMetrics;

@Service
public class MetricCalculator {

	@Autowired
	private ProjectFileRepository fileRepository;
	
	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private PackageRepository packageRepository;
	
	@Autowired
	private GitAnalyseService gitAnalyseService;
	
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
		Collection<ProjectMetrics> result = projectRepository.calculateProjectMetrics();
		for(ProjectMetrics projectMetric : result) {
			Collection<Commit> commits = gitAnalyseService.findCommitsInProject(projectMetric.getProject());
			projectMetric.setCommitTimes(commits.size());
		}
		projectMetricsCache = result;
		return result;
	}
	
	private Collection<PackageMetrics> pckMetricsCache = null;
	public Collection<PackageMetrics> calculatePackageMetrics() {
		if(pckMetricsCache != null) {
			return pckMetricsCache;
		}
		return pckMetricsCache = packageRepository.calculatePackageMetrics();
	}
}
