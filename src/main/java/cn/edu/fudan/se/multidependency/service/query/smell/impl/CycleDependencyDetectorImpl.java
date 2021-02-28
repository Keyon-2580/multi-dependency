package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.smell.CycleASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.ModuleService;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.smell.data.CycleComponents;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class CycleDependencyDetectorImpl implements CyclicDependencyDetector {

	@Autowired
	private CycleASRepository asRepository;

	@Autowired
	private CacheService cache;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private NodeService nodeService;

	private Collection<DependsOn> findCyclePackageRelationsBySCC(CycleComponents<Package> cycle) {
		return asRepository.cyclePackagesBySCC(cycle.getPartition());
	}

	private Collection<DependsOn> findCycleFileRelationsBySCC(CycleComponents<ProjectFile> cycle) {
		return asRepository.cycleFilesBySCC(cycle.getPartition());
	}


	private Collection<DependsOn> findCycleTypeRelationsBySCC(CycleComponents<Type> cycle) {
		return asRepository.cycleTypesBySCC(cycle.getPartition());
	}

	private Collection<DependsOn> findCycleModuleRelationsBySCC(CycleComponents<Module> cycle) {
		return asRepository.cycleModulesBySCC(cycle.getPartition());
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Type>>> cycleTypes() {
		String key = "cycleTypes";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<Type>> cycles = asRepository.typeCycles();
		Map<Long, Map<Integer, Cycle<Type>>> result = new HashMap<>();
		for(CycleComponents<Type> cycle : cycles) {
			Cycle<Type> cycleType = new Cycle<Type>(cycle);
			cycleType.addAll(findCycleTypeRelationsBySCC(cycle));
			boolean flag = false;
			Project project = null;
			for(Type type : cycle.getComponents()) {
				if(!flag) {
					project = containRelationService.findTypeBelongToProject(type);
					flag = true;
				}
			}
			if(project != null) {
				Map<Integer, Cycle<Type>> temp = result.getOrDefault(project.getId(), new HashMap<>());
				temp.put(cycleType.getPartition(), cycleType);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<ProjectFile>>> cycleFiles() {
		String key = "cycleFiles";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<ProjectFile>> cycles = asRepository.fileCycles();
		Map<Long, Map<Integer, Cycle<ProjectFile>>> result = new HashMap<>();
		for(CycleComponents<ProjectFile> cycle : cycles) {
			Cycle<ProjectFile> cycleFile = new Cycle<ProjectFile>(cycle);
			cycleFile.addAll(findCycleFileRelationsBySCC(cycle));
			boolean flag = false;
			Project project = null;
			Module lastModule = null;
			for(ProjectFile file : cycle.getComponents()) {
				Module fileBelongToModule = moduleService.findFileBelongToModule(file);
				if(lastModule == null) {
					lastModule = fileBelongToModule;
				}
				cycleFile.putComponentBelongToGroup(file, fileBelongToModule);
				if(!flag) {
					project = containRelationService.findFileBelongToProject(file);
					flag = true;
				}
			}
			Map<Integer, Cycle<ProjectFile>> temp = result.getOrDefault(project.getId(), new HashMap<>());
			temp.put(cycleFile.getPartition(), cycleFile);
			result.put(project.getId(), temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Module>>> cycleModules() {
		String key = "cycleModules";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<Module>> cycles = asRepository.moduleCycles();
		Map<Long, Map<Integer, Cycle<Module>>> result = new HashMap<>();
		for (CycleComponents<Module> cycle : cycles) {
			Cycle<Module> cyclePackage = new Cycle<Module>(cycle);
			cyclePackage.addAll(findCycleModuleRelationsBySCC(cycle));
			boolean flag = false;
			for (Module pck : cycle.getComponents()) {
				cyclePackage.putComponentBelongToGroup(pck, pck);
				if (!flag) {
					Project project = moduleService.findModuleBelongToProject(pck);
					Map<Integer, Cycle<Module>> temp = result.getOrDefault(project.getId(), new HashMap<>());
					temp.put(cyclePackage.getPartition(), cyclePackage);
					result.put(project.getId(), temp);
					flag = true;
				}
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Package>>> cyclePackages() {
		String key = "cyclePackages";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CycleComponents<Package>> cycles = asRepository.packageCycles();
		Map<Long, Map<Integer, Cycle<Package>>> result = new HashMap<>();
		for(CycleComponents<Package> cycle : cycles) {
			Cycle<Package> cyclePackage = new Cycle<Package>(cycle);
			cyclePackage.addAll(findCyclePackageRelationsBySCC(cycle));
			boolean flag = false;
			for(Package pck : cycle.getComponents()) {
				cyclePackage.putComponentBelongToGroup(pck, pck);
				if(!flag) {
					Project project = containRelationService.findPackageBelongToProject(pck);
					Map<Integer, Cycle<Package>> temp = result.getOrDefault(project.getId(), new HashMap<>());
					temp.put(cyclePackage.getPartition(), cyclePackage);
					result.put(project.getId(), temp);
					flag = true;
				}
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public void exportCycleDependency() {
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, Map<Integer, Cycle<Type>>> cycleTypes = cycleTypes();
		Map<Long, Map<Integer, Cycle<ProjectFile>>> cycleFiles = cycleFiles();
		Map<Long, Map<Integer, Cycle<Module>>> cycleModules = cycleModules();
		for (Project project : projects) {
			try {
				exportPackageCycleDependency(project, cycleTypes.get(project.getId()), cycleFiles.get(project.getId()), cycleModules.get(project.getId()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void exportPackageCycleDependency(Project project, Map<Integer, Cycle<Type>> cycleTypes, Map<Integer, Cycle<ProjectFile>> cycleFiles, Map<Integer, Cycle<Module>> cycleModules) {
		Workbook workbook = new XSSFWorkbook();
		exportTypeCycleDependency(workbook, cycleTypes);
		exportFileCycleDependency(workbook, cycleFiles);
		exportModuleCycleDependency(workbook, cycleModules);
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream("Cycle_Dependency_" + project.getName() + "(" + project.getLanguage() + ")" + ".xlsx");
			workbook.write(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				workbook.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void exportTypeCycleDependency(Workbook workbook, Map<Integer, Cycle<Type>> cycleTypes) {
		Sheet sheet = workbook.createSheet("Types");
		ThreadLocal<Integer> rowKey = new ThreadLocal<>();
		rowKey.set(0);
		Row row = sheet.createRow(rowKey.get());
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("Partition");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("Types");
		cell.setCellStyle(style);
		int startRow;
		int endRow;
		int startCol;
		int endCol;
		for (Map.Entry<Integer, Cycle<Type>> entry : cycleTypes.entrySet()) {
			startRow = rowKey.get() + 1;
			Cycle<Type> cycleType = entry.getValue();
			Collection<Type> types = cycleType.getComponents();
			for (Type type : types) {
				rowKey.set(rowKey.get() + 1);
				row = sheet.createRow(rowKey.get());
				cell = row.createCell(0);
				cell.setCellValue(cycleType.getPartition());
				style.setAlignment(HorizontalAlignment.CENTER);
				cell.setCellStyle(style);
				cell = row.createCell(1);
				cell.setCellValue(type.getName());
				style.setAlignment(HorizontalAlignment.LEFT);
				cell.setCellStyle(style);
			}
			endRow = rowKey.get();
			startCol = 0;
			endCol = 0;
			CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
			sheet.addMergedRegion(region);
		}
	}

	public void exportFileCycleDependency(Workbook workbook, Map<Integer, Cycle<ProjectFile>> cycleFiles) {
		Sheet sheet = workbook.createSheet("Files");
		ThreadLocal<Integer> rowKey = new ThreadLocal<>();
		rowKey.set(0);
		Row row = sheet.createRow(rowKey.get());
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("Partition");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("Files");
		cell.setCellStyle(style);
		int startRow;
		int endRow;
		int startCol;
		int endCol;
		for (Map.Entry<Integer, Cycle<ProjectFile>> entry : cycleFiles.entrySet()) {
			startRow = rowKey.get() + 1;
			Cycle<ProjectFile> cycleFile = entry.getValue();
			Collection<ProjectFile> files = cycleFile.getComponents();
			for (ProjectFile file : files) {
				rowKey.set(rowKey.get() + 1);
				row = sheet.createRow(rowKey.get());
				cell = row.createCell(0);
				cell.setCellValue(cycleFile.getPartition());
				style.setAlignment(HorizontalAlignment.CENTER);
				cell.setCellStyle(style);
				cell = row.createCell(1);
				cell.setCellValue(file.getPath());
				style.setAlignment(HorizontalAlignment.LEFT);
				cell.setCellStyle(style);
			}
			endRow = rowKey.get();
			startCol = 0;
			endCol = 0;
			CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
			sheet.addMergedRegion(region);
		}
	}

	public void exportModuleCycleDependency(Workbook workbook, Map<Integer, Cycle<Module>> cycleModules) {
		Sheet sheet = workbook.createSheet("Modules");
		ThreadLocal<Integer> rowKey = new ThreadLocal<>();
		rowKey.set(0);
		Row row = sheet.createRow(rowKey.get());
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("Partition");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("Modules");
		cell.setCellStyle(style);
		int startRow;
		int endRow;
		int startCol;
		int endCol;
		for (Map.Entry<Integer, Cycle<Module>> entry : cycleModules.entrySet()) {
			startRow = rowKey.get() + 1;
			Cycle<Module> cycleModule = entry.getValue();
			Collection<Module> modules = cycleModule.getComponents();
			for (Module module : modules) {
				rowKey.set(rowKey.get() + 1);
				row = sheet.createRow(rowKey.get());
				cell = row.createCell(0);
				cell.setCellValue(cycleModule.getPartition());
				style.setAlignment(HorizontalAlignment.CENTER);
				cell.setCellStyle(style);
				cell = row.createCell(1);
				cell.setCellValue(module.getName());
				style.setAlignment(HorizontalAlignment.LEFT);
				cell.setCellStyle(style);
			}
			endRow = rowKey.get();
			startCol = 0;
			endCol = 0;
			CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
			sheet.addMergedRegion(region);
		}
	}
}
