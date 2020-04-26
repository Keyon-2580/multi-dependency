package cn.edu.fudan.se.multidependency.service.spring;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.repository.node.code.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.DeveloperSubmitCommitRepository;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

@Service
public class GitAnalyseService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CommitRepository commitRepository;

    @Autowired
    private CommitUpdateFileRepository commitUpdateFileRepository;

    @Autowired
    private DeveloperSubmitCommitRepository developerSubmitCommitRepository;

    public Map<String, Map<String, Integer>> calCntOfDevUpdPro() {
        Map<String, Map<String, Integer>> cntOfDevUpdPro = new HashMap<>();

        for(Project project : projectRepository.findAll()) {
            cntOfDevUpdPro.put(project.getName(),new HashMap<>());
        }
        for(Commit commit : commitRepository.findAll()) {
            List<ProjectFile> files = commitUpdateFileRepository.findUpdatedFilesByCommitId(commit.getId());
            Developer developer = developerSubmitCommitRepository.findDeveloperByCommitId(commit.getId());
            Set<String> updProjectNames = new HashSet<>();
            for(ProjectFile file : files) {
                String projectName = FileUtil.extractProjectNameFromFile(file.getPath());
                if(cntOfDevUpdPro.containsKey(projectName)) {
                    updProjectNames.add(projectName);
                }
            }
            for(String projectName : updProjectNames) {
                Map<String, Integer> map = cntOfDevUpdPro.get(projectName);
                map.put(developer.getName(), map.getOrDefault(developer.getName(),0)+1);
            }
        }
        return cntOfDevUpdPro;
    }

    public JSONArray cntOfDevUpdProToTreeView() {
        JSONArray result = new JSONArray();
        for(Map.Entry<String, Map<String, Integer>> project : calCntOfDevUpdPro().entrySet()){
            JSONObject projectJson = new JSONObject();
            projectJson.put("project", project.getKey());

            JSONArray developers = new JSONArray();
            for(Map.Entry<String, Integer> developer : project.getValue().entrySet()) {
                JSONObject developerJson = new JSONObject();
                developerJson.put("update_count", developer.getValue());
                developerJson.put("name", developer.getKey());
                developers.add(developerJson);
            }
            projectJson.put("developer", developers);
            result.add(projectJson);
        }
        return result;
    }
}
