package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
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
	
	private Map<String, Package> directoryPathToPacakge = new ConcurrentHashMap<>();
	
	private Package findParentPackage(Package pck) {
		directoryPathToPacakge.put(pck.getDirectoryPath(), pck);
		String parentDirectoryPath = pck.lastPackageDirectoryPath();
		if(FileUtil.SLASH_LINUX.equals(parentDirectoryPath)) {
			return null;
		}
		Package parent = directoryPathToPacakge.get(parentDirectoryPath);
		if(parent != null) {
			return parent;
		}
		parent = containRelationService.findPackageInPackage(pck);
		if(parent != null) {
			directoryPathToPacakge.put(parentDirectoryPath, parent);
		}
		return parent;
	}

	@Override
	public Collection<SimilarPackage> detectSimilarPackages(int threshold, double percentage) {
		Map<String, SimilarPackage> idToPackageClone = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Collection<Clone> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		Collection<CloneValueForDoubleNodes<Package>> packageClones = cloneValueService.queryPackageCloneFromFileCloneSort(fileClones);
//		DefaultPackageCloneValueCalculator.getInstance().setCountThreshold(threshold);
//		DefaultPackageCloneValueCalculator.getInstance().setPercentageThreshold(percentage);
		DefaultPackageCloneValueCalculator.getInstance().initThreshold();
		for(CloneValueForDoubleNodes<Package> packageClone : packageClones) {
			boolean isSimilar = (boolean) packageClone.calculateValue(DefaultPackageCloneValueCalculator.getInstance());
			if(!isSimilar) {
				continue;
			}
			if(idToPackageClone.get(packageClone.getId()) != null) {
				continue;
			}
			SimilarPackage temp = new SimilarPackage(packageClone);
			idToPackageClone.put(temp.getId(), temp);
			isChild.put(temp.getId(), false);
			String id = temp.getId();
			Package currentPackage1 = packageClone.getNode1();
			Package currentPackage2 = packageClone.getNode2();
			Package parentPackage1 = findParentPackage(currentPackage1);
			Package parentPackage2 = findParentPackage(currentPackage2);
			while(parentPackage1 != null && parentPackage2 != null) {
				CloneValueForDoubleNodes<Package> parentPackageClone = cloneValueService.queryPackageCloneFromFileCloneSort(fileClones, parentPackage1, parentPackage2);
				if(parentPackageClone == null) {
					break;
				}
				if(!(boolean) parentPackageClone.calculateValue(DefaultPackageCloneValueCalculator.getInstance())) {
					break;
				}
				SimilarPackage parentSimilarPackage = idToPackageClone.getOrDefault(parentPackageClone.getId(), new SimilarPackage(parentPackageClone));
				idToPackageClone.put(parentSimilarPackage.getId(), parentSimilarPackage);
				isChild.put(id, true);
				isChild.put(parentSimilarPackage.getId(), false);
				parentSimilarPackage.addChild(idToPackageClone.get(id));
				id = parentSimilarPackage.getId();
				parentPackage1 = containRelationService.findPackageInPackage(parentPackage1);
				parentPackage2 = containRelationService.findPackageInPackage(parentPackage2);
			}
		}
		
		Map<String, SimilarPackage> parentSimilarPacakges = new HashMap<>();
		for(Map.Entry<String, SimilarPackage> entry : idToPackageClone.entrySet()) {
			String id = entry.getKey();
			if(isChild.get(id) == false) {
				SimilarPackage value = entry.getValue();
				Package pck1 = value.getPackage1();
				Package pck2 = value.getPackage2();
				Package parentPck1 = containRelationService.findPackageInPackage(pck1);
				Package parentPck2 = containRelationService.findPackageInPackage(pck2);
				if(parentPck1 == null && parentPck2 == null) {
					String parentPck1Path = pck1.lastPackageDirectoryPath();
					String parentPck2Path = pck2.lastPackageDirectoryPath();
					String parentId = String.join("_", parentPck1Path, parentPck2Path);
					SimilarPackage parentSimilar = parentSimilarPacakges.get(parentId);
					if(parentSimilar == null) {
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
						parentSimilar = new SimilarPackage(new CloneValueForDoubleNodes<Package>(parentPck1, parentPck2, parentId));
						parentSimilarPacakges.put(parentSimilar.getId(), parentSimilar);
					}
					parentSimilar.addChild(value);
					isChild.put(id, true);
				}
			}
		}
		
		List<SimilarPackage> result = new ArrayList<>();
		
		for(SimilarPackage parentSimilarPackage : parentSimilarPacakges.values()) {
			result.add(parentSimilarPackage);
		}
		
		for(Map.Entry<String, SimilarPackage> entry : idToPackageClone.entrySet()) {
			String id = entry.getKey();
			if(isChild.get(id) == false) {
				result.add(entry.getValue());
			}
		}
		result.sort((d1, d2) -> {
			return d1.getPackage1().getDirectoryPath().compareTo(d2.getPackage1().getDirectoryPath());
		});
		return result;
	}

}
