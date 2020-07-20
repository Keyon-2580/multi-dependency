package cn.edu.fudan.se.multidependency.service.spring.clone;

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
import cn.edu.fudan.se.multidependency.service.spring.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.clone.data.CloneValueForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.spring.clone.data.DuplicatedPackage;
import cn.edu.fudan.se.multidependency.service.spring.clone.data.PackageCloneValueCalculator;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

@Service
public class DuplicatedPackageDetectorImpl implements DuplicatedPackageDetector {
	
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
	public Collection<DuplicatedPackage> detectDuplicatedPackages(int threshold, double percentage) {
		Map<String, DuplicatedPackage> idToPackageClone = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Collection<Clone> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		Collection<CloneValueForDoubleNodes<Package>> packageClones = cloneValueService.queryPackageCloneFromFileCloneSort(fileClones);
		PackageCloneValueCalculator.getInstance().setCountThreshold(threshold);
		PackageCloneValueCalculator.getInstance().setPercentageThreshold(percentage);
		for(CloneValueForDoubleNodes<Package> packageClone : packageClones) {
			boolean isDuplicated = (boolean) packageClone.calculateValue(PackageCloneValueCalculator.getInstance());
			if(!isDuplicated) {
				continue;
			}
			if(idToPackageClone.get(packageClone.getId()) != null) {
				continue;
			}
			DuplicatedPackage temp = new DuplicatedPackage(packageClone);
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
				if(!(boolean) parentPackageClone.calculateValue(PackageCloneValueCalculator.getInstance())) {
					break;
				}
				DuplicatedPackage parentDuplicatedPackage = idToPackageClone.getOrDefault(parentPackageClone.getId(), new DuplicatedPackage(parentPackageClone));
				idToPackageClone.put(parentDuplicatedPackage.getId(), parentDuplicatedPackage);
				isChild.put(id, true);
				isChild.put(parentDuplicatedPackage.getId(), false);
				parentDuplicatedPackage.addChild(idToPackageClone.get(id));
				id = parentDuplicatedPackage.getId();
				parentPackage1 = containRelationService.findPackageInPackage(parentPackage1);
				parentPackage2 = containRelationService.findPackageInPackage(parentPackage2);
			}
		}
		
		Map<String, DuplicatedPackage> parentDuplicatedPacakges = new HashMap<>();
		for(Map.Entry<String, DuplicatedPackage> entry : idToPackageClone.entrySet()) {
			String id = entry.getKey();
			if(isChild.get(id) == false) {
				DuplicatedPackage value = entry.getValue();
				Package pck1 = value.getPackage1();
				Package pck2 = value.getPackage2();
				Package parentPck1 = containRelationService.findPackageInPackage(pck1);
				Package parentPck2 = containRelationService.findPackageInPackage(pck2);
				if(parentPck1 == null && parentPck2 == null) {
					String parentPck1Path = pck1.lastPackageDirectoryPath();
					String parentPck2Path = pck2.lastPackageDirectoryPath();
					String parentId = String.join("_", parentPck1Path, parentPck2Path);
					DuplicatedPackage parentDuplicated = parentDuplicatedPacakges.get(parentId);
					if(parentDuplicated == null) {
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
						parentDuplicated = new DuplicatedPackage(new CloneValueForDoubleNodes<Package>(parentPck1, parentPck2, parentId));
						parentDuplicatedPacakges.put(parentDuplicated.getId(), parentDuplicated);
					}
					parentDuplicated.addChild(value);
					isChild.put(id, true);
				}
			}
		}
		
		List<DuplicatedPackage> result = new ArrayList<>();
		
		for(DuplicatedPackage parentDuplicatedPackage : parentDuplicatedPacakges.values()) {
			result.add(parentDuplicatedPackage);
		}
		
		for(Map.Entry<String, DuplicatedPackage> entry : idToPackageClone.entrySet()) {
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
