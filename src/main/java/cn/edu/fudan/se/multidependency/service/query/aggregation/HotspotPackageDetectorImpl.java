package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.clone.AggregationCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
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
public class HotspotPackageDetectorImpl<ps> implements HotspotPackageDetector {

	@Autowired
	private SummaryAggregationDataService summaryAggregationDataService;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	@Autowired
	private GitAnalyseService gitAnalyseService;

	@Autowired
	private AggregationCloneRepository aggregationCloneRepository;

	@Autowired
	private HasRelationService hasRelationService;

	@Autowired
	private CoChangeRepository coChangeRepository;

	@Autowired
	private ModuleCloneRepository moduleCloneRepository;

	private ThreadLocal<Integer> rowKey = new ThreadLocal<>();

	@Override
	public List<HotspotPackage> detectHotspotPackages() {
		return detectHotspotPackagesByFileClone_3();
		//return detectHotspotPackagesByFileCloneLoc();
		//return detectHotspotPackagesByFileCoChange();
		//return detectHotspotPackagesByFileCoChangeTimes();
	}

	private List<HotspotPackage> aggregatePackageRelation(Collection<? extends Relation> subNodeRelations,
																Collection<RelationDataForDoubleNodes<Node, Relation>> packageRelations,
																RelationAggregator<Boolean> aggregator) {

		Map<String, Package> directoryPathToParentPackage = new HashMap<>();
		Map<String, HotspotPackage> idToPackageRelation = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		for (RelationDataForDoubleNodes<Node, Relation> packageRelation : packageRelations) {
			boolean isSimilar = (boolean) packageRelation.aggregateValue(aggregator);
			if (!isSimilar) {
				continue;
			}
			if (idToPackageRelation.get(packageRelation.getId()) != null) {
				continue;
			}
			HotspotPackage temp = new HotspotPackage(packageRelation);
			idToPackageRelation.put(temp.getId(), temp);
			isChild.put(temp.getId(), false);
			String id = temp.getId();
			Package currentPackage1 = (Package)packageRelation.getNode1();
			Package currentPackage2 = (Package)packageRelation.getNode2();
			Package parentPackage1 = findParentPackage_3(directoryPathToParentPackage, currentPackage1);
			Package parentPackage2 = findParentPackage_3(directoryPathToParentPackage, currentPackage2);
			while (parentPackage1 != null && parentPackage2 != null) {
				RelationDataForDoubleNodes<Node, Relation> parentPackageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(subNodeRelations, parentPackage1, parentPackage2);
				if (parentPackageClone == null) {
					break;
				}
				if (!(boolean) parentPackageClone.aggregateValue(aggregator)) {
					break;
				}
				HotspotPackage parentHotspotPackage = idToPackageRelation.getOrDefault(parentPackageClone.getId(), new HotspotPackage(parentPackageClone));
				idToPackageRelation.put(parentHotspotPackage.getId(), parentHotspotPackage);
				isChild.put(id, true);
				isChild.put(parentHotspotPackage.getId(), false);
				parentHotspotPackage.addHotspotChild(idToPackageRelation.get(id));
				id = parentHotspotPackage.getId();
				parentPackage1 = containRelationService.findPackageInPackage(parentPackage1);
				parentPackage2 = containRelationService.findPackageInPackage(parentPackage2);
			}
		}

		Map<String, HotspotPackage> parentSimilarPackges = new HashMap<>();
		for (Map.Entry<String, HotspotPackage> entry : idToPackageRelation.entrySet()) {
			String id = entry.getKey();
			if (!isChild.get(id)) {
				HotspotPackage value = entry.getValue();
				Package pck1 = value.getPackage1();
				Package pck2 = value.getPackage2();
				Package parentPck1 = containRelationService.findPackageInPackage(pck1);
				Package parentPck2 = containRelationService.findPackageInPackage(pck2);
				if (parentPck1 == null && parentPck2 == null) {
					String parentPck1Path = pck1.lastPackageDirectoryPath();
					String parentPck2Path = pck2.lastPackageDirectoryPath();
					String parentId = String.join("_", parentPck1Path, parentPck2Path);
					HotspotPackage parentSimilar = parentSimilarPackges.get(parentId);
					if (parentSimilar == null) {
						parentPck1 = new Package();
						parentPck1.setId(-1L);
						parentPck1.setEntityId(-1L);
						parentPck1.setDirectoryPath(parentPck1Path);
						parentPck1.setName(parentPck1Path);
						parentPck1.setLanguage(pck1.getLanguage());
						parentPck2 = new Package();
						parentPck2.setId(-1L);
						parentPck2.setEntityId(-1L);
						parentPck2.setDirectoryPath(parentPck2Path);
						parentPck2.setName(parentPck2Path);
						parentPck1.setLanguage(pck2.getLanguage());
						parentSimilar = new HotspotPackage(new RelationDataForDoubleNodes(parentPck1, parentPck2, parentId));
						parentSimilarPackges.put(parentSimilar.getId(), parentSimilar);
					}
					parentSimilar.addHotspotChild(value);
					isChild.put(id, true);
				}
			}
		}

		List<HotspotPackage> result = new ArrayList<>();

		for (HotspotPackage parentHotspotPackage : parentSimilarPackges.values()) {
			result.add(parentHotspotPackage);
		}

		for (Map.Entry<String, HotspotPackage> entry : idToPackageRelation.entrySet()) {
			String id = entry.getKey();
			if (!isChild.get(id)) {
				result.add(entry.getValue());
			}
		}
		result.sort((d1, d2) -> {
			return d1.getPackage1().getDirectoryPath().compareTo(d2.getPackage1().getDirectoryPath());
		});
		return result;
	}

	public List<HotspotPackage> detectHotspotPackagesByFileClone_0() {
		Map<String, HotspotPackage> idToPackageRelation = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Collection<? extends Relation> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		Collection<RelationDataForDoubleNodes<Node, Relation>> packageClones = summaryAggregationDataService.queryPackageCloneFromFileCloneSort(fileClones);
//		DefaultPackageCloneValueCalculator.getInstance().setCountThreshold(threshold);
//		DefaultPackageCloneValueCalculator.getInstance().setPercentageThreshold(percentage);
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByFileClone.getInstance();
//		calculator = PackageCloneValueCalculaƒtorByFileLoc.getInstance();
	    return aggregatePackageRelation(fileClones,packageClones,aggregator);
	}

