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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

    @Override
    public JSONObject getPackageClusteringOverview(long packageId){
        JSONObject result = new JSONObject();
        Set<ProjectFile> addedFiles = new HashSet<>();
        Set<Package> addedPcks = new HashSet<>();
        Set<Coupling> addedCouplings = new HashSet<>();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        JSONArray combos = new JSONArray();

        Package parentPck = packageRepository.findPackageById(packageId);
        List<Coupling> inCouplings = couplingRepository.queryAllCouplingsWithinPackage(packageId);
        List<Coupling> outCouplings = couplingRepository.queryAllCouplingsOutOfPackage(packageId);

        JSONObject parentPckJSON = new JSONObject();
        parentPckJSON.put("id", parentPck.getId().toString());
        parentPckJSON.put("label", parentPck.getName());
        parentPckJSON.put("path", parentPck.getDirectoryPath());
        addedPcks.add(parentPck);
        combos.add(parentPckJSON);

        for(Coupling coupling: outCouplings){
            ProjectFile start = (ProjectFile) coupling.getStartNode();
            ProjectFile end = (ProjectFile) coupling.getEndNode();
            Package startParentPck = packageRepository.findParentPackageByFileId(start.getId());
            Package endParentPck = packageRepository.findParentPackageByFileId(end.getId());
            boolean isStartInner = packageRepository.findIfPackageContainFile(parentPck.getId(), start.getId());

            if(!addedPcks.contains(startParentPck)){
                JSONObject tmpPck = new JSONObject();
                tmpPck.put("id", startParentPck.getId().toString());
                tmpPck.put("label", startParentPck.getName());
                tmpPck.put("path", startParentPck.getDirectoryPath());
                combos.add(tmpPck);
                addedPcks.add(startParentPck);
            }

            if(!addedPcks.contains(endParentPck)){
                JSONObject tmpPck = new JSONObject();
                tmpPck.put("id", endParentPck.getId().toString());
                tmpPck.put("label", endParentPck.getName());
                tmpPck.put("path", endParentPck.getDirectoryPath());
                combos.add(tmpPck);
                addedPcks.add(endParentPck);
            }

            if(!addedFiles.contains(start)){
                JSONObject tmp = putNodeData(start, startParentPck.getId());
                tmp.put("isOuter", isStartInner);
                nodes.add(tmp);
                addedFiles.add(start);
            }

            if(!addedFiles.contains(end)){
                JSONObject tmp = putNodeData(end, endParentPck.getId());
                tmp.put("isOuter", !isStartInner);
                nodes.add(tmp);
                addedFiles.add(end);
            }

            if(!addedCouplings.contains(coupling)){
                JSONObject tmpEdge = new JSONObject();
                tmpEdge.put("id", coupling.getId().toString());
                tmpEdge.put("source", start.getId().toString());
                tmpEdge.put("target", end.getId().toString());
                tmpEdge.put("label", coupling.getClusterDist());
                tmpEdge.put("clusterDistance", coupling.getClusterDist());
                edges.add(tmpEdge);
                addedCouplings.add(coupling);
            }
        }

        for(Coupling coupling: inCouplings){
            ProjectFile start = (ProjectFile) coupling.getStartNode();
            ProjectFile end = (ProjectFile) coupling.getEndNode();

            if(!addedFiles.contains(start)){
                nodes.add(putNodeData(start, parentPck.getId()));
                addedFiles.add(start);
            }

            if(!addedFiles.contains(end)){
                nodes.add(putNodeData(end, parentPck.getId()));
                addedFiles.add(end);
            }

            if(!addedCouplings.contains(coupling)){
                JSONObject tmpEdge = new JSONObject();
                tmpEdge.put("id", coupling.getId().toString());
                tmpEdge.put("source", start.getId().toString());
                tmpEdge.put("target", end.getId().toString());
                tmpEdge.put("label", coupling.getClusterDist());
                tmpEdge.put("clusterDistance", coupling.getClusterDist());
                edges.add(tmpEdge);
                addedCouplings.add(coupling);
            }

        }

        result.put("nodes", nodes);
        result.put("edges", edges);
        result.put("combos", combos);
        return result;
    }

    private JSONObject putNodeData(ProjectFile projectFile, Long comboId){
        JSONObject tmp = new JSONObject();
        tmp.put("id", projectFile.getId().toString());
//        tmp.put("label", projectFile.getName());
        tmp.put("path", projectFile.getPath());
        tmp.put("LOC", projectFile.getLoc());
        tmp.put("comboId", comboId.toString());
        return tmp;
    }
}
