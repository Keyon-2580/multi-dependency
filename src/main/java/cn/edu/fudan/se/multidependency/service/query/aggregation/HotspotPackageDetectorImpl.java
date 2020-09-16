package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.insert.RepositoryService;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
	private HasRelationService hasRelationService;

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryService.class);
	private Map<Long, Integer> allNodesOfPackage = new ConcurrentHashMap<>();
	private Map<String, Integer> cloneNodesOfHotspotPackages = new ConcurrentHashMap<>();
	private Map<String, Integer> cloneNodesOfClonePackages = new ConcurrentHashMap<>();
	private Collection<String> clonePackages = new ArrayList<>();
	private Collection<String> hotspotPackages = new ArrayList<>();
	private Map<String, Collection<String>> cloneChildrenPackagesOfPackages = new ConcurrentHashMap<>();
	private Map<String, Package> directoryPathToPackage = new ConcurrentHashMap<>();
	private ThreadLocal<Integer> rowKey = new ThreadLocal<>();
	private Package findParentPackage(Package pck) {
		directoryPathToPackage.put(pck.getDirectoryPath(), pck);
		String parentDirectoryPath = pck.lastPackageDirectoryPath();
		if (FileUtil.SLASH_LINUX.equals(parentDirectoryPath)) {
			return null;
		}
		Package parent = directoryPathToPackage.get(parentDirectoryPath);
		if (parent != null) {
			return parent;
		}
		parent = containRelationService.findPackageInPackage(pck);
		if (parent != null) {
			directoryPathToPackage.put(parentDirectoryPath, parent);
		}
		return parent;
	}

	private int getAllFilesNum(Package pck) {
		if(allNodesOfPackage.containsKey(pck.getId())) {
			return allNodesOfPackage.get(pck.getId());
		}
		int number = containRelationService.findPackageContainFiles(pck).size();
		Collection<Package> childrenPackages = hasRelationService.findPackageHasPackages(pck);
		for(Package childPackage : childrenPackages) {
			number += getAllFilesNum(childPackage);
		}
		return number;
	}

	//判断pck1和pck2两个包是否可以聚合
	private boolean isHotspotPackages(Package pck1, Package pck2) {
		String packages1 = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
		String packages2 = String.join("_", pck2.getDirectoryPath(), pck1.getDirectoryPath());
		if(hotspotPackages.contains(packages1) || hotspotPackages.contains(packages2)) {
			return true;
		}
		int allNodes1 = containRelationService.findPackageContainFiles(pck1).size();
		int allNodes2 = containRelationService.findPackageContainFiles(pck2).size();
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
				allNodes1 += allNodesOfPackage.get(childPackage1.getId());
				cloneNodes1 += cloneNodesOfHotspotPackages.get(children2);
				childrenHotspotPackageCount1 ++;
			}
			else if(flag && clonePackages.contains(children2)) {
				allNodes1 += allNodesOfPackage.get(childPackage1.getId());
				cloneNodes1 += cloneNodesOfClonePackages.get(children2);
			}
			else {
				allNodes1 += getAllFilesNum(childPackage1);
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
				allNodes2 += allNodesOfPackage.get(childPackage2.getId());
				cloneNodes2 += cloneNodesOfHotspotPackages.get(children1);
				childrenHotspotPackageCount2 ++;
			}
			else if(flag && clonePackages.contains(children1)) {
				allNodes2 += allNodesOfPackage.get(childPackage2.getId());
				cloneNodes2 += cloneNodesOfClonePackages.get(children1);
			}
			else {
				allNodes2 += getAllFilesNum(childPackage2);
			}
		}
		allNodesOfPackage.put(pck1.getId(), allNodes1);
		allNodesOfPackage.put(pck2.getId(), allNodes2);
		if((cloneNodes1 + cloneNodes2 + 0.0) / (allNodes1 + allNodes2) > 0.5 || (childrenPackages1.size() != 0 && (childrenHotspotPackageCount1 + 0.0) / childrenPackages1.size() > 0.5) || (childrenPackages2.size() != 0 && (childrenHotspotPackageCount2 + 0.0) / childrenPackages2.size() > 0.5)) {
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
		//不考虑当前包下的其它非克隆包，原则上应该考虑
//		else if(clonePackages.contains(similarPackage1) || clonePackages.contains(similarPackage2)) {
//			allNodes1 = containRelationService.findPackageContainFiles(pck1).size();
//			allNodes2 = containRelationService.findPackageContainFiles(pck2).size();
//			cloneNodes1 = cloneNodesOfPackages2.get(similarPackage1);
//			cloneNodes2 = cloneNodesOfPackages2.get(similarPackage2);
//			if((cloneNodes1 + cloneNodes2 + 0.0) / (allNodes1 + allNodes2) >= 0.5) {
//				allNodesOfPackage.put(pck1.getId(), allNodes1);
//				allNodesOfPackage.put(pck2.getId(), allNodes2);
//				cloneNodesOfPackages.put(similarPackage1, cloneNodes1);
//				cloneNodesOfPackages.put(similarPackage2, cloneNodes2);
//				similarPackages.add(similarPackage1);
//				similarPackages.add(similarPackage2);
//				return true;
//			}
//			return false;
//		}
	}

	//加载根目录的子目录
	public void AddChildrenPackages(HotspotPackage rootHotspotPackage, Collection<? extends Relation> fileClones, Map<String, HotspotPackage> idToPackageClone) {
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
				}
				else {
					String childrenId1 = String.join("_", childPackage1.getDirectoryPath(), childPackage.getDirectoryPath());
					childHotspotPackage1 = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(childPackage1, childPackage, childrenId1));
				}
				if(!rootHotspotPackage.isContainHotspotChild(childHotspotPackage1)) {
					AddChildrenPackages(childHotspotPackage1, fileClones, idToPackageClone);
					Package childrenPackage1 = childHotspotPackage1.getPackage1();
					Package childrenPackage2 = childHotspotPackage1.getPackage2();
					String packages1 = String.join("_", childrenPackage1.getDirectoryPath(), childrenPackage2.getDirectoryPath());
					String packages2 = String.join("_", childrenPackage2.getDirectoryPath(), childrenPackage1.getDirectoryPath());
					int allNodes1 = allNodesOfPackage.get(childrenPackage1.getId());
					int allNodes2 = allNodesOfPackage.get(childrenPackage2.getId());
					int cloneNodes1 = cloneNodesOfHotspotPackages.get(packages1);
					int cloneNodes2 = cloneNodesOfHotspotPackages.get(packages2);
					childHotspotPackage1.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
					rootHotspotPackage.addHotspotChild(childHotspotPackage1);
				}
			}
			else if(flag && clonePackages.contains(children2)) {
				HotspotPackage childHotspotPackage1;
				RelationDataForDoubleNodes<Node, Relation> childPackageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(fileClones, childPackage1, childPackage);
				if(childPackageClone != null) {
					childHotspotPackage1 = idToPackageClone.getOrDefault(childPackageClone.getId(), new HotspotPackage(childPackageClone));
				}
				else {
					String childrenId1 = String.join("_", childPackage1.getDirectoryPath(), childPackage.getDirectoryPath());
					childHotspotPackage1 = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(childPackage1, childPackage, childrenId1));
				}
				if(!rootHotspotPackage.isContainHotspotChild(childHotspotPackage1)) {
					AddChildrenPackages(childHotspotPackage1, fileClones, idToPackageClone);
					Package childrenPackage1 = childHotspotPackage1.getPackage1();
					Package childrenPackage2 = childHotspotPackage1.getPackage2();
					String packages1 = String.join("_", childrenPackage1.getDirectoryPath(), childrenPackage2.getDirectoryPath());
					String packages2 = String.join("_", childrenPackage2.getDirectoryPath(), childrenPackage1.getDirectoryPath());
					int allNodes1 = allNodesOfPackage.get(childrenPackage1.getId());
					int allNodes2 = allNodesOfPackage.get(childrenPackage2.getId());
					int cloneNodes1 = cloneNodesOfClonePackages.get(packages1);
					int cloneNodes2 = cloneNodesOfClonePackages.get(packages2);
					childHotspotPackage1.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
					rootHotspotPackage.addHotspotChild(childHotspotPackage1);
				}
			}
			else {
				if(!rootHotspotPackage.isContainOtherChild1(childPackage1)) {
					childPackage1.setAllNodes(getAllFilesNum(childPackage1));
					rootHotspotPackage.addOtherChild1(childPackage1);
				}
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
				if(!rootHotspotPackage.isContainOtherChild2(childPackage2)) {
					childPackage2.setAllNodes(getAllFilesNum(childPackage2));
					rootHotspotPackage.addOtherChild2(childPackage2);
				}
			}
		}
	}

	@Override
	public Collection<HotspotPackage> detectHotspotPackages() {
		return detectHotspotPackagesByFileClone();
		//return detectHotspotPackagesByFileCloneLoc();
		//return detectHotspotPackagesByFileCoChange();
		//return detectHotspotPackagesByFileCoChangeTimes();
	}

	private Collection<HotspotPackage> aggregatePackageRelation(Collection<? extends Relation> subNodeRelations,
																Collection<RelationDataForDoubleNodes<Node, Relation>> packageRelations,
																RelationAggregator<Boolean> aggregator) {
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
			Package parentPackage1 = findParentPackage(currentPackage1);
			Package parentPackage2 = findParentPackage(currentPackage2);
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

		Map<String, HotspotPackage> parentSimilarPacakges = new HashMap<>();
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
					HotspotPackage parentSimilar = parentSimilarPacakges.get(parentId);
					if (parentSimilar == null) {
						parentPck1 = new Package();
						parentPck1.setId(-1L);
						parentPck1.setEntityId(-1L);
						parentPck1.setDirectoryPath(parentPck1Path);
						parentPck1.setName(parentPck1Path);
						parentPck2 = new Package();
						parentPck2.setId(-1L);
						parentPck2.setEntityId(-1L);
						parentPck2.setDirectoryPath(parentPck2Path);
						parentPck2.setName(parentPck2Path);
						parentSimilar = new HotspotPackage(new RelationDataForDoubleNodes(parentPck1, parentPck2, parentId));
						parentSimilarPacakges.put(parentSimilar.getId(), parentSimilar);
					}
					parentSimilar.addHotspotChild(value);
					isChild.put(id, true);
				}
			}
		}

		List<HotspotPackage> result = new ArrayList<>();

		for (HotspotPackage parentHotspotPackage : parentSimilarPacakges.values()) {
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

	public Collection<HotspotPackage> detectHotspotPackagesByFileClone2() {
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

	@Override
	public Collection<HotspotPackage> detectHotspotPackagesByFileClone() {
		Map<String, HotspotPackage> idToPackageClone = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Map<String, Boolean> isHotspot = new HashMap<>();
		Collection<? extends Relation> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		List<RelationDataForDoubleNodes<Node, Relation>> packageClones = (List<RelationDataForDoubleNodes<Node, Relation>>) summaryAggregationDataService.queryPackageCloneFromFileCloneSort(fileClones);
		//预处理，算法优化
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package pck1 = (Package) packageClone.getNode1();
			Package pck2 = (Package) packageClone.getNode2();
			Package parentPck1 = findParentPackage(pck1);
			Package parentPck2 = findParentPackage(pck2);
			while(parentPck1 != null && parentPck2 != null && !parentPck1.getId().equals(parentPck2.getId())) {
				String clonePackages1 = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
				String clonePackages2 = String.join("_", pck2.getDirectoryPath(), pck1.getDirectoryPath());
				String parentPackages1 = String.join("_", parentPck1.getDirectoryPath(), parentPck2.getDirectoryPath());
				String parentPackages2 = String.join("_", parentPck2.getDirectoryPath(), parentPck1.getDirectoryPath());
				Collection<String> cloneChildren1 = new ArrayList<>();
				Collection<String> cloneChildren2 = new ArrayList<>();
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages1)) {
					cloneChildren1 = cloneChildrenPackagesOfPackages.get(parentPackages1);
				}
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages2)) {
					cloneChildren2 = cloneChildrenPackagesOfPackages.get(parentPackages2);
				}
				if(cloneChildren1.contains(clonePackages1) || cloneChildren2.contains(clonePackages2)) {
					break;
				}
				cloneChildren1.add(clonePackages1);
				cloneChildren2.add(clonePackages2);
				cloneChildrenPackagesOfPackages.put(parentPackages1, cloneChildren1);
				cloneChildrenPackagesOfPackages.put(parentPackages2, cloneChildren2);
				pck1 = parentPck1;
				pck2 = parentPck2;
				try{
					parentPck1 = findParentPackage(pck1);
					parentPck2 = findParentPackage(pck2);
				}
				catch (Exception e) {
					parentPck1 = null;
					parentPck2 = null;
				}
			}
		}
		//预处理
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			Package pck1 = (Package) packageClone.getNode1();
			Package pck2 = (Package) packageClone.getNode2();
			String clonePackages1 = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
			String clonePackages2 = String.join("_", pck2.getDirectoryPath(), pck1.getDirectoryPath());
			if(clonePackages.contains(clonePackages1) || clonePackages.contains(clonePackages2)) {
				continue;
			}
			allNodesOfPackage.put(pck1.getId(), packageClone.getAllNodesInNode1().size());
			allNodesOfPackage.put(pck2.getId(), packageClone.getAllNodesInNode2().size());
			cloneNodesOfClonePackages.put(clonePackages1, packageClone.getNodesInNode1().size());
			cloneNodesOfClonePackages.put(clonePackages2, packageClone.getNodesInNode2().size());
			clonePackages.add(clonePackages1);
			clonePackages.add(clonePackages2);
		}
		//检测
		for(RelationDataForDoubleNodes<Node, Relation> packageClone : packageClones) {
			if(idToPackageClone.get(packageClone.getId()) != null) {
				continue;
			}
			Package pck1 = (Package) packageClone.getNode1();
			Package pck2 = (Package) packageClone.getNode2();
			String packages1 = String.join("_", pck1.getDirectoryPath(), pck2.getDirectoryPath());
			String packages2 = String.join("_", pck2.getDirectoryPath(), pck1.getDirectoryPath());
			Collection<String> cloneChildren1 = new ArrayList<>();
			Collection<String> cloneChildren2 = new ArrayList<>();
			if(cloneChildrenPackagesOfPackages.containsKey(packages1)) {
				cloneChildren1 = cloneChildrenPackagesOfPackages.get(packages1);
			}
			if(cloneChildrenPackagesOfPackages.containsKey(packages2)) {
				cloneChildren2 = cloneChildrenPackagesOfPackages.get(packages2);
			}
			if(!cloneChildren1.isEmpty() || !cloneChildren2.isEmpty()) {
				continue;
			}
			Package parentPck1 = findParentPackage(pck1);
			Package parentPck2 = findParentPackage(pck2);
			if(parentPck1 != null && parentPck2 != null && !parentPck1.getId().equals(parentPck2.getId())) {
				String parentPackages1 = String.join("_", parentPck1.getDirectoryPath(), parentPck2.getDirectoryPath());
				String parentPackages2 = String.join("_", parentPck2.getDirectoryPath(), parentPck1.getDirectoryPath());
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages1)) {
					cloneChildren1 = cloneChildrenPackagesOfPackages.get(parentPackages1);
					cloneChildren1.remove(packages1);
					cloneChildrenPackagesOfPackages.put(parentPackages1, cloneChildren1);
				}
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages2)) {
					cloneChildren2 = cloneChildrenPackagesOfPackages.get(parentPackages2);
					cloneChildren2.remove(packages2);
					cloneChildrenPackagesOfPackages.put(parentPackages2, cloneChildren2);
				}
			}
			HotspotPackage hotspotPackage = new HotspotPackage(packageClone);
			idToPackageClone.put(hotspotPackage.getId(), hotspotPackage);
			if(isHotspotPackages(pck1, pck2)) {
				isHotspot.put(packages1, true);
				isHotspot.put(packages2, true);
			}
			else {
				isHotspot.put(packages1, false);
				isHotspot.put(packages2, false);
			}
			isChild.put(packages1, false);
			isChild.put(packages2, false);
			Package currentPackage1 = pck1;
			Package currentPackage2 = pck2;
			Package parentPackage1 = parentPck1;
			Package parentPackage2 = parentPck2;
			boolean flag = false;
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
				HotspotPackage parentHotspotPackage;
				RelationDataForDoubleNodes<Node, Relation> childPackageClone = summaryAggregationDataService.querySuperNodeRelationFromSubNodeRelationSort(fileClones, parentPackage1, parentPackage2);
				if(childPackageClone != null) {
					parentHotspotPackage = idToPackageClone.getOrDefault(childPackageClone.getId(), new HotspotPackage(childPackageClone));
				}
				else {
					String parentId = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
					parentHotspotPackage = new HotspotPackage(new RelationDataForDoubleNodes<Node, Relation>(parentPackage1, parentPackage2, parentId));
				}
				String currentPackages1 = String.join("_", currentPackage1.getDirectoryPath(), currentPackage2.getDirectoryPath());
				String currentPackages2 = String.join("_", currentPackage2.getDirectoryPath(), currentPackage1.getDirectoryPath());
				String parentPackages1 = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				String parentPackages2 = String.join("_", parentPackage2.getDirectoryPath(), parentPackage1.getDirectoryPath());
				cloneChildren1 = new ArrayList<>();
				cloneChildren2 = new ArrayList<>();
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages1)) {
					cloneChildren1 = cloneChildrenPackagesOfPackages.get(parentPackages1);
					if(flag) {
						cloneChildren1.remove(currentPackages1);
						cloneChildrenPackagesOfPackages.put(parentPackages1, cloneChildren1);
					}
				}
				if(cloneChildrenPackagesOfPackages.containsKey(parentPackages2)) {
					cloneChildren2 = cloneChildrenPackagesOfPackages.get(parentPackages2);
					if(flag) {
						cloneChildren2.remove(currentPackages2);
						cloneChildrenPackagesOfPackages.put(parentPackages2, cloneChildren2);
					}
				}
				if(!cloneChildren1.isEmpty() || !cloneChildren2.isEmpty()) {
					break;
				}
				if(isHotspotPackages(parentPackage1, parentPackage2)) {
					isChild.put(currentPackages1, true);
					isChild.put(currentPackages2, true);
					isHotspot.put(parentPackages1, true);
					isHotspot.put(parentPackages2, true);
				}
				else {
					isHotspot.put(parentPackages1, false);
					isHotspot.put(parentPackages2, false);
				}
				isChild.put(parentPackages1, false);
				isChild.put(parentPackages2, false);
				idToPackageClone.put(parentHotspotPackage.getId(), parentHotspotPackage);
				currentPackage1 = parentPackage1;
				currentPackage2 = parentPackage2;
				try{
					parentPackage1 = findParentPackage(currentPackage1);
					parentPackage2 = findParentPackage(currentPackage2);
				}
				catch (Exception e) {
					parentPackage1 = null;
					parentPackage2 = null;
				}
				flag = true;
			}
		}

		//确定根目录
		List<HotspotPackage> result = new ArrayList<>();
		for(Map.Entry<String, HotspotPackage> entry : idToPackageClone.entrySet()) {
			HotspotPackage rootHotspotPackage = entry.getValue();
			Package package1 = rootHotspotPackage.getPackage1();
			Package package2 = rootHotspotPackage.getPackage2();
			String packages1 = String.join("_", package1.getDirectoryPath(), package2.getDirectoryPath());
			String packages2 = String.join("_", package2.getDirectoryPath(), package1.getDirectoryPath());
			if((!isChild.get(packages1) && isHotspot.get(packages1)) || (!isChild.get(packages2) && isHotspot.get(packages2))) {
				Package parentPackage1 = findParentPackage(package1);
				Package parentPackage2 = findParentPackage(package2);
				if(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
					String parentPackages1 = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
					String parentPackages2 = String.join("_", parentPackage2.getDirectoryPath(), parentPackage1.getDirectoryPath());
					if((isHotspot.containsKey(parentPackages1) && isHotspot.get(parentPackages1)) || (isHotspot.containsKey(parentPackages2) && isHotspot.get(parentPackages2))) {
						isChild.put(packages1, true);
						isChild.put(packages2, true);
						continue;
					}
				}
				if(!result.contains(rootHotspotPackage)) {
					int allNodes1 = allNodesOfPackage.get(package1.getId());
					int allNodes2 = allNodesOfPackage.get(package2.getId());
					int cloneNodes1 = cloneNodesOfHotspotPackages.get(packages1);
					int cloneNodes2 = cloneNodesOfHotspotPackages.get(packages2);
					rootHotspotPackage.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
					result.add(rootHotspotPackage);
				}
			}
		}

		//加载根目录的子目录
		for(HotspotPackage rootHotspotPackage : result) {
			AddChildrenPackages(rootHotspotPackage, fileClones, idToPackageClone);
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
		cell.setCellValue("目录2");
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("内部克隆文件对数");
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
			prefix += "|--------";
		}
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		row.createCell(0).setCellValue(prefix + hotspotPackage.getPackage1().getDirectoryPath());
		row.createCell(1).setCellValue(prefix + hotspotPackage.getPackage2().getDirectoryPath());
		row.createCell(2).setCellValue(hotspotPackage.getRelationPackages().sizeOfChildren());

		for (HotspotPackage child:hotspotPackage.getChildrenHotspotPackages()){
			printHotspotPackage(sheet, layer + 1, child);
		}
	}
}
