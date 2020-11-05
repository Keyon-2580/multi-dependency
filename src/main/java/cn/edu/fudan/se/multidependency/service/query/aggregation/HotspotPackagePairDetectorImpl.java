package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
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
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
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
public class HotspotPackagePairDetectorImpl<ps> implements HotspotPackagePairDetector {

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

	private ThreadLocal<Integer> rowKey = new ThreadLocal<>();

	@Autowired
	private NodeService nodeService;

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
		Map<String, HotspotPackagePair> directoryPathToHotspotPackagePair = new HashMap<>();
		Map<String, Collection<String>> directoryPathToCloneChildrenPackages = new HashMap<>();
		Map<String, Set<Node>> directoryPathToAllCloneChildrenPackages = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Map<String, Boolean> isHotspot = new HashMap<>();
		Collection<? extends Relation> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		List<BasicDataForDoubleNodes<Node, Relation>> packageClones = summaryAggregationDataService.queryPackageCloneFromFileCloneSort(fileClones);
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByFileClone.getInstance();

		//预处理
		for(BasicDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			String path1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			String path2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
			directoryPathToAllCloneChildrenPackages.put(path1, packageClone.getNodesInNode1());
			directoryPathToAllCloneChildrenPackages.put(path2, packageClone.getNodesInNode2());
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
			HotspotPackagePair currentHotspotPackagePair = findHotspotPackage(fileClones, directoryPathToHotspotPackagePair, currentPackage1, currentPackage2);
			CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();
			currentPackagePairCloneRelationData.setDate(packageClone.getAllNodesInNode1().size(), packageClone.getAllNodesInNode2().size(), packageClone.getNodesInNode1().size(), packageClone.getNodesInNode2().size());
			currentPackagePairCloneRelationData.setClonePairs(packageClone.sizeOfChildren());
			currentHotspotPackagePair.setPackagePairRelationData(currentPackagePairCloneRelationData);
			String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			if(isHotspot.containsKey(currentPackages)) {
				continue;
			}
			Collection<String> cloneChildren = new ArrayList<>();
			if(directoryPathToCloneChildrenPackages.containsKey(currentPackages)) {
				cloneChildren = directoryPathToCloneChildrenPackages.get(currentPackages);
			}
			if(!cloneChildren.isEmpty()) {
				continue;
			}
			Package parentPackage1 = hasRelationService.findPackageInPackage(currentPackage1);
			Package parentPackage2 = hasRelationService.findPackageInPackage(currentPackage2);
			isHotspot.put(currentPackages, isHotspotPackage(aggregator, directoryIdToAllNodes, directoryPathToAllCloneChildrenPackages, currentHotspotPackagePair));
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
				isHotspot.put(parentPackages, isHotspotPackage(aggregator, directoryIdToAllNodes, directoryPathToAllCloneChildrenPackages, parentHotspotPackagePair));
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
		return setHotspotPackageData(aggregationClone, pck1Id, pck2Id, language);
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
		cell.setCellValue("目录1克隆占比");
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("目录2");
		cell.setCellStyle(style);
		cell = row.createCell(3);
		cell.setCellValue("目录2克隆占比");
		cell.setCellStyle(style);
		cell = row.createCell(4);
		cell.setCellValue("总克隆占比");
		cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("包克隆CoChange占比");
		cell = row.createCell(6);
		cell.setCellValue("克隆文件对数");
		cell.setCellStyle(style);
		for(HotspotPackagePair hotspotPackagePair : hotspotPackagePairs){
			loadHotspotPackageResult(sheet, 0, hotspotPackagePair);
		}
	}

