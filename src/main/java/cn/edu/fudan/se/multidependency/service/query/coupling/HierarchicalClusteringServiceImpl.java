package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Coupling;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.coupling.CouplingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用层次聚类算法，将涉及到Coupling关系的文件进行聚类
 */
@Service
public class HierarchicalClusteringServiceImpl implements HierarchicalClusteringService{
    public static int clusterNum = 5;

    @Autowired
    CouplingRepository couplingRepository;

    @Autowired
    ProjectFileRepository projectFileRepository;

    @Override
    public void calFileHCLevel(){
        List<Coupling> allCouplings = couplingRepository.queryAllCouplingsOrderByDist();
        List<ProjectFile> allFilesRelated = projectFileRepository.queryAllFilesRelatedByCouplings();
        List<List<ProjectFile>> clusters = new ArrayList<>();
        int allCouplingsNum = allCouplings.size();

        //初始化cluster，将每个文件都放入一个cluster中
        for(ProjectFile projectFile: allFilesRelated){
            List<ProjectFile> tmpList = new ArrayList<>();
            tmpList.add(projectFile);
            clusters.add(tmpList);
        }

        for(int i = 0; i < clusterNum; i++){
            int start = i * (allCouplingsNum / clusterNum);
            int end = (i + 1) * (allCouplingsNum / clusterNum) - 1;

            while(start <= end){
                Coupling cp = allCouplings.get(start);
                ProjectFile startNode = (ProjectFile) cp.getStartNode();
                ProjectFile endNode = (ProjectFile) cp.getEndNode();
                int startNodeClusterIndex = -1;
                int endNodeClusterIndex = -1;

                for(int j = 0; j < clusters.size(); j++){
                    if(clusters.get(j).contains(startNode)){
                        startNodeClusterIndex = j;
                    }

                    if(clusters.get(j).contains(endNode)){
                        endNodeClusterIndex = j;
                    }

                    if(startNodeClusterIndex >= 0 && endNodeClusterIndex >=0) break;
                }

                int minIndex = Math.min(startNodeClusterIndex, endNodeClusterIndex);
                int maxIndex = Math.max(startNodeClusterIndex, endNodeClusterIndex);

                if(minIndex != maxIndex){
                    clusters.get(minIndex).addAll(clusters.get(maxIndex));
                    clusters.remove(maxIndex);
                }
                start++;
            }

            System.out.println(clusters.size());
        }

//        for(List<ProjectFile> list : clusters){
//            for(ProjectFile projectFile: list){
//                System.out.println(projectFile.getPath());
//            }
//            System.out.println("");
//        }

    }
}
