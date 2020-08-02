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
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

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
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Resource(name="modularityCalculatorImplForFieldMethodLevel")
	private ModularityCalculator modularityCalculator;
	
	private Map<Long, List<FileMetrics>> fileMetricsCache = null;
	/*public Map<Long, List<FileMetrics>> calculateFileMetrics() {
		if(fileMetricsCache != null) {
			return fileMetricsCache;
		}
		Map<Long, List<FileMetrics>> result = new HashMap<>();
		for(FileMetrics fileMetrics : fileRepository.calculateFileMetricsWithCoChangeCommitTimes()) {
			ProjectFile file = fileMetrics.getFile();
			Project project = containRelationService.findFileBelongToProject(file);
			List<FileMetrics> temp = result.getOrDefault(project, new ArrayList<>());
			temp.add(fileMetrics);
			result.put(project.getId(), temp);
		}
		fileMetricsCache = result;
		return result;
	}*/
	public Map<Long, List<FileMetrics>> calculateFileMetrics(boolean calculateCoCangeCommitTimes) {
		if(fileMetricsCache != null) {
			return fileMetricsCache;
		}
		Map<Long, List<FileMetrics>> result = new HashMap<>();
		for(FileMetrics fileMetrics : fileRepository.calculateFileMetrics()) {
			ProjectFile file = fileMetrics.getFile();
			if(calculateCoCangeCommitTimes) {
				fileMetrics.setCochangeCommitTimes(fileRepository.cochangeCommitsWithFile(file.getId()).size());
			}
			Project project = containRelationService.findFileBelongToProject(file);
			List<FileMetrics> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(fileMetrics);
			result.put(project.getId(), temp);
		}
		fileMetricsCache = result;
		return result;
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
	
	private Map<Long, List<PackageMetrics>> packageMetricsCache = null;
	public Map<Long, List<PackageMetrics>> calculatePackageMetrics() {
		if(packageMetricsCache != null) {
			return packageMetricsCache;
		}
		Map<Long, List<PackageMetrics>> result = new HashMap<>();
		System.out.println(packageRepository.calculatePackageMetrics().size());
		for(PackageMetrics pckMetrics : packageRepository.calculatePackageMetrics()) {
			Package pck = pckMetrics.getPck();
			Project project = containRelationService.findPackageBelongToProject(pck);
			List<PackageMetrics> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(pckMetrics);
			result.put(project.getId(), temp);
		}
		packageMetricsCache = result;
		return result;
	}
	
	public Collection<ProjectFile> calculateFanIn(ProjectFile file) {
		return fileRepository.calculateFanIn(file.getId());
	}
	
	public Collection<ProjectFile> calculateFanOut(ProjectFile file) {
		return fileRepository.calculateFanOut(file.getId());
	}
}
