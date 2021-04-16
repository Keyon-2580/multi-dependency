package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class CyclicDependencyDetectorImpl implements CyclicDependencyDetector {

	@Autowired
	private CycleASRepository cycleASRepository;

	@Autowired
	private CacheService cache;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private DependsOnRepository dependsOnRepository;

	@Autowired
	private SmellRepository smellRepository;

	public static final int DEFAULT_THRESHOLD_LONGEST_PATH = 3;
	public static final double DEFAULT_THRESHOLD_MINIMUM_RATE = 0.5;

	private Collection<DependsOn> findCycleTypeRelationsBySCC(Cycle<Type> cycle) {
		return cycleASRepository.cycleTypesBySCC(cycle.getPartition());
	}

	private Collection<DependsOn> findCycleFileRelationsBySCC(Cycle<ProjectFile> cycle) {
		return cycleASRepository.cycleFilesBySCC(cycle.getPartition());
	}

	private Collection<DependsOn> findCyclePackageRelationsBySCC(Cycle<Package> cycle) {
		return cycleASRepository.cyclePackagesBySCC(cycle.getPartition());
	}

	private Collection<DependsOn> findCycleModuleRelationsBySCC(Cycle<Module> cycle) {
		return cycleASRepository.cycleModulesBySCC(cycle.getPartition());
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Type>>> getTypeCyclicDependency() {
		String key = "typeCycles";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, Map<Integer, Cycle<Type>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.TYPE, SmellType.CYCLIC_DEPENDENCY));
		smells.sort((smell1, smell2) -> {
			List<String> namePart1 = Arrays.asList(smell1.getName().split("_"));
			List<String> namePart2 = Arrays.asList(smell2.getName().split("_"));
			int partition1 = Integer.parseInt(namePart1.get(namePart1.size() - 1));
			int partition2 = Integer.parseInt(namePart2.get(namePart2.size() - 1));
			return Integer.compare(partition1, partition2);
		});
		List<Cycle<Type>> typeCycles = new ArrayList<>();
		int partition = 1;
		for (Smell smell : smells) {
			List<Type> components = new ArrayList<>();
			Set<Node> contains = new HashSet<>(smellRepository.findSmellContains(smell.getId()));
			for (Node contain : contains) {
				components.add((Type) contain);
			}
			typeCycles.add(new Cycle<>(partition, components));
			partition ++;
		}
		for(Cycle<Type> typeCycle : typeCycles) {
			Project project = containRelationService.findTypeBelongToProject(typeCycle.getComponents().get(0));
			if (project != null) {
				Map<Integer, Cycle<Type>> temp = result.getOrDefault(project.getId(), new HashMap<>());
				temp.put(typeCycle.getPartition(), typeCycle);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<ProjectFile>>> getFileCyclicDependency() {
		String key = "fileCycles";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, Map<Integer, Cycle<ProjectFile>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.CYCLIC_DEPENDENCY));
		smells.sort((smell1, smell2) -> {
			List<String> namePart1 = Arrays.asList(smell1.getName().split("_"));
			List<String> namePart2 = Arrays.asList(smell2.getName().split("_"));
			int partition1 = Integer.parseInt(namePart1.get(namePart1.size() - 1));
			int partition2 = Integer.parseInt(namePart2.get(namePart2.size() - 1));
			return Integer.compare(partition1, partition2);
		});
		List<Cycle<ProjectFile>> fileCycles = new ArrayList<>();
		int partition = 1;
		for (Smell smell : smells) {
			List<ProjectFile> components = new ArrayList<>();
			Set<Node> contains = new HashSet<>(smellRepository.findSmellContains(smell.getId()));
			for (Node contain : contains) {
				components.add((ProjectFile) contain);
			}
			fileCycles.add(new Cycle<>(partition, components));
			partition ++;
		}
		for(Cycle<ProjectFile> fileCycle : fileCycles) {
			Project project = containRelationService.findFileBelongToProject(fileCycle.getComponents().get(0));
			if (project != null) {
				Map<Integer, Cycle<ProjectFile>> temp = result.getOrDefault(project.getId(), new HashMap<>());
				temp.put(fileCycle.getPartition(), fileCycle);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Package>>> getPackageCyclicDependency() {
		String key = "packageCycles";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, Map<Integer, Cycle<Package>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.PACKAGE, SmellType.CYCLIC_DEPENDENCY));
		smells.sort((smell1, smell2) -> {
			List<String> namePart1 = Arrays.asList(smell1.getName().split("_"));
			List<String> namePart2 = Arrays.asList(smell2.getName().split("_"));
			int partition1 = Integer.parseInt(namePart1.get(namePart1.size() - 1));
			int partition2 = Integer.parseInt(namePart2.get(namePart2.size() - 1));
			return Integer.compare(partition1, partition2);
		});
		List<Cycle<Package>> packageCycles = new ArrayList<>();
		int partition = 1;
		for (Smell smell : smells) {
			List<Package> components = new ArrayList<>();
			Set<Node> contains = new HashSet<>(smellRepository.findSmellContains(smell.getId()));
			for (Node contain : contains) {
				components.add((Package) contain);
			}
			packageCycles.add(new Cycle<>(partition, components));
			partition ++;
		}
		for(Cycle<Package> packageCycle : packageCycles) {
			Project project = containRelationService.findPackageBelongToProject(packageCycle.getComponents().get(0));
			if (project != null) {
				Map<Integer, Cycle<Package>> temp = result.getOrDefault(project.getId(), new HashMap<>());
				temp.put(packageCycle.getPartition(), packageCycle);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Module>>> getModuleCyclicDependency() {
		String key = "moduleCycles";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, Map<Integer, Cycle<Module>>> result = new HashMap<>();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Type>>> detectTypeCyclicDependency() {
		Map<Long, Map<Integer, Cycle<Type>>> result = new HashMap<>();
		Collection<Cycle<Type>> cycles = cycleASRepository.typeCycles();
		List<List<Type>> componentsList = new ArrayList<>();
		for (Cycle<Type> cycle : cycles) {
			List<Type> types = new ArrayList<>(cycle.getComponents());
			List<DependsOn> relations = new ArrayList<>(findCycleTypeRelationsBySCC(cycle));
			Map<Type, Integer> indexMap = new HashMap<>();

			//将节点映射为数字
			int index = 0;
			for (Type type : types) {
				indexMap.put(type, index);
				index++;
			}

			//最短路径初始化
			int number = index;
			int[][] distanceMap = new int[number][number];
			for (int i = 0; i < number; i++) {
				for (int j = 0; j < number; j++) {
					distanceMap[i][j] = -1;
				}
				distanceMap[i][i] = 0;
			}

			//沿途节点初始化
			Map<String, Set<Integer>> pathMap = new HashMap<>();
			for (DependsOn relation : relations) {
				int sourceIndex = indexMap.get((Type) relation.getStartNode());
				int targetIndex = indexMap.get((Type) relation.getEndNode());
				distanceMap[sourceIndex][targetIndex] = 1;
				String pathKey = String.join("_", String.valueOf(sourceIndex), String.valueOf(targetIndex));
				Set<Integer> path = new HashSet<>();
				path.add(sourceIndex);
				path.add(targetIndex);
				pathMap.put(pathKey, path);
			}

			//获取结果
			List<Set<Integer>> indexCycles = new ArrayList<>(ShortestPathCycleFilter(distanceMap, pathMap, number, DEFAULT_THRESHOLD_LONGEST_PATH, DEFAULT_THRESHOLD_MINIMUM_RATE));
			for (Set<Integer> indexCycle : indexCycles) {
				List<Type> components = new ArrayList<>();
				for(Integer i : indexCycle) {
					components.add(types.get(i));
				}
				componentsList.add(components);
			}
		}

		//生成结果
		componentsList.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));
		int partition = 1;
		for (List<Type> components : componentsList) {
			Cycle<Type> typeCycle = new Cycle<>(partition, components);
			Project project = containRelationService.findTypeBelongToProject(components.get(0));
			if (project != null) {
				Map<Integer, Cycle<Type>> temp = result.getOrDefault(project.getId(), new HashMap<>());
				temp.put(typeCycle.getPartition(), typeCycle);
				result.put(project.getId(), temp);
			}
			partition ++;
		}
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<ProjectFile>>> detectFileCyclicDependency() {
		Map<Long, Map<Integer, Cycle<ProjectFile>>> result = new HashMap<>();
		Collection<Cycle<ProjectFile>> cycles = cycleASRepository.fileCycles();
		List<List<ProjectFile>> componentsList = new ArrayList<>();
		for (Cycle<ProjectFile> cycle : cycles) {
			List<ProjectFile> files = new ArrayList<>(cycle.getComponents());
			List<DependsOn> relations = new ArrayList<>(findCycleFileRelationsBySCC(cycle));
			Map<ProjectFile, Integer> indexMap = new HashMap<>();

			//将节点映射为数字
			int index = 0;
			for (ProjectFile file : files) {
				indexMap.put(file, index);
				index++;
			}

			//最短路径初始化
			int number = index;
			int[][] distanceMap = new int[number][number];
			for (int i = 0; i < number; i++) {
				for (int j = 0; j < number; j++) {
					distanceMap[i][j] = -1;
				}
				distanceMap[i][i] = 0;
			}

			//沿途节点初始化
			Map<String, Set<Integer>> pathMap = new HashMap<>();
			for (DependsOn relation : relations) {
				int sourceIndex = indexMap.get((ProjectFile) relation.getStartNode());
				int targetIndex = indexMap.get((ProjectFile) relation.getEndNode());
				distanceMap[sourceIndex][targetIndex] = 1;
				String pathKey = String.join("_", String.valueOf(sourceIndex), String.valueOf(targetIndex));
				Set<Integer> path = new HashSet<>();
				path.add(sourceIndex);
				path.add(targetIndex);
				pathMap.put(pathKey, path);
			}

			//获取结果
			List<Set<Integer>> indexCycles = new ArrayList<>(ShortestPathCycleFilter(distanceMap, pathMap, number, DEFAULT_THRESHOLD_LONGEST_PATH, DEFAULT_THRESHOLD_MINIMUM_RATE));
			for (Set<Integer> indexCycle : indexCycles) {
				List<ProjectFile> components = new ArrayList<>();
				for(Integer i : indexCycle) {
					components.add(files.get(i));
				}
				componentsList.add(components);
			}
		}

		//生成结果
		componentsList.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));
		int partition = 1;
		for (List<ProjectFile> components : componentsList) {
			Cycle<ProjectFile> fileCycle = new Cycle<>(partition, components);
			Project project = containRelationService.findFileBelongToProject(components.get(0));
			if (project != null) {
				Map<Integer, Cycle<ProjectFile>> temp = result.getOrDefault(project.getId(), new HashMap<>());
				temp.put(fileCycle.getPartition(), fileCycle);
				result.put(project.getId(), temp);
			}
			partition ++;
		}
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Package>>> detectPackageCyclicDependency() {
		Map<Long, Map<Integer, Cycle<Package>>> result = new HashMap<>();
		Collection<Cycle<Package>> cycles = cycleASRepository.packageCycles();
		List<List<Package>> componentsList = new ArrayList<>();
		for (Cycle<Package> cycle : cycles) {
			List<Package> files = new ArrayList<>(cycle.getComponents());
			List<DependsOn> relations = new ArrayList<>(findCyclePackageRelationsBySCC(cycle));
			Map<Package, Integer> indexMap = new HashMap<>();

			//将节点映射为数字
			int index = 0;
			for (Package file : files) {
				indexMap.put(file, index);
				index++;
			}

			//最短路径初始化
			int number = index;
			int[][] distanceMap = new int[number][number];
			for (int i = 0; i < number; i++) {
				for (int j = 0; j < number; j++) {
					distanceMap[i][j] = -1;
				}
				distanceMap[i][i] = 0;
			}

			//沿途节点初始化
			Map<String, Set<Integer>> pathMap = new HashMap<>();
			for (DependsOn relation : relations) {
				int sourceIndex = indexMap.get((Package) relation.getStartNode());
				int targetIndex = indexMap.get((Package) relation.getEndNode());
				distanceMap[sourceIndex][targetIndex] = 1;
				String pathKey = String.join("_", String.valueOf(sourceIndex), String.valueOf(targetIndex));
				Set<Integer> path = new HashSet<>();
				path.add(sourceIndex);
				path.add(targetIndex);
				pathMap.put(pathKey, path);
			}

			//获取结果
			List<Set<Integer>> indexCycles = new ArrayList<>(ShortestPathCycleFilter(distanceMap, pathMap, number, DEFAULT_THRESHOLD_LONGEST_PATH, DEFAULT_THRESHOLD_MINIMUM_RATE));
			for (Set<Integer> indexCycle : indexCycles) {
				List<Package> components = new ArrayList<>();
				for(Integer i : indexCycle) {
					components.add(files.get(i));
				}
				componentsList.add(components);
			}
		}

		//生成结果
		componentsList.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));
		int partition = 1;
		for (List<Package> components : componentsList) {
			Cycle<Package> packageCycle = new Cycle<>(partition, components);
			Project project = containRelationService.findPackageBelongToProject(components.get(0));
			if (project != null) {
				Map<Integer, Cycle<Package>> temp = result.getOrDefault(project.getId(), new HashMap<>());
				temp.put(packageCycle.getPartition(), packageCycle);
				result.put(project.getId(), temp);
			}
			partition ++;
		}
		return result;
	}

	@Override
	public Map<Long, Map<Integer, Cycle<Module>>> detectModuleCyclicDependency() {
		Map<Long, Map<Integer, Cycle<Module>>> result = new HashMap<>();
		Collection<Cycle<Module>> cycles = cycleASRepository.moduleCycles();
		List<List<Module>> componentsList = new ArrayList<>();
		for (Cycle<Module> cycle : cycles) {
			List<Module> modules = new ArrayList<>(cycle.getComponents());
			List<DependsOn> relations = new ArrayList<>(findCycleModuleRelationsBySCC(cycle));
			Map<Module, Integer> indexMap = new HashMap<>();

			//将节点映射为数字
			int index = 0;
			for (Module module : modules) {
				indexMap.put(module, index);
				index++;
			}

			//最短路径初始化
			int number = index;
			int[][] distanceMap = new int[number][number];
			for (int i = 0; i < number; i++) {
				for (int j = 0; j < number; j++) {
					distanceMap[i][j] = -1;
				}
				distanceMap[i][i] = 0;
			}

			//沿途节点初始化
			Map<String, Set<Integer>> pathMap = new HashMap<>();
			for (DependsOn relation : relations) {
				int sourceIndex = indexMap.get((Module) relation.getStartNode());
				int targetIndex = indexMap.get((Module) relation.getEndNode());
				distanceMap[sourceIndex][targetIndex] = 1;
				String pathKey = String.join("_", String.valueOf(sourceIndex), String.valueOf(targetIndex));
				Set<Integer> path = new HashSet<>();
				path.add(sourceIndex);
				path.add(targetIndex);
				pathMap.put(pathKey, path);
			}

			//获取结果
			List<Set<Integer>> indexCycles = new ArrayList<>(ShortestPathCycleFilter(distanceMap, pathMap, number, DEFAULT_THRESHOLD_LONGEST_PATH, DEFAULT_THRESHOLD_MINIMUM_RATE));
			for (Set<Integer> indexCycle : indexCycles) {
				List<Module> components = new ArrayList<>();
				for(Integer i : indexCycle) {
					components.add(modules.get(i));
				}
				componentsList.add(components);
			}
		}

		//生成结果
		componentsList.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));
		int partition = 1;
		for(List<Module> components : componentsList) {
			Cycle<Module> moduleCycle = new Cycle<>(partition, components);
			Project project = moduleService.findModuleBelongToProject(components.get(0));
			if (project != null) {
				Map<Integer, Cycle<Module>> temp = result.getOrDefault(project.getId(), new HashMap<>());
				temp.put(moduleCycle.getPartition(), moduleCycle);
				result.put(project.getId(), temp);
			}
			partition ++;
		}
		return result;
	}

	@Override
	public JSONObject getCyclicDependencyJson(Long fileId) {
		JSONObject result = new JSONObject();
		JSONArray nodesJson = new JSONArray();
		JSONArray edgesJson = new JSONArray();
		JSONArray smellsJson = new JSONArray();
		List<Smell> smells = new ArrayList<>(smellRepository.getSmellsWithFileId(fileId));
		List<ProjectFile> files = new ArrayList<>();
		for (Smell smell : smells) {
			JSONObject smellJson = new JSONObject();
			smellJson.put("name", smell.getName());
			List<ProjectFile> smellFiles = new ArrayList<>(smellRepository.getFilesWithSmellName(smell.getName()));
			JSONArray smellFilesJson = new JSONArray();
			for (ProjectFile smellFile : smellFiles) {
				if (!files.contains(smellFile)) {
					files.add(smellFile);
				}
				JSONObject smellFileJson = new JSONObject();
				smellFileJson.put("index", files.indexOf(smellFile) + 1);
				smellFileJson.put("path", smellFile.getPath());
				smellFilesJson.add(smellFileJson);
			}
			smellJson.put("files", smellFilesJson);
			smellsJson.add(smellJson);
		}
		int length = files.size();
		for (int i = 0; i < length; i ++) {
			ProjectFile sourceFile = files.get(i);
			JSONObject nodeJson = new JSONObject();
			nodeJson.put("id", sourceFile.getId().toString());
			nodeJson.put("name", sourceFile.getName());
			nodeJson.put("path", sourceFile.getPath());
			nodeJson.put("label", i + 1);
			nodeJson.put("size", getSizeOfFileByLoc(sourceFile.getLoc()));
			nodesJson.add(nodeJson);
			for (int j = 0 ; j < length; j ++) {
				ProjectFile targetFile = files.get(j);
				if (i != j) {
					DependsOn dependsOn = dependsOnRepository.findDependsOnBetweenFiles(sourceFile.getId(), targetFile.getId());
					if (dependsOn != null) {
						JSONObject edgeJson = new JSONObject();
						edgeJson.put("id", dependsOn.getId().toString());
						edgeJson.put("source", sourceFile.getId().toString());
						edgeJson.put("target", targetFile.getId().toString());
						edgeJson.put("source_name", sourceFile.getName());
						edgeJson.put("target_name", targetFile.getName());
						edgeJson.put("source_label", i + 1);
						edgeJson.put("target_label", j + 1);
						edgeJson.put("times", dependsOn.getTimes());
						edgeJson.put("dependsOnTypes", dependsOn.getDependsOnTypes());
						edgesJson.add(edgeJson);
					}
				}
			}
		}
		result.put("smellType", SmellType.CYCLIC_DEPENDENCY);
		result.put("currentFile", fileId.toString());
		result.put("nodes", nodesJson);
		result.put("edges", edgesJson);
		result.put("smells", smellsJson);
		return result;
	}

	private int getSizeOfFileByLoc(int loc) {
		int size;
		if (loc <= 500) {
			size = 40;
		}
		else if (loc <= 1000) {
			size = 50;
		}
		else if (loc <= 2000) {
			size = 60;
		}
		else {
			size = 70;
		}
		return size;
	}

	@Override
	public void exportCycleDependency() {
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, Map<Integer, Cycle<Type>>> cycleTypes = detectTypeCyclicDependency();
		Map<Long, Map<Integer, Cycle<ProjectFile>>> cycleFiles = detectFileCyclicDependency();
		Map<Long, Map<Integer, Cycle<Module>>> cycleModules = detectModuleCyclicDependency();
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

	//计算最短路径，并记录沿途节点，对结果进行合并，以此得到Cycles
	private List<Set<Integer>> ShortestPathCycleFilter(int[][] distanceMap, Map<String, Set<Integer>> pathMap, int number, int longestPath, double minimumRate) {
		List<Set<Integer>> result = new ArrayList<>();
		List<Set<Integer>> indexCycles = new ArrayList<>();

		//计算最短路径，并记录沿途节点
		for (int k = 0; k < number; k ++) {
			for (int i = 0; i < number; i ++) {
				for (int j = 0; j < number; j ++) {
					if (distanceMap[i][k] != -1 && distanceMap[k][j] != -1) {
						if (distanceMap[i][j] == -1 || distanceMap[i][j] > distanceMap[i][k] + distanceMap[k][j]) {
							distanceMap[i][j] = distanceMap[i][k] + distanceMap[k][j];
							String keyOfIJ = String.join("_", String.valueOf(i), String.valueOf(j));
							String keyOfIK = String.join("_", String.valueOf(i), String.valueOf(k));
							String keyOfKJ = String.join("_", String.valueOf(k), String.valueOf(j));
							Set<Integer> pathOfIJ = new HashSet<>();
							Set<Integer> pathOfIK = pathMap.getOrDefault(keyOfIK, new HashSet<>());
							Set<Integer> pathOfKJ = pathMap.getOrDefault(keyOfKJ, new HashSet<>());
							pathOfIJ.addAll(pathOfIK);
							pathOfIJ.addAll(pathOfKJ);
							pathMap.put(keyOfIJ, pathOfIJ);
						}
					}
				}
			}
		}

		//筛选满足最长路径条件的环
		for (int i = 0; i < number; i ++) {
			for (int j = i + 1; j < number; j++) {
				if (distanceMap[i][j] > 0 && distanceMap[j][i] > 0 && distanceMap[i][j] + distanceMap[j][i] <= longestPath) {
					String keyOfIJ = String.join("_", String.valueOf(i), String.valueOf(j));
					String keyOfJI = String.join("_", String.valueOf(j), String.valueOf(i));
					Set<Integer> path = new HashSet<>();
					path.addAll(pathMap.getOrDefault(keyOfIJ, new HashSet<>()));
					path.addAll(pathMap.getOrDefault(keyOfJI, new HashSet<>()));
					indexCycles.add(path);
				}
			}
		}

		//融合
		int mergeCount;
		int length = indexCycles.size();
		do {
			mergeCount = 0;
			for (int i = 0; i < length; i ++) {
				Set<Integer> firstCycle = new HashSet<>(indexCycles.get(i));
				if(firstCycle.size() == 0) {
					continue;
				}
				for (int j = i + 1; j < length; j ++) {
					Set<Integer> secondCycle = new HashSet<>(indexCycles.get(j));
					if(secondCycle.size() == 0) {
						continue;
					}
					Set<Integer> mergeCycle = new HashSet<>(firstCycle);
					mergeCycle.retainAll(secondCycle);
					double firstSimilarity = ((double) mergeCycle.size() / (double) firstCycle.size());
					double secondSimilarity = ((double) mergeCycle.size() / (double) secondCycle.size());
					if (firstSimilarity >= minimumRate || secondSimilarity >= minimumRate) {
						firstCycle.addAll(secondCycle);
						indexCycles.set(i, firstCycle);
						indexCycles.set(j, new HashSet<>());
						mergeCount ++;
					}
				}
			}
		}
		while(mergeCount > 0);

		//去空
		for (Set<Integer> indexCycle : indexCycles) {
			if(indexCycle.size() == 0) {
				continue;
			}
			result.add(indexCycle);
		}

		return result;
	}
}
