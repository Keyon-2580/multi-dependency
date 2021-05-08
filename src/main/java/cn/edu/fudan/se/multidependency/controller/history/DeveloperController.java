package cn.edu.fudan.se.multidependency.controller.history;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.DeveloperRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/developer")
public class DeveloperController {

    @Autowired
    private ContainRepository containRepository;

    @Autowired
    private CommitRepository commitRepository;

    @Autowired
    private DeveloperRepository developerRepository;

    @GetMapping("/developers")
    @ResponseBody
    public JSONObject getAllDevelopers(){
        List<Developer> developers = developerRepository.queryAllDevelopers();
        List<Integer> commitTimes = new ArrayList<>();
        for(Developer developer : developers){
            commitTimes.add(developerRepository.queryCommitByDeveloper(developer.getId()).size());
        }
        JSONObject value = new JSONObject();
        value.put("developer", developers);
        value.put("committime", commitTimes);
        return value;
    }

    @GetMapping("/detail")
    public String getCommitsDetail(HttpServletRequest request, @RequestParam("developerId") long developerId){
        List<Commit> commits = developerRepository.queryCommitByDeveloper(developerId);
        request.setAttribute("commits", commits);
        return "history/commits";
    }

    @GetMapping("/packages")
    public String getPakcageChanged(HttpServletRequest request, @RequestParam("developerId") long developerId){
        List<Commit> commits = developerRepository.queryCommitByDeveloper(developerId);
        List<Package> packageSet = new ArrayList<>();
        List<Integer> timeSet = new ArrayList<>();
        List<DeveloperUpdateNode<Package>> packageChangedTime = new ArrayList<>();
        for(Commit commit : commits){
            List<Package> pcks = commitRepository.queryUpdatedPackageByCommitId(commit.getId());
            for(Package pck : pcks){
                if(!packageSet.contains(pck)){
                    packageSet.add(pck);
                    timeSet.add(1);
                    continue;
                }
                timeSet.set(packageSet.indexOf(pck), timeSet.get(packageSet.indexOf(pck)) + 1);
            }
        }
        for(int i = 0;i < packageSet.size(); i++){
            packageChangedTime.add(new DeveloperUpdateNode<>(packageSet.get(i), timeSet.get(i)));
        }
        request.setAttribute("packagetimes", packageChangedTime);
        return "relation/packages";
    }
}
