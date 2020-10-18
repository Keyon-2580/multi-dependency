package cn.edu.fudan.se.multidependency.service.query;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
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
            nodeJSON4.put("links",projectGraphCloneLink(-1, -1));
            result.add(nodeJSON4);
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
    public JSONArray projectGraphCloneLink(long package1Id, long package2Id) {
        JSONArray links = new JSONArray();
        List<AggregationClone> aggregationCloneList = hotspotPackageDetector.quickDetectHotspotPackages(package1Id, package2Id);
        for (AggregationClone aggregationClone : aggregationCloneList) {
            JSONObject link = new JSONObject();
            link.put("source_id", "id_" + aggregationClone.getNode1().getId().toString());
            link.put("target_id", "id_" + aggregationClone.getNode2().getId().toString());
            link.put("source_projectBelong", "id_" + containRelationService.findPackageBelongToProject((Package)(aggregationClone.getNode1())).getId());
            link.put("target_projectBelong", "id_" + containRelationService.findPackageBelongToProject((Package)(aggregationClone.getNode2())).getId());
            links.add(link);
        }
        return links;
    }


}
