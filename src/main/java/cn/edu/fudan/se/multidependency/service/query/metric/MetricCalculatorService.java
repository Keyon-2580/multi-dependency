package cn.edu.fudan.se.multidependency.service.query.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
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
public class MetricCalculatorService {

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

	@Autowired
	private MetricRepository metricRepository;
	
	@Resource(name="modularityCalculatorImplForFieldMethodLevel")
	private ModularityCalculator modularityCalculator;
	
	public Map<ProjectFile, FileMetrics> calculateFileMetrics() {
		String key = "calculateFileMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<ProjectFile, FileMetrics> result = new HashMap<>();
		List<FileMetrics.StructureMetric> fileStructureMetricsList = fileRepository.calculateFileStructureMetrics();
		if(fileStructureMetricsList != null && !fileStructureMetricsList.isEmpty()){
			fileStructureMetricsList.forEach(structureMetric -> {
				ProjectFile file = structureMetric.getFile();
				FileMetrics.EvolutionMetric fileEvolutionMetrics = fileRepository.calculateFileEvolutionMetrics(file.getId());

				FileMetrics fileMetrics = new FileMetrics();
				fileMetrics.setStructureMetric(structureMetric);
				fileMetrics.setEvolutionMetric(fileEvolutionMetrics);
				//计算不稳定度
				double instability = (structureMetric.getFanIn() + structureMetric.getFanOut()) == 0 ? -1 : (structureMetric.getFanOut() + 0.0) / (structureMetric.getFanIn() + structureMetric.getFanOut());
				fileMetrics.setInstability(instability);
				fileMetrics.setScore(file.getScore());
				result.put(file, fileMetrics);
			});
			cache.cache(getClass(), key, result);
		}

		return result;
	}
	
	public Map<Long, List<FileMetrics>> calculateFileMetricsWithProjectIdIndex() {
		String key = "calculateFileMetricsWithProjectIdIndex";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<FileMetrics>> result = new HashMap<>();
		Map<ProjectFile, FileMetrics> fileMetricsCache = new HashMap<>(calculateFileMetrics());
		if(fileMetricsCache != null && !fileMetricsCache.isEmpty()){
			fileMetricsCache.forEach((file,fileMetrics)->{
				Project project = containRelationService.findFileBelongToProject(file);
				List<FileMetrics> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileMetrics);
				result.put(project.getId(), temp);
			});
			cache.cache(getClass(), key, result);
		}

		return result;
	}
	
	public Collection<FileMetrics> calculateFileMetrics(Project project) {
		return calculateFileMetricsWithProjectIdIndex().get(project.getId());
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
		FileMetrics fileMetrics = new FileMetrics();
		FileMetrics.StructureMetric fileStructureMetrics = fileRepository.calculateFileStructureMetrics(file.getId());
		FileMetrics.EvolutionMetric fileEvolutionMetrics = fileRepository.calculateFileEvolutionMetrics(file.getId());
		fileMetrics.setStructureMetric(fileStructureMetrics);
		fileMetrics.setEvolutionMetric(fileEvolutionMetrics);
		return fileMetrics;
	}

	public PackageMetrics calculatePackageMetric(Package pck){
		return packageRepository.calculatePackageMetrics(pck.getId());
	}

	public Map<Long, ProjectMetrics> calculateProjectMetrics() {
		String key = "calculateProjectMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, ProjectMetrics> result = new HashMap<>();
		List<ProjectMetrics> projectMetricsList = projectRepository.calculateProjectMetrics();
		if(projectMetricsList != null && !projectMetricsList.isEmpty()) {
			projectMetricsList.forEach(projectMetrics -> {
				Project project = projectMetrics.getProject();
				int commitTimes = calculateProjectCommits(project);
				projectMetrics.setCommitTimes(commitTimes);
				result.put(project.getId(), projectMetrics);
			});
			cache.cache(getClass(), key, result);
		}

		return result;
	}

	public Map<String, List<ProjectMetrics>> calculateProjectMetricsByGitRepository() {
		String key = "calculateProjectMetricsByGitRepository";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<String, List<ProjectMetrics>> result = new HashMap<>();
		List<ProjectMetrics> projectMetricsList = projectRepository.calculateProjectMetrics();
		if(projectMetricsList != null && !projectMetricsList.isEmpty()) {
			projectMetricsList.forEach(projectMetrics -> {
				List<ProjectMetrics> projectMetricsTmp = result.getOrDefault(projectMetrics.getProject().getName(), new ArrayList<>());
				projectMetricsTmp.add(projectMetrics);
				result.put(projectMetrics.getProject().getName() , projectMetricsTmp);
			});
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
