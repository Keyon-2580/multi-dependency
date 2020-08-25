package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicHierarchyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.GodComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.MultipleArchitectureSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.as.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.as.UnstableDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.UnusedComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.as.data.CyclicHierarchy;
import cn.edu.fudan.se.multidependency.service.query.as.data.GodFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HistogramAS;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.LogicCouplingFiles;
import cn.edu.fudan.se.multidependency.service.query.as.data.MultipleAS;
import cn.edu.fudan.se.multidependency.service.query.as.data.MultipleASFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.PieFilesData;
import cn.edu.fudan.se.multidependency.service.query.as.data.SimilarComponents;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;
import cn.edu.fudan.se.multidependency.service.query.history.IssueQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.data.IssueFile;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class MultipleArchitectureSmellDetectorImpl implements MultipleArchitectureSmellDetector {
	
	@Autowired
	private CyclicDependencyDetector cycleASDetector;

	@Autowired
	private HubLikeComponentDetector hubLikeComponentDetector;
	
	@Autowired
	private ImplicitCrossModuleDependencyDetector icdDependencyDetector;
	
	@Autowired
	private SimilarComponentsDetector similarComponentsDetector;
	
	@Autowired
	private UnstableDependencyDetector unstableDependencyDetector;
	
	@Autowired
	private CyclicHierarchyDetector cyclicHierarchyDetector;
	
	@Autowired
	private GodComponentDetector godComponentDetector;
	
	@Autowired
	private UnusedComponentDetector unusedComponentDetector;
	
	@Autowired
	private ContainRelationService containRelationService;
 	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private IssueQueryService issueQueryService;
	
	@Override
	public Map<Long, PieFilesData> smellAndIssueFiles(MultipleAS multipleAS) {
		Map<Long, List<MultipleASFile>> multipleASFiles = multipleASFiles(true);
		Map<Long, List<IssueFile>> issueFilesGroupByProject = issueQueryService.queryRelatedFilesOnAllIssuesGroupByProject();
		
		Map<Long, PieFilesData> result = new HashMap<>();
		Collection<Project> projects = nodeService.allProjects();
		
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
			PieFilesData data = new PieFilesData(project, normalFiles, onlyIssueFiles, onlySmellFiles, issueAndSmellFiles);
			result.put(project.getId(), data);
		}
		return result;
	}
	
	
	@Override
	public Map<Long, List<MultipleASFile>> multipleASFiles(boolean removeNoASFile) {
		Map<ProjectFile, MultipleASFile> map = new HashMap<>();
		Map<Long, Map<Integer, Cycle<ProjectFile>>> cycleFiles = cycleASDetector.cycleFiles();
		Map<Long, List<HubLikeFile>> hubLikeFiles = hubLikeComponentDetector.hubLikeFiles();
		Map<Long, List<UnstableFile>> unstableFiles = unstableDependencyDetector.unstableFiles();
		Map<Long, List<CyclicHierarchy>> cyclicHierarchies = cyclicHierarchyDetector.cyclicHierarchies();
		Map<Long, List<GodFile>> godFiles = godComponentDetector.godFiles();
		Map<Long, List<ProjectFile>> unusedFiles = unusedComponentDetector.unusedFiles();
		
		Collection<LogicCouplingFiles> logicCouplingFiles = icdDependencyDetector.cochangesInDifferentModule();
		Collection<SimilarComponents<ProjectFile>> similarFiles = similarComponentsDetector.similarFiles();
		
		List<ProjectFile> allFiles = nodeService.queryAllFiles();
		
		for(Map<Integer, Cycle<ProjectFile>> cycleFilesGroup : cycleFiles.values()) {
			for(Cycle<ProjectFile> files : cycleFilesGroup.values()) {
				for(ProjectFile file : files.getComponents()) {
					MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
					mas.setCycle(true);
					map.put(file, mas);
					allFiles.remove(file);
				}
			}
		}
		
		for(List<GodFile> files : godFiles.values()) {
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
		
		for(List<HubLikeFile> hubLikeFilesGroup : hubLikeFiles.values()) {
			for(HubLikeFile file : hubLikeFilesGroup) {
				MultipleASFile mas = map.getOrDefault(file.getFile(), new MultipleASFile(file.getFile()));
				mas.setHublike(true);
				map.put(file.getFile(), mas);
				allFiles.remove(file.getFile());
			}
		}
		
		for(List<UnstableFile> unstableFilesGroup : unstableFiles.values()) {
			for(UnstableFile file : unstableFilesGroup) {
				MultipleASFile mas = map.getOrDefault(file.getFile(), new MultipleASFile(file.getFile()));
				mas.setUnstable(true);
				map.put(file.getFile(), mas);
				allFiles.remove(file.getFile());
			}
		}
		
		for(LogicCouplingFiles files : logicCouplingFiles) {
			MultipleASFile mas = map.getOrDefault(files.getFile1(), new MultipleASFile(files.getFile1()));
			mas.setLogicCoupling(true);
			map.put(files.getFile1(), mas);
			mas = map.getOrDefault(files.getFile2(), new MultipleASFile(files.getFile2()));
			mas.setLogicCoupling(true);
			map.put(files.getFile2(), mas);
			allFiles.remove(files.getFile1());
			allFiles.remove(files.getFile2());
		}
		
		for(SimilarComponents<ProjectFile> similarFilesGroup : similarFiles) {
			for(ProjectFile file : similarFilesGroup.getComponents()) {
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				mas.setSimilar(true);
				map.put(file, mas);
				allFiles.remove(file);
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
			entry.getValue().sort((m1, m2) -> {
//				if(m2.smellCount() == m1.smellCount()) {
//					return m2.getFile().getScore() > m1.getFile().getScore() ? 1 : -1;
//				}
				return m2.smellCount() - m1.smellCount();
			});
		}
		
		return result;
	}

	@Override
	public void printMultipleASFiles(OutputStream stream) {
		Workbook hwb = new XSSFWorkbook();
		Map<Long, List<MultipleASFile>> multiple = multipleASFiles(false);
		Collection<Project> projects = nodeService.allProjects();
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
				row.createCell(7).setCellValue(mas.cyclicHierarchyToString());
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
		Map<Long, List<MultipleASFile>> multipleASFiles = multipleASFiles(true);
		Map<Long, List<IssueFile>> issueFiles = issueQueryService.queryRelatedFilesOnAllIssuesGroupByProject();
		
		Collection<Project> projects = nodeService.allProjects();
		
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
