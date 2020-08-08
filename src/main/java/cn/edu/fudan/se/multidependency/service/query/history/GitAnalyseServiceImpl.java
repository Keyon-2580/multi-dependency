package cn.edu.fudan.se.multidependency.service.query.history;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.config.PropertyConfig;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.DeveloperSubmitCommitRepository;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

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
    
    @Autowired
    private CoChangeRepository cochangeRepository;

    Iterable<Commit> allCommitsCache = null;

    Map<Developer, Map<Project, Integer>> cntOfDevUpdProCache = null;

    Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsListCache = null;

    Map<ProjectFile, Integer> cntOfFileBeUpdtdCache = null;
    
//    @Autowired
//    private PropertyConfig propertyConfig;

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
        Map<MicroService, DeveloperUpdateNode<MicroService>> result = new HashMap<>();
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
                if (!result.containsKey(microService)) {
                    DeveloperUpdateNode<MicroService> temp = new DeveloperUpdateNode<>();
                    temp.setDeveloper(d);
                    temp.setNode(microService);
                    temp.setTimes(times);
                    result.put(microService, temp);
                } else {
                    DeveloperUpdateNode<MicroService> temp = result.get(microService);
                    temp.setTimes(temp.getTimes() + times);
                }
            }
        }
        cntOfDevUpdMsListCache = new ArrayList<>(result.values());
        return cntOfDevUpdMsListCache;
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
    
	@Bean("createCoChanges")
	public List<CoChange> createCoChanges(PropertyConfig propertyConfig, CoChangeRepository cochangeRepository) {
		if(propertyConfig.isCalculateCoChange()) {
			System.out.println("创建cochange关系");
			cochangeRepository.deleteAll();
			return cochangeRepository.createCoChanges();
		}
		return new ArrayList<>();
	}

    private Map<ProjectFile, Map<ProjectFile, CoChange>> cntOfFileCoChangeCache = new ConcurrentHashMap<>();
    @Resource(name="createCoChanges")
    private List<CoChange> allCoChangeCache = null;
	@Override
	public synchronized Collection<CoChange> calCntOfFileCoChange() {
		if(allCoChangeCache != null && !allCoChangeCache.isEmpty()) {
			return allCoChangeCache;
		}
		allCoChangeCache = cochangeRepository.findGreaterThanCountCoChanges(1);
		allCoChangeCache.sort((c1, c2) -> {
			return c2.getTimes() - c1.getTimes();
		});
		for(CoChange cochange : allCoChangeCache) {
			ProjectFile file1 = cochange.getFile1();
			ProjectFile file2 = cochange.getFile2();
			Map<ProjectFile, CoChange> ccs = cntOfFileCoChangeCache.getOrDefault(file1, new HashMap<>());
			ccs.put(file2, cochange);
			cntOfFileCoChangeCache.put(file1, ccs);
		}
		return allCoChangeCache;
	}

	@Override
	public Collection<CoChange> getTopKFileCoChange(int k) {
		calCntOfFileCoChange();
		return allCoChangeCache.subList(0, k);
	}

	@Override
	public CoChange findCoChangeBetweenTwoFiles(ProjectFile file1, ProjectFile file2) {
		Map<ProjectFile, CoChange> ccs = cntOfFileCoChangeCache.getOrDefault(file1, new HashMap<>());
		CoChange result = ccs.get(file2);
		if(result == null) {
			ccs = cntOfFileCoChangeCache.getOrDefault(file2, new HashMap<>());
			result = ccs.get(file1);
		}
		if(result == null) {
			result = cochangeRepository.findCoChangesBetweenTwoFiles(file1.getId(), file2.getId());
			if(result != null) {
				ccs = cntOfFileCoChangeCache.getOrDefault(file1, new HashMap<>());
				ccs.put(file2, result);
				cntOfFileCoChangeCache.put(file1, ccs);
			}
		}
		return result;
	}

	@Override
	public Collection<Commit> findCommitsByCoChange(CoChange cochange) {
		ProjectFile file1 = cochange.getFile1();
		ProjectFile file2 = cochange.getFile2();
		List<Commit> result = commitRepository.findCommitsInTwoFiles(file1.getId(), file2.getId());
		if(result.isEmpty()) {
			result = commitRepository.findCommitsInTwoFiles(file2.getId(), file1.getId());
		}
		result.sort((c1, c2) -> {
	        Timestamp time1 = Timestamp.valueOf(c1.getAuthoredDate());
	        Timestamp time2 = Timestamp.valueOf(c2.getAuthoredDate());
			return time2.compareTo(time1);
		});
		return result;
	}

	@Override
	public CoChange findCoChangeById(long cochangeId) {
		return cochangeRepository.findById(cochangeId).get();
	}

	@Override
	public Collection<Commit> findCommitsInProject(Project project) {
		return commitRepository.findCommitsInProject(project.getId());
	}
    
}