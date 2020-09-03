package cn.edu.fudan.se.multidependency.service.query.clone;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueCalculator;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.data.DefaultPackageCloneValueCalculator;
import cn.edu.fudan.se.multidependency.service.query.clone.data.SimilarPackage;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

@Service
public class SimilarPackageDetectorImpl implements SimilarPackageDetector {

	@Autowired
	private CloneValueService cloneValueService;

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private HasRelationService hasRelationService;

	private final Map<Long, Integer> allNodesOfPackage = new HashMap<>();
	private final Map<Collection<Long>, Integer> cloneNodesOfPackages = new HashMap<>();
	private final Map<Collection<Long>, Integer> cloneNodesOfPackages2 = new HashMap<>();
	private final Collection<Collection<Long>> clonePackages = new ArrayList<>();
	private final Collection<Collection<Long>> similarPackages = new ArrayList<>();

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

	//判断pck1和pck2两个包是否可以聚合
	private boolean isSimilarPackages(Package pck1, Package pck2) {
		Collection<Long> similarPackage1 = new ArrayList<>();
		Collection<Long> similarPackage2 = new ArrayList<>();
		similarPackage1.add(pck1.getId());
		similarPackage1.add(pck2.getId());
		similarPackage2.add(pck2.getId());
		similarPackage2.add(pck1.getId());
		if(similarPackages.contains(similarPackage1) || similarPackages.contains(similarPackage2)) {
			similarPackages.remove(similarPackage1);
			similarPackages.remove(similarPackage2);
		}
		int allNodes1 = containRelationService.findPackageContainFiles(pck1).size();
		int allNodes2 = containRelationService.findPackageContainFiles(pck2).size();
		int cloneNodes1 = 0;
		int cloneNodes2 = 0;
		if(clonePackages.contains(similarPackage1) || clonePackages.contains(similarPackage2)) {
			cloneNodes1 = cloneNodesOfPackages2.get(similarPackage1);
			cloneNodes2 = cloneNodesOfPackages2.get(similarPackage2);
		}
		//遍历包下文件或子包，函数待改进
		Collection<Package> childrenPackages1 = hasRelationService.findPackageHasPackages(pck1);
		Collection<Package> childrenPackages2 = hasRelationService.findPackageHasPackages(pck2);
		int childrenSimilarPackages1 = 0;
		int childrenSimilarPackages2 = 0;
		for(Package childPackage1 : childrenPackages1) {
			boolean flag = false;
			for(Package childPackage2 : childrenPackages2) {
				Collection<Long> children1 = new ArrayList<>();
				children1.add(childPackage1.getId());
				children1.add(childPackage2.getId());
				if(similarPackages.contains(children1)) {
					allNodes1 += allNodesOfPackage.get(childPackage1.getId());
					cloneNodes1 += cloneNodesOfPackages.get(children1);
					childrenSimilarPackages1 ++;
					flag = true;
					break;
				}
				else if(clonePackages.contains(children1)) {
					allNodes1 += allNodesOfPackage.get(childPackage1.getId());
					cloneNodes1 += cloneNodesOfPackages2.get(children1);
					flag = true;
					break;
				}
			}
			if(!flag) {
				allNodes1 += getAllFilesNum(childPackage1);
			}
		}
		for(Package childPackage2 : childrenPackages2) {
			boolean flag = false;
			for(Package childPackage1 : childrenPackages1) {
				Collection<Long> children2 = new ArrayList<>();
				children2.add(childPackage2.getId());
				children2.add(childPackage1.getId());
				if(similarPackages.contains(children2)) {
					allNodes2 += allNodesOfPackage.get(childPackage2.getId());
					cloneNodes2 += cloneNodesOfPackages.get(children2);
					childrenSimilarPackages2 ++;
					flag = true;
					break;
				}
				else if(clonePackages.contains(children2)) {
					allNodes2 += allNodesOfPackage.get(childPackage2.getId());
					cloneNodes2 += cloneNodesOfPackages2.get(children2);
					flag = true;
					break;
				}
			}
			if(!flag) {
				allNodes2 += getAllFilesNum(childPackage2);
			}
		}
		if((cloneNodes1 + cloneNodes2 + 0.0) / (allNodes1 + allNodes2) >= 0.5 || (childrenPackages1.size() != 0 && (childrenSimilarPackages1 + 0.0) / childrenPackages1.size() >= 0.5) || (childrenPackages2.size() != 0 && (childrenSimilarPackages2 + 0.0) / childrenPackages2.size() >= 0.5)) {
			allNodesOfPackage.put(pck1.getId(), allNodes1);
			allNodesOfPackage.put(pck2.getId(), allNodes2);
			cloneNodesOfPackages.put(similarPackage1, cloneNodes1);
			cloneNodesOfPackages.put(similarPackage2, cloneNodes2);
			similarPackages.add(similarPackage1);
			similarPackages.add(similarPackage2);
			return true;
		}
//		if((cloneNodes1 + cloneNodes2 + 0.0) / (allNodes1 + allNodes2) >= 0.5 || (childrenPackages1.size() + childrenPackages2.size() != 0 && (childrenSimilarPackages1 + childrenSimilarPackages2 + 0.0) / (childrenPackages1.size() + childrenPackages2.size()) >= 0.5)) {
//			allNodesOfPackage.put(pck1.getId(), allNodes1);
//			allNodesOfPackage.put(pck2.getId(), allNodes2);
//			cloneNodesOfPackages.put(similarPackage1, cloneNodes1);
//			cloneNodesOfPackages.put(similarPackage2, cloneNodes2);
//			similarPackages.add(similarPackage1);
//			similarPackages.add(similarPackage2);
//			return true;
//		}
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
		return false;
	}

