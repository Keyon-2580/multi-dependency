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

    @GetMapping("/group/package")
    @CrossOrigin
    @ResponseBody
    public JSONObject getPackageCouplingValue(@RequestParam("pckId") Long pckId){
        List<ProjectFile> fileList = containRepository.findPackageContainAllFiles(pckId);
        List<Long> fileIds = new ArrayList<>();

        for(ProjectFile projectFile: fileList){
            fileIds.add(projectFile.getId());
        }

        return couplingService.getCouplingValueByFileIds(fileIds);
    }
}
