package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.repository.node.code.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.DeveloperSubmitCommitRepository;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

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

    @Autowired
    private MicroserviceService microserviceService;
    
    
    Iterable<Commit> allCommitsCache = null;
    public Iterable<Commit> findAllCommits() {
    	if(allCommitsCache == null) {
    		allCommitsCache = commitRepository.findAll();
    	}
    	return allCommitsCache;
    }

    public Map<Project, Map<Developer, Integer>> calCntOfDevUpdPro() {
        Map<Project, Map<Developer, Integer>> cntOfDevUpdPro = new HashMap<>();

        for(Commit commit : findAllCommits()) {
            List<ProjectFile> files = commitUpdateFileRepository.findUpdatedFilesByCommitId(commit.getId());
            Developer developer = developerSubmitCommitRepository.findDeveloperByCommitId(commit.getId());
            Set<Project> updProjects = new HashSet<>();
            for(ProjectFile file : files) {
                String projectName = FileUtil.extractProjectNameFromFile(file.getPath());
                Project project = projectRepository.findProjectByProjectName(projectName);
                if(project != null) {
                    updProjects.add(project);
                    if(!cntOfDevUpdPro.containsKey(project)) {
                        cntOfDevUpdPro.put(project,new HashMap<>());
                    }
                }
            }
            for(Project project : updProjects) {
                Map<Developer, Integer> map = cntOfDevUpdPro.get(project);
                map.put(developer, map.getOrDefault(developer,0)+1);
            }
        }
        return cntOfDevUpdPro;
    }

    public JSONArray cntOfDevUpdPro() {
        JSONArray result = new JSONArray();
        for(Map.Entry<Project, Map<Developer, Integer>> project : calCntOfDevUpdPro().entrySet()){
            JSONObject projectJson = new JSONObject();
            projectJson.put("project", project.getKey());

            JSONArray developers = new JSONArray();
            for(Map.Entry<Developer, Integer> developer : project.getValue().entrySet()) {
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
    
    public Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsList() {
    	List<DeveloperUpdateNode<MicroService>> result = new ArrayList<>();
    	
    	for(Map.Entry<Project, Map<Developer, Integer>> project : calCntOfDevUpdPro().entrySet()){
            MicroService microService = microserviceService.findProjectBelongToMicroService(project.getKey());

            for(Map.Entry<Developer, Integer> developer : project.getValue().entrySet()) {
            	Developer d = developer.getKey();
            	int times = developer.getValue();
            	DeveloperUpdateNode<MicroService> temp = new DeveloperUpdateNode<>();
            	temp.setDeveloper(d);
            	temp.setNode(microService);
            	temp.setTimes(times);
            	result.add(temp);
            }
            
        }
    	
    	return result;
    }

    public JSONArray cntOfDevUpdMs() {
        JSONArray result = new JSONArray();
        for(Map.Entry<Project, Map<Developer, Integer>> project : calCntOfDevUpdPro().entrySet()){
            MicroService microService = microserviceService.findProjectBelongToMicroService(project.getKey());
            JSONObject microServiceJson = new JSONObject();
            microServiceJson.put("micro_service", microService);

            JSONArray developers = new JSONArray();
            for(Map.Entry<Developer, Integer> developer : project.getValue().entrySet()) {
                JSONObject developerJson = new JSONObject();
                developerJson.put("update_count", developer.getValue());
                developerJson.put("name", developer.getKey());
                developers.add(developerJson);
            }
            microServiceJson.put("developer", developers);
            result.add(microServiceJson);
        }
        return result;
    }
}
