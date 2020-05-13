package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.DeveloperSubmitCommitRepository;

@Service
public class GitAnalyseService {

    @Autowired
    private CommitRepository commitRepository;

    @Autowired
    private CommitUpdateFileRepository commitUpdateFileRepository;

    @Autowired
    private DeveloperSubmitCommitRepository developerSubmitCommitRepository;

    @Autowired
    private ContainRelationService containRelationService;
    
    
    Iterable<Commit> allCommitsCache = null;

    Map<Developer, Map<Project, Integer>> cntOfDevUpdProCache = null;

    public Iterable<Commit> findAllCommits() {
    	if(allCommitsCache == null) {
    		allCommitsCache = commitRepository.findAll();
    	}
    	return allCommitsCache;
    }

    public Map<Developer, Map<Project, Integer>> calCntOfDevUpdPro() {
        if(cntOfDevUpdProCache == null) {
            cntOfDevUpdProCache = new HashMap<>();
        }else {
            return cntOfDevUpdProCache;
        }
        for(Commit commit : findAllCommits()) {
            List<ProjectFile> files = commitUpdateFileRepository.findUpdatedFilesByCommitId(commit.getId());
            Developer developer = developerSubmitCommitRepository.findDeveloperByCommitId(commit.getId());
            if(!cntOfDevUpdProCache.containsKey(developer)) {
                cntOfDevUpdProCache.put(developer,new HashMap<>());
            }
            Set<Project> updProjects = new HashSet<>();
            for(ProjectFile file : files) {
//                String projectName = FileUtil.extractProjectNameFromFile(file.getPath());
//                Project project = projectRepository.findProjectByProjectName(projectName);
            	Project project = containRelationService.findFileBelongToProject(file);
                if(project != null) {
                    updProjects.add(project);
                }
            }
            for(Project project : updProjects) {
                Map<Project, Integer> map = cntOfDevUpdProCache.get(developer);
                map.put(project, map.getOrDefault(project,0)+1);
            }
        }
        return cntOfDevUpdProCache;
    }
    
    Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsListCache = null;

    public Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsList() {
    	if(cntOfDevUpdMsListCache != null) {
    		return cntOfDevUpdMsListCache;
    	}
    	
    	List<DeveloperUpdateNode<MicroService>> result = new ArrayList<>();
    	
    	for(Map.Entry<Developer, Map<Project, Integer>> developer : calCntOfDevUpdPro().entrySet()){
//            int totalCnt = 0;
//            for(Map.Entry<Project, Integer> project : developer.getValue().entrySet()) {
//                totalCnt += project.getValue();
//            }
            List<Map.Entry<Project, Integer>> list = new ArrayList<>(developer.getValue().entrySet());
            list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            int cnt = 0;
            int k = 5;
            for(Map.Entry<Project, Integer> project : list) {
//                if(!(project.getValue() >= totalCnt/3 || project.getValue() >= 5)) continue;
                if(cnt++ == k) break;
            	Developer d = developer.getKey();
            	int times = project.getValue();
                MicroService microService = containRelationService.findProjectBelongToMicroService(project.getKey());
            	DeveloperUpdateNode<MicroService> temp = new DeveloperUpdateNode<>();
            	temp.setDeveloper(d);
            	temp.setNode(microService);
            	temp.setTimes(times);
            	result.add(temp);
            }
        }
    	
    	cntOfDevUpdMsListCache = result;
    	return result;
    }
}