	//加载根目录的子目录
	public void AddChildrenPackages(SimilarPackage rootSimilarPackage, Collection<Clone> fileClones, Map<String, SimilarPackage> idToPackageClone) {
		Package package1 = rootSimilarPackage.getPackage1();
		Package package2 = rootSimilarPackage.getPackage2();
		Collection<Package> childrenPackages1 = hasRelationService.findPackageHasPackages(package1);
		Collection<Package> childrenPackages2 = hasRelationService.findPackageHasPackages(package2);
		for(Package childPackage1 : childrenPackages1) {
			boolean flag = false;
			for(Package childPackage2 : childrenPackages2) {
				Collection<Long> children1 = new ArrayList<>();
				children1.add(childPackage1.getId());
				children1.add(childPackage2.getId());
				if(similarPackages.contains(children1)) {
					SimilarPackage childSimilarPackage1;
					CloneValueForDoubleNodes<Package> childPackageClone = cloneValueService.queryPackageCloneFromFileCloneSort(fileClones, childPackage1, childPackage2);
					if(childPackageClone != null) {
						childSimilarPackage1 = idToPackageClone.getOrDefault(childPackageClone.getId(), new SimilarPackage(childPackageClone));
					}
					else {
						String childrenId1 = String.join("_", childPackage1.getDirectoryPath(), childPackage2.getDirectoryPath());
						childSimilarPackage1 = new SimilarPackage(new CloneValueForDoubleNodes<Package>(childPackage1, childPackage2, childrenId1));
					}
					if(!rootSimilarPackage.isContainSimilarChild(childSimilarPackage1)) {
						AddChildrenPackages(childSimilarPackage1, fileClones, idToPackageClone);
						Collection<Long> packages1 = new ArrayList<>();
						Collection<Long> packages2 = new ArrayList<>();
						packages1.add(childSimilarPackage1.getPackage1().getId());
						packages1.add(childSimilarPackage1.getPackage2().getId());
						packages2.add(childSimilarPackage1.getPackage2().getId());
						packages2.add(childSimilarPackage1.getPackage1().getId());
						int allNodes1 = allNodesOfPackage.get(childSimilarPackage1.getPackage1().getId());
						int allNodes2 = allNodesOfPackage.get(childSimilarPackage1.getPackage2().getId());
						int cloneNodes1 = cloneNodesOfPackages.get(packages1);
						int cloneNodes2 = cloneNodesOfPackages.get(packages2);
						childSimilarPackage1.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
						rootSimilarPackage.addSimilarChild(childSimilarPackage1);
					}
					flag = true;
					break;
				}
				else if(clonePackages.contains(children1)) {
					CloneValueForDoubleNodes<Package> childPackageClone1 = cloneValueService.queryPackageCloneFromFileCloneSort(fileClones, childPackage1, childPackage2);
					SimilarPackage childSimilarPackage1 = new SimilarPackage(childPackageClone1);
					if(!rootSimilarPackage.isContainSimilarChild(childSimilarPackage1)) {
						AddChildrenPackages(childSimilarPackage1, fileClones, idToPackageClone);
						Collection<Long> packages1 = new ArrayList<>();
						Collection<Long> packages2 = new ArrayList<>();
						packages1.add(childSimilarPackage1.getPackage1().getId());
						packages1.add(childSimilarPackage1.getPackage2().getId());
						packages2.add(childSimilarPackage1.getPackage2().getId());
						packages2.add(childSimilarPackage1.getPackage1().getId());
						int allNodes1 = allNodesOfPackage.get(childSimilarPackage1.getPackage1().getId());
						int allNodes2 = allNodesOfPackage.get(childSimilarPackage1.getPackage2().getId());
						int cloneNodes1 = cloneNodesOfPackages2.get(packages1);
						int cloneNodes2 = cloneNodesOfPackages2.get(packages2);
						childSimilarPackage1.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
						rootSimilarPackage.addSimilarChild(childSimilarPackage1);
					}
					flag = true;
					break;
				}
			}
			if(!flag) {
				if(!rootSimilarPackage.isContainOtherChild1(childPackage1)) {
					childPackage1.setAllNodes(getAllFilesNum(childPackage1));
					rootSimilarPackage.addOtherChild1(childPackage1);
				}
			}
		}
		for(Package childPackage2 : childrenPackages2) {
			boolean flag = false;
			for(Package childPackage1 : childrenPackages1) {
				Collection<Long> children2 = new ArrayList<>();
				children2.add(childPackage2.getId());
				children2.add(childPackage1.getId());
				if(similarPackages.contains(children2)) {
					flag = true;
					break;
				}
				else if(clonePackages.contains(children2)) {
					flag = true;
					break;
				}
			}
			if(!flag) {
				if(!rootSimilarPackage.isContainOtherChild2(childPackage2)) {
					childPackage2.setAllNodes(getAllFilesNum(childPackage2));
					rootSimilarPackage.addOtherChild2(childPackage2);
				}
			}
		}
	}

