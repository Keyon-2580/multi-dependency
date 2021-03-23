package cn.edu.fudan.se.multidependency.service.query;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import cn.edu.fudan.se.multidependency.service.query.data.ProjectStructure;
import cn.edu.fudan.se.multidependency.service.query.smell.BasicSmellQueryService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectServiceImpl implements ProjectService{
    @Autowired
    private BasicCloneQueryService basicCloneQueryService;

    @Autowired
    private HotspotPackagePairDetector hotspotPackagePairDetector;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private DependsOnRepository dependsOnRepository;

    @Autowired
    private CloneRepository cloneRepository;

    @Autowired
    private CoChangeRepository coChangeRepository;

    @Autowired
    private BasicSmellQueryService basicSmellQueryService;

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
    public JSONArray getMultipleProjectsGraphJson(JSONObject dataList, String type) {
        JSONArray projectIds = dataList.getJSONArray("projectIds");
        JSONArray result = new JSONArray();
        JSONObject nodeJSON2 = new JSONObject();
        JSONObject nodeJSON4 = new JSONObject();
        JSONObject nodeJSON5 = new JSONObject();

        JSONObject projectJson = new JSONObject();
        if(projectIds.size() == 1){
            projectJson = joinMultipleProjectsGraphJson(projectIds.getJSONObject(0).getLong("id"), type);
        }else{
            projectJson.put("name", "default");
            projectJson.put("id", "id_default");
            JSONArray multipleProjectsJson = new JSONArray();
            for(int i = 0; i < projectIds.size(); i++){
                multipleProjectsJson.add(joinMultipleProjectsGraphJson(projectIds.getJSONObject(i).getLong("id"), type));
            }
            projectJson.put("children",multipleProjectsJson);
        }

        nodeJSON2.put("result",projectJson);
        result.add(nodeJSON2);

//        JSONObject temp_allprojects = getAllProjectsLinks();
//        nodeJSON4.put("links", temp_allprojects);
//        result.add(nodeJSON4);

        JSONArray temp_allprojects = getAllProjectsLinks2();
        nodeJSON4.put("links", temp_allprojects);
        result.add(nodeJSON4);

        if(Constant.PROJECT_STRUCTURE_TREEMAP.equals(type)){
        nodeJSON5.put("smell", basicSmellQueryService.smellsToTreemap());
        result.add(nodeJSON5);
        }

        return result;
    }

    private JSONObject joinMultipleProjectsGraphJson(long projectId, String type){
        JSONObject result = new JSONObject();
        Project project = nodeService.queryProject(projectId);
        ProjectStructure projectStructure = containRelationService.projectStructureInitialize(project);
        Package packageOfProject = projectStructure.getChildren().get(0).getPck();

        List<PackageStructure> childrenPackages = containRelationService.packageStructureInitialize(packageOfProject,type).getChildrenPackages();
        List<PackageStructure> childrenPackagesnew = new ArrayList<>();

        for(PackageStructure pckstru : childrenPackages){
            PackageStructure pcknew = containRelationService.packageStructureInitialize(pckstru.getPck(),type);
            childrenPackagesnew.add(pcknew);
        }

//        result.put("name", packageOfProject.getName());
//        result.put("id", "id_" + packageOfProject.getId().toString());
//        Collection<ProjectFile> clonefiles = basicCloneQueryService.findProjectContainCloneFiles(project);
//        result.put("children", getPackageContainJson(clonefiles,childrenPackagesnew,type));

        JSONArray combo = new JSONArray();

        for(PackageStructure pckstru2 : childrenPackagesnew.get(0).getChildrenPackages()){
            JSONObject temp = new JSONObject();
            List<PackageStructure> pckList = pckstru2.getChildrenPackages();
            JSONArray temp_children = new JSONArray();
            temp.put("name",pckstru2.getPck().getDirectoryPath());
            temp.put("depth",pckstru2.getPck().getDepth());
            temp.put("id","id_" + pckstru2.getPck().getId().toString());

            List<ProjectFile> fileList = pckstru2.getChildrenFiles();
            if(fileList.size() > 0){
                for(ProjectFile profile : fileList){
                    JSONObject jsonObject2 = new JSONObject();
                    jsonObject2.put("size",profile.getLoc());
//					jsonObject2.put("value",profile.getLoc());
                    jsonObject2.put("long_name",profile.getPath());
//                        if(clonefiles.contains(profile)){
//                            jsonObject2.put("clone",true);
//                        }else{
//                            jsonObject2.put("clone",false);
//                        }
                    jsonObject2.put("name",profile.getName());
                    jsonObject2.put("id","id_" + profile.getId().toString());
                    temp_children.add(jsonObject2);
                }
            }

            JSONArray children = getPackageContainJson2(pckList);
            temp_children.addAll(children);
            temp.put("children", temp_children);

            combo.add(temp);
        }

        result.put("result", combo);

        return result;
    }

    @Override
    /**
     * 递归遍历项目中所有package的包含关系
     */
    public JSONArray getPackageContainJson(Collection<ProjectFile> clonefiles, List<PackageStructure> childrenPackages,String type){
        JSONArray result = new JSONArray();
        for(PackageStructure pckstru :childrenPackages){
            List<PackageStructure> pckList = pckstru.getChildrenPackages();
            List<ProjectFile> fileList = pckstru.getChildrenFiles();
            JSONObject jsonObject = new JSONObject();
//
//            jsonObject.put("name",pckstru.getPck().getName());
            jsonObject.put("name",pckstru.getPck().getDirectoryPath());
//			jsonObject.put("long_name",pckstru.getPck().getDirectoryPath());
            jsonObject.put("size",fileList.size());
            jsonObject.put("depth",pckstru.getPck().getDepth());
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

            if(Constant.PROJECT_STRUCTURE_TREEMAP.equals(type)){
                JSONArray jsonArray = new JSONArray();
                if(fileList.size() > 0){
                    for(ProjectFile profile : fileList){
                        JSONObject jsonObject2 = new JSONObject();
                        jsonObject2.put("size",profile.getLoc());
//					jsonObject2.put("value",profile.getLoc());
                        jsonObject2.put("long_name",profile.getPath());
                        if(clonefiles.contains(profile)){
                            jsonObject2.put("clone",true);
                        }else{
                            jsonObject2.put("clone",false);
                        }
                        jsonObject2.put("name",profile.getName());
                        jsonObject2.put("id","id_" + profile.getId().toString());
                        jsonArray.add(jsonObject2);
                    }
                }

                if(jsonArray.size() > 0){
                    jsonObject.put("children",jsonArray);
                }
            }

            if(pckList.size()>0){
                //如果该属性还有子属性,继续做查询,直到该属性没有孩子,也就是最后一个节点
                jsonObject.put("children", getPackageContainJson(clonefiles,pckList,type));
            }
            result.add(jsonObject);
        }
        return result;
    }

    @Override
    public JSONArray getPackageContainJson2(List<PackageStructure> childrenPackages){
        JSONArray result = new JSONArray();
        for(PackageStructure pckstru :childrenPackages){
            List<PackageStructure> pckList = pckstru.getChildrenPackages();
            List<ProjectFile> fileList = pckstru.getChildrenFiles();
//            JSONObject jsonObject = new JSONObject();
//
//            jsonObject.put("name",pckstru.getPck().getName());
//            jsonObject.put("path",pckstru.getPck().getDirectoryPath());
//            jsonObject.put("depth",pckstru.getPck().getDepth());
//            jsonObject.put("id","id_" + pckstru.getPck().getId().toString());
            float cloneFilesInAllFiles = 0;

            if(fileList.size() > 0){
                for(ProjectFile profile : fileList){
                    JSONObject jsonObject2 = new JSONObject();
                    jsonObject2.put("size",profile.getLoc());
                    jsonObject2.put("long_name",profile.getPath());
                    jsonObject2.put("name",profile.getName());
                    jsonObject2.put("id","id_" + profile.getId().toString());
                    result.add(jsonObject2);
                }
            }

            if(pckList.size()>0){
                //如果该属性还有子属性,继续做查询,直到该属性没有孩子,也就是最后一个节点
                JSONArray temp_children = getPackageContainJson2(pckList);
                result.addAll(temp_children);
            }
        }
        return result;
    }

    @Override
    public JSONObject getAllProjectsLinks(){
        List<HotspotPackagePair> cloneHotspotPackageList = hotspotPackagePairDetector.getHotspotPackagePairWithFileCloneByParentId(-1, -1, "all");
        List<HotspotPackagePair> dependsonHotspotPackageList = hotspotPackagePairDetector.getHotspotPackagePairWithDependsOn();
        List<HotspotPackagePair> cochangeHotspotPackageList = hotspotPackagePairDetector.getHotspotPackagePairWithCoChange();
        return hotspotPackagesToCloneJson(cloneHotspotPackageList, dependsonHotspotPackageList, cochangeHotspotPackageList);
    }

    @Override
    public JSONArray getAllProjectsLinks2(){
        JSONArray result = new JSONArray();

        List<DependsOn> dependsOnList= dependsOnRepository.findFileDependsInProject(582);
        List<Clone> cloneList= cloneRepository.findAllFileClones();
        List<CoChange> coChangeList= coChangeRepository.findFileCoChange();

        for(DependsOn dependsOn : dependsOnList){
            JSONObject temp1 = new JSONObject();
            temp1.put("type", "dependson");
            temp1.put("source_id", "id_" + dependsOn.getStartNode().getId());
            temp1.put("target_id", "id_" + dependsOn.getEndNode().getId());
            temp1.put("source_name", dependsOn.getEndNode().getName());
            temp1.put("target_name", dependsOn.getEndNode().getName());
            temp1.put("pair_id", dependsOn.getStartNode().getId() + "_" + dependsOn.getEndNode().getId());
            temp1.put("dependsOnTypes", dependsOn.getDependsOnType());
            result.add(temp1);
        }

        for(Clone clone : cloneList){
            JSONObject temp2 = new JSONObject();
            temp2.put("type", "clone");
            temp2.put("source_id", "id_" + clone.getCodeNode1().getId());
            temp2.put("target_id", "id_" + clone.getCodeNode2().getId());
            temp2.put("source_name", clone.getCodeNode1().getName());
            temp2.put("target_name", clone.getCodeNode2().getName());
            temp2.put("pair_id", clone.getCodeNode1().getId() + "_" + clone.getCodeNode2().getId());
            result.add(temp2);
        }

        for(CoChange coChange : coChangeList){
            if(coChange.getTimes() > 20){
                JSONObject temp3 = new JSONObject();
                temp3.put("type", "cochange");
                temp3.put("source_id", "id_" + coChange.getNode1().getId());
                temp3.put("target_id", "id_" + coChange.getNode2().getId());
                temp3.put("source_name", coChange.getNode1().getName());
                temp3.put("target_name", coChange.getNode2().getName());
                temp3.put("pair_id", coChange.getNode1().getId() + "_" + coChange.getNode2().getId());
                result.add(temp3);
            }
        }
        return result;
    }

//    @Override
//    public JSONObject cloneGraphAndTableOfChildrenPackages(long package1Id, long package2Id) {
//        HotspotPackagePair parentHotspotPackage = hotspotPackagePairDetector.getHotspotPackagePairWithFileCloneByPackageId(package1Id, package2Id, "all");
//        List<HotspotPackagePair> childrenHotspotPackages = parentHotspotPackage.getChildrenHotspotPackagePairs();
//        return hotspotPackagesToCloneJson(childrenHotspotPackages, parentHotspotPackage, "clone_package_table");
//    }

    private JSONObject hotspotPackagesToCloneJson(List<HotspotPackagePair> cloneHotspotPackageList, List<HotspotPackagePair> dependsonHotspotPackageList, List<HotspotPackagePair> cochangeHotspotPackageList){
        JSONObject result = new JSONObject();

        JSONArray clone_links = new JSONArray();
        JSONArray dependson_links = new JSONArray();
        JSONArray cochange_links = new JSONArray();

//        for(HotspotPackagePair hotspotPackagePair: hotspotPackagePairList){
//            JSONObject link = new JSONObject();
//            JSONObject link_common = new JSONObject();
//
//            Project source_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage1());
//            Project target_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage2());
//
//            link_common.put("source_id", "id_" + hotspotPackagePair.getPackage1().getId().toString());
//            link_common.put("target_id", "id_" + hotspotPackagePair.getPackage2().getId().toString() );
//            link_common.put("pair_id", hotspotPackagePair.getPackage1().getId().toString() + "_" + hotspotPackagePair.getPackage2().getId().toString());
//            link_common.put("source_name", hotspotPackagePair.getPackage1().getDirectoryPath());
//            link_common.put("target_name", hotspotPackagePair.getPackage2().getDirectoryPath());
//            link_common.put("source_projectBelong", "id_" + source_projectBelong.getId());
//            link_common.put("target_projectBelong", "id_" + target_projectBelong.getId());
//
//            switch (hotspotPackagePair.getHotspotRelationType().toString()) {
//                case "CLONE":
//                    CloneRelationDataForDoubleNodes<Node, Relation> cloneRelationDataForDoubleNodes = (CloneRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();
//
//                    link.put("type", "clone");
//                    link.put("clonePairs", cloneRelationDataForDoubleNodes.getClonePairs());
//                    link.put("cloneNodesCount1", cloneRelationDataForDoubleNodes.getCloneNodesCount1());
//                    link.put("cloneNodesCount2", cloneRelationDataForDoubleNodes.getCloneNodesCount2());
//                    link.put("allNodesCount1", cloneRelationDataForDoubleNodes.getAllNodesCount1());
//                    link.put("allNodesCount2", cloneRelationDataForDoubleNodes.getAllNodesCount2());
//                    link.put("cloneMatchRate", cloneRelationDataForDoubleNodes.getCloneMatchRate());
//                    link.put("cloneNodesLoc1", cloneRelationDataForDoubleNodes.getCloneNodesLoc1());
//                    link.put("cloneNodesLoc2", cloneRelationDataForDoubleNodes.getCloneNodesLoc2());
//                    link.put("allNodesLoc1", cloneRelationDataForDoubleNodes.getAllNodesLoc1());
//                    link.put("allNodesLoc2", cloneRelationDataForDoubleNodes.getAllNodesLoc2());
//                    link.put("cloneLocRate", cloneRelationDataForDoubleNodes.getCloneLocRate());
//                    link.put("cloneNodesCoChangeTimes", cloneRelationDataForDoubleNodes.getCloneNodesCoChangeTimes());
//                    link.put("allNodesCoChangeTimes", cloneRelationDataForDoubleNodes.getAllNodesCoChangeTimes());
//                    link.put("cloneCoChangeRate", cloneRelationDataForDoubleNodes.getCloneCoChangeRate());
//                    link.put("cloneType1Count", cloneRelationDataForDoubleNodes.getCloneType1Count());
//                    link.put("cloneType2Count", cloneRelationDataForDoubleNodes.getCloneType2Count());
//                    link.put("cloneType3Count", cloneRelationDataForDoubleNodes.getCloneType3Count());
//                    link.put("cloneType", cloneRelationDataForDoubleNodes.getCloneType());
//                    link.put("cloneSimilarityValue", cloneRelationDataForDoubleNodes.getCloneSimilarityValue());
//                    link.put("cloneSimilarityRate", cloneRelationDataForDoubleNodes.getCloneSimilarityRate());
//                    if (cloneRelationDataForDoubleNodes.getClonePairs() == 0) {
//                        link.put("bottom_package", false);
//                    } else {
//                        link.put("bottom_package", true);
//                    }
//
//                    clone_links.add(JSONUtil.combineJSONObjectWithoutMerge(link, link_common));
//                    break;
//                case "DEPENDS_ON":
//                    DependsRelationDataForDoubleNodes<Node, Relation> dependsRelationDataForDoubleNodes = (DependsRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();
//
//                    link.put("type", "dependson");
//                    link.put("dependsOnTypes", dependsRelationDataForDoubleNodes.getDependsOnTypes());
//                    link.put("dependsByTypes", dependsRelationDataForDoubleNodes.getDependsByTypes());
//                    link.put("dependsOnTimes", dependsRelationDataForDoubleNodes.getDependsOnTimes());
//                    link.put("dependsByTimes", dependsRelationDataForDoubleNodes.getDependsByTimes());
//                    link.put("dependsOnWeightedTimes", dependsRelationDataForDoubleNodes.getDependsOnWeightedTimes());
//                    link.put("dependsByWeightedTimes", dependsRelationDataForDoubleNodes.getDependsByWeightedTimes());
//                    link.put("dependsOnIntensity", dependsRelationDataForDoubleNodes.getDependsOnInstability());
//                    link.put("dependsByIntensity", dependsRelationDataForDoubleNodes.getDependsByInstability());
//
//                    if(dependsRelationDataForDoubleNodes.getDependsOnTypes().equals("") ||
//                            dependsRelationDataForDoubleNodes.getDependsByTypes().equals("")){
//                        link.put("two_way", false);
//                    }else{
//                        link.put("two_way", true);
//                    }
//
//                    Map<String, Long> dependsOnTypesMap = dependsRelationDataForDoubleNodes.getDependsOnTypesMap();
//                    Map<String, Long> dependsByTypesMap = dependsRelationDataForDoubleNodes.getDependsByTypesMap();
//                    JSONArray dependsOnTypesArray = new JSONArray();
//                    JSONArray dependsByTypesArray = new JSONArray();
//
//                    for(Map.Entry<String, Long> entry: dependsOnTypesMap.entrySet()){
//                        JSONObject temp_dependsOnType = new JSONObject();
//                        temp_dependsOnType.put("dependsOnType", entry.getKey());
//                        temp_dependsOnType.put("dependsOnTime", entry.getValue());
//
//                        dependsOnTypesArray.add(temp_dependsOnType);
//                    }
//
//                    for(Map.Entry<String, Long> entry: dependsByTypesMap.entrySet()){
//                        JSONObject temp_dependsByType = new JSONObject();
//                        temp_dependsByType.put("dependsByType", entry.getKey());
//                        temp_dependsByType.put("dependsByTime", entry.getValue());
//
//                        dependsByTypesArray.add(temp_dependsByType);
//                    }
//
//                    link.put("dependsOnTypesMap", dependsOnTypesArray);
//                    link.put("dependsByTypesMap", dependsByTypesArray);
//
//                    dependson_links.add(JSONUtil.combineJSONObjectWithoutMerge(link, link_common));
//
//                    break;
//                case "CO_CHANGE":
//                    CoChangeRelationDataForDoubleNodes<Node, Relation> coChangeRelationDataForDoubleNodes = (CoChangeRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();
//
//                    if (coChangeRelationDataForDoubleNodes.getCoChangeTimes() >= 10) {
//                        link.put("type", "cochange");
//                        link.put("coChangeTimes", coChangeRelationDataForDoubleNodes.getCoChangeTimes());
//                        link.put("node1ChangeTimes", coChangeRelationDataForDoubleNodes.getNode1ChangeTimes());
//                        link.put("node2ChangeTimes", coChangeRelationDataForDoubleNodes.getNode2ChangeTimes());
//                        cochange_links.add(JSONUtil.combineJSONObjectWithoutMerge(link, link_common));
//                    }
//                    break;
//            }
//        }

        result.put("clone_links", getLinksJson(cloneHotspotPackageList, "CLONE", "default"));
        result.put("dependson_links", getLinksJson(dependsonHotspotPackageList, "DEPENDS_ON", "default"));
        result.put("cochange_links", getLinksJson(cochangeHotspotPackageList, "CO_CHANGE", "default"));

//        if(type.equals("clone_package_table")){
//            Collection<Package> nonClonePacksges1 = parentHotspotPackagePair.getChildrenOtherPackages1();
//            Collection<Package> nonClonePacksges2 = parentHotspotPackagePair.getChildrenOtherPackages2();
//            JSONArray nonCloneFiles1 = new JSONArray();
//            JSONArray nonCloneFiles2 = new JSONArray();
//
//            for(Package pck: nonClonePacksges1){
//                JSONObject nonClone1 = new JSONObject();
//                nonClone1.put("name", pck.getDirectoryPath());
//                nonCloneFiles1.add(nonClone1);
//            }
//
//            for(Package pck: nonClonePacksges2){
//                JSONObject nonClone2 = new JSONObject();
//                nonClone2.put("name", pck.getDirectoryPath());
//                nonCloneFiles2.add(nonClone2);
//            }
//
//            temp_relation.put("nonclonefiles1", nonCloneFiles1);
//            temp_relation.put("nonclonefiles2", nonCloneFiles2);
//        }

//        result.put("children_graphlinks", temp_relation);
        return result;
    }

    private JSONArray getLinksJson(List<HotspotPackagePair> hotspotPackagePairList, String linkType, String parentPairId){
        JSONArray result = new JSONArray();
        for(HotspotPackagePair hotspotPackagePair: hotspotPackagePairList){
            JSONObject link = new JSONObject();
            JSONObject link_common = new JSONObject();

            Project source_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage1());
            Project target_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage2());

            link_common.put("source_id", "id_" + hotspotPackagePair.getPackage1().getId().toString());
            link_common.put("target_id", "id_" + hotspotPackagePair.getPackage2().getId().toString());
            link_common.put("depth", Math.max(hotspotPackagePair.getPackage1().getDepth(), hotspotPackagePair.getPackage2().getDepth()));
            link_common.put("pair_id", hotspotPackagePair.getPackage1().getId().toString() + "_" + hotspotPackagePair.getPackage2().getId().toString());
            link_common.put("parent_pair_id", parentPairId);
            link_common.put("source_name", hotspotPackagePair.getPackage1().getDirectoryPath());
            link_common.put("target_name", hotspotPackagePair.getPackage2().getDirectoryPath());
            link_common.put("source_projectBelong", "id_" + source_projectBelong.getId());
            link_common.put("target_projectBelong", "id_" + target_projectBelong.getId());

            switch (linkType) {
                case "CLONE":
                    CloneRelationDataForDoubleNodes<Node, Relation> cloneRelationDataForDoubleNodes = (CloneRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();

                    link.put("type", "clone");
                    link.put("clonePairs", cloneRelationDataForDoubleNodes.getClonePairs());
                    link.put("cloneNodesCount1", cloneRelationDataForDoubleNodes.getCloneNodesCount1());
                    link.put("cloneNodesCount2", cloneRelationDataForDoubleNodes.getCloneNodesCount2());
                    link.put("allNodesCount1", cloneRelationDataForDoubleNodes.getAllNodesCount1());
                    link.put("allNodesCount2", cloneRelationDataForDoubleNodes.getAllNodesCount2());
                    link.put("cloneMatchRate", cloneRelationDataForDoubleNodes.getCloneMatchRate());
                    link.put("cloneNodesLoc1", cloneRelationDataForDoubleNodes.getCloneNodesLoc1());
                    link.put("cloneNodesLoc2", cloneRelationDataForDoubleNodes.getCloneNodesLoc2());
                    link.put("allNodesLoc1", cloneRelationDataForDoubleNodes.getAllNodesLoc1());
                    link.put("allNodesLoc2", cloneRelationDataForDoubleNodes.getAllNodesLoc2());
                    link.put("cloneLocRate", cloneRelationDataForDoubleNodes.getCloneLocRate());
                    link.put("cloneNodesCoChangeTimes", cloneRelationDataForDoubleNodes.getCloneNodesCoChangeTimes());
                    link.put("allNodesCoChangeTimes", cloneRelationDataForDoubleNodes.getAllNodesCoChangeTimes());
                    link.put("cloneCoChangeRate", cloneRelationDataForDoubleNodes.getCloneCoChangeRate());
                    link.put("cloneType1Count", cloneRelationDataForDoubleNodes.getCloneType1Count());
                    link.put("cloneType2Count", cloneRelationDataForDoubleNodes.getCloneType2Count());
                    link.put("cloneType3Count", cloneRelationDataForDoubleNodes.getCloneType3Count());
                    link.put("cloneType", cloneRelationDataForDoubleNodes.getCloneType());
                    link.put("cloneSimilarityValue", cloneRelationDataForDoubleNodes.getCloneSimilarityValue());
                    link.put("cloneSimilarityRate", cloneRelationDataForDoubleNodes.getCloneSimilarityRate());
                    if (cloneRelationDataForDoubleNodes.getClonePairs() == 0) {
                        link.put("bottom_package", false);
                    } else {
                        link.put("bottom_package", true);
                    }

                    result.add(JSONUtil.combineJSONObjectWithoutMerge(link, link_common));
                    break;
                case "DEPENDS_ON":
                    DependsRelationDataForDoubleNodes<Node, Relation> dependsRelationDataForDoubleNodes = (DependsRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();

                    link.put("type", "dependson");
                    link.put("dependsOnTypes", dependsRelationDataForDoubleNodes.getDependsOnTypes());
                    link.put("dependsByTypes", dependsRelationDataForDoubleNodes.getDependsByTypes());
                    link.put("dependsOnTimes", dependsRelationDataForDoubleNodes.getDependsOnTimes());
                    link.put("dependsByTimes", dependsRelationDataForDoubleNodes.getDependsByTimes());
                    link.put("dependsOnWeightedTimes", dependsRelationDataForDoubleNodes.getDependsOnWeightedTimes());
                    link.put("dependsByWeightedTimes", dependsRelationDataForDoubleNodes.getDependsByWeightedTimes());
                    link.put("dependsOnIntensity", dependsRelationDataForDoubleNodes.getDependsOnInstability());
                    link.put("dependsByIntensity", dependsRelationDataForDoubleNodes.getDependsByInstability());
                    link.put("bottom_package", !hotspotPackagePair.isAggregatePackagePair());

                    if(dependsRelationDataForDoubleNodes.getDependsOnTypes().equals("") ||
                            dependsRelationDataForDoubleNodes.getDependsByTypes().equals("")){
                        link.put("two_way", false);
                    }else{
                        link.put("two_way", true);
                    }

                    Map<String, Long> dependsOnTypesMap = dependsRelationDataForDoubleNodes.getDependsOnTypesMap();
                    Map<String, Long> dependsByTypesMap = dependsRelationDataForDoubleNodes.getDependsByTypesMap();
                    JSONArray dependsOnTypesArray = new JSONArray();
                    JSONArray dependsByTypesArray = new JSONArray();

                    for(Map.Entry<String, Long> entry: dependsOnTypesMap.entrySet()){
                        JSONObject temp_dependsOnType = new JSONObject();
                        temp_dependsOnType.put("dependsOnType", entry.getKey());
                        temp_dependsOnType.put("dependsOnTime", entry.getValue());

                        dependsOnTypesArray.add(temp_dependsOnType);
                    }

                    for(Map.Entry<String, Long> entry: dependsByTypesMap.entrySet()){
                        JSONObject temp_dependsByType = new JSONObject();
                        temp_dependsByType.put("dependsByType", entry.getKey());
                        temp_dependsByType.put("dependsByTime", entry.getValue());

                        dependsByTypesArray.add(temp_dependsByType);
                    }

                    link.put("dependsOnTypesMap", dependsOnTypesArray);
                    link.put("dependsByTypesMap", dependsByTypesArray);

                    result.add(JSONUtil.combineJSONObjectWithoutMerge(link, link_common));

                    break;
                case "CO_CHANGE":
                    CoChangeRelationDataForDoubleNodes<Node, Relation> coChangeRelationDataForDoubleNodes = (CoChangeRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();

                    if (coChangeRelationDataForDoubleNodes.getCoChangeTimes() >= 3) {
                        link.put("type", "cochange");
                        link.put("coChangeTimes", coChangeRelationDataForDoubleNodes.getCoChangeTimes());
                        link.put("node1ChangeTimes", coChangeRelationDataForDoubleNodes.getNode1ChangeTimes());
                        link.put("node2ChangeTimes", coChangeRelationDataForDoubleNodes.getNode2ChangeTimes());
                        link.put("bottom_package", !hotspotPackagePair.isAggregatePackagePair());
                        result.add(JSONUtil.combineJSONObjectWithoutMerge(link, link_common));
                    }
                    break;
            }

            if(hotspotPackagePair.hasChildrenHotspotPackagePairs()){
//                System.out.println(hotspotPackagePair.getHotspotRelationType());
                result.addAll(getLinksJson(hotspotPackagePair.getChildrenHotspotPackagePairs(), linkType, hotspotPackagePair.getPackage1().getId().toString() + "_" + hotspotPackagePair.getPackage2().getId().toString()));
            }
        }
        return result;
    }
}

