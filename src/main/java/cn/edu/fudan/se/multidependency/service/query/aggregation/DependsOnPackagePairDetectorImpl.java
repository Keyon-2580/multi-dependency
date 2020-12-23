package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DependsOnPackagePairDetectorImpl implements DependsOnPackagePairDetector{

    @Autowired
    private DependsOnRepository dependsOnRepository;

    @Autowired
    private HasRelationService hasRelationService;

    @Override
    public Map<Package, Map<Package, DependsOn>> detectDependsOnPackagePairs() {
        Map<Package, Map<Package, DependsOn>> packageDependsOnMap = new HashMap<>();
        List<DependsOn> fileDependsOns = dependsOnRepository.findFileDepends();
        for(DependsOn fileDependsOn : fileDependsOns){
            if(fileDependsOn.getDependsOnTypes() != null && !fileDependsOn.getDependsOnTypes().isEmpty()) {
                Package currentPackage1 = dependsOnRepository.findFileBelongPackageByFileId(fileDependsOn.getStartNode().getId());
                Package currentPackage2 = dependsOnRepository.findFileBelongPackageByFileId(fileDependsOn.getEndNode().getId());
                DependsOn currentDependsOn = fileDependsOn;
                boolean isAggregatePackagePair = false;
                while(currentPackage1 != null && currentPackage2 != null && !currentPackage1.getId().equals(currentPackage2.getId())){
                    Map<Package, DependsOn> currentPackage1DependsOnMap = packageDependsOnMap.getOrDefault(currentPackage1, new HashMap<>());
                    DependsOn dependsOnCurrentPackage2 = currentPackage1DependsOnMap.get(currentPackage2);
                    if(dependsOnCurrentPackage2 != null ){
                        int currentDependsOnPairTimes = dependsOnCurrentPackage2.getTimes();
                        for(Map.Entry<String, Long> entry : currentDependsOn.getDependsOnTypes().entrySet()) {
                            String dependsOnKey = entry.getKey();
                            Long typeTimes = entry.getValue();
                            if(dependsOnCurrentPackage2.getDependsOnTypes().containsKey(dependsOnKey)) {
                                Long currentDependsOnPairTypeTimes = dependsOnCurrentPackage2.getDependsOnTypes().get(dependsOnKey);
                                currentDependsOnPairTypeTimes += typeTimes;
                                dependsOnCurrentPackage2.getDependsOnTypes().put(dependsOnKey, currentDependsOnPairTypeTimes);
                            }
                            else {
                                dependsOnCurrentPackage2.getDependsOnTypes().put(dependsOnKey, typeTimes);
                                String dependsOnType = dependsOnCurrentPackage2.getDependsOnType();
                                dependsOnCurrentPackage2.setDependsOnType(dependsOnType + "__" + dependsOnKey);
                            }
                            currentDependsOnPairTimes += typeTimes.intValue();
                        }
                        dependsOnCurrentPackage2.setTimes(currentDependsOnPairTimes);
                    }
                    else {
                        dependsOnCurrentPackage2 = new DependsOn(currentPackage1, currentPackage2);
                        dependsOnCurrentPackage2.getDependsOnTypes().putAll(currentDependsOn.getDependsOnTypes());
                        dependsOnCurrentPackage2.setDependsOnType(currentDependsOn.getDependsOnType());
                        dependsOnCurrentPackage2.setTimes(currentDependsOn.getTimes());
                    }
                    dependsOnCurrentPackage2.setAggregatePackagePair(isAggregatePackagePair);
                    currentPackage1DependsOnMap.put(currentPackage2, dependsOnCurrentPackage2);
                    packageDependsOnMap.put(currentPackage1, currentPackage1DependsOnMap);
                    currentPackage1 = hasRelationService.findPackageInPackage(currentPackage1);
                    currentPackage2 = hasRelationService.findPackageInPackage(currentPackage2);
                    currentDependsOn = dependsOnCurrentPackage2;
                    isAggregatePackagePair = true;
                }
            }
        }
        return packageDependsOnMap;
    }
}