	@Override
	public Collection<SimilarPackage> detectSimilarPackages(int threshold, double percentage) {
		Map<String, SimilarPackage> idToPackageClone = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Collection<Clone> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		Collection<CloneValueForDoubleNodes<Package>> packageClones = cloneValueService.queryPackageCloneFromFileCloneSort(fileClones);
		CloneValueCalculator<Boolean> calculator = DefaultPackageCloneValueCalculator.getInstance();
		//预处理
		for(CloneValueForDoubleNodes<Package> packageClone : packageClones) {
			Package pck1 = packageClone.getNode1();
			Package pck2 = packageClone.getNode2();
			Collection<Long> clonePackages1 = new ArrayList<>();
			Collection<Long> clonePackages2 = new ArrayList<>();
			clonePackages1.add(pck1.getId());
			clonePackages1.add(pck2.getId());
			clonePackages2.add(pck2.getId());
			clonePackages2.add(pck1.getId());
			if(clonePackages.contains(clonePackages1) || clonePackages.contains(clonePackages2)) {
				continue;
			}
			allNodesOfPackage.put(pck1.getId(), packageClone.getAllNodesInNode1().size());
			allNodesOfPackage.put(pck2.getId(), packageClone.getAllNodesInNode2().size());
			cloneNodesOfPackages2.put(clonePackages1, packageClone.getNodesInNode1().size());
			cloneNodesOfPackages2.put(clonePackages2, packageClone.getNodesInNode2().size());
			clonePackages.add(clonePackages1);
			clonePackages.add(clonePackages2);
		}
		//预处理
		for(CloneValueForDoubleNodes<Package> packageClone : packageClones) {
			if(idToPackageClone.get(packageClone.getId()) != null) {
				continue;
			}
			if(!isSimilarPackages(packageClone.getNode1(), packageClone.getNode2())) {
				continue;
			}
			SimilarPackage temp = new SimilarPackage(packageClone);
			idToPackageClone.put(temp.getId(), temp);
		}
		idToPackageClone.clear();
		for(CloneValueForDoubleNodes<Package> packageClone : packageClones) {
			if(idToPackageClone.get(packageClone.getId()) != null) {
				continue;
			}
			if(!isSimilarPackages(packageClone.getNode1(), packageClone.getNode2())) {
				continue;
			}
			SimilarPackage temp = new SimilarPackage(packageClone);
			idToPackageClone.put(temp.getId(), temp);
			isChild.put(temp.getId(), false);
			String id = temp.getId();
			Package currentPackage1 = temp.getPackage1();
			Package currentPackage2 = temp.getPackage2();
			Package parentPackage1 = findParentPackage(currentPackage1);
			Package parentPackage2 = findParentPackage(currentPackage2);
			while(parentPackage1 != null && parentPackage2 != null && !parentPackage1.getId().equals(parentPackage2.getId())) {
//				CloneValueForDoubleNodes<Package> parentPackageClone = cloneValueService.queryPackageCloneFromFileCloneSort(fileClones, parentPackage1, parentPackage2);
//				if(parentPackageClone == null) {
//					break;
//				}
//				SimilarPackage parentSimilarPackage = idToPackageClone.getOrDefault(parentPackageClone.getId(), new SimilarPackage(parentPackageClone));
				String parentId = String.join("_", parentPackage1.getDirectoryPath(), parentPackage2.getDirectoryPath());
				SimilarPackage parentSimilarPackage = new SimilarPackage(new CloneValueForDoubleNodes<Package>(parentPackage1, parentPackage2, parentId));
				if(!isSimilarPackages(parentPackage1, parentPackage2)) {
					break;
				}
				idToPackageClone.put(parentSimilarPackage.getId(), parentSimilarPackage);
				isChild.put(id, true);
				isChild.put(parentSimilarPackage.getId(), false);
				Collection<Long> similarParentPackage1 = new ArrayList<>();
				Collection<Long> similarParentPackage2 = new ArrayList<>();
				similarParentPackage1.add(parentPackage1.getId());
				similarParentPackage1.add(parentPackage2.getId());
				similarParentPackage2.add(parentPackage2.getId());
				similarParentPackage2.add(parentPackage1.getId());
				parentSimilarPackage.setData(allNodesOfPackage.get(parentPackage1.getId()), allNodesOfPackage.get(parentPackage2.getId()), cloneNodesOfPackages.get(similarParentPackage1), cloneNodesOfPackages.get(similarParentPackage2));
				id = parentSimilarPackage.getId();
				try{
					parentPackage1 = findParentPackage(parentPackage1);
					parentPackage2 = findParentPackage(parentPackage2);
				}
				catch (Exception e) {
					parentPackage1 = null;
					parentPackage2 = null;
				}
			}
		}

		//确定根目录
		List<SimilarPackage> result = new ArrayList<>();
		for(Map.Entry<String, SimilarPackage> entry : idToPackageClone.entrySet()) {
			String rootId = entry.getKey();
			if(!isChild.get(rootId)) {
				SimilarPackage rootSimilarPackage = entry.getValue();
				if(!result.contains(rootSimilarPackage)) {
					Collection<Long> packages1 = new ArrayList<>();
					Collection<Long> packages2 = new ArrayList<>();
					packages1.add(rootSimilarPackage.getPackage1().getId());
					packages1.add(rootSimilarPackage.getPackage2().getId());
					packages2.add(rootSimilarPackage.getPackage2().getId());
					packages2.add(rootSimilarPackage.getPackage1().getId());
					int allNodes1 = allNodesOfPackage.get(rootSimilarPackage.getPackage1().getId());
					int allNodes2 = allNodesOfPackage.get(rootSimilarPackage.getPackage2().getId());
					int cloneNodes1 = cloneNodesOfPackages.get(packages1);
					int cloneNodes2 = cloneNodesOfPackages.get(packages2);
					rootSimilarPackage.setData(allNodes1, allNodes2, cloneNodes1, cloneNodes2);
					result.add(rootSimilarPackage);
				}
			}
		}

		//加载根目录的子目录
		for(SimilarPackage rootSimilarPackage : result) {
			AddChildrenPackages(rootSimilarPackage, fileClones, idToPackageClone);
		}

		//聚合排序
		result.sort((d1, d2) -> {
			int allNodes1 = d1.getAllNodes1() + d1.getAllNodes2();
			int cloneNodes1 = d1.getCloneNodes1() + d1.getCloneNodes2();
			double percentageThreshold1 = (cloneNodes1 + 0.0) / allNodes1;
			int allNodes2 = d2.getAllNodes1() + d2.getAllNodes2();
			int cloneNodes2 = d2.getCloneNodes1() + d2.getCloneNodes2();
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

	public void exportSimilarPackages(OutputStream stream) {
		rowKey.set(0);
		Workbook hwb = new XSSFWorkbook();
		Collection<SimilarPackage> similarPackages = detectSimilarPackages(10,0.5);
		Sheet sheet = hwb.createSheet(new StringBuilder().append("SimilarPackages").toString());
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

		for(SimilarPackage similarPackage:similarPackages){
			printSimilarPackage(sheet, 0, similarPackage);
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

	private void printSimilarPackage(Sheet sheet, int layer, SimilarPackage similarPackage){
		String prefix = "";
		for(int i = 0; i < layer; i++) {
			prefix += "|--------";
		}
		Row row = sheet.createRow(rowKey.get());
		rowKey.set(rowKey.get()+1);
		row.createCell(0).setCellValue(prefix + similarPackage.getPackage1().getDirectoryPath());
		row.createCell(1).setCellValue(prefix + similarPackage.getPackage2().getDirectoryPath());
		row.createCell(2).setCellValue(similarPackage.getClonePackages().sizeOfChildren());

		for (SimilarPackage child:similarPackage.getChildrenSimilarPackages()){
			printSimilarPackage(sheet, layer + 1, child);
		}
	}
}
