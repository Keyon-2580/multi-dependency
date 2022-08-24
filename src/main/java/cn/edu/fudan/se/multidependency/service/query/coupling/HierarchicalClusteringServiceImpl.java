package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.hierarchical_clustering.HierarchicalCluster;
import cn.edu.fudan.se.multidependency.model.relation.Coupling;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.hierarchical_clustering.HierarchicalClusterRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.coupling.CouplingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class HierarchicalClusteringServiceImpl implements HierarchicalClusteringService{

    @Autowired
    PackageRepository packageRepository;

    @Autowired
    ContainRepository containRepository;

    @Autowired
    HierarchicalClusterRepository hierarchicalClusterRepository;

    @Override
    public void calPackageComplexityByCluster(long packageId) {
        List<ProjectFile> allFiles = containRepository.findPackageContainAllFiles(packageId);
        Map<HierarchicalCluster, List<ProjectFile>> clusterMap = new HashMap<>();
        List<HierarchicalCluster> clusterList = new ArrayList<>();

        for(ProjectFile projectFile: allFiles){
            HierarchicalCluster zeroLevelCluster = hierarchicalClusterRepository.findClusterContainFiles(projectFile.getId());
            if(zeroLevelCluster != null){
                if(!clusterList.contains(zeroLevelCluster)) clusterList.add(zeroLevelCluster);

                if(!clusterMap.containsKey(zeroLevelCluster)){
                    List<ProjectFile> tmpList = new ArrayList<>();
                    tmpList.add(projectFile);
                    clusterMap.put(zeroLevelCluster, tmpList);
                }else{
                    clusterMap.get(zeroLevelCluster).add(projectFile);
                }
            }
        }

        HierarchicalCluster maxCluster = clusterList.get(0);
        for(HierarchicalCluster hierarchicalCluster: clusterList){
            if(clusterMap.get(hierarchicalCluster).size() > clusterMap.get(maxCluster).size()){
                maxCluster = hierarchicalCluster;
            }
        }

        for(HierarchicalCluster hierarchicalCluster: clusterMap.keySet()){
            if(!hierarchicalCluster.equals(maxCluster)){
                System.out.println(
                        hierarchicalClusterRepository.calDistanceBetweenClusters(hierarchicalCluster.getId(), maxCluster.getId()));
            }
        }
    }
}
