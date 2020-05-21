package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
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
public class GitAnalyseServiceImpl implements GitAnalyseService {

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

    Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsListCache = null;

    Map<ProjectFile, Integer> cntOfFileBeUpdtdCache = null;

    Map<ProjectFile, Map<ProjectFile, Integer>> cntOfFileCoChangeCache = null;

    @Override
    public Iterable<Commit> findAllCommits() {
        if (allCommitsCache == null) {
            allCommitsCache = commitRepository.findAll();
        }
        return allCommitsCache;
    }

    @Override
    public Map<Developer, Map<Project, Integer>> calCntOfDevUpdPro() {
        if (cntOfDevUpdProCache == null) {
            cntOfDevUpdProCache = new HashMap<>();
        } else {
            return cntOfDevUpdProCache;
        }
        for (Commit commit : findAllCommits()) {
            List<ProjectFile> files = commitUpdateFileRepository.findUpdatedFilesByCommitId(commit.getId());
            Developer developer = developerSubmitCommitRepository.findDeveloperByCommitId(commit.getId());
            if (!cntOfDevUpdProCache.containsKey(developer)) {
                cntOfDevUpdProCache.put(developer, new HashMap<>());
            }
            Set<Project> updProjects = new HashSet<>();
            for (ProjectFile file : files) {
            	Project project = containRelationService.findFileBelongToProject(file);
                if (project != null) {
                    updProjects.add(project);
                }
            }
            for (Project project : updProjects) {
                Map<Project, Integer> map = cntOfDevUpdProCache.get(developer);
                map.put(project, map.getOrDefault(project, 0) + 1);
            }
        }
        return cntOfDevUpdProCache;
    }

    @Override
    public Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsList() {
        if (cntOfDevUpdMsListCache != null) {
            return cntOfDevUpdMsListCache;
        }
        List<DeveloperUpdateNode<MicroService>> result = new ArrayList<>();
        for (Map.Entry<Developer, Map<Project, Integer>> developer : calCntOfDevUpdPro().entrySet()) {
            List<Map.Entry<Project, Integer>> list = new ArrayList<>(developer.getValue().entrySet());
            list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            int cnt = 0;
            int k = 5;
            for (Map.Entry<Project, Integer> project : list) {
                if (cnt++ == k) break;
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

    @Override
    public Map<ProjectFile, Integer> calCntOfFileBeUpd() {
        if (cntOfFileBeUpdtdCache != null) {
            return cntOfFileBeUpdtdCache;
        }
        Map<ProjectFile, Integer> result = new HashMap<>();
        for (Commit commit : findAllCommits()) {
            List<ProjectFile> files = commitUpdateFileRepository.findUpdatedFilesByCommitId(commit.getId());
            for (ProjectFile file : files) {
                result.put(file, result.getOrDefault(file, 0) + 1);
            }
        }
        cntOfFileBeUpdtdCache = result;
        return result;
    }

    @Override
    public Map<ProjectFile, Integer> getTopKFileBeUpd(int k) {
        if (cntOfFileBeUpdtdCache == null) {
            cntOfFileBeUpdtdCache = calCntOfFileBeUpd();
        }
        Queue<ProjectFile> files = new PriorityQueue<>(Comparator.comparingInt(o -> cntOfFileBeUpdtdCache.get(o)));
        for (ProjectFile file : cntOfFileBeUpdtdCache.keySet()) {
            files.offer(file);
            if (files.size() > k) files.poll();
        }
        Map<ProjectFile, Integer> result = new HashMap<>();
        for (ProjectFile file : files) {
            result.put(file, cntOfFileBeUpdtdCache.get(file));
        }
        return result;
    }

    @Override
    public Map<ProjectFile, Map<ProjectFile, Integer>> calCntOfFileCoChange() {
        if (cntOfFileCoChangeCache != null) {
            return cntOfFileCoChangeCache;
        }
        Map<ProjectFile, Map<ProjectFile, Integer>> result = new HashMap<>();
        for (Commit commit : findAllCommits()) {
            List<ProjectFile> files = commitUpdateFileRepository.findUpdatedFilesByCommitId(commit.getId());
            int num = files.size();
            for (int i = 0; i < num; i++) {
                ProjectFile from = files.get(i);
                if (!result.containsKey(from)) {
                    result.put(from, new HashMap<>());
                }
                Map<ProjectFile, Integer> m = result.get(from);
                for (int j = i + 1; j < num; j++) {
                    ProjectFile to = files.get(j);
                    m.put(to, m.getOrDefault(to, 0)+1);
                }
            }
        }
        cntOfFileCoChangeCache = result;
        return result;
    }

    @Override
    public Map<ProjectFile, Map<ProjectFile, Integer>> getTopKFileCoChange(int k) {
        if (cntOfFileCoChangeCache == null) {
            cntOfFileCoChangeCache = calCntOfFileCoChange();
        }
        Queue<ProjectFile[]> filePairs = new PriorityQueue<>(
                Comparator.comparingInt(o -> cntOfFileCoChangeCache.get(o[0]).get(o[1])));
        for (ProjectFile from : cntOfFileCoChangeCache.keySet()) {
            for (ProjectFile to : cntOfFileCoChangeCache.get(from).keySet()) {
                filePairs.offer(new ProjectFile[]{from, to});
                if (filePairs.size() > k) filePairs.poll();
            }
        }
        Map<ProjectFile, Map<ProjectFile, Integer>> result = new HashMap<>();
        for (ProjectFile[] filePair : filePairs) {
            ProjectFile from = filePair[0];
            ProjectFile to = filePair[1];
            if (!result.containsKey(from)) {
                result.put(from, new HashMap<>());
            }
            result.get(from).put(to, cntOfFileCoChangeCache.get(from).get(to));
        }
        return result;
    }
}
