package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.MetricType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.service.query.CoDeveloperService;
import cn.edu.fudan.se.multidependency.service.query.coupling.CouplingService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/coupling")
public class CouplingController {

    @Autowired
    private CouplingService couplingService;

    @Autowired
    private ContainRepository containRepository;

    @Autowired
    private PackageRepository packageRepository;

    @PostMapping("/group/fileIds")
    @ResponseBody
    public JSONObject getRelatedFilesCouplingValue(@RequestBody JSONObject requestBody){
        JSONArray fileIdsArray = requestBody.getJSONArray("fileIds");
        List<Long> fileIds = new ArrayList<>();
        for(int i = 0; i < fileIdsArray.size(); i++){
            fileIds.add(fileIdsArray.getJSONObject(i).getLong("id"));
        }

        return couplingService.getCouplingValueByFileIds(fileIds);
    }

    @GetMapping("/group/filesofpackage")
    @CrossOrigin
    @ResponseBody
    public JSONObject getFilesOfPackageCouplingValue(@RequestParam("pckId") Long pckId){
        List<ProjectFile> fileList = containRepository.findPackageContainAllFiles(pckId);
        List<Long> fileIds = new ArrayList<>();

        for(ProjectFile projectFile: fileList){
            fileIds.add(projectFile.getId());
        }

        return couplingService.getCouplingValueByFileIds(fileIds);
    }

    @GetMapping("/group/package")
    @CrossOrigin
    @ResponseBody
    public JSONObject getPackageCouplingValue(@RequestParam("pckId") Long pckId){
        JSONObject result = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();

        Package parentPackage = packageRepository.findPackageById(pckId);
        int parentPackagePathLength = parentPackage.getDirectoryPath().split("/").length;
        JSONObject parentPckJson = new JSONObject();
        parentPckJson.put("id", parentPackage.getId().toString());
        parentPckJson.put("directoryPath", parentPackage.getDirectoryPath());
        parentPckJson.put("name", parentPackage.getName());
        result.put("parentPackage", parentPckJson);

        List<Package> pckList = packageRepository.findTwoStepsChildPackagesById(pckId);
        Map<Map<Package, Package>, List<DependsOn>> dependsOnBetweenPackages = new HashMap<>();

        for(Package pck: pckList) {
            JSONObject tmpPck = new JSONObject();
            String[] pckPathSplit = pck.getDirectoryPath().split("/");
            StringBuilder pckName = new StringBuilder("/");
            for (int i = parentPackagePathLength; i < pckPathSplit.length; i++) {
                pckName.append(pckPathSplit[i]).append("/");
            }

            tmpPck.put("id", pck.getId().toString());
            tmpPck.put("directoryPath", pck.getDirectoryPath());
            tmpPck.put("name", pckName);
            tmpPck.put("label", pckName);

            List<ProjectFile> fileList = containRepository.findPackageContainFiles(pck.getId());
            List<Long> fileIds = new ArrayList<>();

            for (ProjectFile projectFile : fileList) {
                fileIds.add(projectFile.getId());
            }
            List<List<DependsOn>> listTmp = couplingService.getGroupInsideAndOutDependsOn(fileIds);

            List<DependsOn> GroupInsideToOutDependsOns = listTmp.get(1);
            List<DependsOn> GroupOutToInsideDependsOns = listTmp.get(2);
            int GroupInsideToOutDependsOnTimes = 0;
            int GroupOutToInsideDependsOnTimes = 0;

            if (GroupInsideToOutDependsOns.size() > 0) {
                for (DependsOn dependsOn : GroupInsideToOutDependsOns) {
                    Package endPackage = containRepository.findFileBelongToPackage(dependsOn.getEndNode().getId());
                    if (pckList.contains(endPackage)) {
                        Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                        for (String type : dependsOnTypes.keySet()) {
                            if (type.equals("EXTENDS") || type.equals("IMPLEMENTS")) {
                                GroupOutToInsideDependsOnTimes += 10;
                            } else if (type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                                    || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                                    || type.equals("MEMBER_VARIABLE")) {
                                GroupInsideToOutDependsOnTimes += 1;
                            }
                        }

                        Map<Package, Package> pckDependsOnTmp = new HashMap<>();
                        pckDependsOnTmp.put(pck, endPackage);
                        if (dependsOnBetweenPackages.containsKey(pckDependsOnTmp)) {
                            dependsOnBetweenPackages.get(pckDependsOnTmp).add(dependsOn);
                        } else {
                            List<DependsOn> dependsOnsListTmp = new ArrayList<>();
                            dependsOnsListTmp.add(dependsOn);
                            dependsOnBetweenPackages.put(pckDependsOnTmp, dependsOnsListTmp);
                        }
                    }
                }
            }

            if (GroupOutToInsideDependsOns.size() > 0) {
                for (DependsOn dependsOn : GroupOutToInsideDependsOns) {
                    Package startPackage = containRepository.findFileBelongToPackage(dependsOn.getStartNode().getId());
                    if (pckList.contains(startPackage)) {
                        Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                        for (String type : dependsOnTypes.keySet()) {
                            if (type.equals("EXTENDS") || type.equals("IMPLEMENTS")) {
                                GroupInsideToOutDependsOnTimes += 10;
                            } else if (type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                                    || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                                    || type.equals("MEMBER_VARIABLE")) {
                                GroupOutToInsideDependsOnTimes += 1;
                            }
                        }

                        Map<Package, Package> pckDependsOnTmp = new HashMap<>();
                        pckDependsOnTmp.put(startPackage, pck);
                        if (dependsOnBetweenPackages.containsKey(pckDependsOnTmp)) {
                            dependsOnBetweenPackages.get(pckDependsOnTmp).add(dependsOn);
                        } else {
                            List<DependsOn> dependsOnsListTmp = new ArrayList<>();
                            dependsOnsListTmp.add(dependsOn);
                            dependsOnBetweenPackages.put(pckDependsOnTmp, dependsOnsListTmp);
                        }
                    }
                }
            }

            int allDependsOnTimes = GroupInsideToOutDependsOnTimes + GroupOutToInsideDependsOnTimes;
            double pckInstability = 0.0;
            if (allDependsOnTimes != 0) {
                pckInstability = (double) GroupInsideToOutDependsOnTimes / (double) allDependsOnTimes;
            }
            tmpPck.put("instability", pckInstability);
            nodes.add(tmpPck);
        }

        for(Map<Package, Package> map: dependsOnBetweenPackages.keySet()){
            JSONObject tmpEdge = new JSONObject();
            for(Package pck: map.keySet()){
                tmpEdge.put("source", pck.getId().toString());
                tmpEdge.put("target", map.get(pck).getId().toString());
            }
            tmpEdge.put("dependsOnNum", dependsOnBetweenPackages.get(map).size());
            edges.add(tmpEdge);
        }

        result.put("nodes", nodes);
        result.put("edges", edges);
        return result;
    }

    @PostMapping("/group/packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getPackagesCouplingValue(@RequestBody JSONObject requestBody){
        JSONArray fileIdsArray = requestBody.getJSONArray("pckIds");
        List<Long> pckIds = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for(int i = 0; i < fileIdsArray.size(); i++){
            pckIds.add(fileIdsArray.getJSONObject(i).getLong("id"));
        }

        for(Long pckId: pckIds){
            List<ProjectFile> fileList = containRepository.findPackageContainFiles(pckId);
            for(ProjectFile projectFile: fileList){
                fileIds.add(projectFile.getId());
            }
        }

        return couplingService.getCouplingValueByFileIds(fileIds);
    }
}
