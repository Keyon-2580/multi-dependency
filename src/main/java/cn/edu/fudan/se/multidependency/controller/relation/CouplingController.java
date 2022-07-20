package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
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

    @GetMapping("/group/all_child_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getAllChildPackagesCouplingValue(@RequestParam("pckId") Long pckId){
        Package parentPackage = packageRepository.findPackageById(pckId);
        JSONObject parentPckJson = new JSONObject();
        parentPckJson.put("id", parentPackage.getId().toString());
        parentPckJson.put("directoryPath", parentPackage.getDirectoryPath());
        parentPckJson.put("name", parentPackage.getName());

        List<Package> pckList = packageRepository.findAllChildPackagesById(pckId);
        JSONObject result = couplingService.getCouplingValueByPcks(pckList, false);
        result.put("parentPackage", parentPckJson);

        return result;
    }

    @GetMapping("/group/one_step_child_packages")
    @CrossOrigin
    @ResponseBody
    public JSONObject getOneStepChildPackagesCouplingValue(@RequestParam("pckId") Long pckId){
        Package parentPackage = packageRepository.findPackageById(pckId);
        JSONObject parentPckJson = new JSONObject();
        parentPckJson.put("id", parentPackage.getId().toString());
        parentPckJson.put("directoryPath", parentPackage.getDirectoryPath());
        parentPckJson.put("name", parentPackage.getName());

        List<Package> pckList = packageRepository.findOneStepPackagesById(pckId);
        JSONObject result = couplingService.getCouplingValueByPcks(pckList, true);
        result.put("parentPackage", parentPckJson);

        return result;
    }

    @PostMapping("/group/files_of_packages")
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
