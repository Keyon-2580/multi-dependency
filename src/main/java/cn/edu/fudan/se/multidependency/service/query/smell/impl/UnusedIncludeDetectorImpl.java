package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.repository.smell.UnusedIncludeASRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.UnusedIncludeDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UnusedIncludeDetectorImpl implements UnusedIncludeDetector {

    @Autowired
    private UnusedIncludeASRepository unusedIncludeASRepository;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private SmellRepository smellRepository;

    @Override
    public Map<Long, List<UnusedInclude>> detectUnusedInclude() {
        Map<Long, List<UnusedInclude>> result = new HashMap<>();
        Set<ProjectFile> files = unusedIncludeASRepository.findFileWithUnusedInclude();
        for (ProjectFile file : files) {
            Project project = containRelationService.findFileBelongToProject(file);
            if (project != null) {
                List<UnusedInclude> unusedIncludeList = result.getOrDefault(project.getId(), new ArrayList<>());
                unusedIncludeList.add(new UnusedInclude(file, unusedIncludeASRepository.findUnusedIncludeByFileId(file.getId())));
                result.put(project.getId(), unusedIncludeList);
            }
        }
        return result;
    }

    @Override
    public Map<Long, List<UnusedInclude>> getUnusedIncludeFromSmell() {
        Map<Long, List<UnusedInclude>> result = new HashMap<>();
        List<Smell> smells = smellRepository.findSmellsByType(SmellType.UNUSED_INCLUDE);
        if (smells != null) {
            for (Smell smell : smells) {
                long projectId = smell.getProjectId();
                List<UnusedInclude> unusedIncludeList = result.getOrDefault(projectId, new ArrayList<>());
                unusedIncludeList.add(smellRepository.getUnusedIncludeBySmellId(smell.getId()));
                result.put(projectId, unusedIncludeList);
            }
        }
        for (Map.Entry<Long, List<UnusedInclude>> entry : result.entrySet()) {
            long projectId = entry.getKey();
            List<UnusedInclude> unusedIncludeList = result.getOrDefault(projectId, new ArrayList<>());
            unusedIncludeList.sort((u1, u2) ->{
                return Integer.compare(u2.getUnusedIncludeFiles().size(), u1.getUnusedIncludeFiles().size());
            });
            result.put(projectId, unusedIncludeList);
        }
        return result;
    }
}
