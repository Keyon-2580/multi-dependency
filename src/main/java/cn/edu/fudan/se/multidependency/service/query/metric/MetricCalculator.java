package cn.edu.fudan.se.multidependency.service.query.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

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
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private CacheService cache;
	
	@Resource(name="modularityCalculatorImplForFieldMethodLevel")
	private ModularityCalculator modularityCalculator;
	
	public Map<ProjectFile, FileMetrics> calculateCommitFileMetrics() {
		String key = "calculateCommitFileMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<ProjectFile, FileMetrics> result = new HashMap<>();
		List<FileMetrics> metrics = fileRepository.calculateFileMetricsWithCoChangeCommitTimes();
		for(FileMetrics metric : metrics) {
			result.put(metric.getFile(), metric);
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	public Map<Long, List<FileMetrics>> calculateFileMetrics() {
		String key = "calculateFileMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<FileMetrics>> result = new HashMap<>();
		Map<ProjectFile, FileMetrics> commitFileMetricsCache = calculateCommitFileMetrics();
		for(FileMetrics fileMetrics : fileRepository.calculateFileMetrics()) {
			ProjectFile file = fileMetrics.getFile();
			if(commitFileMetricsCache.get(file) != null) {
				fileMetrics.setCochangeCommitTimes(commitFileMetricsCache.get(file).getCochangeCommitTimes());
			}
			Project project = containRelationService.findFileBelongToProject(file);
			List<FileMetrics> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(fileMetrics);
			result.put(project.getId(), temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	public Collection<FileMetrics> calculateFileMetrics(Project project) {
		return calculateFileMetrics().get(project.getId());
	}
	
	public Collection<PackageMetrics> calculatePackageMetrics(Project project) {
		return calculatePackageMetrics().get(project.getId());
	}
	
	public Map<Long, List<PackageMetrics>> calculatePackageMetrics() {
		String key = "calculatePackageMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<PackageMetrics>> result = new HashMap<>();
		for(PackageMetrics pckMetrics : packageRepository.calculatePackageMetrics()) {
			Package pck = pckMetrics.getPck();
			Project project = containRelationService.findPackageBelongToProject(pck);
			List<PackageMetrics> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(pckMetrics);
			result.put(project.getId(), temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	public Collection<ProjectFile> calculateFanIn(ProjectFile file) {
		return fileRepository.calculateFanIn(file.getId());
	}
	
	public Collection<ProjectFile> calculateFanOut(ProjectFile file) {
		return fileRepository.calculateFanOut(file.getId());
	}
	
	public FileMetrics calculateFileMetric(ProjectFile file) {
		FileMetrics fileMetrics = fileRepository.calculateFileMetrics(file.getId());
		Map<ProjectFile, FileMetrics> commitFileMetricsCache = calculateCommitFileMetrics();
		if(commitFileMetricsCache.get(file) != null) {
			fileMetrics.setCochangeCommitTimes(commitFileMetricsCache.get(file).getCochangeCommitTimes());
		}
		return fileMetrics;
	}

	public Map<Long, ProjectMetrics> calculateProjectMetrics(boolean calculateModularityAndCommits) {
		String key = "calculateProjectMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, ProjectMetrics> result = new HashMap<>();
		for(ProjectMetrics projectMetric : projectRepository.calculateProjectMetrics()) {
			if(calculateModularityAndCommits) {
				projectMetric.setCommitTimes(calculateProjectCommits(projectMetric.getProject()));
				projectMetric.setModularity(calculateProjectModularity(projectMetric.getProject()));
			}
			result.put(projectMetric.getProject().getId(), projectMetric);
		}
		if(calculateModularityAndCommits) {
			cache.cache(getClass(), key, result);
		}
		return result;
	}
	
	public double calculateProjectModularity(Project project) {
		return modularityCalculator.calculate(project).getValue();
	}
	
	public int calculateProjectCommits(Project project) {
		return gitAnalyseService.findCommitsInProject(project).size();
	}
}
