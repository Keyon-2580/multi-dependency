package cn.edu.fudan.se.multidependency.service.query;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackage;
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
            JSONObject clone_links = new JSONObject();
            clone_links.put("clone_links",temp_allprojects.getJSONArray("children_graphlinks"));
            nodeJSON4.put("links", clone_links);
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

        List<PackageStructure> childrenPackages = projectStructure.getChildren();
        List<PackageStructure> childrenPackagesnew = new ArrayList<>();


        for(PackageStructure pckstru : childrenPackages){
            PackageStructure pcknew = hasRelationService.packageHasInitialize(pckstru.getPck());
            childrenPackagesnew.add(pcknew);
        }

        result.put("name", project.getName());
        result.put("id", "id_" + project.getId().toString());
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
        List<HotspotPackage> hotspotPackageList = hotspotPackageDetector.detectHotspotPackagesByParentId(-1, -1, "all");
        System.out.println("hotspotPackageList " + hotspotPackageList);
        return hotspotPackagesToCloneJson(hotspotPackageList,new HotspotPackage(), "project");
    }

    @Override
    public JSONObject cloneGraphAndTableOfChildrenPackages(long package1Id, long package2Id) {
        HotspotPackage parentHotspotPackage = hotspotPackageDetector.detectHotspotPackagesByPackageId(package1Id, package2Id, "all");
        List<HotspotPackage> childrenHotspotPackages = new ArrayList<>(parentHotspotPackage.getChildrenHotspotPackages());
//        Collection<HotspotPackage> childrenHotspotPackages2 = parentHotspotPackage.getChildrenHotspotPackages();
//        System.out.println(childrenHotspotPackages2);
//        System.out.println(parentHotspotPackage.getPackage2());
        return hotspotPackagesToCloneJson(childrenHotspotPackages, parentHotspotPackage, "package");
    }


    private JSONObject hotspotPackagesToCloneJson(List<HotspotPackage> hotspotPackageList, HotspotPackage parentHotspotPackage, String type){
        JSONObject result = new JSONObject();

        JSONArray graph_links = new JSONArray();
        JSONObject table = new JSONObject();
        JSONArray cloneFiles1 = new JSONArray();
        JSONArray cloneFiles2 = new JSONArray();

        for(HotspotPackage hotspotPackage: hotspotPackageList){
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            JSONObject link = new JSONObject();

            JSONObject temp_clonefile1 = new JSONObject();
            JSONObject temp_clonefile2 = new JSONObject();

            Project source_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackage.getPackage1());
            Project target_projectBelong = containRelationService.findPackageBelongToProject(hotspotPackage.getPackage2());

            link.put("type", "clone");
            link.put("source_id", "id_" + hotspotPackage.getPackage1().getId().toString());
            link.put("target_id", "id_" + hotspotPackage.getPackage2().getId().toString());
            link.put("source_name", hotspotPackage.getPackage1().getDirectoryPath());
            link.put("target_name", hotspotPackage.getPackage2().getDirectoryPath());
            link.put("packageCochangeTimes", hotspotPackage.getPackageCochangeTimes());
            link.put("packageCloneCochangeTimes", hotspotPackage.getPackageCloneCochangeTimes());
            link.put("clonePairs", hotspotPackage.getClonePairs());
            link.put("source_projectBelong", "id_" + source_projectBelong.getId());
            link.put("target_projectBelong", "id_" + target_projectBelong.getId());
            if(hotspotPackage.getClonePairs() == 0){
                link.put("bottom_package", false);
            }else{
                link.put("bottom_package", true);
            }
//            link.put("similarityValue", decimalFormat.format(hotspotPackage.getSimilarityValue()));
            double similarityValue = hotspotPackage.getSimilarityValue();
            if(similarityValue == 1){
                link.put("similarityValue", hotspotPackage.getSimilarityValue());
            }else{
                link.put("similarityValue", decimalFormat.format(hotspotPackage.getSimilarityValue()));
            }
            graph_links.add(link);

            temp_clonefile1.put("id", hotspotPackage.getPackage1().getId().toString());
            temp_clonefile2.put("id", hotspotPackage.getPackage2().getId().toString());
            temp_clonefile1.put("projectBelong", source_projectBelong.getId().toString());
            temp_clonefile2.put("projectBelong", target_projectBelong.getId().toString());
            temp_clonefile1.put("name", hotspotPackage.getPackage1().getDirectoryPath());
            temp_clonefile2.put("name", hotspotPackage.getPackage2().getDirectoryPath());
            temp_clonefile1.put("relationNodes1", hotspotPackage.getRelationNodes1());
            temp_clonefile2.put("relationNodes2", hotspotPackage.getRelationNodes2());
            temp_clonefile1.put("allNodes1", hotspotPackage.getAllNodes1());
            temp_clonefile2.put("allNodes2", hotspotPackage.getAllNodes2());
            temp_clonefile1.put("packageCochangeTimes", hotspotPackage.getPackageCochangeTimes());
            temp_clonefile1.put("packageCloneCochangeTimes", hotspotPackage.getPackageCloneCochangeTimes());
            temp_clonefile1.put("clonePairs", hotspotPackage.getClonePairs());

            cloneFiles1.add(temp_clonefile1);
            cloneFiles2.add(temp_clonefile2);
        }
        table.put("clonefiles1", cloneFiles1);
        table.put("clonefiles2", cloneFiles2);

        if(type.equals("package")){
            Collection<Package> nonClonePacksges1 = parentHotspotPackage.getChildrenOtherPackages1();
            Collection<Package> nonClonePacksges2 = parentHotspotPackage.getChildrenOtherPackages2();
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
            table.put("parentpackage1", parentHotspotPackage.getPackage1().getDirectoryPath());
            table.put("parentpackage2", parentHotspotPackage.getPackage2().getDirectoryPath());
            table.put("relationNodes1", parentHotspotPackage.getRelationNodes1());
            table.put("relationNodes2", parentHotspotPackage.getRelationNodes2());
            table.put("allNodes1", parentHotspotPackage.getAllNodes1());
            table.put("allNodes2", parentHotspotPackage.getAllNodes2());
            table.put("similarityValue", parentHotspotPackage.getSimilarityValue());
            table.put("packageCochangeTimes", parentHotspotPackage.getPackageCochangeTimes());
            table.put("packageCloneCochangeTimes", parentHotspotPackage.getPackageCloneCochangeTimes());
            table.put("clonePairs", parentHotspotPackage.getClonePairs());
        }

        result.put("children_graphlinks", graph_links);
        result.put("table", table);

        return result;
    }
}

