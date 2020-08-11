package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import cn.edu.fudan.se.multidependency.service.query.as.ArchitectureSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicHierarchyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.as.UnstableDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.as.data.CyclicHierarchy;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.LogicCouplingFiles;
import cn.edu.fudan.se.multidependency.service.query.as.data.MultipleASFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.SimilarComponents;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class ArchitectureSmellDetectorImpl implements ArchitectureSmellDetector {
	
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
	private ContainRelationService containRelationService;
	
	@Autowired
	private NodeService nodeService;
	
	@Override
	public Map<Project, List<MultipleASFile>> multipleASFiles(boolean removeNoASFile) {
		Map<Project, List<MultipleASFile>> result = new HashMap<>();
		
		Map<ProjectFile, MultipleASFile> map = new HashMap<>();
		Map<Long, List<Cycle<ProjectFile>>> cycleFiles = cycleASDetector.cycleFiles();
		Map<Long, List<HubLikeFile>> hubLikeFiles = hubLikeComponentDetector.hubLikeFiles();
		Map<Long, List<UnstableFile>> unstableFiles = unstableDependencyDetector.unstableFiles();
		Map<Long, List<CyclicHierarchy>> cyclicHierarchies = cyclicHierarchyDetector.cyclicHierarchies();
		Collection<LogicCouplingFiles> logicCouplingFiles = icdDependencyDetector.cochangesInDifferentModule();
		Collection<SimilarComponents<ProjectFile>> similarFiles = similarComponentsDetector.similarFiles();
		
		for(List<Cycle<ProjectFile>> cycleFilesGroup : cycleFiles.values()) {
			for(Cycle<ProjectFile> files : cycleFilesGroup) {
				for(ProjectFile file : files.getComponents()) {
					MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
					mas.setCycle(true);
					map.put(file, mas);
				}
			}
		}
		
		for(Map.Entry<Long, List<CyclicHierarchy>> entry : cyclicHierarchies.entrySet()) {
			for(CyclicHierarchy cyclicHierarchy : entry.getValue()) {
				Type superType = cyclicHierarchy.getSuperType();
				ProjectFile file = containRelationService.findTypeBelongToFile(superType);
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				mas.setCyclicHierarchy(true);
				map.put(file, mas);
			}
		}
		
		for(List<HubLikeFile> hubLikeFilesGroup : hubLikeFiles.values()) {
			for(HubLikeFile file : hubLikeFilesGroup) {
				MultipleASFile mas = map.getOrDefault(file.getFile(), new MultipleASFile(file.getFile()));
				mas.setHublike(true);
				map.put(file.getFile(), mas);
			}
		}
		
		for(List<UnstableFile> unstableFilesGroup : unstableFiles.values()) {
			for(UnstableFile file : unstableFilesGroup) {
				MultipleASFile mas = map.getOrDefault(file.getFile(), new MultipleASFile(file.getFile()));
				mas.setUnstable(true);
				map.put(file.getFile(), mas);
			}
		}
		
		for(LogicCouplingFiles files : logicCouplingFiles) {
			MultipleASFile mas = map.getOrDefault(files.getFile1(), new MultipleASFile(files.getFile1()));
			mas.setLogicCoupling(true);
			map.put(files.getFile1(), mas);
			mas = map.getOrDefault(files.getFile2(), new MultipleASFile(files.getFile2()));
			mas.setLogicCoupling(true);
			map.put(files.getFile2(), mas);
		}
		
		for(SimilarComponents<ProjectFile> similarFilesGroup : similarFiles) {
			for(ProjectFile file : similarFilesGroup.getComponents()) {
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				mas.setSimilar(true);
				map.put(file, mas);
			}
		}
		
		if(!removeNoASFile) {
			for(ProjectFile file : nodeService.queryAllFiles()) {
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				map.put(file, mas);
			}
		}
		
		for(Map.Entry<ProjectFile, MultipleASFile> entry : map.entrySet()) {
			ProjectFile file = entry.getKey();
			MultipleASFile value = entry.getValue();
			Project project = containRelationService.findFileBelongToProject(file);
			value.setProject(project);
			List<MultipleASFile> temp = result.getOrDefault(project, new ArrayList<>());
			temp.add(value);
			result.put(project, temp);
		}
		
		for(Map.Entry<Project, List<MultipleASFile>> entry : result.entrySet()) {
			entry.getValue().sort((m1, m2) -> {
				if(m2.smellCount() == m1.smellCount()) {
					return m2.getFile().getScore() > m1.getFile().getScore() ? 1 : -1;
				}
				return m2.smellCount() - m1.smellCount();
			});
		}
		
		return result;
	}

	@Override
	public void printMultipleASFiles(OutputStream stream) {
		Workbook hwb = new XSSFWorkbook();
		Map<Project, List<MultipleASFile>> multiple = multipleASFiles(false);
		List<Project> projects = new ArrayList<>(multiple.keySet());
		projects.sort((p1, p2) -> {
			return p1.getName().compareTo(p2.getName());
		});
		for(Project project : projects) {
			Sheet sheet = hwb.createSheet(new StringBuilder().append(project.getName()).append("(").append(project.getLanguage()).append(")").toString());
			List<MultipleASFile> packageMetrics = multiple.get(project);
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

}
