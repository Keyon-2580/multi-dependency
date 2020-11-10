package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.AggregationCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneValueService;
import cn.edu.fudan.se.multidependency.service.query.clone.data.FileCloneWithCoChange;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class HotspotPackagePairDetectorImpl implements HotspotPackagePairDetector {

	@Autowired
	private DependsOnRepository dependsOnRepository;

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	@Autowired
	private SummaryAggregationDataService summaryAggregationDataService;

	@Autowired
	private HasRelationService hasRelationService;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private AggregationCloneRepository aggregationCloneRepository;

	@Autowired
	private CoChangeRepository coChangeRepository;

	@Autowired
	private ModuleCloneRepository moduleCloneRepository;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private CloneValueService cloneValueService;

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairs() {
		return detectHotspotPackagePairWithFileClone();
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithDependsOnByProjectId(long projectId) {
		List<HotspotPackagePair> result = new ArrayList<>();
		List<DependsOn> projectDependsOn = dependsOnRepository.findPackageDependsInProject(projectId);
		if(projectDependsOn != null && !projectDependsOn.isEmpty()){
			Map<Package, Map<Package, List<DependsOn>>> packageDependsPackage = new HashMap<>();
			for (DependsOn dependsOn : projectDependsOn){
				Package startNode = (Package) dependsOn.getStartNode();
				Package endNode = (Package) dependsOn.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				Map<Package, List<DependsOn>> dependsPackage = packageDependsPackage.getOrDefault(pck1, new HashMap<>());
				List<DependsOn> dependsOns = dependsPackage.getOrDefault(pck2, new ArrayList<>());
				dependsOns.add(dependsOn);
				dependsPackage.put(pck2, dependsOns);
				packageDependsPackage.put(pck1, dependsPackage);
			}
			for(Map.Entry<Package, Map<Package, List<DependsOn>>> entry : packageDependsPackage.entrySet()){
				Package pck1 = entry.getKey();
				Map<Package, List<DependsOn>> dependsPackage = entry.getValue();
				for(Map.Entry<Package, List<DependsOn>> entryKey : dependsPackage.entrySet()){
					Package pck2 = entryKey.getKey();
					List<DependsOn> dependsOns = dependsPackage.getOrDefault(pck2, new ArrayList<>());
					HotspotPackagePair hotspotPackagePair = createHotspotPackagePairWithDepends(pck1, pck2, dependsOns);
					result.add(hotspotPackagePair);
				}
			}
		}
		return result;
	}

	@Override
	public HotspotPackagePair detectHotspotPackagePairWithDependsOnByPackageId(long pck1Id, long pck2Id) {
		List<DependsOn> packageDependsOnList = dependsOnRepository.findPackageDependsByPackageId(pck1Id, pck2Id);
		HotspotPackagePair hotspotPackagePair = null;
		if(packageDependsOnList != null && !packageDependsOnList.isEmpty()){
			Package tmp1 = (Package) packageDependsOnList.get(0).getStartNode();
			Package tmp2 = (Package) packageDependsOnList.get(0).getEndNode();
			Package pck1 = tmp1.getId() == pck1Id ? tmp1 : tmp2;
			Package pck2 = tmp2.getId() == pck2Id ? tmp2 : tmp1;
			hotspotPackagePair = createHotspotPackagePairWithDepends(pck1, pck2, packageDependsOnList);
		}
		return hotspotPackagePair;
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithFileClone() {
		Map<Long, Integer> directoryIdToAllNodes = new HashMap<>();
		Map<Long, Integer> directoryIdToAllLoc = new HashMap<>();
		Map<String, HotspotPackagePair> directoryPathToHotspotPackagePair = new HashMap<>();
		Map<String, Collection<String>> directoryPathToCloneChildrenPackages = new HashMap<>();
		Map<String, Set<ProjectFile>> directoryPathToAllCloneChildrenPackages = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Map<String, Boolean> isHotspot = new HashMap<>();
		Collection<? extends Relation> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		List<BasicDataForDoubleNodes<Node, Relation>> packageClones = summaryAggregationDataService.queryPackageCloneFromFileCloneSort(fileClones);
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByFileClone.getInstance();

		//预处理
		for(BasicDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			Package parentPackage1 = hasRelationService.findPackageInPackage(currentPackage1);
			Package parentPackage2 = hasRelationService.findPackageInPackage(currentPackage2);
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				String parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				Collection<String> cloneChildren = new ArrayList<>();
				if(!directoryPathToCloneChildrenPackages.containsKey(parentPackages)) {
					directoryPathToCloneChildrenPackages.put(parentPackages, cloneChildren);
				}
				cloneChildren = directoryPathToCloneChildrenPackages.get(parentPackages);
				if(cloneChildren.contains(currentPackages)) {
					break;
				}
				cloneChildren.add(currentPackages);
				currentPackage1 = parentPackage1;
				currentPackage2 = parentPackage2;
				parentPackage1 = hasRelationService.findPackageInPackage(currentPackage1);
				parentPackage2 = hasRelationService.findPackageInPackage(currentPackage2);
			}
		}

		//检测
		for(BasicDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			if(isHotspot.containsKey(currentPackages)) {
				continue;
			}

			//统计包内文件克隆情况，克隆类型、代码行以及克隆相似度
			int clonePairs = packageClone.sizeOfChildren();
			int cloneNodesCount1 = packageClone.getNodesInNode1().size();
			int cloneNodesCount2 = packageClone.getNodesInNode2().size();
			int allNodesCount1 = packageClone.getAllNodesInNode1().size();
			int allNodesCount2 = packageClone.getAllNodesInNode2().size();
			int cloneNodesLoc1 = 0;
			int cloneNodesLoc2 = 0;
			int allNodesLoc1 = getAllFilesLoc(directoryIdToAllLoc, currentPackage1);
			int allNodesLoc2 = getAllFilesLoc(directoryIdToAllLoc, currentPackage2);
			int cloneType1Count = 0;
			int cloneType2Count = 0;
			int cloneType3Count = 0;
			double cloneSimilarityValue = 0.00;
			List<FileCloneWithCoChange> childrenClonePairs;
			try {
				childrenClonePairs = cloneValueService.queryPackageCloneWithFileCoChange(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), currentPackage1, currentPackage2).getChildren();
				childrenClonePairs.sort((d1, d2) -> {
					double value1 = d1.getFileClone().getValue();
					double value2 = d2.getFileClone().getValue();
					if(value1 != value2) {
						return Double.compare(value2, value1);
					}
					else {
						String cloneType1 = d1.getFileClone().getCloneType();
						String cloneType2 = d2.getFileClone().getCloneType();
						return cloneType1.compareTo(cloneType2);
					}
				});
				String path1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				String path2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
				Set<ProjectFile> cloneChildrenFiles1 = new HashSet<>();
				Set<ProjectFile> cloneChildrenFiles2 = new HashSet<>();
				for(FileCloneWithCoChange childrenClonePair : childrenClonePairs) {
					String cloneType = childrenClonePair.getFileClone().getCloneType();
					switch (cloneType){
						case "type_1":
							cloneType1Count++;
							break;
						case "type_2":
							cloneType2Count++;
							break;
						case "type_3":
							cloneType3Count++;
							break;
					}
					cloneSimilarityValue += childrenClonePair.getFileClone().getValue();
					cloneChildrenFiles1.add(childrenClonePair.getFile1());
					cloneChildrenFiles2.add(childrenClonePair.getFile2());
				}
				for(ProjectFile cloneChildFile1 : cloneChildrenFiles1) {
					cloneNodesLoc1 += cloneChildFile1.getLoc();
				}
				for(ProjectFile cloneChildFile2 : cloneChildrenFiles2) {
					cloneNodesLoc2 += cloneChildFile2.getLoc();
				}
				directoryPathToAllCloneChildrenPackages.put(path1, cloneChildrenFiles1);
				directoryPathToAllCloneChildrenPackages.put(path2, cloneChildrenFiles2);
			} catch (Exception e) {
				System.out.println(e.toString());
			}

			//存储数据
			HotspotPackagePair currentHotspotPackagePair = findHotspotPackage(fileClones, directoryPathToHotspotPackagePair, currentPackage1, currentPackage2);
			CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();
			currentPackagePairCloneRelationData.setClonePairs(clonePairs);
			currentPackagePairCloneRelationData.setCloneCountDate(cloneNodesCount1, cloneNodesCount2, allNodesCount1, allNodesCount2);
			currentPackagePairCloneRelationData.setCloneTypeDate(cloneType1Count, cloneType2Count, cloneType3Count, cloneSimilarityValue);
			currentPackagePairCloneRelationData.setCloneLocDate(cloneNodesLoc1, cloneNodesLoc2, allNodesLoc1, allNodesLoc2);
			currentHotspotPackagePair.setPackagePairRelationData(currentPackagePairCloneRelationData);
			//检测
			Collection<String> cloneChildren = new ArrayList<>();
			if(directoryPathToCloneChildrenPackages.containsKey(currentPackages)) {
				cloneChildren = directoryPathToCloneChildrenPackages.get(currentPackages);
			}
			if(!cloneChildren.isEmpty()) {
				continue;
			}
			Package parentPackage1 = hasRelationService.findPackageInPackage(currentPackage1);
			Package parentPackage2 = hasRelationService.findPackageInPackage(currentPackage2);
			isHotspot.put(currentPackages, isHotspotPackage(aggregator, directoryIdToAllNodes, directoryIdToAllLoc, directoryPathToAllCloneChildrenPackages, currentHotspotPackagePair));
			isChild.put(currentPackages, false);
			String parentPackages;
			HotspotPackagePair parentHotspotPackagePair;
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				currentHotspotPackagePair = findHotspotPackage(fileClones, directoryPathToHotspotPackagePair, currentPackage1, currentPackage2);
				parentHotspotPackagePair = findHotspotPackage(fileClones, directoryPathToHotspotPackagePair, parentPackage1, parentPackage2);
				currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				if(isHotspot.containsKey(parentPackages)) {
					break;
				}
				cloneChildren = new ArrayList<>();
				if(directoryPathToCloneChildrenPackages.containsKey(parentPackages)) {
					cloneChildren = directoryPathToCloneChildrenPackages.get(parentPackages);
					cloneChildren.remove(currentPackages);
					parentHotspotPackagePair.addHotspotChild(currentHotspotPackagePair);
				}
				if(!cloneChildren.isEmpty()) {
					break;
				}
				isHotspot.put(parentPackages, isHotspotPackage(aggregator, directoryIdToAllNodes, directoryIdToAllLoc, directoryPathToAllCloneChildrenPackages, parentHotspotPackagePair));
				if(isHotspot.get(parentPackages)) {
					isChild.put(currentPackages, true);
				}
				isChild.put(parentPackages, false);
				currentPackage1 = parentPackage1;
				currentPackage2 = parentPackage2;
				parentPackage1 = hasRelationService.findPackageInPackage(currentPackage1);
				parentPackage2 = hasRelationService.findPackageInPackage(currentPackage2);
			}
		}

		//确定根目录
		List<HotspotPackagePair> result = new ArrayList<>();
		for(Map.Entry<String, HotspotPackagePair> entry : directoryPathToHotspotPackagePair.entrySet()) {
			String currentPackages = entry.getKey();
			HotspotPackagePair hotspotPackagePair = entry.getValue();
			Package currentPackage1 = hotspotPackagePair.getPackage1();
			Package currentPackage2 = hotspotPackagePair.getPackage2();
			if((!isChild.get(currentPackages) && isHotspot.get(currentPackages))) {
				Package parentPackage1 = hasRelationService.findPackageInPackage(currentPackage1);
				Package parentPackage2 = hasRelationService.findPackageInPackage(currentPackage2);
				if(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
					String parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
					if((isHotspot.containsKey(parentPackages) && isHotspot.get(parentPackages))) {
						isChild.put(currentPackages, true);
						continue;
					}
				}
				if(!result.contains(hotspotPackagePair)) {
					result.add(hotspotPackagePair);
				}
			}
		}
		return result;
	}

	@Override
	public HotspotPackagePair detectHotspotPackagePairWithFileCloneByPackageId(long pck1Id, long pck2Id, String language) {
		AggregationClone aggregationClone = aggregationCloneRepository.findAggregationCloneByPackageId(pck1Id, pck2Id);
		return setHotspotPackageData(aggregationClone, language);
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithFileCloneByParentId(long parent1Id, long parent2Id, String language) {
		List<HotspotPackagePair> result = new ArrayList<>();
		if(language.equals("all")) {
			result.addAll(loadHotspotPackagesByParentId(parent1Id, parent2Id, "java"));
			result.addAll(loadHotspotPackagesByParentId(parent1Id, parent2Id, "cpp"));
		}
		else {
			result.addAll(loadHotspotPackagesByParentId(parent1Id, parent2Id, language));
		}
		return result;
	}

	@Override
	public void exportHotspotPackages(OutputStream stream) {
		Workbook hwb = new XSSFWorkbook();
		setSheetInformation(hwb, "java");
		setSheetInformation(hwb, "cpp");
		try {
			hwb.write(stream);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				stream.close();
				hwb.close();
			}
			catch (IOException ignored) {
			}
		}
	}

	private void setSheetInformation(Workbook hwb, String language) {
		ThreadLocal<Integer> rowKey = new ThreadLocal<>();
		rowKey.set(0);
		Collection<HotspotPackagePair> hotspotPackagePairs = detectHotspotPackagePairWithFileCloneByParentId(-1 ,-1, language);
		Sheet sheet = hwb.createSheet(language);
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		CellStyle style = hwb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("目录1");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("目录2");
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("克隆文件占比");
		cell.setCellStyle(style);
		cell = row.createCell(3);
		cell.setCellValue("克隆CoChange占比");
		cell.setCellStyle(style);
		cell = row.createCell(4);
		cell.setCellValue("克隆Loc占比");
		cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("克隆相似度");
		cell = row.createCell(6);
		cell.setCellValue("type");
		cell = row.createCell(7);
		cell.setCellValue("克隆文件对数");
		cell.setCellStyle(style);
		for(HotspotPackagePair hotspotPackagePair : hotspotPackagePairs){
			loadHotspotPackageResult(sheet, rowKey, 0, hotspotPackagePair);
		}
	}

	private void loadHotspotPackageResult(Sheet sheet, ThreadLocal<Integer> rowKey, int layer, HotspotPackagePair currentHotspotPackagePair){
		StringBuilder prefix = new StringBuilder();
		for(int i = 0; i < layer; i++) {
			prefix.append("|---");
		}
		CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();

		int clonePairs = currentPackagePairCloneRelationData.getClonePairs();
		int cloneNodesCount1 = currentPackagePairCloneRelationData.getCloneNodesCount1();
		int cloneNodesCount2 = currentPackagePairCloneRelationData.getCloneNodesCount2();
		int allNodesCount1 = currentPackagePairCloneRelationData.getAllNodesCount1();
		int allNodesCount2 = currentPackagePairCloneRelationData.getAllNodesCount2();
		double cloneMatchRate = currentPackagePairCloneRelationData.getCloneMatchRate();
		int cloneNodesLoc1 = currentPackagePairCloneRelationData.getCloneNodesLoc1();
		int cloneNodesLoc2 = currentPackagePairCloneRelationData.getCloneNodesLoc2();
		int allNodesLoc1 = currentPackagePairCloneRelationData.getAllNodesLoc1();
		int allNodesLoc2 = currentPackagePairCloneRelationData.getAllNodesLoc2();
		double cloneLocRate = currentPackagePairCloneRelationData.getCloneLocRate();
		int cloneNodesCoChangeTimes = currentPackagePairCloneRelationData.getCloneNodesCoChangeTimes();
		int allNodesCoChangeTimes = currentPackagePairCloneRelationData.getAllNodesCoChangeTimes();
		double cloneCoChangeRate = currentPackagePairCloneRelationData.getCloneCoChangeRate();
		int cloneType1Count = currentPackagePairCloneRelationData.getCloneType1Count();
		int cloneType2Count = currentPackagePairCloneRelationData.getCloneType2Count();
		int cloneType3Count = currentPackagePairCloneRelationData.getCloneType3Count();
		String cloneType = currentPackagePairCloneRelationData.getCloneType();
		double cloneSimilarityValue = currentPackagePairCloneRelationData.getCloneSimilarityValue();
		double cloneSimilarityRate = currentPackagePairCloneRelationData.getCloneSimilarityRate();

		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		row.createCell(0).setCellValue(prefix + currentHotspotPackagePair.getPackage1().getDirectoryPath());
		row.createCell(1).setCellValue(prefix + currentHotspotPackagePair.getPackage2().getDirectoryPath());
		row.createCell(2).setCellValue("(" + cloneNodesCount1 + "+" + cloneNodesCount2 + ")/(" + allNodesCount1 + "+" + allNodesCount2 + ")=" + String .format("%.2f", cloneMatchRate));
		row.createCell(3).setCellValue(cloneNodesCoChangeTimes + "/" + allNodesCoChangeTimes + "=" + String .format("%.2f", cloneCoChangeRate));
		row.createCell(4).setCellValue("(" + cloneNodesLoc1 + "+" + cloneNodesLoc2 + ")/(" + allNodesLoc1 + "+" + allNodesLoc2 + ")=" + String .format("%.2f", cloneLocRate));
		row.createCell(5).setCellValue(String .format("%.2f", cloneSimilarityValue) + "/(" + cloneType1Count + "+" + cloneType2Count + "+" + cloneType3Count + ")=" + String .format("%.2f", cloneSimilarityRate));
		row.createCell(6).setCellValue(cloneType);
		row.createCell(7).setCellValue(clonePairs);

		for (HotspotPackagePair childHotspotPackagePair : currentHotspotPackagePair.getChildrenHotspotPackagePairs()){
			loadHotspotPackageResult(sheet, rowKey, layer + 1, childHotspotPackagePair);
		}
		for (Package packageChild1 : currentHotspotPackagePair.getChildrenOtherPackages1()){
			printOtherPackage(sheet, rowKey, -1, layer + 1, packageChild1);
		}
		for (Package packageChild2:currentHotspotPackagePair.getChildrenOtherPackages2()){
			printOtherPackage(sheet, rowKey, 1, layer + 1, packageChild2);
		}
	}
	private void printOtherPackage(Sheet sheet, ThreadLocal<Integer> rowKey, int index, int layer, Package otherPackage){
		StringBuilder prefix = new StringBuilder();
		for(int i = 0; i < layer; i++) {
			prefix.append("|---");
		}
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		if(index == -1) {
			row.createCell(0).setCellValue(prefix + otherPackage.getDirectoryPath());
			row.createCell(1).setCellValue("0/" + otherPackage.getAllNodes() + "=0.00");
			row.createCell(2).setCellValue("");
			row.createCell(3).setCellValue("");
			row.createCell(4).setCellValue("");
			row.createCell(5).setCellValue("");
			row.createCell(6).setCellValue("");
		}
		if(index == 1) {
			row.createCell(0).setCellValue("");
			row.createCell(1).setCellValue("");
			row.createCell(2).setCellValue(prefix + otherPackage.getDirectoryPath());
			row.createCell(3).setCellValue("0/" + otherPackage.getAllNodes() + "=0.00");
			row.createCell(4).setCellValue("");
			row.createCell(5).setCellValue("");
			row.createCell(6).setCellValue("");
		}
	}

	private HotspotPackagePair createHotspotPackagePairWithDepends(Package pck1, Package pck2, List<DependsOn> packageDependsOnList) {
		StringBuilder dependsOnStr = new StringBuilder();
		StringBuilder dependsByStr = new StringBuilder();
		int dependsOnTimes = 0;
		int dependsByTimes = 0;
		for (DependsOn dependsOn : packageDependsOnList){
			if(dependsOn.getStartNode().getId().equals(pck1.getId())){
				dependsOnStr.append(dependsOn.getDependsOnType());
				dependsOnTimes += dependsOn.getTimes();
			}else {
				dependsByStr.append(dependsOn.getDependsOnType());
				dependsByTimes += dependsOn.getTimes();
			}
		}
		DependsRelationDataForDoubleNodes<Node, Relation> dependsRelationDataForDoubleNodes = new DependsRelationDataForDoubleNodes<>(pck1, pck2);
		dependsRelationDataForDoubleNodes.setDependsOnTypes(dependsOnStr.toString());
		dependsRelationDataForDoubleNodes.setDependsByTypes(dependsByStr.toString());
		dependsRelationDataForDoubleNodes.setDependsOnTimes(dependsOnTimes);
		dependsRelationDataForDoubleNodes.setDependsByTimes(dependsByTimes);
		dependsRelationDataForDoubleNodes.calDependsIntensity();
		return new HotspotPackagePair(dependsRelationDataForDoubleNodes);
	}

	//寻找HotspotPackage
	private HotspotPackagePair findHotspotPackage(Collection<? extends Relation> fileClones, Map<String, HotspotPackagePair> directoryPathToHotspotPackage, Package pck1, Package pck2) {
		String key = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
		if(!directoryPathToHotspotPackage.containsKey(key)) {
			HotspotPackagePair hotspotPackagePair;
			BasicDataForDoubleNodes<Node, Relation> packageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(fileClones, pck1, pck2);
			if(packageClone != null) {
				hotspotPackagePair = new HotspotPackagePair(packageClone);
				if(hotspotPackagePair.getPackage1().getDirectoryPath().equals(pck2.getDirectoryPath()) && hotspotPackagePair.getPackage2().getDirectoryPath().equals(pck1.getDirectoryPath())) {
					hotspotPackagePair.swapPackages();
				}
			}
			else {
				hotspotPackagePair = new HotspotPackagePair(new CloneRelationDataForDoubleNodes<>(pck1, pck2));
			}
			directoryPathToHotspotPackage.put(key, hotspotPackagePair);
		}
		return directoryPathToHotspotPackage.get(key);
	}

	//判断是否为包含文件的分支结点
	private boolean isBranchPackageWithFiles(Package pck) {
		return hasRelationService.findPackageHasPackages(pck).size() > 0 && containRelationService.findPackageContainFiles(pck).size() > 0;
	}

	//判断是否为叶子结点
	private boolean isLeafPackage(Package pck) {
		return hasRelationService.findPackageHasPackages(pck).size() == 0;
	}

	//包下有文件也有包时，为包下文件创建一个包
	private Package buildPackageForFiles(Package pck) {
		String childDirectoryPath = pck.getDirectoryPath() + "./";
		Package childPackage = new Package();
		childPackage.setId(pck.getId());
		childPackage.setEntityId(pck.getEntityId());
		childPackage.setDirectoryPath(childDirectoryPath);
		childPackage.setName(childDirectoryPath);
		childPackage.setLanguage(pck.getLanguage());
		return childPackage;
	}

	//获取包下文件总数
	private int getAllFilesNum(Map<Long, Integer> directoryIdToAllNodes, Package pck) {
		if(directoryIdToAllNodes.containsKey(pck.getId())) {
			return directoryIdToAllNodes.get(pck.getId());
		}
		int number = containRelationService.findPackageContainFiles(pck).size();
		Collection<Package> childrenPackages = hasRelationService.findPackageHasPackages(pck);
		for(Package childPackage : childrenPackages) {
			number += getAllFilesNum(directoryIdToAllNodes, childPackage);
		}
		directoryIdToAllNodes.put(pck.getId(), number);
		return number;
	}

	//获取包下文件代码行
	private int getAllFilesLoc(Map<Long, Integer> directoryIdToAllLoc, Package pck) {
		if(directoryIdToAllLoc.containsKey(pck.getId())) {
			return directoryIdToAllLoc.get(pck.getId());
		}
		int Loc = 0;
		Collection<ProjectFile> allChildrenFiles = new ArrayList<>(containRelationService.findPackageContainFiles(pck));
		for(ProjectFile allChildFile : allChildrenFiles) {
			Loc += allChildFile.getLoc();
		}
		Collection<Package> childrenPackages = hasRelationService.findPackageHasPackages(pck);
		for(Package childPackage : childrenPackages) {
			Loc += getAllFilesLoc(directoryIdToAllLoc, childPackage);
		}
		directoryIdToAllLoc.put(pck.getId(), Loc);
		return Loc;
	}

	//判断是否符合聚合条件
	private boolean isHotspotPackage(RelationAggregator<Boolean> aggregator, Map<Long, Integer> directoryIdToAllNodes, Map<Long, Integer> directoryIdToAllLoc, Map<String, Set<ProjectFile>> directoryPathToAllCloneChildrenPackages, HotspotPackagePair currentHotspotPackagePair) {
		Package currentPackage1 = currentHotspotPackagePair.getPackage1();
		Package currentPackage2 = currentHotspotPackagePair.getPackage2();
		String path1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
		String path2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
		CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();

		//包下只有文件情况
		if(isLeafPackage(currentPackage1) || isLeafPackage(currentPackage2)) {
			return aggregator.aggregate(currentPackagePairCloneRelationData);
		}

		Set<ProjectFile> cloneChildrenFiles1 = new HashSet<>();
		Set<ProjectFile> cloneChildrenFiles2 = new HashSet<>();
		//包下另有文件情况
		if(isBranchPackageWithFiles(currentPackage1) || isBranchPackageWithFiles(currentPackage2)) {
			if(directoryPathToAllCloneChildrenPackages.containsKey(path1)) {
				cloneChildrenFiles1.addAll(directoryPathToAllCloneChildrenPackages.get(path1));
			}
			if(directoryPathToAllCloneChildrenPackages.containsKey(path2)) {
				cloneChildrenFiles2.addAll(directoryPathToAllCloneChildrenPackages.get(path2));
			}
		}
		//遍历包下子包
		Collection<HotspotPackagePair> childrenHotspotPackagePairs = currentHotspotPackagePair.getChildrenHotspotPackagePairs();
		Collection<Package> childrenPackage1 = hasRelationService.findPackageHasPackages(currentPackage1);
		Collection<Package> childrenPackage2 = hasRelationService.findPackageHasPackages(currentPackage2);
		for(HotspotPackagePair childHotspotPackagePair : childrenHotspotPackagePairs) {
			Package childPackage1 = childHotspotPackagePair.getPackage1();
			Package childPackage2 = childHotspotPackagePair.getPackage2();
			String childPath1 = String.join("_", childPackage1.getDirectoryPath(), childPackage2.getDirectoryPath());
			String childPath2 = String.join("_", childPackage2.getDirectoryPath(), childPackage1.getDirectoryPath());
			cloneChildrenFiles1.addAll(directoryPathToAllCloneChildrenPackages.get(childPath1));
			cloneChildrenFiles2.addAll(directoryPathToAllCloneChildrenPackages.get(childPath2));
			childrenPackage1.remove(childPackage1);
			childrenPackage2.remove(childPackage2);
		}
		for(Package otherChild1 : childrenPackage1) {
			currentHotspotPackagePair.addOtherChild1(otherChild1);
		}
		for(Package otherChild2 : childrenPackage2) {
			currentHotspotPackagePair.addOtherChild2(otherChild2);
		}
		int allNodes1 = getAllFilesNum(directoryIdToAllNodes, currentPackage1);
		int allNodes2 = getAllFilesNum(directoryIdToAllNodes, currentPackage2);
		int cloneNodes1 = cloneChildrenFiles1.size();
		int cloneNodes2 = cloneChildrenFiles2.size();
		int cloneNodesLoc1 = 0;
		int cloneNodesLoc2 = 0;
		for(ProjectFile cloneChildFile1 : cloneChildrenFiles1) {
			cloneNodesLoc1 += cloneChildFile1.getLoc();
		}
		for(ProjectFile cloneChildFile2 : cloneChildrenFiles2) {
			cloneNodesLoc2 += cloneChildFile2.getLoc();
		}
		int allNodesLoc1 = getAllFilesLoc(directoryIdToAllLoc, currentPackage1);
		int allNodesLoc2 = getAllFilesLoc(directoryIdToAllLoc, currentPackage2);
		directoryPathToAllCloneChildrenPackages.put(path1, cloneChildrenFiles1);
		directoryPathToAllCloneChildrenPackages.put(path2, cloneChildrenFiles2);
		currentPackagePairCloneRelationData.setCloneCountDate(cloneNodes1, cloneNodes2, allNodes1, allNodes2);
		currentPackagePairCloneRelationData.setCloneLocDate(cloneNodesLoc1, cloneNodesLoc2, allNodesLoc1, allNodesLoc2);
		currentHotspotPackagePair.setPackagePairRelationData(currentPackagePairCloneRelationData);
		return aggregator.aggregate(currentPackagePairCloneRelationData);
	}

	//设置HotspotPackage信息++
	public HotspotPackagePair setHotspotPackageData(AggregationClone aggregationClone, String language) {
		Package currentPackage1 = (Package) aggregationClone.getStartNode();
		Package currentPackage2 = (Package) aggregationClone.getEndNode();
		HotspotPackagePair currentHotspotPackagePair = new HotspotPackagePair(new CloneRelationDataForDoubleNodes<>(currentPackage1, currentPackage2));
		CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();
		int cloneNodesCoChangeTimes = 0;
		int allNodesCoChangeTimes = 0;
		ModuleClone packageCloneCoChanges = moduleCloneRepository.findModuleClone(currentPackage1.getId(), currentPackage2.getId());
		CoChange packageCoChanges = coChangeRepository.findPackageCoChange(currentPackage1.getId(), currentPackage2.getId());
		if(packageCloneCoChanges != null) {
			cloneNodesCoChangeTimes = packageCloneCoChanges.getModuleCloneCochangeTimes();
		}
		if(packageCoChanges != null) {
			allNodesCoChangeTimes = packageCoChanges.getTimes();
		}
		currentPackagePairCloneRelationData.setClonePairs(aggregationClone.getClonePairs());
		currentPackagePairCloneRelationData.setCloneCountDate(aggregationClone.getCloneNodesCount1(), aggregationClone.getCloneNodesCount2(), aggregationClone.getAllNodesCount1(), aggregationClone.getAllNodesCount2());
		currentPackagePairCloneRelationData.setCloneLocDate(aggregationClone.getCloneNodesLoc1(), aggregationClone.getCloneNodesLoc2(), aggregationClone.getAllNodesLoc1(), aggregationClone.getAllNodesLoc2());
		currentPackagePairCloneRelationData.setCoChangeTimesData(cloneNodesCoChangeTimes, allNodesCoChangeTimes);
		currentPackagePairCloneRelationData.setCloneTypeDate(aggregationClone.getCloneType1Count(), aggregationClone.getCloneType2Count(), aggregationClone.getCloneType3Count(), aggregationClone.getCloneSimilarityValue());
		List<HotspotPackagePair> childrenHotspotPackagePairs = new ArrayList<>();
		if(language.equals("all")) {
			childrenHotspotPackagePairs.addAll(loadHotspotPackagesByParentId(currentPackage1.getId(), currentPackage2.getId(), "java"));
			childrenHotspotPackagePairs.addAll(loadHotspotPackagesByParentId(currentPackage1.getId(), currentPackage2.getId(), "cpp"));
		}
		else {
			childrenHotspotPackagePairs.addAll(loadHotspotPackagesByParentId(currentPackage1.getId(), currentPackage2.getId(), language));
		}
		if(childrenHotspotPackagePairs.size() > 0) {
			//将有子包的包下文件打包
			if(isBranchPackageWithFiles(currentPackage1) && isBranchPackageWithFiles(currentPackage2)) {
				ModuleClone moduleClone = moduleCloneRepository.findModuleClone(currentPackage1.getId(), currentPackage2.getId());
				Package childPackage1 = buildPackageForFiles(currentPackage1);
				Package childPackage2 = buildPackageForFiles(currentPackage2);
				if(moduleClone != null) {
					HotspotPackagePair childHotspotPackagePair = new HotspotPackagePair(new CloneRelationDataForDoubleNodes<>(childPackage1, childPackage2));
					CloneRelationDataForDoubleNodes<Node, Relation> childPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) childHotspotPackagePair.getPackagePairRelationData();
					childPackagePairCloneRelationData.setClonePairs(moduleClone.getClonePairs());
					childPackagePairCloneRelationData.setCloneCountDate(moduleClone.getNodesInNode1(), moduleClone.getNodesInNode2(), moduleClone.getAllNodesInNode1(), moduleClone.getAllNodesInNode2());
					childPackagePairCloneRelationData.setCloneLocDate(aggregationClone.getCloneNodesLoc1(), aggregationClone.getCloneNodesLoc2(), aggregationClone.getAllNodesLoc1(), aggregationClone.getAllNodesLoc2());
					childPackagePairCloneRelationData.setCloneTypeDate(aggregationClone.getCloneType1Count(), aggregationClone.getCloneType2Count(), aggregationClone.getCloneType3Count(), aggregationClone.getCloneSimilarityValue());
					childPackagePairCloneRelationData.setCoChangeTimesData(cloneNodesCoChangeTimes, allNodesCoChangeTimes);
					childHotspotPackagePair.setPackagePairRelationData(childPackagePairCloneRelationData);
					currentHotspotPackagePair.addHotspotChild(childHotspotPackagePair);
				}
				else {
					childPackage1.setAllNodes(containRelationService.findPackageContainFiles(currentPackage1).size());
					childPackage2.setAllNodes(containRelationService.findPackageContainFiles(currentPackage2).size());
					currentHotspotPackagePair.addOtherChild1(childPackage1);
					currentHotspotPackagePair.addOtherChild2(childPackage2);
				}
			}

			Collection<Package> childrenPackages1 = hasRelationService.findPackageHasPackages(currentHotspotPackagePair.getPackage1());
			Collection<Package> childrenPackages2 = hasRelationService.findPackageHasPackages(currentHotspotPackagePair.getPackage2());
			for(HotspotPackagePair childHotspotPackagePair : childrenHotspotPackagePairs) {
				double childSimilarityValue = ((CloneRelationDataForDoubleNodes<Node, Relation>) childHotspotPackagePair.getPackagePairRelationData()).getCloneMatchRate();
				if(childSimilarityValue > 0.5 || childrenPackages1.contains(childHotspotPackagePair.getPackage1()) || childrenPackages1.contains(childHotspotPackagePair.getPackage2())) {
					currentHotspotPackagePair.addHotspotChild(childHotspotPackagePair);
					childrenPackages1.remove(childHotspotPackagePair.getPackage1());
					childrenPackages2.remove(childHotspotPackagePair.getPackage2());
				}
			}
			for(Package childPackage1 : childrenPackages1) {
				currentHotspotPackagePair.addOtherChild1(childPackage1);
			}
			for(Package childPackage2 : childrenPackages2) {
				currentHotspotPackagePair.addOtherChild2(childPackage2);
			}
		}
		currentHotspotPackagePair.setPackagePairRelationData(currentPackagePairCloneRelationData);
		return currentHotspotPackagePair;
	}

	//递归加载聚合结果
	public List<HotspotPackagePair> loadHotspotPackagesByParentId(long parent1Id, long parent2Id, String language) {
		List<HotspotPackagePair> result = new ArrayList<>();
		List<AggregationClone> aggregationClones = aggregationCloneRepository.findAggregationCloneByParentId(parent1Id, parent2Id, language);
		for(AggregationClone aggregationClone : aggregationClones) {
			result.add(setHotspotPackageData(aggregationClone, language));
		}
		//聚合排序，每一次均进行排序
		result.sort((hotspotPackagePair1, hotspotPackagePair2) -> {
			CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData1 = (CloneRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair1.getPackagePairRelationData();
			CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData2 = (CloneRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair1.getPackagePairRelationData();
			int allNodes1 = packagePairCloneRelationData1.getAllNodesCount1() + packagePairCloneRelationData1.getAllNodesCount2();
			int cloneNodes1 = packagePairCloneRelationData1.getCloneNodesCount1() + packagePairCloneRelationData1.getCloneNodesCount2();
			double percentageThreshold1 = (cloneNodes1 + 0.0) / allNodes1;
			int allNodes2 = packagePairCloneRelationData2.getAllNodesCount1() + packagePairCloneRelationData2.getAllNodesCount2();
			int cloneNodes2 = packagePairCloneRelationData2.getCloneNodesCount1() + packagePairCloneRelationData2.getCloneNodesCount2();
			double percentageThreshold2 = (cloneNodes2 + 0.0) / allNodes2;
			if(percentageThreshold1 < percentageThreshold2) {
				return 1;
			}
			else if(percentageThreshold1 == percentageThreshold2) {
				return Integer.compare(cloneNodes2, cloneNodes1);
			}
			else {
				return -1;
			}
		});
		return result;
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagesByDependsOnInAllProjects(){
		Collection<Project> projects = nodeService.allProjects();
		List<HotspotPackagePair> result = new ArrayList<>();
		for(Project project : projects) {
			result.addAll(detectHotspotPackagePairWithDependsOnByProjectId(project.getId()));
		}
		return result;
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithCoChangeByProjectId(long projectId) {
		List<HotspotPackagePair> result = new ArrayList<>();
		List<CoChange> projectCoChanges = coChangeRepository.findPackageCoChangeInProject(projectId);
		if(projectCoChanges != null && !projectCoChanges.isEmpty()){
			Map<Package, Map<Package, CoChange>> packageCoChangePackage = new HashMap<>();
			for (CoChange coChange : projectCoChanges){
				Package startNode = (Package) coChange.getStartNode();
				Package endNode = (Package) coChange.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				Map<Package, CoChange> coChangePackage = packageCoChangePackage.getOrDefault(pck1, new HashMap<>());
				coChangePackage.put(pck2, coChange);
				packageCoChangePackage.put(pck1, coChangePackage);
			}
			for(Map.Entry<Package, Map<Package, CoChange>> entry : packageCoChangePackage.entrySet()){
				Package pck1 = entry.getKey();
				Map<Package, CoChange> coChangePackage = entry.getValue();
				for(Map.Entry<Package, CoChange> entryKey : coChangePackage.entrySet()){
					Package pck2 = entryKey.getKey();
					CoChange coChange = entryKey.getValue();
					HotspotPackagePair hotspotPackagePair = createHotspotPackagePairWithCoChange(pck1, pck2, coChange);
					result.add(hotspotPackagePair);
				}
			}
		}
		return result;
	}

	@Override
	public HotspotPackagePair detectHotspotPackagePairWithCoChangeByPackageId(long pck1Id, long pck2Id) {
		CoChange packageCoChange = coChangeRepository.findPackageCoChange(pck1Id, pck2Id);
		HotspotPackagePair hotspotPackagePair = null;
		if(packageCoChange != null){
			Package tmp1 = (Package) packageCoChange.getStartNode();
			Package tmp2 = (Package) packageCoChange.getEndNode();
			Package pck1 = tmp1.getId() == pck1Id ? tmp1 : tmp2;
			Package pck2 = tmp2.getId() == pck2Id ? tmp2 : tmp1;
			hotspotPackagePair = createHotspotPackagePairWithCoChange(pck1, pck2, packageCoChange);
		}
		return hotspotPackagePair;
	}

	private HotspotPackagePair createHotspotPackagePairWithCoChange(Package pck1, Package pck2, CoChange packageCoChange) {
		CoChangeRelationDataForDoubleNodes<Node, Relation> coChangeRelationDataForDoubleNodes = new CoChangeRelationDataForDoubleNodes<>(pck1, pck2);
		coChangeRelationDataForDoubleNodes.setCoChangeTimes(packageCoChange.getTimes());
		return new HotspotPackagePair(coChangeRelationDataForDoubleNodes);
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagesByCoChangeInAllProjects() {
		Collection<Project> projects = nodeService.allProjects();
		List<HotspotPackagePair> result = new ArrayList<>();
		for(Project project : projects) {
			result.addAll(detectHotspotPackagePairWithCoChangeByProjectId(project.getId()));
		}
		return result;
	}

}
