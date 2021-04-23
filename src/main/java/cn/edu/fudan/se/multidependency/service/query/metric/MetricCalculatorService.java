package cn.edu.fudan.se.multidependency.service.query.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.edu.fudan.se.multidependency.model.MetricType;
import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.relation.Has;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.relation.HasRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.DeveloperSubmitCommitRepository;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(MetricCalculatorService.class);

	@Autowired
	private ProjectFileRepository fileRepository;
	
	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private PackageRepository packageRepository;

	@Autowired
	private CommitRepository commitRepository;

	@Autowired
	private DeveloperSubmitCommitRepository developerSubmitCommitRepository;

	@Autowired
	private GitAnalyseService gitAnalyseService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private CacheService cache;

	@Autowired
	private HasRepository hasRepository;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private CoChangeRepository coChangeRepository;
	
	@Resource(name="modularityCalculatorImplForFieldMethodLevel")
	private ModularityCalculator modularityCalculator;

	public void setBasicMetric(boolean isRecreate) {
		List<Metric> metricsTmp = metricRepository.findFileMetricsWithLimit();
		if(metricsTmp != null && !metricsTmp.isEmpty()){
			LOGGER.info("已存在File Metric度量值节点和关系");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}

		metricRepository.deleteAllFileMetric();
		Map<Long, Metric> fileMetricNodesMap = generateFileMetricNodes();
		if(fileMetricNodesMap != null && !fileMetricNodesMap.isEmpty()){
			Collection<Metric> fileMetricNodes = fileMetricNodesMap.values();
			metricRepository.saveAll(fileMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<Long, Metric> entry : fileMetricNodesMap.entrySet()){
				ProjectFile file = fileRepository.findFileById(entry.getKey());
				Has has = new Has(file, entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
		}
	}

	public void createFileMetric(boolean isRecreate) {
		List<Metric> metricsTmp = metricRepository.findFileMetricsWithLimit();
		if(metricsTmp != null && !metricsTmp.isEmpty()){
			LOGGER.info("已存在File Metric度量值节点和关系");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}

		metricRepository.deleteAllFileMetric();

		LOGGER.info("计算Project/Package/ProjectFile基本度量值...");
		fileRepository.setFileMetrics();
		packageRepository.setEmptyPackageMetrics();
		packageRepository.setPackageMetrics();
		projectRepository.setProjectMetrics();

		Map<Long, Metric> fileMetricNodesMap = generateFileMetricNodes();
		if(fileMetricNodesMap != null && !fileMetricNodesMap.isEmpty()){
			Collection<Metric> fileMetricNodes = fileMetricNodesMap.values();
			metricRepository.saveAll(fileMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<Long, Metric> entry : fileMetricNodesMap.entrySet()){
				ProjectFile file = fileRepository.findFileById(entry.getKey());
				Has has = new Has(file, entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
		}
	}

	public void createPackageMetric(boolean isRecreate) {
		List<Metric> metricsTmp = metricRepository.findPackageMetricsWithLimit();
		if(metricsTmp != null && !metricsTmp.isEmpty()){
			LOGGER.info("已存在Package Metric度量值节点和关系");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}

		metricRepository.deleteAllPackageMetric();
		Map<Long, Metric> packageMetricNodesMap = generatePackageMetricNodes();
		if(packageMetricNodesMap != null && !packageMetricNodesMap.isEmpty()){
			Collection<Metric> pckMetricNodes = packageMetricNodesMap.values();
			metricRepository.saveAll(pckMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<Long, Metric> entry : packageMetricNodesMap.entrySet()){
				Package pck = packageRepository.findPackageById(entry.getKey());
				Has has = new Has(pck, entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
		}
	}

	public void createProjectMetric(boolean isRecreate) {
		List<Metric> metricsTmp = metricRepository.findProjectMetricsWithLimit();
		if(metricsTmp != null && !metricsTmp.isEmpty()){
			LOGGER.info("已存在Project Metric度量值节点和关系");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}

		metricRepository.deleteAllProjectMetric();
		Map<Long, Metric> projectMetricNodesMap = generateProjectMetricNodes();
		if(projectMetricNodesMap != null && !projectMetricNodesMap.isEmpty()){
			Collection<Metric> projectMetricNodes = projectMetricNodesMap.values();
			metricRepository.saveAll(projectMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			for(Map.Entry<Long, Metric> entry : projectMetricNodesMap.entrySet()){
				Project project = projectRepository.findProjectById(entry.getKey());
				Has has = new Has(project, entry.getValue());
				hasMetrics.add(has);
			}
			hasRepository.saveAll(hasMetrics);
		}
	}

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
				int fanInOut = fileMetrics.getStructureMetric().getFanOut() + fileMetrics.getStructureMetric().getFanIn();
				double instability  = (fanInOut > 0 ? (double) (fileMetrics.getStructureMetric().getFanOut()) / fanInOut : 0.0);
				metricValues.put(MetricType.INSTABILITY, instability);
				FileMetrics.EvolutionMetric evolutionMetric = fileMetrics.getEvolutionMetric();
				if (evolutionMetric != null){
					metricValues.put(MetricType.COMMITS, evolutionMetric.getCommits());
					metricValues.put(MetricType.DEVELOPERS, evolutionMetric.getDevelopers());
					metricValues.put(MetricType.CO_CHANGE_FILES, evolutionMetric.getCoChangeFiles());
					metricValues.put(MetricType.ADD_LINES, evolutionMetric.getAddLines());
					metricValues.put(MetricType.SUB_LINES, evolutionMetric.getSubLines());
				}

				FileMetrics.DebtMetric detMetric = fileMetrics.getDebtMetric();
				if(detMetric != null){
					metricValues.put(MetricType.ISSUES, detMetric.getIssues());
					metricValues.put(MetricType.BUG_ISSUES, detMetric.getBugIssues());
					metricValues.put(MetricType.NEW_FEATURE_ISSUES, detMetric.getNewFeatureIssues());
					metricValues.put(MetricType.IMPROVEMENT_ISSUES, detMetric.getImprovementIssues());
				}

				FileMetrics.DeveloperMetric developerMetric = fileMetrics.getDeveloperMetric();
				if(developerMetric != null){
					metricValues.put(MetricType.CREATOR, developerMetric.getCreator());
					metricValues.put(MetricType.LAST_UPDATOR, developerMetric.getLastUpdator());
					metricValues.put(MetricType.MOST_UPDATOR, developerMetric.getMostUpdator());
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
				int fanInOut = packageMetrics.getFanOut() + packageMetrics.getFanIn();
				double instability  = (fanInOut > 0 ? (double)(packageMetrics.getFanOut())/fanInOut : 0.0);
				metricValues.put(MetricType.INSTABILITY, instability);
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
				metricValues.put(MetricType.MED_FILE_FAN_IN, projectMetrics.getMedFileFanIn());
				metricValues.put(MetricType.MED_FILE_FAN_OUT, projectMetrics.getMedFileFanOut());
				metricValues.put(MetricType.MED_PACKAGE_FAN_IN, projectMetrics.getMedPackageFanIn());
				metricValues.put(MetricType.MED_PACKAGE_FAN_OUT, projectMetrics.getMedPackageFanOut());
				metricValues.put(MetricType.MED_FILE_CO_CHANGE, projectMetrics.getMedFileCoChange());
				metricValues.put(MetricType.MED_PACKAGE_CO_CHANGE, projectMetrics.getMedPackageCoChange());
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
				FileMetrics.DeveloperMetric developerMetric = calculateFileDeveloperMetrics(file);

				fileMetrics.setStructureMetric(structureMetric);
				fileMetrics.setEvolutionMetric(fileEvolutionMetrics);
				fileMetrics.setDebtMetric(fileDebtMetrics);
				fileMetrics.setDeveloperMetric(developerMetric);

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
		if(!fileMetricsCache.isEmpty()){
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
		FileMetrics.DeveloperMetric fileDeveloperMetrics = calculateFileDeveloperMetrics(file);
		fileMetrics.setStructureMetric(fileStructureMetrics);
		fileMetrics.setEvolutionMetric(fileEvolutionMetrics);
		fileMetrics.setDebtMetric(fileDebtMetrics);
		fileMetrics.setDeveloperMetric(fileDeveloperMetrics);
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
				projectMetrics.setMedFileFanIn(calculateMedFileFanIn(project.getId()));
				projectMetrics.setMedFileFanOut(calculateMedFileFanOut(project.getId()));
				projectMetrics.setMedPackageFanIn(calculateMedPackageFanIn(project.getId()));
				projectMetrics.setMedPackageFanOut(calculateMedPackageFanOut(project.getId()));
				projectMetrics.setMedFileCoChange(calculateMedFileCoChange(project.getId()));
				projectMetrics.setMedPackageCoChange(calculateMedPackageCoChange(project.getId()));
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

	private FileMetrics.DeveloperMetric calculateFileDeveloperMetrics(ProjectFile file){
		long fileId = file.getId();
		FileMetrics.DeveloperMetric developerMetric = new FileMetrics().new DeveloperMetric();
		List<Commit> commits = commitRepository.queryUpdatedByCommits(fileId);
		if(commits.size() > 0){
			Developer creator = developerSubmitCommitRepository.findDeveloperByCommitId(commits.get(commits.size() - 1).getId());
			Developer lastUpdator = developerSubmitCommitRepository.findDeveloperByCommitId(commits.get(0).getId());
			Developer mostUpdator = new Developer();
			Map<Developer, Integer> updateTimes= new HashMap<>();
			int mostUpdateTime = 0;
			for (Commit commit:
					commits) {
				Developer developer = developerSubmitCommitRepository.findDeveloperByCommitId(commit.getId());
				updateTimes.put(developer, updateTimes.getOrDefault(developer, 0) + 1);
				if(updateTimes.get(developer) >= mostUpdateTime){
					mostUpdator = developer;
					mostUpdateTime = updateTimes.get(developer);
				}
			}
			developerMetric.setFile(file);
			developerMetric.setCreator(creator.getName());
			developerMetric.setMostUpdator(mostUpdator.getName());
			developerMetric.setLastUpdator(lastUpdator.getName());
		}
		return developerMetric;
	}

	private int calculateMedian(List<Integer> list) {
		int median = 0;
		int size = list.size();
		if (size > 0) {
			if (size % 2 == 0) {
				median = (list.get((size / 2) - 1) + list.get(size / 2)) / 2;
			}
			else {
				median = list.get(size / 2);
			}
		}
		return median;
	}

	private int calculateMedFileFanIn(long projectId) {
		List<Integer> fileFanInList = new ArrayList<>(fileRepository.findFileFanInByProjectId(projectId));
		return calculateMedian(fileFanInList);
	}

	private int calculateMedFileFanOut(long projectId) {
		List<Integer> fileFanOutList = new ArrayList<>(fileRepository.findFileFanOutByProjectId(projectId));
		return calculateMedian(fileFanOutList);
	}

	private int calculateMedPackageFanIn(long projectId) {
		List<Integer> packageFanInList = new ArrayList<>(packageRepository.findPackageFanInByProjectId(projectId));
		return calculateMedian(packageFanInList);
	}

	private int calculateMedPackageFanOut(long projectId) {
		List<Integer> packageFanOutList = new ArrayList<>(packageRepository.findPackageFanOutByProjectId(projectId));
		return calculateMedian(packageFanOutList);
	}

	private int calculateMedFileCoChange(long projectId) {
		List<Integer> fileCoChangeList = new ArrayList<>(coChangeRepository.findFileCoChangeByProjectId(projectId));
		return calculateMedian(fileCoChangeList);
	}

	private int calculateMedPackageCoChange(long projectId) {
		List<Integer> packageCoChangeList = new ArrayList<>(coChangeRepository.findPackageCoChangeByProjectId(projectId));
		return calculateMedian(packageCoChangeList);
	}
}