	//第一版本的HotspotPackage检测代码，通过克隆文件数进行聚合，但多对多的克隆信息只保存了最大的一对一克隆
	@Override
	public List<HotspotPackage> detectHotspotPackagesByFileClone_1() {
		Map<String, Package> directoryPathToParentPackage = new HashMap<>();
		Map<Long, Integer> allNodesOfPackage = new HashMap<>();
		Map<String, Integer> cloneNodesOfHotspotPackages = new HashMap<>();
		Map<String, Integer> cloneNodesOfClonePackages = new HashMap<>();
		Collection<String> clonePackages = new ArrayList<>();
		Collection<String> hotspotPackages = new ArrayList<>();
		Map<String, Collection<String>> cloneChildrenPackagesOfPackages = new HashMap<>();
		Map<String, HotspotPackage> idToPackageClone = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Map<String, Boolean> isHotspot = new HashMap<>();
		Collection<? extends Relation> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		List<RelationDataForDoubleNodes<Node, Relation>> packageClones = (List<RelationDataForDoubleNodes<Node, Relation>>) summaryAggregationDataService.queryPackageCloneFromFileCloneSort(fileClones);
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByFileClone.getInstance();
		//预处理，算法优化
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			Package parentPackage1 = findParentPackage_3(directoryPathToParentPackage, currentPackage1);
			Package parentPackage2 = findParentPackage_3(directoryPathToParentPackage, currentPackage2);
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				String parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				Collection<String> cloneChildren = new ArrayList<>();
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages)) {
					cloneChildren = cloneChildrenPackagesOfPackages.get(parentPackages);
				}
				if(cloneChildren.contains(currentPackages)) {
					break;
				}
				cloneChildren.add(currentPackages);
				cloneChildrenPackagesOfPackages.put(parentPackages, cloneChildren);
				currentPackage1 = parentPackage1;
				currentPackage2 = parentPackage2;
				parentPackage1 = findParentPackage_3(directoryPathToParentPackage, currentPackage1);
				parentPackage2 = findParentPackage_3(directoryPathToParentPackage, currentPackage2);
			}
		}
		//预处理
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			String currentPackages1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			String currentPackages2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
			if(clonePackages.contains(currentPackages1) || clonePackages.contains(currentPackages2)) {
				continue;
			}
			cloneNodesOfClonePackages.put(currentPackages1, packageClone.getNodesInNode1().size());
			cloneNodesOfClonePackages.put(currentPackages2, packageClone.getNodesInNode2().size());
			clonePackages.add(currentPackages1);
		}
		//检测
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			if(idToPackageClone.get(packageClone.getId()) != null) {
				continue;
			}
			HotspotPackage hotspotPackage = new HotspotPackage(packageClone);
			Package currentPackage1 = hotspotPackage.getPackage1();
			Package currentPackage2 = hotspotPackage.getPackage2();
			String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			Collection<String> cloneChildren = new ArrayList<>();
			if(cloneChildrenPackagesOfPackages.containsKey(currentPackages)) {
				cloneChildren = cloneChildrenPackagesOfPackages.get(currentPackages);
			}
			if(!cloneChildren.isEmpty()) {
				continue;
			}
			Package parentPackage1 = findParentPackage_3(directoryPathToParentPackage, currentPackage1);
			Package parentPackage2 = findParentPackage_3(directoryPathToParentPackage, currentPackage2);
			String parentPackages = "";
			if(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages)) {
					cloneChildren = cloneChildrenPackagesOfPackages.get(parentPackages);
					cloneChildren.remove(currentPackages);
					cloneChildrenPackagesOfPackages.put(parentPackages, cloneChildren);
				}
			}
			if(isHotspotPackages_1(aggregator, allNodesOfPackage, cloneNodesOfClonePackages, cloneNodesOfHotspotPackages, clonePackages, hotspotPackages, currentPackage1, currentPackage2)) {
				isHotspot.put(currentPackages, true);
			}
			else {
				isHotspot.put(currentPackages, false);
			}
			isChild.put(currentPackages, false);
			idToPackageClone.put(hotspotPackage.getId(), hotspotPackage);
			boolean flag = false;
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				HotspotPackage parentHotspotPackage;
				RelationDataForDoubleNodes<Node, Relation> childPackageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(fileClones, parentPackage1, parentPackage2);
				if(childPackageClone != null) {
					parentHotspotPackage = idToPackageClone.getOrDefault(childPackageClone.getId(), new HotspotPackage(childPackageClone));
					if(parentHotspotPackage.getPackage1().getDirectoryPath().equals(parentPackage2.getDirectoryPath()) && parentHotspotPackage.getPackage2().getDirectoryPath().equals(parentPackage1.getDirectoryPath())) {
						parentHotspotPackage.swapPackages();
					}
				}
				else {
					parentHotspotPackage = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(parentPackage1, parentPackage2));
				}
				currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				cloneChildren = new ArrayList<>();
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages)) {
					cloneChildren = cloneChildrenPackagesOfPackages.get(parentPackages);
					if(flag) {
						cloneChildren.remove(currentPackages);
						cloneChildrenPackagesOfPackages.put(parentPackages, cloneChildren);
					}
				}
				if(!cloneChildren.isEmpty()) {
					break;
				}
				if(isHotspotPackages_1(aggregator, allNodesOfPackage, cloneNodesOfClonePackages, cloneNodesOfHotspotPackages, clonePackages, hotspotPackages, parentPackage1, parentPackage2)) {
					isChild.put(currentPackages, true);
					isHotspot.put(parentPackages, true);
				}
				else {
					isHotspot.put(parentPackages, false);
				}
				isChild.put(parentPackages, false);
				idToPackageClone.put(parentHotspotPackage.getId(), parentHotspotPackage);
				currentPackage1 = parentPackage1;
				currentPackage2 = parentPackage2;
				parentPackage1 = findParentPackage_3(directoryPathToParentPackage, currentPackage1);
				parentPackage2 = findParentPackage_3(directoryPathToParentPackage, currentPackage2);
				flag = true;
			}
		}

		//确定根目录
		List<HotspotPackage> result = new ArrayList<>();
		for(Map.Entry<String, HotspotPackage> entry : idToPackageClone.entrySet()) {
			HotspotPackage rootHotspotPackage = entry.getValue();
			Package currentPackage1 = rootHotspotPackage.getPackage1();
			Package currentPackage2 = rootHotspotPackage.getPackage2();
			String currentPackages1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			String currentPackages2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
			if((!isChild.get(currentPackages1) && isHotspot.get(currentPackages1))) {
				Package parentPackage1 = findParentPackage_3(directoryPathToParentPackage, currentPackage1);
				Package parentPackage2 = findParentPackage_3(directoryPathToParentPackage, currentPackage2);
				if(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
					String parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
					if((isHotspot.containsKey(parentPackages) && isHotspot.get(parentPackages))) {
						isChild.put(currentPackages1, true);
						continue;
					}
				}
				if(!result.contains(rootHotspotPackage)) {
					int allNodes1 = allNodesOfPackage.get(currentPackage1.getId());
					int allNodes2 = allNodesOfPackage.get(currentPackage2.getId());
					int cloneNodes1 = cloneNodesOfHotspotPackages.get(currentPackages1);
					int cloneNodes2 = cloneNodesOfHotspotPackages.get(currentPackages2);
					rootHotspotPackage.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
					result.add(rootHotspotPackage);
				}
			}
		}

		//加载根目录的子目录
		for(HotspotPackage rootHotspotPackage : result) {
			AddChildrenPackages_1(allNodesOfPackage, cloneNodesOfClonePackages, cloneNodesOfHotspotPackages, clonePackages, hotspotPackages, rootHotspotPackage, fileClones, idToPackageClone);
		}

		//聚合排序
		result.sort((d1, d2) -> {
			int allNodes1 = d1.getAllNodes1() + d1.getAllNodes2();
			int cloneNodes1 = d1.getRelationNodes1() + d1.getRelationNodes2();
			double percentageThreshold1 = (cloneNodes1 + 0.0) / allNodes1;
			int allNodes2 = d2.getAllNodes1() + d2.getAllNodes2();
			int cloneNodes2 = d2.getRelationNodes1() + d2.getRelationNodes2();
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


	//第二版本的HotspotPackage检测代码，通过克隆包数进行聚合，保留了多对多的克隆信息
	@Override
	public List<HotspotPackage> detectHotspotPackagesByFileClone_2() {
		Map<String, HotspotPackage> directoryPathToHotspotPackage = new HashMap<>();
		Map<String, Collection<String>> cloneChildrenPackagesOfPackages = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Map<String, Boolean> isHotspot = new HashMap<>();
		Collection<? extends Relation> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		List<RelationDataForDoubleNodes<Node, Relation>> packageClones = (List<RelationDataForDoubleNodes<Node, Relation>>) summaryAggregationDataService.queryPackageCloneFromFileCloneSort(fileClones);
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByFileClone.getInstance();

		//预处理
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			Package parentPackage1 = hasRelationService.findPackageInPackage(currentPackage1);
			Package parentPackage2 = hasRelationService.findPackageInPackage(currentPackage2);
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				String parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				Collection<String> cloneChildren = new ArrayList<>();
				if(!cloneChildrenPackagesOfPackages.containsKey(parentPackages)) {
					cloneChildrenPackagesOfPackages.put(parentPackages, cloneChildren);
				}
				cloneChildren = cloneChildrenPackagesOfPackages.get(parentPackages);
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
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			HotspotPackage hotspotPackage = findHotspotPackage(fileClones, directoryPathToHotspotPackage, currentPackage1, currentPackage2);
			hotspotPackage.setData(packageClone.getAllNodesInNode1().size(), packageClone.getAllNodesInNode2().size(), packageClone.getNodesInNode1().size(), packageClone.getNodesInNode2().size());
			String currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
			if(isHotspot.containsKey(currentPackages)) {
				continue;
			}
			Collection<String> cloneChildren = new ArrayList<>();
			if(cloneChildrenPackagesOfPackages.containsKey(currentPackages)) {
				cloneChildren = cloneChildrenPackagesOfPackages.get(currentPackages);
			}
			if(!cloneChildren.isEmpty()) {
				continue;
			}
			Package parentPackage1 = hasRelationService.findPackageInPackage(currentPackage1);
			Package parentPackage2 = hasRelationService.findPackageInPackage(currentPackage2);
			isHotspot.put(currentPackages, isHotspotPackage_2(aggregator, isHotspot, hotspotPackage));
			isChild.put(currentPackages, false);
			String parentPackages;
			HotspotPackage parentHotspotPackage;
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				hotspotPackage = findHotspotPackage(fileClones, directoryPathToHotspotPackage, currentPackage1, currentPackage2);
				parentHotspotPackage = findHotspotPackage(fileClones, directoryPathToHotspotPackage, parentPackage1, parentPackage2);
				currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				if(isHotspot.containsKey(parentPackages)) {
					break;
				}
				cloneChildren = new ArrayList<>();
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages)) {
					cloneChildren = cloneChildrenPackagesOfPackages.get(parentPackages);
					cloneChildren.remove(currentPackages);
					parentHotspotPackage.addHotspotChild(hotspotPackage);
				}
				if(!cloneChildren.isEmpty()) {
					break;
				}
				isHotspot.put(parentPackages, isHotspotPackage_2(aggregator, isHotspot, parentHotspotPackage));
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
		List<HotspotPackage> result = new ArrayList<>();
		for(Map.Entry<String, HotspotPackage> entry : directoryPathToHotspotPackage.entrySet()) {
			String currentPackages = entry.getKey();
			HotspotPackage hotspotPackage = entry.getValue();
			Package currentPackage1 = hotspotPackage.getPackage1();
			Package currentPackage2 = hotspotPackage.getPackage2();
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
				if(!result.contains(hotspotPackage)) {
					result.add(hotspotPackage);
				}
			}
		}
		return result;
	}

	//第三版本的HotspotPackage检测代码，通过克隆文件数进行聚合，保留了多对多的克隆信息
	@Override
	public List<HotspotPackage> detectHotspotPackagesByFileClone_3() {
		Map<Long, Integer> directoryIdToAllNodes = new HashMap<>();
		Map<String, HotspotPackage> directoryPathToHotspotPackage = new HashMap<>();
		Map<String, Collection<String>> directoryPathToCloneChildrenPackages = new HashMap<>();
		Map<String, Set<CodeNode>> directoryPathToAllCloneChildrenPackages = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Map<String, Boolean> isHotspot = new HashMap<>();
		Collection<? extends Relation> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		List<RelationDataForDoubleNodes<Node, Relation>> packageClones = (List<RelationDataForDoubleNodes<Node, Relation>>) summaryAggregationDataService.queryPackageCloneFromFileCloneSort(fileClones);
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByFileClone.getInstance();

		//预处理
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
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
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package currentPackage1 = (Package) packageClone.getNode1();
			Package currentPackage2 = (Package) packageClone.getNode2();
			HotspotPackage hotspotPackage = findHotspotPackage(fileClones, directoryPathToHotspotPackage, currentPackage1, currentPackage2);
			hotspotPackage.setData(packageClone.getAllNodesInNode1().size(), packageClone.getAllNodesInNode2().size(), packageClone.getNodesInNode1().size(), packageClone.getNodesInNode2().size());
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
			isHotspot.put(currentPackages, isHotspotPackage_3(aggregator, directoryIdToAllNodes, directoryPathToAllCloneChildrenPackages, hotspotPackage));
			isChild.put(currentPackages, false);
			String parentPackages;
			HotspotPackage parentHotspotPackage;
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				hotspotPackage = findHotspotPackage(fileClones, directoryPathToHotspotPackage, currentPackage1, currentPackage2);
				parentHotspotPackage = findHotspotPackage(fileClones, directoryPathToHotspotPackage, parentPackage1, parentPackage2);
				currentPackages = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				parentPackages = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				if(isHotspot.containsKey(parentPackages)) {
					break;
				}
				cloneChildren = new ArrayList<>();
				if(directoryPathToCloneChildrenPackages.containsKey(parentPackages)) {
					cloneChildren = directoryPathToCloneChildrenPackages.get(parentPackages);
					cloneChildren.remove(currentPackages);
					parentHotspotPackage.addHotspotChild(hotspotPackage);
				}
				if(!cloneChildren.isEmpty()) {
					break;
				}
				isHotspot.put(parentPackages, isHotspotPackage_3(aggregator, directoryIdToAllNodes, directoryPathToAllCloneChildrenPackages, parentHotspotPackage));
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
		List<HotspotPackage> result = new ArrayList<>();
		for(Map.Entry<String, HotspotPackage> entry : directoryPathToHotspotPackage.entrySet()) {
			String currentPackages = entry.getKey();
			HotspotPackage hotspotPackage = entry.getValue();
			Package currentPackage1 = hotspotPackage.getPackage1();
			Package currentPackage2 = hotspotPackage.getPackage2();
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
				if(!result.contains(hotspotPackage)) {
					result.add(hotspotPackage);
				}
			}
		}
		return result;
	}

	@Override
	public HotspotPackage detectHotspotPackagesByPackageId(long pck1Id, long pck2Id) {
		AggregationClone aggregationClone = aggregationCloneRepository.findAggregationCloneById(pck1Id, pck2Id);
		return setHotspotPackageData(aggregationClone, pck1Id, pck2Id);
	}

	@Override
	public List<HotspotPackage> detectHotspotPackagesByParentId(long parent1Id, long parent2Id) {
		return loadHotspotPackagesByParentId(parent1Id, parent2Id);
	}

	@Override
	public Collection<HotspotPackage> detectHotspotPackagesByFileCloneLoc() {
		return null;
	}

	@Override
	public Collection<HotspotPackage> detectHotspotPackagesByFileCoChange() {
		Map<String, HotspotPackage> idToPackageRelation = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Collection<CoChange> fileCoChanges = gitAnalyseService.calCntOfFileCoChange();
		Collection<RelationDataForDoubleNodes<Node, Relation>> packageRelations = summaryAggregationDataService.queryPackageCoChangeFromFileCoChangeSort(fileCoChanges);
		RelationAggregator<Boolean> aggregator = RelationAggregatorForPackageByCoChange.getInstance();
		return aggregatePackageRelation(fileCoChanges,packageRelations,aggregator);
	}

	@Override
	public Collection<HotspotPackage> detectHotspotPackagesByFileCoChangeTimes() {
		return null;
	}

	@Override
	public void exportHotspotPackages(OutputStream stream) {
		rowKey.set(0);
		Workbook hwb = new XSSFWorkbook();
		Collection<HotspotPackage> HotspotPackages = detectHotspotPackages();
		Sheet sheet = hwb.createSheet(new StringBuilder().append("HotspotPackages").toString());
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		CellStyle style = hwb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell = null;
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
		cell.setCellValue("克隆文件对数");
		cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("总克隆占比");
		cell.setCellStyle(style);
		for(HotspotPackage HotspotPackage:HotspotPackages){
			printHotspotPackage(sheet, 0, HotspotPackage);
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

	private void printHotspotPackage(Sheet sheet, int layer, HotspotPackage hotspotPackage){
		String prefix = "";
		for(int i = 0; i < layer; i++) {
			prefix += "|---";
		}
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		row.createCell(0).setCellValue(prefix + hotspotPackage.getPackage1().getDirectoryPath());
		BigDecimal percent1 = BigDecimal.valueOf((hotspotPackage.getRelationNodes1() + 0.0) / hotspotPackage.getAllNodes1());
		percent1 = percent1.setScale(2, RoundingMode.HALF_UP);
		row.createCell(1).setCellValue(hotspotPackage.getRelationNodes1() + "/" + hotspotPackage.getAllNodes1() + "=" + percent1.toString());
		row.createCell(2).setCellValue(prefix + hotspotPackage.getPackage2().getDirectoryPath());
		BigDecimal percent2 = BigDecimal.valueOf((hotspotPackage.getRelationNodes2() + 0.0) / hotspotPackage.getAllNodes2());
		percent2 = percent2.setScale(2, RoundingMode.HALF_UP);
		row.createCell(3).setCellValue(hotspotPackage.getRelationNodes2() + "/" + hotspotPackage.getAllNodes2() + "=" + percent2.toString());
		row.createCell(4).setCellValue(hotspotPackage.getRelationPackages().sizeOfChildren());
		BigDecimal percent = BigDecimal.valueOf((hotspotPackage.getRelationNodes1() + hotspotPackage.getRelationNodes2() + 0.0) / (hotspotPackage.getAllNodes1() + hotspotPackage.getAllNodes2()));
		percent = percent.setScale(2, RoundingMode.HALF_UP);
		row.createCell(5).setCellValue("(" + hotspotPackage.getRelationNodes1() + "+" + hotspotPackage.getRelationNodes2() + ")/(" + hotspotPackage.getAllNodes1() + "+" + hotspotPackage.getAllNodes2() + "=" + percent.toString());
		for (HotspotPackage hotspotPackageChild : hotspotPackage.getChildrenHotspotPackages()){
			printHotspotPackage(sheet, layer + 1, hotspotPackageChild);
		}
		for (Package packageChild1 : hotspotPackage.getChildrenOtherPackages1()){
			printOtherPackage(sheet, -1, layer + 1, packageChild1);
		}
		for (Package packageChild2:hotspotPackage.getChildrenOtherPackages2()){
			printOtherPackage(sheet, 1, layer + 1, packageChild2);
		}
	}
	private void printOtherPackage(Sheet sheet, int index, int layer, Package otherPackage){
		String prefix = "";
		for(int i = 0; i < layer; i++) {
			prefix += "|---";
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
		}
		if(index == 1) {
			row.createCell(0).setCellValue("");
			row.createCell(1).setCellValue("");
			row.createCell(2).setCellValue(prefix + otherPackage.getDirectoryPath());
			row.createCell(3).setCellValue("0/" + otherPackage.getAllNodes() + "=0.00");
			row.createCell(4).setCellValue("");
			row.createCell(5).setCellValue("");
		}
	}

	//寻找HotspotPackage
	private HotspotPackage findHotspotPackage(Collection<? extends Relation> fileClones, Map<String, HotspotPackage> directoryPathToHotspotPackage, Package pck1, Package pck2) {
		String key = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
		if(!directoryPathToHotspotPackage.containsKey(key)) {
			HotspotPackage hotspotPackage;
			RelationDataForDoubleNodes<Node, Relation> packageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(fileClones, pck1, pck2);
			if(packageClone != null) {
				hotspotPackage = new HotspotPackage(packageClone);
				if(hotspotPackage.getPackage1().getDirectoryPath().equals(pck2.getDirectoryPath()) && hotspotPackage.getPackage2().getDirectoryPath().equals(pck1.getDirectoryPath())) {
					hotspotPackage.swapPackages();
				}
			}
			else {
				hotspotPackage = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(pck1, pck2));
			}
			directoryPathToHotspotPackage.put(key, hotspotPackage);
		}
		return directoryPathToHotspotPackage.get(key);
	}

	//寻找父包，删除空包
//	private Package findParentPackageDeleteEmptyPackage(Map<String, Package> directoryPathToParentPackage, Package pck) {
//		String parentPackageDirectoryPath = pck.lastPackageDirectoryPath();
//		if(FileUtil.SLASH_LINUX.equals(parentPackageDirectoryPath)) {
//			return null;
//		}
//		Package parentPackage;
//		if(directoryPathToParentPackage.containsKey(parentPackageDirectoryPath)) {
//			parentPackage = directoryPathToParentPackage.get(parentPackageDirectoryPath);
//		}
//		else {
//			parentPackage = containRelationService.findPackageInPackage(pck);
//		}
//		if(parentPackage != null && isEmptyPackage(parentPackage)) {
//			Package grandParentPackage = findParentPackageDeleteEmptyPackage(directoryPathToParentPackage, parentPackage);
//			if(grandParentPackage != null) {
//				parentPackage = grandParentPackage;
//			}
//		}
//		if(parentPackage != null) {
//			directoryPathToParentPackage.put(parentPackageDirectoryPath, parentPackage);
//		}
//		return parentPackage;
//	}

	//寻找子包集
//	private Collection<Package> getChildrenPackagesDeleteEmptyPackage(Map<String, Collection<Package>> directoryPathToChildrenPackages, Package pck) {
//		String packageDirectoryPath = pck.getDirectoryPath();
//		if(directoryPathToChildrenPackages.containsKey(packageDirectoryPath)) {
//			return directoryPathToChildrenPackages.get(packageDirectoryPath);
//		}
//		Collection<Package> result = new ArrayList<>();
//		Collection<Package> children = hasRelationService.findPackageHasPackages(pck);
//		for(Package child : children) {
//			if(isEmptyPackage(child)) {
//				result.addAll(getChildrenPackagesDeleteEmptyPackage(directoryPathToChildrenPackages, child));
//			}
//			else {
//				result.add(child);
//			}
//		}
//		directoryPathToChildrenPackages.put(packageDirectoryPath, result);
//		return result;
//	}
	
	//判断是否为空包
//	private boolean isEmptyPackage(Package pck) {
//		return hasRelationService.findPackageHasPackages(pck).size() == 1 && containRelationService.findPackageContainFiles(pck).size() == 0;
//	}
	
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

	//判断是否符合聚合条件
	private boolean isHotspotPackage_3(RelationAggregator<Boolean> aggregator, Map<Long, Integer> directoryIdToAllNodes, Map<String, Set<CodeNode>> directoryPathToAllCloneChildrenPackages, HotspotPackage hotspotPackage) {
		Package currentPackage1 = hotspotPackage.getPackage1();
		Package currentPackage2 = hotspotPackage.getPackage2();
		String path1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
		String path2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
		RelationDataForDoubleNodes<Node, Relation> packageClone = new RelationDataForDoubleNodes<Node, Relation>(currentPackage1, currentPackage2);

		//包下只有文件情况
		if(isLeafPackage(currentPackage1) || isLeafPackage(currentPackage2)) {
			packageClone.setDate(hotspotPackage.getAllNodes1(), hotspotPackage.getAllNodes2(), hotspotPackage.getRelationNodes1(), hotspotPackage.getRelationNodes2());
			return aggregator.aggregate(packageClone);
		}

		Set<CodeNode> cloneChildrenPackages1 = new HashSet<>();
		Set<CodeNode> cloneChildrenPackages2 = new HashSet<>();
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
		Collection<HotspotPackage> childrenHotspotPackages = hotspotPackage.getChildrenHotspotPackages();
		Collection<Package> childrenPackage1 = hasRelationService.findPackageHasPackages(currentPackage1);
		Collection<Package> childrenPackage2 = hasRelationService.findPackageHasPackages(currentPackage2);
		for(HotspotPackage childHotspotPackage : childrenHotspotPackages) {
			Package childPackage1 = childHotspotPackage.getPackage1();
			Package childPackage2 = childHotspotPackage.getPackage2();
			String childPath1 = String.join("_", childPackage1.getDirectoryPath(), childPackage2.getDirectoryPath());
			String childPath2 = String.join("_", childPackage2.getDirectoryPath(), childPackage1.getDirectoryPath());
			cloneChildrenPackages1.addAll(directoryPathToAllCloneChildrenPackages.get(childPath1));
			cloneChildrenPackages2.addAll(directoryPathToAllCloneChildrenPackages.get(childPath2));
			childrenPackage1.remove(childPackage1);
			childrenPackage2.remove(childPackage2);
		}
		for(Package otherChild1 : childrenPackage1) {
			hotspotPackage.addOtherChild1(otherChild1);
		}
		for(Package otherChild2 : childrenPackage2) {
			hotspotPackage.addOtherChild2(otherChild2);
		}
		int allNodes1 = getAllFilesNum_3(directoryIdToAllNodes, currentPackage1);
		int allNodes2 = getAllFilesNum_3(directoryIdToAllNodes, currentPackage2);
		int cloneNodes1 = cloneChildrenPackages1.size();
		int cloneNodes2 = cloneChildrenPackages2.size();
		directoryPathToAllCloneChildrenPackages.put(path1, cloneChildrenPackages1);
		directoryPathToAllCloneChildrenPackages.put(path2, cloneChildrenPackages2);
		hotspotPackage.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
		packageClone.setDate(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
		return aggregator.aggregate(packageClone);
	}

	//设置HotspotPackage信息
	public HotspotPackage setHotspotPackageData(AggregationClone aggregationClone, long parent1Id, long parent2Id) {
		Package currentPackage1 = (Package) aggregationClone.getStartNode();
		Package currentPackage2 = (Package) aggregationClone.getEndNode();
		CoChange packageCoChanges = null;
		ModuleClone packageCloneCoChanges = null;
		if(parent1Id > -1 && parent2Id > -1){
			packageCoChanges = coChangeRepository.findModuleCoChange(currentPackage1.getId(), currentPackage2.getId());
			packageCloneCoChanges = moduleCloneRepository.findModuleClone(currentPackage1.getId(), currentPackage2.getId());
		}
		RelationDataForDoubleNodes<Node, Relation> relationDataForDoubleNodes = new RelationDataForDoubleNodes<Node, Relation>(currentPackage1, currentPackage2);
		HotspotPackage hotspotPackage = new HotspotPackage(relationDataForDoubleNodes);
		hotspotPackage.setClonePairs(aggregationClone.getClonePairs());
		hotspotPackage.setData(aggregationClone.getAllNodesInNode1(), aggregationClone.getAllNodesInNode2(), aggregationClone.getNodesInNode1(), aggregationClone.getNodesInNode2());
		if(packageCoChanges != null){
			hotspotPackage.setPackageCochangeTimes(packageCoChanges.getTimes());
		}
		else {
			hotspotPackage.setPackageCochangeTimes(0);
		}
		if(packageCloneCoChanges != null){
			hotspotPackage.setPackageCloneCochangeTimes(packageCloneCoChanges.getModuleCloneCochangeTimes());
		}
		else {
			hotspotPackage.setPackageCloneCochangeTimes(0);
		}
		List<HotspotPackage> childrenHotspotPackages = loadHotspotPackagesByParentId(currentPackage1.getId(), currentPackage2.getId());
		if(childrenHotspotPackages.size() > 0) {
			//将有子包的包下文件打包
			if(isBranchPackageWithFiles(currentPackage1) && isBranchPackageWithFiles(currentPackage2)) {
				ModuleClone moduleClone = moduleCloneRepository.findModuleClone(currentPackage1.getId(), currentPackage2.getId());
				Package childPackage1 = buildPackageForFiles(currentPackage1);
				Package childPackage2 = buildPackageForFiles(currentPackage2);
				if(moduleClone != null) {
					HotspotPackage childHotspotPackage = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(childPackage1, childPackage2));
					childHotspotPackage.setData(moduleClone.getAllNodesInNode1(), moduleClone.getAllNodesInNode2(), moduleClone.getNodesInNode1(), moduleClone.getNodesInNode2());
					childHotspotPackage.setClonePairs(moduleClone.getClonePairs());
					if(packageCoChanges != null){
						childHotspotPackage.setPackageCochangeTimes(packageCoChanges.getTimes());
					}
					else {
						childHotspotPackage.setPackageCochangeTimes(0);
					}
					if(packageCloneCoChanges != null){
						childHotspotPackage.setPackageCloneCochangeTimes(packageCloneCoChanges.getModuleCloneCochangeTimes());
					}
					else {
						childHotspotPackage.setPackageCloneCochangeTimes(0);
					}
					hotspotPackage.setPackageCochangeTimes(0);
					hotspotPackage.setPackageCloneCochangeTimes(0);
					hotspotPackage.setClonePairs(0);
					hotspotPackage.addHotspotChild(childHotspotPackage);
				}
				else {
					childPackage1.setAllNodes(containRelationService.findPackageContainFiles(currentPackage1).size());
					childPackage2.setAllNodes(containRelationService.findPackageContainFiles(currentPackage2).size());
					hotspotPackage.addOtherChild1(childPackage1);
					hotspotPackage.addOtherChild2(childPackage2);
				}
			}

			Collection<Package> childrenPackages1 = hasRelationService.findPackageHasPackages(hotspotPackage.getPackage1());
			Collection<Package> childrenPackages2 = hasRelationService.findPackageHasPackages(hotspotPackage.getPackage2());
			for(HotspotPackage childHotspotPackage : childrenHotspotPackages) {
				hotspotPackage.addHotspotChild(childHotspotPackage);
				childrenPackages1.remove(childHotspotPackage.getPackage1());
				childrenPackages2.remove(childHotspotPackage.getPackage2());
			}
			for(Package childPackage1 : childrenPackages1) {
				hotspotPackage.addOtherChild1(childPackage1);
			}
			for(Package childPackage2 : childrenPackages2) {
				hotspotPackage.addOtherChild2(childPackage2);
			}
		}
		return hotspotPackage;
	}

	//递归加载聚合结果
	public List<HotspotPackage> loadHotspotPackagesByParentId(long parent1Id, long parent2Id) {
		List<HotspotPackage> result = new ArrayList<>();
		List<AggregationClone> aggregationClones = aggregationCloneRepository.findAggregationCloneByParentId(parent1Id, parent2Id);
		for(AggregationClone aggregationClone : aggregationClones) {
			result.add(setHotspotPackageData(aggregationClone, parent1Id, parent2Id));
		}
		//聚合排序，每一次均进行排序
		result.sort((d1, d2) -> {
			int allNodes1 = d1.getAllNodes1() + d1.getAllNodes2();
			int cloneNodes1 = d1.getRelationNodes1() + d1.getRelationNodes2();
			double percentageThreshold1 = (cloneNodes1 + 0.0) / allNodes1;
			int allNodes2 = d2.getAllNodes1() + d2.getAllNodes2();
			int cloneNodes2 = d2.getRelationNodes1() + d2.getRelationNodes2();
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

	//寻找父包
	private Package findParentPackage_3(Map<String, Package> directoryPathToParentPackage, Package pck) {
		directoryPathToParentPackage.put(pck.getDirectoryPath(), pck);
		String parentDirectoryPath = pck.lastPackageDirectoryPath();
		if (FileUtil.SLASH_LINUX.equals(parentDirectoryPath)) {
			return null;
		}
		Package parent = directoryPathToParentPackage.get(parentDirectoryPath);
		if (parent == null) {
			parent = containRelationService.findPackageInPackage(pck);
			if (parent != null) {
				directoryPathToParentPackage.put(parentDirectoryPath, parent);
			}
		}
		return parent;
	}

	//获取包下文件总数
	private int getAllFilesNum_1(Map<Long, Integer> directoryIdToAllNodes, Package pck) {
		if(directoryIdToAllNodes.containsKey(pck.getId())) {
			return directoryIdToAllNodes.get(pck.getId());
		}
		int number = containRelationService.findPackageContainFiles(pck).size();
		Collection<Package> childrenPackages = hasRelationService.findPackageHasPackages(pck);
		for(Package childPackage : childrenPackages) {
			number += getAllFilesNum_1(directoryIdToAllNodes, childPackage);
		}
		directoryIdToAllNodes.put(pck.getId(), number);
		return number;
	}

	//获取包下文件总数
	private int getAllFilesNum_3(Map<Long, Integer> directoryIdToAllNodes, Package pck) {
		if(directoryIdToAllNodes.containsKey(pck.getId())) {
			return directoryIdToAllNodes.get(pck.getId());
		}
		int number = containRelationService.findPackageContainFiles(pck).size();
		Collection<Package> childrenPackages = hasRelationService.findPackageHasPackages(pck);
		for(Package childPackage : childrenPackages) {
			number += getAllFilesNum_1(directoryIdToAllNodes, childPackage);
		}
		directoryIdToAllNodes.put(pck.getId(), number);
		return number;
	}

	//判断pck1和pck2两个包是否可以聚合
	private boolean isHotspotPackages_1(RelationAggregator<Boolean> aggregator, Map<Long, Integer> allNodesOfPackage, Map<String, Integer> cloneNodesOfClonePackages, Map<String, Integer> cloneNodesOfHotspotPackages, Collection<String> clonePackages, Collection<String> hotspotPackages, Package pck1, Package pck2) {
		String packages1 = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
		String packages2 = String.join("_", pck2.getDirectoryPath(), pck1.getDirectoryPath());
		if(hotspotPackages.contains(packages1) || hotspotPackages.contains(packages2)) {
			return true;
		}
		int allNodes1 = getAllFilesNum_1(allNodesOfPackage, pck1);
		int allNodes2 = getAllFilesNum_1(allNodesOfPackage, pck2);
		int cloneNodes1 = 0;
		int cloneNodes2 = 0;
		if(clonePackages.contains(packages1) || clonePackages.contains(packages2)) {
			cloneNodes1 = cloneNodesOfClonePackages.get(packages1);
			cloneNodes2 = cloneNodesOfClonePackages.get(packages2);
		}
		//遍历包下文件或子包，函数待改进
		Collection<Package> childrenPackages1 = hasRelationService.findPackageHasPackages(pck1);
		Collection<Package> childrenPackages2 = hasRelationService.findPackageHasPackages(pck2);
		int childrenHotspotPackageCount1 = 0;
		int childrenHotspotPackageCount2 = 0;
		for(Package childPackage1 : childrenPackages1) {
			boolean flag = false;
			int index = 0;
			String children1 = "";
			String children2 = "";
			for(Package childPackage2 : childrenPackages2) {
				children1 = String.join("_", childPackage1.getDirectoryPath(), childPackage2.getDirectoryPath());
				if(hotspotPackages.contains(children1)) {
					if(!flag) {
						children2 = children1;
						index = 0;
						flag = true;
					}
					else {
						int cloneNodesOfChildren1 = cloneNodesOfHotspotPackages.get(children1);
						int cloneNodesOfChildren2;
						if(index == 0) {
							cloneNodesOfChildren2 = cloneNodesOfHotspotPackages.get(children2);
						}
						else {
							cloneNodesOfChildren2 = cloneNodesOfClonePackages.get(children2);
						}
						if(cloneNodesOfChildren1 > cloneNodesOfChildren2) {
							children2 = children1;
							index = 0;
							flag = true;
						}
					}
				}
				else if(clonePackages.contains(children1)) {
					if(!flag) {
						children2 = children1;
						index = 1;
						flag = true;
					}
					else {
						int cloneNodesOfChildren1 = cloneNodesOfClonePackages.get(children1);
						int cloneNodesOfChildren2;
						if(index == 0) {
							cloneNodesOfChildren2 = cloneNodesOfHotspotPackages.get(children2);
						}
						else {
							cloneNodesOfChildren2 = cloneNodesOfClonePackages.get(children2);
						}
						if(cloneNodesOfChildren1 > cloneNodesOfChildren2) {
							children2 = children1;
							index = 1;
							flag = true;
						}
					}
				}
			}
			if(flag && hotspotPackages.contains(children2)) {
				cloneNodes1 += cloneNodesOfHotspotPackages.get(children2);
				childrenHotspotPackageCount1 ++;
			}
			else if(flag && clonePackages.contains(children2)) {
				cloneNodes1 += cloneNodesOfClonePackages.get(children2);
			}
		}
		for(Package childPackage2 : childrenPackages2) {
			int index = 0;
			boolean flag = false;
			String children2 = "";
			String children1 = "";
			for(Package childPackage1 : childrenPackages1) {
				children2 = String.join("_", childPackage2.getDirectoryPath(), childPackage1.getDirectoryPath());
				if(hotspotPackages.contains(children2)) {
					if(!flag) {
						children1 = children2;
						index = 0;
						flag = true;
					}
					else {
						int cloneNodesOfChildren2 = cloneNodesOfHotspotPackages.get(children2);
						int cloneNodesOfChildren1;
						if(index == 0) {
							cloneNodesOfChildren1 = cloneNodesOfHotspotPackages.get(children1);
						}
						else {
							cloneNodesOfChildren1 = cloneNodesOfClonePackages.get(children1);
						}
						if(cloneNodesOfChildren2 > cloneNodesOfChildren1) {
							children1 = children2;
							index = 0;
							flag = true;
						}
					}
				}
				else if(clonePackages.contains(children2)) {
					if(!flag) {
						children1 = children2;
						index = 1;
						flag = true;
					}
					else {
						int cloneNodesOfChildren2 = cloneNodesOfClonePackages.get(children2);
						int cloneNodesOfChildren1;
						if(index == 0) {
							cloneNodesOfChildren1 = cloneNodesOfHotspotPackages.get(children1);
						}
						else {
							cloneNodesOfChildren1 = cloneNodesOfClonePackages.get(children1);
						}
						if(cloneNodesOfChildren2 > cloneNodesOfChildren1) {
							children1 = children2;
							index = 1;
							flag = true;
						}
					}
				}
			}
			if(flag && hotspotPackages.contains(children1)) {
				cloneNodes2 += cloneNodesOfHotspotPackages.get(children1);
				childrenHotspotPackageCount2 ++;
			}
			else if(flag && clonePackages.contains(children1)) {
				cloneNodes2 += cloneNodesOfClonePackages.get(children1);
			}
		}
		RelationDataForDoubleNodes<Node, Relation> packageClone = new RelationDataForDoubleNodes<Node, Relation>(pck1, pck2);
		packageClone.setDate3(cloneNodes1, cloneNodes2, allNodes1, allNodes2, childrenPackages1.size(), childrenPackages2.size(), childrenHotspotPackageCount1, childrenHotspotPackageCount2);
		if(aggregator.aggregate(packageClone)) {
			cloneNodesOfHotspotPackages.put(packages1, cloneNodes1);
			cloneNodesOfHotspotPackages.put(packages2, cloneNodes2);
			hotspotPackages.add(packages1);
			hotspotPackages.add(packages2);
			return true;
		}
		else {
			cloneNodesOfClonePackages.put(packages1, cloneNodes1);
			cloneNodesOfClonePackages.put(packages2, cloneNodes2);
			clonePackages.add(packages1);
			clonePackages.add(packages2);
			return false;
		}
	}

	//加载根目录的子目录
	public void AddChildrenPackages_1(Map<Long, Integer> allNodesOfPackage, Map<String, Integer> cloneNodesOfClonePackages, Map<String, Integer> cloneNodesOfHotspotPackages, Collection<String> clonePackages, Collection<String> hotspotPackages, HotspotPackage rootHotspotPackage, Collection<? extends Relation> fileClones, Map<String, HotspotPackage> idToPackageClone) {
		Package package1 = rootHotspotPackage.getPackage1();
		Package package2 = rootHotspotPackage.getPackage2();
		Collection<Package> childrenPackages1 = hasRelationService.findPackageHasPackages(package1);
		Collection<Package> childrenPackages2 = hasRelationService.findPackageHasPackages(package2);
		for(Package childPackage1 : childrenPackages1) {
			int index = 0;
			boolean flag = false;
			String children1 = "";
			String children2 = "";
			Package childPackage = null;
			for(Package childPackage2 : childrenPackages2) {
				children1 = String.join("_", childPackage1.getDirectoryPath(), childPackage2.getDirectoryPath());
				if(hotspotPackages.contains(children1)) {
					if(!flag) {
						children2 = children1;
						childPackage = childPackage2;
						index = 0;
						flag = true;
					}
					else {
						int cloneNodesOfChildren1 = cloneNodesOfHotspotPackages.get(children1);
						int cloneNodesOfChildren2;
						if(index == 0) {
							cloneNodesOfChildren2 = cloneNodesOfHotspotPackages.get(children2);
						}
						else {
							cloneNodesOfChildren2 = cloneNodesOfClonePackages.get(children2);
						}
						if(cloneNodesOfChildren1 > cloneNodesOfChildren2) {
							children2 = children1;
							childPackage = childPackage2;
							index = 0;
							flag = true;
						}
					}
				}
				else if(clonePackages.contains(children1)) {
					if(!flag) {
						children2 = children1;
						childPackage = childPackage2;
						index = 1;
						flag = true;
					}
					else {
						int cloneNodesOfChildren1 = cloneNodesOfClonePackages.get(children1);
						int cloneNodesOfChildren2;
						if(index == 0) {
							cloneNodesOfChildren2 = cloneNodesOfHotspotPackages.get(children2);
						}
						else {
							cloneNodesOfChildren2 = cloneNodesOfClonePackages.get(children2);
						}
						if(cloneNodesOfChildren1 > cloneNodesOfChildren2) {
							children2 = children1;
							childPackage = childPackage2;
							index = 1;
							flag = true;
						}
					}
				}
			}
			if(flag && hotspotPackages.contains(children2)) {
				HotspotPackage childHotspotPackage1;
				RelationDataForDoubleNodes<Node, Relation> childPackageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(fileClones, childPackage1, childPackage);
				if(childPackageClone != null) {
					childHotspotPackage1 = idToPackageClone.getOrDefault(childPackageClone.getId(), new HotspotPackage(childPackageClone));
					if(childHotspotPackage1.getPackage1().getDirectoryPath().equals(childPackage.getDirectoryPath()) && childHotspotPackage1.getPackage2().getDirectoryPath().equals(childPackage1.getDirectoryPath())) {
						childHotspotPackage1.swapPackages();
					}
				}
				else {
					childHotspotPackage1 = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(childPackage1, childPackage));
				}
				AddChildrenPackages_1(allNodesOfPackage, cloneNodesOfClonePackages, cloneNodesOfHotspotPackages, clonePackages, hotspotPackages, childHotspotPackage1, fileClones, idToPackageClone);
				Package childrenPackage1 = childHotspotPackage1.getPackage1();
				Package childrenPackage2 = childHotspotPackage1.getPackage2();
				String packages1 = String.join("_", childrenPackage1.getDirectoryPath(), childrenPackage2.getDirectoryPath());
				String packages2 = String.join("_", childrenPackage2.getDirectoryPath(), childrenPackage1.getDirectoryPath());
				int allNodes1 = getAllFilesNum_1(allNodesOfPackage, childrenPackage1);
				int allNodes2 = getAllFilesNum_1(allNodesOfPackage, childrenPackage2);
				int cloneNodes1 = cloneNodesOfHotspotPackages.get(packages1);
				int cloneNodes2 = cloneNodesOfHotspotPackages.get(packages2);
				childHotspotPackage1.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
				rootHotspotPackage.addHotspotChild(childHotspotPackage1);
			}
			else if(flag && clonePackages.contains(children2)) {
				HotspotPackage childHotspotPackage1;
				RelationDataForDoubleNodes<Node, Relation> childPackageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(fileClones, childPackage1, childPackage);
				if(childPackageClone != null) {
					childHotspotPackage1 = idToPackageClone.getOrDefault(childPackageClone.getId(), new HotspotPackage(childPackageClone));
					if(childHotspotPackage1.getPackage1().getDirectoryPath().equals(childPackage.getDirectoryPath()) && childHotspotPackage1.getPackage2().getDirectoryPath().equals(childPackage1.getDirectoryPath())) {
						childHotspotPackage1.swapPackages();
					}
				}
				else {
					childHotspotPackage1 = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(childPackage1, childPackage));
				}
				AddChildrenPackages_1(allNodesOfPackage, cloneNodesOfClonePackages, cloneNodesOfHotspotPackages, clonePackages, hotspotPackages, childHotspotPackage1, fileClones, idToPackageClone);
				Package childrenPackage1 = childHotspotPackage1.getPackage1();
				Package childrenPackage2 = childHotspotPackage1.getPackage2();
				String packages1 = String.join("_", childrenPackage1.getDirectoryPath(), childrenPackage2.getDirectoryPath());
				String packages2 = String.join("_", childrenPackage2.getDirectoryPath(), childrenPackage1.getDirectoryPath());
				int allNodes1 = getAllFilesNum_1(allNodesOfPackage, childrenPackage1);
				int allNodes2 = getAllFilesNum_1(allNodesOfPackage, childrenPackage2);
				int cloneNodes1 = cloneNodesOfClonePackages.get(packages1);
				int cloneNodes2 = cloneNodesOfClonePackages.get(packages2);
				childHotspotPackage1.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
				rootHotspotPackage.addHotspotChild(childHotspotPackage1);
			}
			else {
				childPackage1.setAllNodes(getAllFilesNum_1(allNodesOfPackage, childPackage1));
				rootHotspotPackage.addOtherChild1(childPackage1);
			}
		}
		for(Package childPackage2 : childrenPackages2) {
			int index = 0;
			boolean flag = false;
			String children2 = "";
			String children1 = "";
			for(Package childPackage1 : childrenPackages1) {
				children2 = String.join("_", childPackage2.getDirectoryPath(), childPackage1.getDirectoryPath());
				if(hotspotPackages.contains(children2)) {
					if(!flag) {
						children1 = children2;
						index = 0;
						flag = true;
					}
					else {
						int cloneNodesOfChildren2 = cloneNodesOfHotspotPackages.get(children2);
						int cloneNodesOfChildren1;
						if(index == 0) {
							cloneNodesOfChildren1 = cloneNodesOfHotspotPackages.get(children1);
						}
						else {
							cloneNodesOfChildren1 = cloneNodesOfClonePackages.get(children1);
						}
						if(cloneNodesOfChildren2 > cloneNodesOfChildren1) {
							children1 = children2;
							index = 0;
							flag = true;
						}
					}
				}
				else if(clonePackages.contains(children2)) {
					if(!flag) {
						children1 = children2;
						index = 1;
						flag = true;
					}
					else {
						int cloneNodesOfChildren2 = cloneNodesOfClonePackages.get(children2);
						int cloneNodesOfChildren1;
						if(index == 0) {
							cloneNodesOfChildren1 = cloneNodesOfHotspotPackages.get(children1);
						}
						else {
							cloneNodesOfChildren1 = cloneNodesOfClonePackages.get(children1);
						}
						if(cloneNodesOfChildren2 > cloneNodesOfChildren1) {
							children1 = children2;
							index = 1;
							flag = true;
						}
					}
				}
			}
			if(!flag) {
				childPackage2.setAllNodes(getAllFilesNum_1(allNodesOfPackage, childPackage2));
				rootHotspotPackage.addOtherChild2(childPackage2);
			}
		}
	}
	//判断是否符合聚合条件
	private boolean isHotspotPackage_2(RelationAggregator<Boolean> aggregator, Map<String, Boolean> isHotspot, HotspotPackage hotspotPackage) {
		Package currentPackage1 = hotspotPackage.getPackage1();
		Package currentPackage2 = hotspotPackage.getPackage2();
		RelationDataForDoubleNodes<Node, Relation> packageClone = new RelationDataForDoubleNodes<Node, Relation>(currentPackage1, currentPackage2);
		int allNodes1 = 0;
		int allNodes2 = 0;
		int cloneNodes1 = 0;
		int	cloneNodes2 = 0;

		//包下只有文件情况
		if(isLeafPackage(currentPackage1) || isLeafPackage(currentPackage2)) {
			packageClone.setDate(hotspotPackage.getAllNodes1(), hotspotPackage.getAllNodes2(), hotspotPackage.getRelationNodes1(), hotspotPackage.getRelationNodes2());
			return aggregator.aggregate(packageClone);
		}

		//包下另有文件情况
		if(isBranchPackageWithFiles(currentPackage1) || isBranchPackageWithFiles(currentPackage2)) {
			allNodes1 = 1;
			allNodes2 = 1;
			packageClone.setDate(hotspotPackage.getAllNodes1(), hotspotPackage.getAllNodes2(), hotspotPackage.getRelationNodes1(), hotspotPackage.getRelationNodes2());
			if(aggregator.aggregate(packageClone)) {
				cloneNodes1 = 1;
				cloneNodes2 = 1;
			}
		}

		//遍历包下子包
		Collection<HotspotPackage> childrenHotspotPackages = hotspotPackage.getChildrenHotspotPackages();
		Collection<Package> childrenPackage1 = hasRelationService.findPackageHasPackages(currentPackage1);
		Collection<Package> childrenPackage2 = hasRelationService.findPackageHasPackages(currentPackage2);
		Collection<Package> childrenHotspotPackage1 = new ArrayList<>();
		Collection<Package> childrenHotspotPackage2 = new ArrayList<>();
		allNodes1 += childrenPackage1.size();
		allNodes2 += childrenPackage2.size();
		for(HotspotPackage childHotspotPackage : childrenHotspotPackages) {
			Package childPackage1 = childHotspotPackage.getPackage1();
			Package childPackage2 = childHotspotPackage.getPackage2();
			String childrenPackages = String.join("_", childPackage1.getDirectoryPath(), childPackage2.getDirectoryPath());
			if(isHotspot.get(childrenPackages)) {
				if(!childrenHotspotPackage1.contains(childPackage1)) {
					childrenHotspotPackage1.add(childPackage1);
					cloneNodes1 += 1;
				}
				if(!childrenHotspotPackage2.contains(childPackage2)) {
					childrenHotspotPackage2.add(childPackage2);
					cloneNodes2 += 1;
				}
			}
			childrenPackage1.remove(childPackage1);
			childrenPackage2.remove(childPackage2);
		}
		for(Package otherChild1 : childrenPackage1) {
			hotspotPackage.addOtherChild1(otherChild1);
		}
		for(Package otherChild2 : childrenPackage2) {
			hotspotPackage.addOtherChild2(otherChild2);
		}
		hotspotPackage.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
		packageClone.setDate(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
		return aggregator.aggregate(packageClone);
	}
}
