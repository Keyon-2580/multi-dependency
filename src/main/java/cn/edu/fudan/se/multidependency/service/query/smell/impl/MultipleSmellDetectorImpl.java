package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.data.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.repository.smell.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.MultipleSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableDependencyDetectorUsingInstability;
import cn.edu.fudan.se.multidependency.service.query.smell.UnusedComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.UnutilizedAbstractionDetector;
import cn.edu.fudan.se.multidependency.service.query.history.IssueQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.data.IssueFile;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class MultipleSmellDetectorImpl implements MultipleSmellDetector {
	
	@Autowired
	private CyclicDependencyDetector cycleASDetector;

	@Autowired
	private HubLikeComponentDetector hubLikeComponentDetector;
	
	@Autowired
	private ImplicitCrossModuleDependencyDetector icdDependencyDetector;
	
	@Autowired
	private SimilarComponentsDetector similarComponentsDetector;
	
	@Autowired
	private UnstableDependencyDetectorUsingInstability unstableDependencyDetectorUsingInstability;
	
	@Autowired
	private UnusedComponentDetector unusedComponentDetector;
	
	@Autowired
	private ContainRelationService containRelationService;
 	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private IssueQueryService issueQueryService;
	
	@Autowired
	private UnutilizedAbstractionDetector unutilizedAbstractionDetector;
	
	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private CacheService cache;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private ProjectFileRepository projectFileRepository;

	@Autowired
	private CommitRepository commitRepository;
	
	public int fileCommitsCount(ProjectFile file) {
		String key = "fileCommitsCount_" + file.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		int result = asRepository.findCommitsUsingForIssue(file.getId()).size();
		cache.cache(getClass(), key, result);
		return result;
	}
	
	@Override
	public Map<Long, List<CirclePacking>> circlePacking(MultipleAS multipleAS) {
		Map<Long, List<CirclePacking>> result = new HashMap<>();
		Map<Long, List<MultipleASFile>> multipleASFiles = detectMultipleSmellASFiles(true);
		Map<Long, List<IssueFile>> issueFilesGroupByProject = issueQueryService.queryRelatedFilesOnAllIssuesGroupByProject();
		
		List<Project> projects = nodeService.allProjects();
		
		for(Project project : projects) {
			List<CirclePacking> circles = new ArrayList<>();
			CirclePacking onlySmellCircle = new CirclePacking(CirclePacking.TYPE_ONLY_SMELL);
			CirclePacking onlyIssueCircle = new CirclePacking(CirclePacking.TYPE_ONLY_ISSUE);
			CirclePacking smellAndIssueCircle = new CirclePacking(CirclePacking.TYPE_SMELL_ISSUE);
			
			List<IssueFile> issueFiles = new ArrayList<>(issueFilesGroupByProject.get(project.getId()));
			List<MultipleASFile> asFiles = multipleASFiles.get(project.getId());
			for(MultipleASFile smellFile : asFiles) {
				if(!smellFile.isSmellFile(multipleAS)) {
					continue;
				}
				IssueFile issueFile = IssueFile.contains(issueFiles, smellFile.getFile());
				if(issueFile != null) {
					// 既是issueFile又是smellFile
					smellAndIssueCircle.addProjectFile(smellFile.getFile(), issueFile.getIssues());
					smellAndIssueCircle.setFileSmellCount(smellFile.getFile(), smellFile.getSmellCount());
					smellAndIssueCircle.setFileCommitsCount(smellFile.getFile(), fileCommitsCount(smellFile.getFile()));
					issueFiles.remove(issueFile);
				} else {
					// 仅是smellFile
					onlySmellCircle.addProjectFile(smellFile.getFile());
					onlySmellCircle.setFileSmellCount(smellFile.getFile(), smellFile.getSmellCount());
					onlySmellCircle.setFileCommitsCount(smellFile.getFile(), fileCommitsCount(smellFile.getFile()));
				}
			}
			for(IssueFile issueFile : issueFiles) {
				// 仅是issueFile
				onlyIssueCircle.addProjectFile(issueFile.getFile(), issueFile.getIssues());
				onlyIssueCircle.setFileSmellCount(issueFile.getFile(), 0);
				onlyIssueCircle.setFileCommitsCount(issueFile.getFile(), fileCommitsCount(issueFile.getFile()));
			}
			
			circles.add(smellAndIssueCircle);
			circles.add(onlySmellCircle);
			circles.add(onlyIssueCircle);
			result.put(project.getId(), circles);
		}
		
		return result;
	}

	@Override
	public Map<Long, PieFilesData> smellAndIssueFiles(MultipleAS multipleAS) {
		Map<Long, List<MultipleASFile>> multipleASFiles = detectMultipleSmellASFiles(true);
		Map<Long, List<IssueFile>> issueFilesGroupByProject = issueQueryService.queryRelatedFilesOnAllIssuesGroupByProject();
		
		Map<Long, PieFilesData> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		
		Set<ProjectFile> isSmellFiles = new HashSet<>();
		Set<ProjectFile> isIssueFiles = new HashSet<>();
		
		for(Project project : projects) {
			List<IssueFile> issueFiles = issueFilesGroupByProject.get(project.getId());
			List<MultipleASFile> asFiles = multipleASFiles.get(project.getId());
			for(IssueFile issueFile : issueFiles) {
				isIssueFiles.add(issueFile.getFile());
			}
			for(MultipleASFile smellFile : asFiles) {
				if(smellFile.isSmellFile(multipleAS)) {
					isSmellFiles.add(smellFile.getFile());
				}
			}
			
			Set<ProjectFile> normalFiles = new HashSet<>();
			Set<ProjectFile> onlyIssueFiles = new HashSet<>();
			Set<ProjectFile> onlySmellFiles = new HashSet<>();
			Set<ProjectFile> issueAndSmellFiles = new HashSet<>();
			
			for(ProjectFile file : containRelationService.findProjectContainAllFiles(project)) {
				if(isSmellFiles.contains(file) && isIssueFiles.contains(file)) {
					issueAndSmellFiles.add(file);
				} else if(isSmellFiles.contains(file)) {
					onlySmellFiles.add(file);
				} else if(isIssueFiles.contains(file)) {
					onlyIssueFiles.add(file);
				} else {
					normalFiles.add(file);
				}
			}
			Set<Issue> allIssues = new HashSet<>(issueQueryService.queryIssues(project));
			Set<Issue> smellIssues = new HashSet<>();
			for(ProjectFile file : onlySmellFiles) {
				smellIssues.addAll(issueQueryService.queryRelatedIssuesOnFile(file));
			}
			for(ProjectFile file : issueAndSmellFiles) {
				smellIssues.addAll(issueQueryService.queryRelatedIssuesOnFile(file));
			}
			
			PieFilesData data = new PieFilesData(project, normalFiles, onlyIssueFiles, onlySmellFiles, issueAndSmellFiles, allIssues, smellIssues);
			result.put(project.getId(), data);
		}
		return result;
	}

	@Override
	public Map<Long, JSONObject> getProjectTotal() {
		String key = "projectTotal";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, JSONObject> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		for (Project project : projects) {
			JSONObject projectProjectTotalObject = new JSONObject();
			calculateProjectTotalObject(project.getId(), projectProjectTotalObject);
			result.put(project.getId(), projectProjectTotalObject);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, JSONObject> getFileSmellOverview() {
		String key = "fileSmellOverview";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, JSONObject> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		List<String> smellTypes = new ArrayList<>();
		smellTypes.add(SmellType.CYCLIC_DEPENDENCY);
		smellTypes.add(SmellType.HUBLIKE_DEPENDENCY);
		smellTypes.add(SmellType.UNSTABLE_DEPENDENCY);
		smellTypes.add(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		smellTypes.add(SmellType.UNUTILIZED_ABSTRACTION);
		smellTypes.add(SmellType.UNUSED_INCLUDE);
		for (Project project : projects) {
			JSONObject projectFileSmellOverviewObject = new JSONObject();
			JSONArray projectFileSmellArray = new JSONArray();
			for (String smellType : smellTypes) {
				JSONObject projectFileSmellObject = new JSONObject();
				List<Smell> projectFileSmells = new ArrayList<>(smellRepository.findSmells(project.getId(), smellType, SmellLevel.FILE));
				calculateProjectFileSmellObject(project.getId(), smellType, projectFileSmells, projectFileSmellObject);
				projectFileSmellArray.add(projectFileSmellObject);
			}
			projectFileSmellOverviewObject.put("ProjectFileSmell", projectFileSmellArray);
			result.put(project.getId(), projectFileSmellOverviewObject);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, JSONObject> getPackageSmellOverview() {
		String key = "packageSmellOverview";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, JSONObject> result = new HashMap<>();
		List<Project> projects = nodeService.allProjects();
		List<String> smellTypes = new ArrayList<>();
		smellTypes.add(SmellType.CYCLIC_DEPENDENCY);
		smellTypes.add(SmellType.HUBLIKE_DEPENDENCY);
		smellTypes.add(SmellType.UNSTABLE_DEPENDENCY);
		smellTypes.add(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		smellTypes.add(SmellType.UNUTILIZED_ABSTRACTION);
		smellTypes.add(SmellType.UNUSED_INCLUDE);
		for (Project project : projects) {
			JSONObject projectPackageSmellOverviewObject = new JSONObject();
			JSONArray projectPackageSmellArray = new JSONArray();
			for (String smellType : smellTypes) {
				JSONObject projectPackageSmellObject = new JSONObject();
				List<Smell> projectPackageSmells = new ArrayList<>(smellRepository.findSmells(project.getId(), smellType, SmellLevel.PACKAGE));
				calculateProjectPackageSmellObject(project.getId(), smellType, projectPackageSmells, projectPackageSmellObject);
				projectPackageSmellArray.add(projectPackageSmellObject);
			}
			projectPackageSmellOverviewObject.put("ProjectPackageSmell", projectPackageSmellArray);
			result.put(project.getId(), projectPackageSmellOverviewObject);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	private void calculateProjectTotalObject(Long projectId, JSONObject projectTotalObject) {
		projectTotalObject.put("FileCount", metricRepository.getProjectFileCountByProjectId(projectId));
		projectTotalObject.put("IssueCommits", metricRepository.getProjectIssueCommitsByProjectId(projectId));
		projectTotalObject.put("Commits", metricRepository.getProjectCommitsByProjectId(projectId));
		projectTotalObject.put("IssueChangeLines", metricRepository.getProjectIssueChangeLinesByProjectId(projectId));
		projectTotalObject.put("ChangeLines", metricRepository.getProjectChangeLinesByProjectId(projectId));
	}

	private void calculateProjectFileSmellObject(Long projectId, String smellType, List<Smell> projectFileSmells, JSONObject projectFileSmellObject) {
		int smellCount = projectFileSmells.size();
		Integer fileCount = projectFileRepository.calculateFileSmellFileCountByProjectId(projectId, smellType, SmellLevel.FILE);
		Integer issueCommits = commitRepository.calculateFileSmellIssueCommitsByProjectId(projectId, smellType, SmellLevel.FILE);
		Integer commits = commitRepository.calculateFileSmellCommitsByProjectId(projectId, smellType, SmellLevel.FILE);
		Integer issueChangeLines = commitRepository.calculateFileSmellIssueChangeLinesByProjectId(projectId, smellType, SmellLevel.FILE);
		Integer changeLines = commitRepository.calculateFileSmellChangeLinesByProjectId(projectId, smellType, SmellLevel.FILE);
		if (fileCount == null) {
			fileCount = 0;
		}
		if (issueCommits == null) {
			issueCommits = 0;
		}
		if (commits == null) {
			commits = 0;
		}
		if (issueChangeLines == null) {
			issueChangeLines = 0;
		}
		if (changeLines == null) {
			changeLines = 0;
		}
		projectFileSmellObject.put("SmellType", smellType);
		projectFileSmellObject.put("SmellCount", smellCount);
		projectFileSmellObject.put("FileCount", fileCount);
		projectFileSmellObject.put("IssueCommits", issueCommits);
		projectFileSmellObject.put("Commits", commits);
		projectFileSmellObject.put("IssueChangeLines", issueChangeLines);
		projectFileSmellObject.put("ChangeLines", changeLines);
	}

	private void calculateProjectPackageSmellObject(Long projectId, String smellType, List<Smell> projectPackageSmells, JSONObject projectPackageSmellObject) {
		int smellCount = projectPackageSmells.size();
		Integer fileCount = projectFileRepository.calculatePackageSmellFileCountByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		Integer issueCommits = commitRepository.calculatePackageSmellIssueCommitsByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		Integer commits = commitRepository.calculatePackageSmellCommitsByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		Integer issueChangeLines = commitRepository.calculatePackageSmellIssueChangeLinesByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		Integer changeLines = commitRepository.calculatePackageSmellChangeLinesByProjectId(projectId, smellType, SmellLevel.PACKAGE);
		if (fileCount == null) {
			fileCount = 0;
		}
		if (issueCommits == null) {
			issueCommits = 0;
		}
		if (commits == null) {
			commits = 0;
		}
		if (issueChangeLines == null) {
			issueChangeLines = 0;
		}
		if (changeLines == null) {
			changeLines = 0;
		}
		projectPackageSmellObject.put("SmellType", smellType);
		projectPackageSmellObject.put("SmellCount", smellCount);
		projectPackageSmellObject.put("FileCount", fileCount);
		projectPackageSmellObject.put("IssueCommits", issueCommits);
		projectPackageSmellObject.put("Commits", commits);
		projectPackageSmellObject.put("IssueChangeLines", issueChangeLines);
		projectPackageSmellObject.put("ChangeLines", changeLines);
	}

	@Override
	public Map<Long, List<MultipleASFile>> queryMultipleSmellASFiles(boolean removeNoASFile) {
		Map<Long, List<Cycle<ProjectFile>>> cycleFiles = cycleASDetector.queryFileCyclicDependency();
		Map<Long, List<FileHubLike>> hubLikeFiles = hubLikeComponentDetector.queryFileHubLikeDependency();
		Map<Long, List<UnstableComponentByInstability<ProjectFile>>> unstableFilesUsingInstability = unstableDependencyDetectorUsingInstability.queryFileUnstableDependency();
		Map<Long, List<ProjectFile>> unusedFiles = unusedComponentDetector.unusedFiles();
		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> unutilizedFiles = unutilizedAbstractionDetector.queryFileUnutilizedAbstraction();
		Map<Long, List<LogicCouplingComponents<ProjectFile>>> logicCouplingFiles = icdDependencyDetector.queryFileImplicitCrossModuleDependency();
		Map<Long, List<SimilarComponents<ProjectFile>>> similarFiles = similarComponentsDetector.queryFileSimilarComponents();
		return calculateMultipleSmellASFiles(removeNoASFile, cycleFiles, hubLikeFiles, unstableFilesUsingInstability, unusedFiles, unutilizedFiles, logicCouplingFiles, similarFiles);
	}
	
	@Override
	public Map<Long, List<MultipleASFile>> detectMultipleSmellASFiles(boolean removeNoASFile) {
		Map<Long, List<Cycle<ProjectFile>>> cycleFiles = cycleASDetector.detectFileCyclicDependency();
		Map<Long, List<FileHubLike>> hubLikeFiles = hubLikeComponentDetector.detectFileHubLikeDependency();
		Map<Long, List<UnstableComponentByInstability<ProjectFile>>> unstableFilesUsingInstability = unstableDependencyDetectorUsingInstability.detectFileUnstableDependency();
		Map<Long, List<ProjectFile>> unusedFiles = unusedComponentDetector.unusedFiles();
		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> unutilizedFiles = unutilizedAbstractionDetector.detectFileUnutilizedAbstraction();
		Map<Long, List<LogicCouplingComponents<ProjectFile>>> logicCouplingFiles = icdDependencyDetector.detectFileImplicitCrossModuleDependency();
		Map<Long, List<SimilarComponents<ProjectFile>>> similarFiles = similarComponentsDetector.detectFileSimilarComponents();
		return calculateMultipleSmellASFiles(removeNoASFile, cycleFiles, hubLikeFiles, unstableFilesUsingInstability, unusedFiles, unutilizedFiles, logicCouplingFiles, similarFiles);
	}

	private Map<Long, List<MultipleASFile>> calculateMultipleSmellASFiles(boolean removeNoASFile,
																		  Map<Long, List<Cycle<ProjectFile>>> cycleFiles,
																		  Map<Long, List<FileHubLike>> hubLikeFiles,
																		  Map<Long, List<UnstableComponentByInstability<ProjectFile>>> unstableFilesUsingInstability,
																		  Map<Long, List<ProjectFile>> unusedFiles,
																		  Map<Long, List<UnutilizedAbstraction<ProjectFile>>> unutilizedFiles,
																		  Map<Long, List<LogicCouplingComponents<ProjectFile>>> logicCouplingFiles,
																		  Map<Long, List<SimilarComponents<ProjectFile>>> similarFiles) {
		Map<ProjectFile, MultipleASFile> map = new HashMap<>();
		List<ProjectFile> allFiles = nodeService.queryAllFiles();
		for (List<Cycle<ProjectFile>> cycleFilesGroup : cycleFiles.values()) {
			for (Cycle<ProjectFile> files : cycleFilesGroup) {
				for (ProjectFile file : files.getComponents()) {
					MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
					mas.setCycle(true);
					map.put(file, mas);
					allFiles.remove(file);
				}
			}
		}

		for (List<FileHubLike> fileHubLikeGroup : hubLikeFiles.values()) {
			for (FileHubLike file : fileHubLikeGroup) {
				MultipleASFile mas = map.getOrDefault(file.getFile(), new MultipleASFile(file.getFile()));
				mas.setHublike(true);
				map.put(file.getFile(), mas);
				allFiles.remove(file.getFile());
			}
		}

		for (List<UnstableComponentByInstability<ProjectFile>> unstableFilesGroup : unstableFilesUsingInstability.values()) {
			for (UnstableComponentByInstability<ProjectFile> file : unstableFilesGroup) {
				MultipleASFile mas = map.getOrDefault(file.getComponent(), new MultipleASFile(file.getComponent()));
				mas.setUnstable(true);
				map.put(file.getComponent(), mas);
				allFiles.remove(file.getComponent());
			}
		}

		for (List<LogicCouplingComponents<ProjectFile>> logicCouplingFile : logicCouplingFiles.values()) {
			for (LogicCouplingComponents<ProjectFile> files : logicCouplingFile) {
				MultipleASFile mas = map.getOrDefault(files.getNode1(), new MultipleASFile(files.getNode1()));
				mas.setLogicCoupling(true);
				map.put(files.getNode1(), mas);
				mas = map.getOrDefault(files.getNode2(), new MultipleASFile(files.getNode2()));
				mas.setLogicCoupling(true);
				map.put(files.getNode2(), mas);
				allFiles.remove(files.getNode1());
				allFiles.remove(files.getNode2());
			}
		}

		for (List<SimilarComponents<ProjectFile>> similarFile : similarFiles.values()) {
			for (SimilarComponents<ProjectFile> similarFilesGroup : similarFile) {
				ProjectFile file1 = similarFilesGroup.getNode1();
				ProjectFile file2 = similarFilesGroup.getNode2();
				MultipleASFile mas = map.getOrDefault(file1, new MultipleASFile(file1));
				mas.setSimilar(true);
				map.put(file1, mas);
				allFiles.remove(file1);
				mas = map.getOrDefault(file2, new MultipleASFile(file2));
				mas.setSimilar(true);
				map.put(file2, mas);
				allFiles.remove(file2);
			}
		}

		for(List<ProjectFile> files : unusedFiles.values()) {
			for(ProjectFile file : files) {
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				mas.setUnused(true);
				map.put(file, mas);
				allFiles.remove(file);
			}
		}

		for(List<UnutilizedAbstraction<ProjectFile>> files : unutilizedFiles.values()) {
			for(UnutilizedAbstraction<ProjectFile> file : files) {
				MultipleASFile mas = map.getOrDefault(file.getComponent(), new MultipleASFile(file.getComponent()));
				mas.setUnutilized(true);
				map.put(file.getComponent(), mas);
				allFiles.remove(file.getComponent());
			}
		}

		/*for(List<GodFile> files : godFiles.values()) {
			for(GodFile file : files) {
				MultipleASFile mas = map.getOrDefault(file.getFile(), new MultipleASFile(file.getFile()));
				mas.setGod(true);
				map.put(file.getFile(), mas);
				allFiles.remove(file.getFile());
			}
		}

		for(Map.Entry<Long, List<CyclicHierarchy>> entry : cyclicHierarchies.entrySet()) {
			for(CyclicHierarchy cyclicHierarchy : entry.getValue()) {
				Type superType = cyclicHierarchy.getSuperType();
				ProjectFile file = containRelationService.findTypeBelongToFile(superType);
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				mas.setCyclicHierarchy(true);
				map.put(file, mas);
				allFiles.remove(file);
			}
		}

		for (List<UnstableFileInHistory> unstableFilesGroup : unstableFilesInHistory.values()) {
			for (UnstableFileInHistory file : unstableFilesGroup) {
				MultipleASFile mas = map.getOrDefault(file.getComponent(), new MultipleASFile(file.getComponent()));
				mas.setUnstable(true);
				map.put(file.getComponent(), mas);
				allFiles.remove(file.getComponent());
			}
		}*/

		if(!removeNoASFile) {
			for(ProjectFile file : allFiles) {
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				map.put(file, mas);
			}
		}

		Map<Long, List<MultipleASFile>> result = new HashMap<>();
		for(Map.Entry<ProjectFile, MultipleASFile> entry : map.entrySet()) {
			ProjectFile file = entry.getKey();
			MultipleASFile value = entry.getValue();
			Project project = containRelationService.findFileBelongToProject(file);
			value.setProject(project);
			List<MultipleASFile> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(value);
			result.put(project.getId(), temp);
		}

		for(Map.Entry<Long, List<MultipleASFile>> entry : result.entrySet()) {
			entry.getValue().sort((m1, m2) -> m2.getSmellCount() - m1.getSmellCount());
		}
		return result;
	}

	@Override
	public void printMultipleASFiles(OutputStream stream) {
		Workbook hwb = new XSSFWorkbook();
		Map<Long, List<MultipleASFile>> multiple = detectMultipleSmellASFiles(false);
		List<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			Sheet sheet = hwb.createSheet(new StringBuilder().append(project.getName()).append("(").append(project.getLanguage()).append(")").toString());
			List<MultipleASFile> packageMetrics = multiple.get(project.getId());
			Row row = sheet.createRow(0);
			CellStyle style = hwb.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER);
//			sheet.setColumnWidth(0, "xxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(1, "xxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(2, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(3, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(4, "xxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(5, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(6, "xxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(7, "xxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(8, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(9, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(10, "xxxxxx".length() * 256);
//			sheet.setColumnWidth(11, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(12, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(13, "xxxxxxxxxx".length() * 256);
			Cell cell = null;
			cell = row.createCell(0);
			cell.setCellValue("id");
			cell.setCellStyle(style);
			cell = row.createCell(1);
			cell.setCellValue("文件");
			cell.setCellStyle(style);
			cell = row.createCell(2);
			cell.setCellValue("cycle");
			cell.setCellStyle(style);
			cell = row.createCell(3);
			cell.setCellValue("hublike");
			cell.setCellStyle(style);
			cell = row.createCell(4);
			cell.setCellValue("unstable");
			cell.setCellStyle(style);
			cell = row.createCell(5);
			cell.setCellValue("logicCoupling");
			cell.setCellStyle(style);
			cell = row.createCell(6);
			cell.setCellValue("similar");
			cell.setCellStyle(style);
			cell = row.createCell(7);
			cell.setCellValue("cyclicHierarchy");
			cell.setCellStyle(style);
			for (int i = 0; i < packageMetrics.size(); i++) {
				MultipleASFile mas = packageMetrics.get(i);
				row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(mas.getFile().getId());
				row.createCell(1).setCellValue(mas.getFile().getPath());
				row.createCell(2).setCellValue(mas.cycleToString());
				row.createCell(3).setCellValue(mas.hubLikeToString());
				row.createCell(4).setCellValue(mas.unstableToString());
				row.createCell(5).setCellValue(mas.logicCouplingToString());
				row.createCell(6).setCellValue(mas.similarToString());
//				row.createCell(7).setCellValue(mas.cyclicHierarchyToString());
			}			
		}
		try {
			hwb.write(stream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
				hwb.close();
			} catch (IOException e) {
			}
		}
		
	}


	@Override
	public Map<Long, HistogramAS> projectHistogramOnVersion() {
		Map<Long, HistogramAS> result = new HashMap<>();
		Map<Long, List<MultipleASFile>> multipleASFiles = detectMultipleSmellASFiles(true);
		Map<Long, List<IssueFile>> issueFiles = issueQueryService.queryRelatedFilesOnAllIssuesGroupByProject();
		
		List<Project> projects = nodeService.allProjects();
		
		for(Project project : projects) {
			HistogramAS data = new HistogramAS(project);
			data.setAllFilesCount(containRelationService.findProjectContainAllFiles(project).size());
			data.setIssueFilesCount(issueFiles.get(project.getId()).size());
			data.setSmellFilesCount(multipleASFiles.get(project.getId()).size());
			result.put(project.getId(), data);
		}
		
		return result;
	}

}
