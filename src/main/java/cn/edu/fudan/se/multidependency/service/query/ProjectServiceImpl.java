package cn.edu.fudan.se.multidependency.service.query;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import cn.edu.fudan.se.multidependency.service.query.data.ProjectStructure;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectServiceImpl implements ProjectService{
    @Autowired
    private BasicCloneQueryService basicCloneQueryService;

    @Autowired
    private HasRelationService hasRelationService;

    @Autowired
    private HotspotPackageDetector hotspotPackageDetector;

    @Autowired
    private HotspotPackagePairDetector hotspotPackagePairDetector;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private NodeService nodeService;

    private Map<Project, String> projectToAbsolutePath = new ConcurrentHashMap<>();

    @Override
    public String getAbsolutePath(Project project) {
        if(project == null) {
            return "";
        }
        return projectToAbsolutePath.getOrDefault(project, "");
    }

    @Override
    public void setAbsolutePath(Project project, String path) {
        this.projectToAbsolutePath.put(project, path);
    }

    @Override
    public JSONArray getMultipleProjectsGraphJson(JSONObject dataList) {
        String showType = dataList.getString("showType");
        JSONArray projectIds = dataList.getJSONArray("projectIds");
        JSONArray result = new JSONArray();

        JSONObject nodeJSON2 = new JSONObject();
        JSONObject nodeJSON4 = new JSONObject();

        JSONObject projectJson = new JSONObject();
        if(projectIds.size() == 1){
            projectJson = joinMultipleProjectsGraphJson(projectIds.getJSONObject(0).getLong("id"),showType);
        }else{
            projectJson.put("name", "default");
            projectJson.put("id", "id_default");
            JSONArray multipleProjectsJson = new JSONArray();
            for(int i = 0; i < projectIds.size(); i++){
                multipleProjectsJson.add(joinMultipleProjectsGraphJson(projectIds.getJSONObject(i).getLong("id"),showType));
            }
            projectJson.put("children",multipleProjectsJson);
        }

        nodeJSON2.put("result",projectJson);

        result.add(nodeJSON2);

        if(showType.equals("graph")) {
            JSONObject temp_allprojects = getAllProjectsCloneLinks();
            JSONObject temp = new JSONObject();
            JSONObject links = new JSONObject();
            links.put("clone_links",temp_allprojects.getJSONObject("children_graphlinks").getJSONArray("clone_links"));
            links.put("dependson_links",temp_allprojects.getJSONObject("children_graphlinks").getJSONArray("dependson_links"));
            links.put("cochange_links",temp_allprojects.getJSONObject("children_graphlinks").getJSONArray("cochange_links"));
            nodeJSON4.put("links", links);
            temp.put("table",temp_allprojects.getJSONObject("table"));
            result.add(nodeJSON4);
            result.add(temp);
        }

        return result;
    }

    private JSONObject joinMultipleProjectsGraphJson(long projectId, String showType){
        JSONObject result = new JSONObject();
        Project project = nodeService.queryProject(projectId);
        ProjectStructure projectStructure = hasRelationService.projectHasInitialize(project);
        Package packageOfProject = projectStructure.getChildren().get(0).getPck();

        List<PackageStructure> childrenPackages = hasRelationService.packageHasInitialize(packageOfProject).getChildrenPackages();
        List<PackageStructure> childrenPackagesnew = new ArrayList<>();


        for(PackageStructure pckstru : childrenPackages){
            PackageStructure pcknew = hasRelationService.packageHasInitialize(pckstru.getPck());
            childrenPackagesnew.add(pcknew);
        }

        result.put("name", packageOfProject.getName());
        result.put("id", "id_" + packageOfProject.getId().toString());
        Collection<ProjectFile> clonefiles = basicCloneQueryService.findProjectContainCloneFiles(project);
        result.put("children",getHasJson(clonefiles,childrenPackagesnew, showType));

        return result;
    }

    @Override
    /**
     * 递归遍历项目中所有package的包含关系
     */
    public JSONArray getHasJson(Collection<ProjectFile> clonefiles, List<PackageStructure> childrenPackages, String showType){
        JSONArray rtJA = new JSONArray();
        for(PackageStructure pckstru :childrenPackages){
            List<PackageStructure> pckList = pckstru.getChildrenPackages();
            List<ProjectFile> fileList = pckstru.getChildrenFiles();
            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("name",pckstru.getPck().getName());
            jsonObject.put("name",pckstru.getPck().getDirectoryPath());
//			jsonObject.put("long_name",pckstru.getPck().getDirectoryPath());
            jsonObject.put("size",fileList.size());
            jsonObject.put("id","id_" + pckstru.getPck().getId().toString());
            float cloneFilesInAllFiles = 0;
            if(fileList.size() > 0){
                for(ProjectFile profile : fileList){
                    if(clonefiles.contains(profile)){
                        cloneFilesInAllFiles += 1;
                    }
                }
            }
            if(fileList.size() > 0){
                jsonObject.put("clone_ratio",cloneFilesInAllFiles / (float)(fileList.size()));
            }else{
                jsonObject.put("clone_ratio", 0);
            }
//			JSONArray jsonArray = new JSONArray();
//			if(fileList.size() > 0){
//				for(ProjectFile profile : fileList){
//					JSONObject jsonObject2 = new JSONObject();
//					jsonObject2.put("size",1000);
//					jsonObject2.put("long_name",profile.getPath());
//					if(clonefiles.contains(profile)){
//						jsonObject2.put("clone",true);
//					}else{
//						jsonObject2.put("clone",false);
//					}
//					jsonObject2.put("name",profile.getName());
//					jsonObject2.put("id","id_" + profile.getId().toString());
//					jsonArray.add(jsonObject2);
//				}
//			}

//			if(jsonArray.size() > 0){
//				if(showType.equals("graph")){
//					jsonObject.put("children",jsonArray);
//				}else{
//					jsonObject.put("collapse_children",jsonArray);
//				}
//			}

            if(pckList.size()>0){//如果该属性还有子属性,继续做查询,直到该属性没有孩子,也就是最后一个节点
                jsonObject.put("children", getHasJson(clonefiles,pckList, showType));
            }
//			System.out.println(pckList.size());
            rtJA.add(jsonObject);
        }
        return rtJA;
    }

    @Override
    public JSONObject getAllProjectsCloneLinks(){
        List<HotspotPackagePair> cloneHotspotPackageList = hotspotPackagePairDetector.detectHotspotPackagePairWithFileCloneByParentId(-1, -1, "all");
        List<HotspotPackagePair> dependsonHotspotPackageList = hotspotPackagePairDetector.detectHotspotPackagesByDependsOnInAllProjects();
        List<HotspotPackagePair> cochangeHotspotPackageList = hotspotPackagePairDetector.detectHotspotPackagesByCoChangeInAllProjects();
        cloneHotspotPackageList.addAll(dependsonHotspotPackageList);
        cloneHotspotPackageList.addAll(cochangeHotspotPackageList);
        return hotspotPackagesToCloneJson(cloneHotspotPackageList,new HotspotPackagePair(), "project");
    }

    @Override
    public JSONObject cloneGraphAndTableOfChildrenPackages(long package1Id, long package2Id) {
        HotspotPackagePair parentHotspotPackage = hotspotPackagePairDetector.detectHotspotPackagePairWithFileCloneByPackageId(package1Id, package2Id, "all");
        List<HotspotPackagePair> childrenHotspotPackages = parentHotspotPackage.getChildrenHotspotPackagePairs();
        return hotspotPackagesToCloneJson(childrenHotspotPackages, parentHotspotPackage, "clone_package");
    }


    private JSONObject hotspotPackagesToCloneJson(List<HotspotPackagePair> hotspotPackagePairList, HotspotPackagePair parentHotspotPackagePair, String type){
//    private JSONObject hotspotPackagesToCloneJson(List<HotspotPackage> hotspotPackageList, HotspotPackage parentHotspotPackage, String type){
        JSONObject result = new JSONObject();

        JSONArray clone_links = new JSONArray();
        JSONArray dependson_links = new JSONArray();
        JSONArray cochange_links = new JSONArray();
        JSONObject table = new JSONObject();
        JSONArray cloneFiles1 = new JSONArray();
        JSONArray cloneFiles2 = new JSONArray();

        for(HotspotPackagePair hotspotPackagePair: hotspotPackagePairList){
            JSONObject link = new JSONObject();

            JSONObject temp_clonefile1 = new JSONObject();
            JSONObject temp_clonefile2 = new JSONObject();

            Project source_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage1());
            Project target_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage2());

            link.put("source_id", "id_" + hotspotPackagePair.getPackage1().getId().toString());
            link.put("target_id", "id_" + hotspotPackagePair.getPackage2().getId().toString());
            link.put("source_name", hotspotPackagePair.getPackage1().getDirectoryPath());
            link.put("target_name", hotspotPackagePair.getPackage2().getDirectoryPath());
            link.put("source_projectBelong", "id_" + source_projectBelong.getId());
            link.put("target_projectBelong", "id_" + target_projectBelong.getId());

            if(hotspotPackagePair.getHotspotRelationType().toString().equals("CLONE")){
                CloneRelationDataForDoubleNodes<Node, Relation> cloneRelationDataForDoubleNodes = (CloneRelationDataForDoubleNodes<Node, Relation>)hotspotPackagePair.getPackagePairRelationData();
                link.put("type", "clone");
                link.put("packageCochangeTimes", cloneRelationDataForDoubleNodes.getAllNodesCoChangeTimes());
                link.put("packageCloneCochangeTimes", cloneRelationDataForDoubleNodes.getCloneNodesCoChangeTimes());
                link.put("clonePairs", cloneRelationDataForDoubleNodes.getClonePairs());

                if(cloneRelationDataForDoubleNodes.getClonePairs() == 0){
                    link.put("bottom_package", false);
                }else{
                    link.put("bottom_package", true);
                }
                link.put("similarityValue", cloneRelationDataForDoubleNodes.getSimilarityValue());

                temp_clonefile1.put("id", hotspotPackagePair.getPackage1().getId().toString());
                temp_clonefile2.put("id", hotspotPackagePair.getPackage2().getId().toString());
                temp_clonefile1.put("projectBelong", source_projectBelong.getId().toString());
                temp_clonefile2.put("projectBelong", target_projectBelong.getId().toString());
                temp_clonefile1.put("name", hotspotPackagePair.getPackage1().getDirectoryPath());
                temp_clonefile2.put("name", hotspotPackagePair.getPackage2().getDirectoryPath());
                temp_clonefile1.put("relationNodes1", cloneRelationDataForDoubleNodes.getCloneNodesCount1());
                temp_clonefile2.put("relationNodes2", cloneRelationDataForDoubleNodes.getCloneNodesCount2());
                temp_clonefile1.put("allNodes1", cloneRelationDataForDoubleNodes.getAllNodesCount1());
                temp_clonefile2.put("allNodes2", cloneRelationDataForDoubleNodes.getAllNodesCount2());
                temp_clonefile1.put("packageCochangeTimes", cloneRelationDataForDoubleNodes.getAllNodesCoChangeTimes());
                temp_clonefile1.put("packageCloneCochangeTimes", cloneRelationDataForDoubleNodes.getCloneNodesCoChangeTimes());
                temp_clonefile1.put("clonePairs", cloneRelationDataForDoubleNodes.getClonePairs());

                cloneFiles1.add(temp_clonefile1);
                cloneFiles2.add(temp_clonefile2);

                clone_links.add(link);
            }else if(hotspotPackagePair.getHotspotRelationType().toString().equals("DEPENDS_ON")){
                DependsRelationDataForDoubleNodes<Node, Relation> dependsRelationDataForDoubleNodes = (DependsRelationDataForDoubleNodes<Node, Relation>)hotspotPackagePair.getPackagePairRelationData();

                link.put("type", "dependson");
                link.put("dependsOnTypes", dependsRelationDataForDoubleNodes.getDependsOnTimes());
                link.put("dependsByTypes", dependsRelationDataForDoubleNodes.getDependsByTypes());
                link.put("dependsOnTimes", dependsRelationDataForDoubleNodes.getDependsOnTimes());
                link.put("dependsByTimes", dependsRelationDataForDoubleNodes.getDependsByTimes());
                link.put("dependsIntensity", dependsRelationDataForDoubleNodes.getDependsIntensity());
                dependson_links.add(link);
            }else if(hotspotPackagePair.getHotspotRelationType().toString().equals("CO_CHANGE")){
                CoChangeRelationDataForDoubleNodes<Node, Relation> coChangeRelationDataForDoubleNodes = (CoChangeRelationDataForDoubleNodes<Node, Relation>)hotspotPackagePair.getPackagePairRelationData();

                if(coChangeRelationDataForDoubleNodes.getCoChangeTimes() >= 10){
                    link.put("type", "cochange");
                    link.put("coChangeTimes", coChangeRelationDataForDoubleNodes.getCoChangeTimes());
                    link.put("node1ChangeTimes", coChangeRelationDataForDoubleNodes.getNode1ChangeTimes());
                    link.put("node2ChangeTimes", coChangeRelationDataForDoubleNodes.getNode2ChangeTimes());
                    cochange_links.add(link);
                }
            }

//            link.put("similarityValue", decimalFormat.format(hotspotPackage.getSimilarityValue()));

//            graph_links.add(link);
        }
        table.put("clonefiles1", cloneFiles1);
        table.put("clonefiles2", cloneFiles2);

        if(type.equals("clone_package")){
            CloneRelationDataForDoubleNodes<Node, Relation> cloneRelationDataForDoubleNodes = (CloneRelationDataForDoubleNodes<Node, Relation>)parentHotspotPackagePair.getPackagePairRelationData();

            Collection<Package> nonClonePacksges1 = parentHotspotPackagePair.getChildrenOtherPackages1();
            Collection<Package> nonClonePacksges2 = parentHotspotPackagePair.getChildrenOtherPackages2();
            JSONArray nonCloneFiles1 = new JSONArray();
            JSONArray nonCloneFiles2 = new JSONArray();
            JSONObject nonClone1 = new JSONObject();
            JSONObject nonClone2 = new JSONObject();

            for(Package pck: nonClonePacksges1){
                nonClone1.put("name", pck.getDirectoryPath());
                nonCloneFiles1.add(nonClone1);
            }

            for(Package pck: nonClonePacksges2){
                nonClone2.put("name", pck.getDirectoryPath());
                nonCloneFiles2.add(nonClone2);
            }

            table.put("nonclonefiles1", nonCloneFiles1);
            table.put("nonclonefiles2", nonCloneFiles2);
            table.put("parentpackage1", parentHotspotPackagePair.getPackage1().getDirectoryPath());
            table.put("parentpackage2", parentHotspotPackagePair.getPackage2().getDirectoryPath());
            table.put("relationNodes1", cloneRelationDataForDoubleNodes.getCloneNodesCount1());
            table.put("relationNodes2", cloneRelationDataForDoubleNodes.getCloneNodesCount2());
            table.put("allNodes1", cloneRelationDataForDoubleNodes.getAllNodesCount1());
            table.put("allNodes2", cloneRelationDataForDoubleNodes.getAllNodesCount2());
            table.put("similarityValue", cloneRelationDataForDoubleNodes.getSimilarityValue());
            table.put("packageCochangeTimes", cloneRelationDataForDoubleNodes.getAllNodesCoChangeTimes());
            table.put("packageCloneCochangeTimes", cloneRelationDataForDoubleNodes.getCloneNodesCoChangeTimes());
            table.put("clonePairs", cloneRelationDataForDoubleNodes.getClonePairs());
        }

        JSONObject temp_relation = new JSONObject();
        temp_relation.put("clone_links", clone_links);
        temp_relation.put("dependson_links", dependson_links);
        temp_relation.put("cochange_links", cochange_links);

        result.put("children_graphlinks", temp_relation);
        result.put("table", table);

        return result;
    }
}

