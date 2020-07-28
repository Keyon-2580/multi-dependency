package cn.edu.fudan.se.multidependency.service.query.metric;

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;

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
	
	@Resource(name="modularityCalculatorImplForFieldMethodLevel")
	private ModularityCalculator modularityCalculator;
	
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
			projectMetric.setModularity(modularityCalculator.calculate(projectMetric.getProject()).getValue());
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
