package cn.edu.fudan.se.multidependency.service.query.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.edu.fudan.se.multidependency.model.MetricType;
import cn.edu.fudan.se.multidependency.model.node.Metric;
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

	public Map<Long, Metric> generateFileMetricNodes(){
		Map<Long, Metric> result = new HashMap<>();
		Map<Long, FileMetrics> fileFileMetricsMap = calculateFileMetrics();
		if(fileFileMetricsMap != null && !fileFileMetricsMap.isEmpty()){
			fileFileMetricsMap.forEach((fileId, fileMetrics) ->{
				ProjectFile file = fileRepository.findFileById(fileId);
				Metric metric = new Metric();
				metric.setEntityId((long) -1);
				metric.setLanguage(file.getLanguage());
				metric.setName(file.getName());
				metric.setNodeType(file.getNodeType());

				Map<String, Object> metricValues =  new HashMap<>();
				metricValues.put(MetricType.NOC, fileMetrics.getStructureMetric().getNoc());
				metricValues.put(MetricType.NOM, fileMetrics.getStructureMetric().getNom());
				metricValues.put(MetricType.LOC, fileMetrics.getStructureMetric().getLoc());
				metricValues.put(MetricType.FAN_OUT, fileMetrics.getStructureMetric().getFanOut());
				metricValues.put(MetricType.FAN_IN, fileMetrics.getStructureMetric().getFanIn());

				FileMetrics.EvolutionMetric evolutionMetric = fileMetrics.getEvolutionMetric();
				if (evolutionMetric != null){
					metricValues.put(MetricType.COMMITS, evolutionMetric.getCommits());
					metricValues.put(MetricType.DEVELOPERS, evolutionMetric.getDevelopers());
					metricValues.put(MetricType.CO_CHANGE_FILES, evolutionMetric.getCoChangeFiles());
//					metricValues.put(MetricType.CO_CHANGE_COMMIT_TIMES, evolutionMetric.getCoChangeCommitTimes());
				}

				FileMetrics.DebtMetric detMetric = fileMetrics.getDebtMetric();
				if(detMetric != null){
					metricValues.put(MetricType.ISSUES, detMetric.getIssues());
					metricValues.put(MetricType.BUG_ISSUES, detMetric.getBugIssues());
					metricValues.put(MetricType.NEW_FEATURE_ISSUES, detMetric.getNewFeatureIssues());
					metricValues.put(MetricType.IMPROVEMENT_ISSUES, detMetric.getImprovementIssues());
				}

				metric.setMetricValues(metricValues);
				result.put(file.getId(), metric);
			});
		}

		return result;
	}

	public Map<Long, Metric> generatePackageMetricNodes(){
		Map<Long, Metric> result = new HashMap<>();
		Map<Long, PackageMetrics> packageMetricsMap = calculatePackageMetrics();
		if(packageMetricsMap != null && !packageMetricsMap.isEmpty()){
			packageMetricsMap.forEach((pckId, packageMetrics) ->{
				Package pck = packageRepository.findPackageById(pckId);
				Metric metric = new Metric();
				metric.setEntityId((long) -1);
				metric.setLanguage(pck.getLanguage());
				metric.setName(pck.getName());
				metric.setNodeType(pck.getNodeType());

				Map<String, Object> metricValues =  new HashMap<>();
				metricValues.put(MetricType.NOF, packageMetrics.getNof());
				metricValues.put(MetricType.NOC, packageMetrics.getNoc());
				metricValues.put(MetricType.NOM, packageMetrics.getNom());
				metricValues.put(MetricType.LOC, packageMetrics.getLoc());
				metricValues.put(MetricType.FAN_OUT, packageMetrics.getFanOut());
				metricValues.put(MetricType.FAN_IN, packageMetrics.getFanIn());
				metricValues.put(MetricType.LINES, packageMetrics.getLines());
				metric.setMetricValues(metricValues);

				result.put(pck.getId(), metric);
			});
		}

		return result;
	}

	public Map<Long, Metric> generateProjectMetricNodes(){
		Map<Long, Metric> result = new HashMap<>();
		Map<Long, ProjectMetrics> projectMetricsMap = calculateProjectMetrics();
		if(projectMetricsMap != null && !projectMetricsMap.isEmpty()){
			projectMetricsMap.forEach((pck, projectMetrics) ->{
				Metric metric = new Metric();
				metric.setEntityId((long) -1);
				metric.setLanguage(projectMetrics.getProject().getLanguage());
				metric.setName(projectMetrics.getProject().getName());
				metric.setNodeType(projectMetrics.getProject().getNodeType());

				Map<String, Object> metricValues =  new HashMap<>();
				metricValues.put(MetricType.NOP, projectMetrics.getNop());
				metricValues.put(MetricType.NOF, projectMetrics.getNof());
				metricValues.put(MetricType.NOC, projectMetrics.getNoc());
				metricValues.put(MetricType.NOM, projectMetrics.getNom());
				metricValues.put(MetricType.LOC, projectMetrics.getLoc());
				metricValues.put(MetricType.LINES, projectMetrics.getLines());
				metricValues.put(MetricType.COMMITS, projectMetrics.getCommits());
				metricValues.put(MetricType.MODULARITY, projectMetrics.getModularity());

				metric.setMetricValues(metricValues);

				result.put(projectMetrics.getProject().getId(), metric);
			});
		}

		return result;
	}
	
	public Map<Long, FileMetrics> calculateFileMetrics() {
		String key = "calculateFileMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, FileMetrics> result = new HashMap<>();
		List<FileMetrics.StructureMetric> fileStructureMetricsList = fileRepository.calculateFileStructureMetrics();
		if(fileStructureMetricsList != null && !fileStructureMetricsList.isEmpty()){
			fileStructureMetricsList.forEach(structureMetric -> {
				FileMetrics fileMetrics = new FileMetrics();

				ProjectFile file = structureMetric.getFile();
				fileMetrics.setFile(file);
				FileMetrics.EvolutionMetric fileEvolutionMetrics = fileRepository.calculateFileEvolutionMetrics(file.getId());
				FileMetrics.DebtMetric fileDebtMetrics = fileRepository.calculateFileDebtMetrics(file.getId());

				fileMetrics.setStructureMetric(structureMetric);
				fileMetrics.setEvolutionMetric(fileEvolutionMetrics);
				fileMetrics.setDebtMetric(fileDebtMetrics);

				fileMetrics.setFanIn(structureMetric.getFanIn());
				fileMetrics.setFanOut(structureMetric.getFanOut());
				//计算不稳定度
				double instability = (structureMetric.getFanIn() + structureMetric.getFanOut()) == 0 ? -1 : (structureMetric.getFanOut() + 0.0) / (structureMetric.getFanIn() + structureMetric.getFanOut());
				fileMetrics.setInstability(instability);
				fileMetrics.setPageRankScore(file.getScore());
				result.put(file.getId(), fileMetrics);
			});
			cache.cache(getClass(), key, result);
		}

		return result;
	}
	
	public Map<Long, List<FileMetrics>> calculateProjectFileMetrics() {
		String key = "calculateProjectFileMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<FileMetrics>> result = new HashMap<>();
		Map<Long, FileMetrics> fileMetricsCache = new HashMap<>(calculateFileMetrics());
		if(fileMetricsCache != null && !fileMetricsCache.isEmpty()){
			fileMetricsCache.forEach((fileId, fileMetrics)->{
				ProjectFile file = fileRepository.findFileById(fileId);
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
		return calculateProjectFileMetrics().get(project.getId());
	}

	public Map<Long,PackageMetrics> calculatePackageMetrics() {
		String key = "calculatePackageMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, PackageMetrics> result = new HashMap<>();
		for(PackageMetrics pckMetrics : packageRepository.calculatePackageMetrics()) {
			Package pck = pckMetrics.getPck();
			result.put(pck.getId(), pckMetrics);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	public Map<Long, List<PackageMetrics>> calculateProjectPackageMetrics() {
		String key = "calculateProjectPackageMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<PackageMetrics>> result = new HashMap<>();
		Map<Long, PackageMetrics> packageMetricsCache = new HashMap<>(calculatePackageMetrics());
		packageMetricsCache.forEach((pckId, packageMetrics)->{
			Package pck = packageRepository.findPackageById(pckId);
			Project project = containRelationService.findPackageBelongToProject(pck);
			List<PackageMetrics> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(packageMetrics);
			result.put(project.getId(), temp);
		});
		cache.cache(getClass(), key, result);
		return result;
	}
	
	public Collection<PackageMetrics> calculateProjectPackageMetrics(Project project) {
		return calculateProjectPackageMetrics().get(project.getId());
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
		FileMetrics.DebtMetric fileDebtMetrics = fileRepository.calculateFileDebtMetrics(file.getId());
		fileMetrics.setStructureMetric(fileStructureMetrics);
		fileMetrics.setEvolutionMetric(fileEvolutionMetrics);
		fileMetrics.setDebtMetric(fileDebtMetrics);
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
				projectMetrics.setCommits(commitTimes);
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
