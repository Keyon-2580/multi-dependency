package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.repository.smell.UnusedIncludeASRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.UnusedIncludeDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

    @Autowired
    private ProjectFileRepository projectFileRepository;

    @Override
    public Map<Long, List<UnusedInclude>> detectUnusedInclude() {
        Map<Long, List<UnusedInclude>> result = new HashMap<>();
        Set<UnusedInclude> headFileUnusedIncludeSet = new HashSet<>();
        Set<UnusedInclude> codeFileUnusedIncludeSet = new HashSet<>();
        headFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".h"));
        headFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".hpp"));
        codeFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".c"));
        codeFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".cc"));
        codeFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".cpp"));
        for (UnusedInclude headFileUnusedInclude : headFileUnusedIncludeSet) {
            ProjectFile headFile = headFileUnusedInclude.getCoreFile();
            //无后缀名的头文件名
            String headFileNameWithoutSuffix = headFile.getName().substring(0, headFile.getName().length() - headFile.getSuffix().length());
            Set<ProjectFile> allFiles = new HashSet<>(unusedIncludeASRepository.findFileByHeadFileId(headFile.getId()));
            Set<ProjectFile> codeFiles = new HashSet<>();
            //获取当前头文件对应的源文件，条件：名字相同、引用该同文件、后缀为.c/.cc/.cpp
            for (ProjectFile file : allFiles) {
                String codeFileNameWithoutSuffix = file.getName().substring(0, file.getName().length() - file.getSuffix().length());
                if (headFileNameWithoutSuffix.equals(codeFileNameWithoutSuffix) && (file.getSuffix().equals(".c") || file.getSuffix().equals(".cc") || file.getSuffix().equals(".cpp"))) {
                    codeFiles.add(file);
                }
            }
            if (!codeFiles.isEmpty()) {
                //任取一个元素做为该头文件的对应源文件
                ProjectFile codeFile = codeFiles.iterator().next();
                Set<ProjectFile> headFileUnusedIncludeFileSet = new HashSet<>(headFileUnusedInclude.getUnusedIncludeFiles());
                Set<ProjectFile> codeFileUnusedIncludeFileSet = new HashSet<>();
                //获取该头文件无效引用中在源文件依旧无效的引用
                for (ProjectFile headFileUnusedIncludeFile : headFileUnusedIncludeFileSet) {
                    if (!unusedIncludeASRepository.isUsedInclude(codeFile.getId(), headFileUnusedIncludeFile.getId())) {
                        codeFileUnusedIncludeFileSet.add(headFileUnusedIncludeFile);
                    }
                }
                //更新最终结果，若没有该源文件则创建实例
                for (UnusedInclude codeUnusedInclude : codeFileUnusedIncludeSet) {
                    if (codeFile.getId().equals(codeUnusedInclude.getCoreFile().getId())) {
                        codeFileUnusedIncludeFileSet.addAll(codeUnusedInclude.getUnusedIncludeFiles());
                        codeFileUnusedIncludeSet.remove(codeUnusedInclude);
                        break;
                    }
                }
                codeFileUnusedIncludeSet.add(new UnusedInclude(codeFile, codeFileUnusedIncludeFileSet));
            }
        }
        for (UnusedInclude codeUnusedInclude : codeFileUnusedIncludeSet) {
            Project project = containRelationService.findFileBelongToProject(codeUnusedInclude.getCoreFile());
            if (project != null) {
                List<UnusedInclude> unusedIncludeList = result.getOrDefault(project.getId(), new ArrayList<>());
                unusedIncludeList.add(codeUnusedInclude);
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
                unusedIncludeList.add(smellRepository.getUnusedIncludeWithSmellId(smell.getId()));
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

    @Override
    public JSONObject getUnusedIncludeJson(Long fileId) {
        JSONObject result = new JSONObject();
        JSONArray nodesJson = new JSONArray();
        JSONArray edgesJson = new JSONArray();
        JSONArray smellsJson = new JSONArray();
        ProjectFile coreFile = projectFileRepository.findFileById(fileId);
        Smell smell = smellRepository.getSmellWithCoreFileId(fileId);
        List<ProjectFile> files = new ArrayList<>();
        files.add(coreFile);
        if (coreFile != null && smell != null) {
            JSONObject smellJson = new JSONObject();
            smellJson.put("name", smell.getName());
            List<ProjectFile> smellFiles = new ArrayList<>(smellRepository.getFilesWithSmellName(smell.getName()));
            files.addAll(smellFiles);
            JSONArray smellFilesJson = new JSONArray();
            for (ProjectFile smellFile : smellFiles) {
                if (!files.contains(smellFile)) {
                    files.add(smellFile);
                }
                JSONObject smellFileJson = new JSONObject();
                smellFileJson.put("index", files.indexOf(smellFile) + 1);
                smellFileJson.put("path", smellFile.getPath());
                smellFilesJson.add(smellFileJson);
            }
            smellJson.put("files", smellFilesJson);
            smellJson.put("coreFilePath", smell.getCoreNodePath());
            smellsJson.add(smellJson);
            int length = files.size();
            for (int i = 0; i < length; i ++) {
                ProjectFile file = files.get(i);
                JSONObject nodeJson = new JSONObject();
                nodeJson.put("id", file.getId().toString());
                nodeJson.put("name", file.getName());
                nodeJson.put("path", file.getPath());
                nodeJson.put("label", i + 1);
                nodeJson.put("size", getSizeOfFileByLoc(file.getLoc()));
                nodesJson.add(nodeJson);
                if (i > 0) {
                    JSONObject edgeJson = new JSONObject();
                    edgeJson.put("id", i);
                    edgeJson.put("source", coreFile.getId().toString());
                    edgeJson.put("target", file.getId().toString());
                    edgeJson.put("source_name", coreFile.getName());
                    edgeJson.put("target_name", file.getName());
                    edgeJson.put("source_label", 1);
                    edgeJson.put("target_label", i + 1);
                    edgesJson.add(edgeJson);
                }
            }
        }
        result.put("smellType", SmellType.UNUSED_INCLUDE);
        result.put("currentFile", fileId.toString());
        result.put("nodes", nodesJson);
        result.put("edges", edgesJson);
        result.put("smells", smellsJson);
        return result;
    }

    private int getSizeOfFileByLoc(int loc) {
        int size;
        if (loc <= 500) {
            size = 40;
        }
        else if (loc <= 1000) {
            size = 50;
        }
        else if (loc <= 2000) {
            size = 60;
        }
        else {
            size = 70;
        }
        return size;
    }
}
