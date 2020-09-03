package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HotspotPackageDetectorImpl implements HotspotPackageDetector {

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

	private Map<Long, Integer> allNodesOfPackage = new ConcurrentHashMap<>();
	private Map<Collection<Long>, Integer> cloneNodesOfPackages = new ConcurrentHashMap<>();
	private Map<Collection<Long>, Integer> cloneNodesOfPackages2 = new ConcurrentHashMap<>();
	private Collection<Collection<Long>> clonePackages = new ArrayList<>();
	private Collection<Collection<Long>> similarPackages = new ArrayList<>();

	private Map<String, Package> directoryPathToPacakge = new ConcurrentHashMap<>();

	private ThreadLocal<Integer> rowKey = new ThreadLocal<>();

	private Package findParentPackage(Package pck) {
		directoryPathToPacakge.put(pck.getDirectoryPath(), pck);
		String parentDirectoryPath = pck.lastPackageDirectoryPath();
		if (FileUtil.SLASH_LINUX.equals(parentDirectoryPath)) {
			return null;
		}
		Package parent = directoryPathToPacakge.get(parentDirectoryPath);
		if (parent != null) {
			return parent;
		}
		parent = containRelationService.findPackageInPackage(pck);
		if (parent != null) {
			directoryPathToPacakge.put(parentDirectoryPath, parent);
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
				if (!(boolean) parentPackageClone.aggregateValue(RelationAggregatorForPackageByFileClone.getInstance())) {
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
			if (isChild.get(id) == false) {
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
			if (isChild.get(id) == false) {
				result.add(entry.getValue());
			}
		}
		result.sort((d1, d2) -> {
			return d1.getPackage1().getDirectoryPath().compareTo(d2.getPackage1().getDirectoryPath());
		});
		return result;
	}


	@Override
	public Collection<HotspotPackage> detectHotspotPackagesByFileClone() {
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
