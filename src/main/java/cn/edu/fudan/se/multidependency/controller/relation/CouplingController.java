package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.service.query.BeanCreator;
import cn.edu.fudan.se.multidependency.service.query.coupling.CouplingService;
import cn.edu.fudan.se.multidependency.utils.GraphLayoutUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/coupling")
public class CouplingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanCreator.class);

    @Autowired
    private CouplingService couplingService;

    @Autowired
    private ContainRepository containRepository;

    @Autowired
    private PackageRepository packageRepository;


    /**
     * 返回一个包下的所有子包（打平）的耦合数据
     * @param requestBody
     * @return
     */
    @PostMapping("/group/all_child_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getAllChildPackagesCouplingValue(@RequestBody JSONObject requestBody){
        JSONArray parentPcks = new JSONArray();
        Map<Long, Double> parentPcksInstability = new HashMap<>();
        Map<Package, List<Package>> pckMap = new HashMap<>();

        JSONArray fileIdsArray = requestBody.getJSONArray("pcks");
        List<Long> pckIds = new ArrayList<>();
        for(int i = 0; i < fileIdsArray.size(); i++){
            pckIds.add(fileIdsArray.getJSONObject(i).getLong("id"));
            parentPcksInstability.put(fileIdsArray.getJSONObject(i).getLong("id"),
                    fileIdsArray.getJSONObject(i).getDouble("instability"));
        }

        for(Long pckId: pckIds){
            Package parentPackage = packageRepository.findPackageById(pckId);

            JSONObject parentPckJson = new JSONObject();
            parentPckJson.put("id", parentPackage.getId().toString());
            parentPckJson.put("directoryPath", parentPackage.getDirectoryPath());
            parentPckJson.put("name", parentPackage.getName());
            parentPcks.add(parentPckJson);

            List<Package> pckList = new ArrayList<>(packageRepository.findAllChildPackagesById(pckId));
            pckMap.put(parentPackage, pckList);
        }

        JSONObject result = couplingService.getCouplingValueByPcks(pckMap, parentPcksInstability, false, false);
        result.put("parentPackage", parentPcks);

        return result;
    }

    void convertY2Levels(JSONArray nodes) {
        Set<Double> ySet = new TreeSet<>();
        for (int i = 0; i < nodes.size(); i++) {
            ySet.add(nodes.getJSONObject(i).getDouble("y"));
        }
        List<Double> yList = new ArrayList<>(ySet);
        for (int i = 0; i < nodes.size(); i++) {
            int level = yList.indexOf(nodes.getJSONObject(i).getDouble("y"));
            nodes.getJSONObject(i).put("level", level);
        }
    }
    void convertY2Levels(JSONArray unfoldPkgsJsonArray, JSONArray otherPkgsJsonArray) {
        Set<Double> ySet = new TreeSet<>();
        for (int i = 0; i < otherPkgsJsonArray.size(); i++) {
            ySet.add(otherPkgsJsonArray.getJSONObject(i).getDouble("y"));
        }
        for (int i = 0; i <unfoldPkgsJsonArray.size() ; i++) {
            ySet.add(unfoldPkgsJsonArray.getJSONObject(i).getDouble("y"));
        }
        List<Double> yList = new ArrayList<>(ySet);
        for (int i = 0; i < otherPkgsJsonArray.size(); i++) {
            int level = yList.indexOf(otherPkgsJsonArray.getJSONObject(i).getDouble("y"));
            otherPkgsJsonArray.getJSONObject(i).put("level", level);
        }
        for (int i = 0; i < unfoldPkgsJsonArray.size(); i++) {
            int level = yList.indexOf(unfoldPkgsJsonArray.getJSONObject(i).getDouble("y"));
            unfoldPkgsJsonArray.getJSONObject(i).put("level", level);
        }
    }
    /**
     * 展开若干个包，返回这些包下的第一层子包的耦合数据
     * @param requestBody 前端请求
     * @return result json
     */
    @PostMapping("/group/unfold_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject unfoldPackages(@RequestBody JSONObject requestBody) {
        JSONArray otherPkgsJsonArray = requestBody.getJSONArray("otherPcks");
        JSONArray unfoldPkgsJsonArray = requestBody.getJSONArray("unfoldPcks");
        convertY2Levels(unfoldPkgsJsonArray, otherPkgsJsonArray);
        List<Package> allPackages = new ArrayList<>();
//        Map<Long, Integer> levelMap = new HashMap<>();
//        Map<Package, List<Package>> unfoldPkgMap = new HashMap<>();
        for (int i = 0; i < unfoldPkgsJsonArray.size(); i++) {
            JSONObject pkgJson = unfoldPkgsJsonArray.getJSONObject(i);
            Long pkgId = pkgJson.getLong("id");
//            levelMap.put(pkgId, pkgJson.getIntValue("level"));
//            Package parentPackage = packageRepository.findPackageById(pkgId);
            List<Package> childPkgs = new ArrayList<>(packageRepository.findOneStepPackagesById(pkgId));
            allPackages.addAll(childPkgs);
            if (childPkgs.size() == 0) {
                JSONObject failJson = new JSONObject();
                failJson.put("code", -1);
                return failJson;
            }
//            if (packageRepository.findIfPackageContainFiles(parentPackage.getId())) {
//                childPkgs.add(parentPackage);
//            }
//            unfoldPkgMap.put(parentPackage, childPkgs);
        }
        for (int i = 0; i < otherPkgsJsonArray.size(); i++) {
            Package tmp = packageRepository.findPackageById(otherPkgsJsonArray.getJSONObject(i).getLong("id"));
            allPackages.add(tmp);
        }
//        JSONObject result = couplingService.getChildPackagesCouplingValue(unfoldPkgMap, otherPkgsJsonArray, levelMap);
        JSONObject result = couplingService.unfoldPackages(unfoldPkgsJsonArray, otherPkgsJsonArray, allPackages);
        result.put("code", 200);
        return result;
    }

    /**
     * 展开若干个包，返回这些包下的第一层子包的耦合数据
     * @param requestBody 前端请求
     * @return result json
     */
    @PostMapping("/group/unfold_packages_to_files")
    @CrossOrigin
    @ResponseBody
    public JSONObject unfoldPackagesToFiles(@RequestBody JSONObject requestBody) {
        JSONArray selectedPkgs = requestBody.getJSONArray("pckIds");
        convertY2Levels(selectedPkgs);
        List<ProjectFile> allFiles = new ArrayList<>();
        for (int i = 0; i < selectedPkgs.size(); i++) {
            Long pkgId = selectedPkgs.getJSONObject(i).getLong("id");
            List<ProjectFile> fileList = containRepository.findPackageContainAllFiles(pkgId);
            allFiles.addAll(fileList);
        }
        JSONObject result = couplingService.unfoldPackagesToFile(selectedPkgs, allFiles);
        return result;
    }

    /**
     * 返回一个包下的第一层子包的耦合数据
     * @param requestBody
     * @return
     */
    @PostMapping("/group/one_step_child_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getOneStepChildPackagesCouplingValue(@RequestBody JSONObject requestBody){

//        JSONArray parentPcks = new JSONArray();
        Map<Long, Double> parentPcksInstability = new HashMap<>();
        Map<Package, List<Package>> pckMap = new HashMap<>();

        JSONArray otherPcks = requestBody.getJSONArray("otherPcks");
        JSONArray unfoldPcks = requestBody.getJSONArray("unfoldPcks");

        List<Long> otherPckIds = new ArrayList<>();
        List<Long> unfoldPckIds = new ArrayList<>();
        for(int i = 0; i < otherPcks.size(); i++){
            otherPckIds.add(otherPcks.getJSONObject(i).getLong("id"));
            parentPcksInstability.put(otherPcks.getJSONObject(i).getLong("id"),
                    otherPcks.getJSONObject(i).getDouble("instability"));
        }

        for(int i = 0; i < unfoldPcks.size(); i++){
            unfoldPckIds.add(unfoldPcks.getJSONObject(i).getLong("id"));
            parentPcksInstability.put(unfoldPcks.getJSONObject(i).getLong("id"),
                    unfoldPcks.getJSONObject(i).getDouble("instability"));
        }

        for(Long pckId: otherPckIds){
            Package pck = packageRepository.findPackageById(pckId);
            JSONObject parentPckJson = new JSONObject();
            parentPckJson.put("id", pck.getId().toString());
            parentPckJson.put("directoryPath", pck.getDirectoryPath());
            parentPckJson.put("name", pck.getName());
            parentPckJson.put("label", pck.getName());
//            parentPcks.add(parentPckJson);

            List<Package> tmpList = new ArrayList<>();
            tmpList.add(pck);
            pckMap.put(pck, tmpList);
        }

        for(Long pckId: unfoldPckIds){
            Package parentPackage = packageRepository.findPackageById(pckId);
            JSONObject parentPckJson = new JSONObject();
            parentPckJson.put("id", parentPackage.getId().toString());
            parentPckJson.put("directoryPath", parentPackage.getDirectoryPath());
            parentPckJson.put("name", parentPackage.getName());
//            parentPcks.add(parentPckJson);

            List<Package> pckList = new ArrayList<>(packageRepository.findOneStepPackagesById(pckId));

            if(pckList.size() == 0){
                JSONObject failJson = new JSONObject();
                failJson.put("code", -1);
                failJson.put("pck", parentPckJson);
                return failJson;
            }
//            if(packageRepository.findIfPackageContainFiles(parentPackage.getId())) pckList.add(parentPackage);
            pckMap.put(parentPackage, pckList);
        }

        JSONObject result = couplingService.getCouplingValueByPcks(pckMap, parentPcksInstability, false, true);
        result.put("code", 200);
        return result;
    }

    @PostMapping("/table/node_one_step_detail")
    @CrossOrigin
    @ResponseBody
    public JSONObject getNodeOneStepDetail(@RequestBody JSONObject requestBody) {
        long pkgId = requestBody.getLong("id");
        return couplingService.getChildPkgsCouplingValue(pkgId);
    }
    @PostMapping("/group/one_step_child_packages_no_layout")
    @CrossOrigin
    @ResponseBody
    public JSONObject getOneStepChildPackagesCouplingValueNoLayout(@RequestBody JSONObject requestBody){
//        JSONArray parentPcks = new JSONArray();
        Map<Long, Double> parentPcksInstability = new HashMap<>();
        Map<Package, List<Package>> pckMap = new HashMap<>();

        JSONArray otherPcks = requestBody.getJSONArray("otherPcks");
        JSONArray unfoldPcks = requestBody.getJSONArray("unfoldPcks");

        List<Long> otherPckIds = new ArrayList<>();
        List<Long> unfoldPckIds = new ArrayList<>();
        for(int i = 0; i < otherPcks.size(); i++){
            otherPckIds.add(otherPcks.getJSONObject(i).getLong("id"));
            parentPcksInstability.put(otherPcks.getJSONObject(i).getLong("id"),
                    otherPcks.getJSONObject(i).getDouble("instability"));
        }

        for(int i = 0; i < unfoldPcks.size(); i++){
            unfoldPckIds.add(unfoldPcks.getJSONObject(i).getLong("id"));
            parentPcksInstability.put(unfoldPcks.getJSONObject(i).getLong("id"),
                    unfoldPcks.getJSONObject(i).getDouble("instability"));
        }

        for(Long pckId: otherPckIds){
            Package pck = packageRepository.findPackageById(pckId);
            JSONObject parentPckJson = new JSONObject();
            parentPckJson.put("id", pck.getId().toString());
            parentPckJson.put("directoryPath", pck.getDirectoryPath());
            parentPckJson.put("name", pck.getName());
            parentPckJson.put("label", pck.getName());
//            parentPcks.add(parentPckJson);

            List<Package> tmpList = new ArrayList<>();
            tmpList.add(pck);
            pckMap.put(pck, tmpList);
        }

        for(Long pckId: unfoldPckIds){
            Package parentPackage = packageRepository.findPackageById(pckId);
            JSONObject parentPckJson = new JSONObject();
            parentPckJson.put("id", parentPackage.getId().toString());
            parentPckJson.put("directoryPath", parentPackage.getDirectoryPath());
            parentPckJson.put("name", parentPackage.getName());
//            parentPcks.add(parentPckJson);

            List<Package> pckList = new ArrayList<>(packageRepository.findOneStepPackagesById(pckId));

            if(pckList.size() == 0){
                JSONObject failJson = new JSONObject();
                failJson.put("code", -1);
                failJson.put("pck", parentPckJson);
                return failJson;
            }
            if(packageRepository.findIfPackageContainFiles(parentPackage.getId())) pckList.add(parentPackage);
            pckMap.put(parentPackage, pckList);
        }

        JSONObject result = couplingService.getCouplingValueByPcks(pckMap, parentPcksInstability, false, false);
        result.put("code", 200);
        return result;
    }
    /**
     * 返回顶层模块的耦合值
     * @param
     * @return
     */
    @GetMapping("/group/top_level_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getTopLevelPackagesCouplingValue(){
        List<Package> topLevelPackages = packageRepository.findPackagesAtDepth1();
        JSONArray otherPkgs = new JSONArray();
        for (Package pkg : topLevelPackages) {
            JSONObject rootPackage = new JSONObject();
            rootPackage.put("level", "0");
            rootPackage.put("id", pkg.getId());
            otherPkgs.add(rootPackage);
        }
//        Map<Package, List<Package>> pckMap = new HashMap<>();
//        Map<Long, Double> parentPcksInstability = new HashMap<>();
//
//        pckMap.put(containRepository.findPackageInPackage(topLevelPackages.get(0).getId()), topLevelPackages);
//
//        JSONObject result = couplingService.getCouplingValueByPcks(pckMap, parentPcksInstability, true, true);


        JSONObject result = couplingService.getTopLevelPackages();
        result.put("code", 200);

        return result;
    }

    @PostMapping("/group/files_of_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getPackagesCouplingValue(@RequestBody JSONObject requestBody){
        JSONArray fileIdsArray = requestBody.getJSONArray("pckIds");
        Map<Long, Long> parentPckMap = new HashMap<>();

        List<Long> pckIds = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for(int i = 0; i < fileIdsArray.size(); i++){
            pckIds.add(fileIdsArray.getJSONObject(i).getLong("id"));
        }

        for(Long pckId: pckIds){
            List<ProjectFile> fileList = containRepository.findPackageContainAllFiles(pckId);
            for(ProjectFile projectFile: fileList){
                parentPckMap.put(projectFile.getId(), pckId);
                fileIds.add(projectFile.getId());
            }
        }
        return couplingService.getCouplingValueByFileIds(fileIds, parentPckMap);
    }
}