	private void loadHotspotPackageResult(Sheet sheet, int layer, HotspotPackagePair currentHotspotPackagePair){
		StringBuilder prefix = new StringBuilder();
		for(int i = 0; i < layer; i++) {
			prefix.append("|---");
		}
		CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();

		int allNodes1 = currentPackagePairCloneRelationData.getAllNodesCount1();
		int cloneNodes1 = currentPackagePairCloneRelationData.getCloneNodesCount2();
		int allNodes2 = currentPackagePairCloneRelationData.getAllNodesCount1();
		int cloneNodes2 = currentPackagePairCloneRelationData.getCloneNodesCount2();
		int cloneNodesCoChangeTimes = currentPackagePairCloneRelationData.getCloneNodesCoChangeTimes();
		int allNodesCochangeTimes = currentPackagePairCloneRelationData.getAllNodesCoChangeTimes();
		int clonePairs = currentPackagePairCloneRelationData.getClonePairs();

		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		row.createCell(0).setCellValue(prefix + currentHotspotPackagePair.getPackage1().getDirectoryPath());
		BigDecimal clonePercent1 = BigDecimal.valueOf((cloneNodes1 + 0.0) / allNodes1);
		clonePercent1 = clonePercent1.setScale(2, RoundingMode.HALF_UP);
		row.createCell(1).setCellValue(cloneNodes1 + "/" + allNodes1 + "=" + clonePercent1.toString());
		row.createCell(2).setCellValue(prefix + currentHotspotPackagePair.getPackage2().getDirectoryPath());
		BigDecimal clonePercent2 = BigDecimal.valueOf((cloneNodes2 + 0.0) / allNodes2);
		clonePercent2 = clonePercent2.setScale(2, RoundingMode.HALF_UP);
		row.createCell(3).setCellValue(cloneNodes2 + "/" + allNodes2 + "=" + clonePercent2.toString());
		BigDecimal clonePercent = BigDecimal.valueOf((cloneNodes1 + cloneNodes2 + 0.0) / (allNodes1 + allNodes2));
		clonePercent = clonePercent.setScale(2, RoundingMode.HALF_UP);
		row.createCell(4).setCellValue("(" + cloneNodes1 + "+" + cloneNodes2 + ")/(" + allNodes1 + "+" + allNodes2 + ")=" + clonePercent.toString());
		BigDecimal cochangePercent = BigDecimal.valueOf(0);
		if(allNodesCochangeTimes != 0) {
			cochangePercent = BigDecimal.valueOf((cloneNodesCoChangeTimes + 0.0) / allNodesCochangeTimes);
		}
		cochangePercent = cochangePercent.setScale(2, RoundingMode.HALF_UP);
		row.createCell(5).setCellValue(cloneNodesCoChangeTimes + "/" + allNodesCochangeTimes + "=" + cochangePercent.toString());
		row.createCell(6).setCellValue(clonePairs);

		for (HotspotPackagePair childHotspotPackagePair : currentHotspotPackagePair.getChildrenHotspotPackagePairs()){
			loadHotspotPackageResult(sheet, layer + 1, childHotspotPackagePair);
		}
		for (Package packageChild1 : currentHotspotPackagePair.getChildrenOtherPackages1()){
			printOtherPackage(sheet, -1, layer + 1, packageChild1);
		}
		for (Package packageChild2:currentHotspotPackagePair.getChildrenOtherPackages2()){
			printOtherPackage(sheet, 1, layer + 1, packageChild2);
		}
	}
	private void printOtherPackage(Sheet sheet, int index, int layer, Package otherPackage){
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

	//判断是否符合聚合条件
	private boolean isHotspotPackage(RelationAggregator<Boolean> aggregator, Map<Long, Integer> directoryIdToAllNodes, Map<String, Set<Node>> directoryPathToAllCloneChildrenPackages, HotspotPackagePair currentHotspotPackagePair) {
		Package currentPackage1 = currentHotspotPackagePair.getPackage1();
		Package currentPackage2 = currentHotspotPackagePair.getPackage2();
		String path1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
		String path2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
		CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();

		//包下只有文件情况
		if(isLeafPackage(currentPackage1) || isLeafPackage(currentPackage2)) {
			return aggregator.aggregate(currentPackagePairCloneRelationData);
		}

		Set<Node> cloneChildrenPackages1 = new HashSet<>();
		Set<Node> cloneChildrenPackages2 = new HashSet<>();
		//包下另有文件情况
		if(isBranchPackageWithFiles(currentPackage1) || isBranchPackageWithFiles(currentPackage2)) {
			if(directoryPathToAllCloneChildrenPackages.containsKey(path1)) {
				cloneChildrenPackages1.addAll(directoryPathToAllCloneChildrenPackages.get(path1));
			}
			if(directoryPathToAllCloneChildrenPackages.containsKey(path2)) {
				cloneChildrenPackages2.addAll(directoryPathToAllCloneChildrenPackages.get(path2));
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
			cloneChildrenPackages1.addAll(directoryPathToAllCloneChildrenPackages.get(childPath1));
			cloneChildrenPackages2.addAll(directoryPathToAllCloneChildrenPackages.get(childPath2));
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
		int cloneNodes1 = cloneChildrenPackages1.size();
		int cloneNodes2 = cloneChildrenPackages2.size();
		directoryPathToAllCloneChildrenPackages.put(path1, cloneChildrenPackages1);
		directoryPathToAllCloneChildrenPackages.put(path2, cloneChildrenPackages2);
		currentPackagePairCloneRelationData.setDate(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
		currentHotspotPackagePair.setPackagePairRelationData(currentPackagePairCloneRelationData);
		return aggregator.aggregate(currentPackagePairCloneRelationData);
	}

	//设置HotspotPackage信息++
	public HotspotPackagePair setHotspotPackageData(AggregationClone aggregationClone, long parent1Id, long parent2Id, String language) {
		Package currentPackage1 = (Package) aggregationClone.getStartNode();
		Package currentPackage2 = (Package) aggregationClone.getEndNode();
		CoChange packageCoChanges = null;
		ModuleClone packageCloneCoChanges = null;
		if(parent1Id > -1 && parent2Id > -1){
			packageCoChanges = coChangeRepository.findPackageCoChange(currentPackage1.getId(), currentPackage2.getId());
			packageCloneCoChanges = moduleCloneRepository.findModuleClone(currentPackage1.getId(), currentPackage2.getId());
		}
		HotspotPackagePair currentHotspotPackagePair = new HotspotPackagePair(new CloneRelationDataForDoubleNodes<>(currentPackage1, currentPackage2));
		CloneRelationDataForDoubleNodes<Node, Relation> currentPackagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) currentHotspotPackagePair.getPackagePairRelationData();
		currentPackagePairCloneRelationData.setClonePairs(aggregationClone.getClonePairs());
		currentPackagePairCloneRelationData.setDate(aggregationClone.getAllNodesInNode1(), aggregationClone.getAllNodesInNode2(), aggregationClone.getNodesInNode1(), aggregationClone.getNodesInNode2());
		if(packageCoChanges != null){
			currentPackagePairCloneRelationData.setAllNodesCoChangeTimes(packageCoChanges.getTimes());
		}
		else {
			currentPackagePairCloneRelationData.setAllNodesCoChangeTimes(0);
		}
		if(packageCloneCoChanges != null){
			currentPackagePairCloneRelationData.setCloneNodesCoChangeTimes(packageCloneCoChanges.getModuleCloneCochangeTimes());
		}
		else {
			currentPackagePairCloneRelationData.setCloneNodesCoChangeTimes(0);
		}
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
					childPackagePairCloneRelationData.setDate(moduleClone.getAllNodesInNode1(), moduleClone.getAllNodesInNode2(), moduleClone.getNodesInNode1(), moduleClone.getNodesInNode2());
					childPackagePairCloneRelationData.setClonePairs(moduleClone.getClonePairs());
					if(packageCoChanges != null){
						childPackagePairCloneRelationData.setAllNodesCoChangeTimes(packageCoChanges.getTimes());
					}
					else {
						childPackagePairCloneRelationData.setAllNodesCoChangeTimes(0);
					}
					if(packageCloneCoChanges != null){
						childPackagePairCloneRelationData.setCloneNodesCoChangeTimes(packageCloneCoChanges.getModuleCloneCochangeTimes());
					}
					else {
						childPackagePairCloneRelationData.setCloneNodesCoChangeTimes(0);
					}
					currentPackagePairCloneRelationData.setClonePairs(0);
					currentPackagePairCloneRelationData.setCloneNodesCoChangeTimes(0);
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
				double childSimilarityValue = ((CloneRelationDataForDoubleNodes<Node, Relation>) childHotspotPackagePair.getPackagePairRelationData()).getSimilarityValue();
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
			result.add(setHotspotPackageData(aggregationClone, parent1Id, parent2Id, language));
		}
		//聚合排序，每一次均进行排序
		result.sort((d1, d2) -> {
			CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData1 = (CloneRelationDataForDoubleNodes<Node, Relation>) d1.getPackagePairRelationData();
			CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData2 = (CloneRelationDataForDoubleNodes<Node, Relation>) d1.getPackagePairRelationData();
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
