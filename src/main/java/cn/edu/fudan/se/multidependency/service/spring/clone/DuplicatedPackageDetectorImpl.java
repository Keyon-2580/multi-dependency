package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@Service
public class DuplicatedPackageDetectorImpl implements DuplicatedPackageDetector {
	
	@Autowired
	private CloneValueService cloneValueService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private ContainRelationService containRelationService;

	@Override
	public Collection<DuplicatedPackage> detectDuplicatedPackages(int threshold) {
		Map<String, DuplicatedPackage> idToPackageClone = new HashMap<>();
		Map<String, Boolean> isChild = new HashMap<>();
		Collection<Clone> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		Collection<CloneValueForDoubleNodes<Package>> packageClones = cloneValueService.queryPackageCloneFromFileCloneSort(fileClones);
		PackageCloneValueCalculator.getInstance().setThreshold(threshold);
		PackageCloneValueCalculator.getInstance().setContainRelationService(containRelationService);
		for(CloneValueForDoubleNodes<Package> packageClone : packageClones) {
			boolean isDuplicated = (boolean) packageClone.calculateValue(PackageCloneValueCalculator.getInstance());
			if(isDuplicated) {
				continue;
			}
			if(idToPackageClone.get(packageClone.getId()) != null) {
				continue;
			}
			DuplicatedPackage temp = new DuplicatedPackage(packageClone);
			idToPackageClone.put(temp.getClonePackages().getId(), temp);
			if(isChild.get(temp.getId()) == null) {
				isChild.put(temp.getId(), false);
			}
			
			Package currentPackage1 = packageClone.getNode1();
			Package currentPackage2 = packageClone.getNode2();
			Package parentPackage1 = containRelationService.findPackageInPackage(currentPackage1);
			Package parentPackage2 = containRelationService.findPackageInPackage(currentPackage2);
			String id = temp.getId();
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

		List<DuplicatedPackage> result = new ArrayList<>();
		for(Map.Entry<String, DuplicatedPackage> entry : idToPackageClone.entrySet()) {
			String id = entry.getKey();
			if(isChild.get(id) == false) {
				result.add(entry.getValue());
			}
		}
		
		return result;
	}

}
