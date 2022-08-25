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
import org.bouncycastle.util.Pack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class HierarchicalClusteringServiceImpl implements HierarchicalClusteringService{

    @Autowired
    PackageRepository packageRepository;

    @Autowired
    ContainRepository containRepository;

    @Autowired
    CouplingRepository couplingRepository;

    @Autowired
    HierarchicalClusterRepository hierarchicalClusterRepository;

    @Override
    public double calPackageComplexityByCluster(long packageId) {
        Package pck = packageRepository.findPackageById(packageId);
        int allFilesNum = containRepository.findPackageContainAllFilesNum(packageId);
        List<Coupling> inCouplings = couplingRepository.queryAllCouplingsWithinPackage(packageId);
        int inPackageCouplingFileNum = couplingRepository.queryAllCouplingsWithinPackageFilesNum(packageId);
        int outPackageCouplingFileNum = couplingRepository.queryAllCouplingsOutOfPackageFilesNum(packageId);
        double clusterDistAvg = 0.0;
        double looseDegree;

        for(Coupling coupling: inCouplings){
            clusterDistAvg += coupling.getClusterDist();
        }

        clusterDistAvg = clusterDistAvg / inCouplings.size();
        if(inPackageCouplingFileNum == 0){
            looseDegree = -1;
        }else{
            looseDegree = (allFilesNum / (double) inPackageCouplingFileNum + (double) outPackageCouplingFileNum / allFilesNum)
                    * Math.log(clusterDistAvg);
        }
        packageRepository.setPackageLooseDegree(pck.getId(), looseDegree);
//        System.out.print(pck.getDirectoryPath() + "  ");
//        System.out.println(looseDegree);
        return looseDegree;
    }

}
