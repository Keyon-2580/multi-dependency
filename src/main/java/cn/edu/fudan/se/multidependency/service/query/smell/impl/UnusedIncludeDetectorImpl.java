package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.repository.smell.UnusedIncludeASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.smell.UnusedIncludeDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

@Service
public class UnusedIncludeDetectorImpl implements UnusedIncludeDetector {

    @Autowired
    private CacheService cache;

    @Autowired
    private UnusedIncludeASRepository unusedIncludeASRepository;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private SmellRepository smellRepository;

    @Autowired
    private ProjectFileRepository projectFileRepository;

    @Override
    public Map<Long, List<UnusedInclude>> queryFileUnusedInclude() {
        String key = "fileUnusedInclude";
        if (cache.get(getClass(), key) != null) {
            return cache.get(getClass(), key);
        }

        Map<Long, List<UnusedInclude>> result = new HashMap<>();
        List<Smell> smells = new ArrayList<>(smellRepository.findSmellsByType(SmellType.UNUSED_INCLUDE));
        SmellUtils.sortSmellByName(smells);
        for (Smell smell : smells) {
            long projectId = smell.getProjectId();
            List<UnusedInclude> unusedIncludeList = result.getOrDefault(projectId, new ArrayList<>());
            unusedIncludeList.add(smellRepository.getUnusedIncludeWithSmellId(smell.getId()));
            result.put(projectId, unusedIncludeList);
        }
        for (Map.Entry<Long, List<UnusedInclude>> entry : result.entrySet()) {
            long projectId = entry.getKey();
            List<UnusedInclude> unusedIncludeList = result.getOrDefault(projectId, new ArrayList<>());
            result.put(projectId, unusedIncludeList);
        }
        cache.cache(getClass(), key, result);
        return result;
    }

    @Override
    public Map<Long, List<UnusedInclude>> detectFileUnusedInclude() {
        Map<Long, List<UnusedInclude>> result = new HashMap<>();
//        Set<UnusedInclude> headFileUnusedIncludeSet = new HashSet<>();
        Set<UnusedInclude> codeFileUnusedIncludeSet = new HashSet<>();
//        headFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".h"));
//        headFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".hpp"));
        codeFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".c"));
        codeFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".cc"));
        codeFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".cpp"));
//        for (UnusedInclude headFileUnusedInclude : headFileUnusedIncludeSet) {
//            ProjectFile headFile = headFileUnusedInclude.getCoreFile();
//            //无后缀名的头文件名
//            String headFileNameWithoutSuffix = headFile.getName().substring(0, headFile.getName().length() - headFile.getSuffix().length());
//            Set<ProjectFile> allFiles = new HashSet<>(unusedIncludeASRepository.findFileByHeadFileId(headFile.getId()));
//            Set<ProjectFile> codeFiles = new HashSet<>();
//            //获取当前头文件对应的源文件，条件：名字相同、引用该同文件、后缀为.c/.cc/.cpp
//            for (ProjectFile file : allFiles) {
//                String codeFileNameWithoutSuffix = file.getName().substring(0, file.getName().length() - file.getSuffix().length());
//                if (headFileNameWithoutSuffix.equals(codeFileNameWithoutSuffix) && (file.getSuffix().equals(".c") || file.getSuffix().equals(".cc") || file.getSuffix().equals(".cpp"))) {
//                    codeFiles.add(file);
//                }
//            }
//            if (!codeFiles.isEmpty()) {
//                //任取一个元素做为该头文件的对应源文件，【待改】
//                ProjectFile codeFile = codeFiles.iterator().next();
//                Set<ProjectFile> headFileUnusedIncludeFileSet = new HashSet<>(headFileUnusedInclude.getUnusedIncludeFiles());
//                Set<ProjectFile> codeFileUnusedIncludeFileSet = new HashSet<>();
//                //获取该头文件无效引用中在源文件依旧无效的引用
//                for (ProjectFile headFileUnusedIncludeFile : headFileUnusedIncludeFileSet) {
//                    Boolean isUsedFile = unusedIncludeASRepository.isUsedFile(codeFile.getId(), headFileUnusedIncludeFile.getId());
//                    if (isUsedFile == null || !isUsedFile) {
//                        codeFileUnusedIncludeFileSet.add(headFileUnusedIncludeFile);
//                    }
//                }
//                //更新最终结果，若没有该源文件则创建实例
//                for (UnusedInclude codeUnusedInclude : codeFileUnusedIncludeSet) {
//                    if (codeFile.getId().equals(codeUnusedInclude.getCoreFile().getId())) {
//                        codeFileUnusedIncludeFileSet.addAll(codeUnusedInclude.getUnusedIncludeFiles());
//                        codeFileUnusedIncludeSet.remove(codeUnusedInclude);
//                        break;
//                    }
//                }
//                if (codeFileUnusedIncludeFileSet.size() > 0) {
//                    codeFileUnusedIncludeSet.add(new UnusedInclude(codeFile, codeFileUnusedIncludeFileSet));
//                }
//            }
//        }
        List<UnusedInclude> codeFileUnusedIncludeList = new ArrayList<>(codeFileUnusedIncludeSet);
        codeFileUnusedIncludeList.sort((u1, u2) -> Integer.compare(u2.getUnusedIncludeFiles().size(), u1.getUnusedIncludeFiles().size()));
        for (UnusedInclude codeUnusedInclude : codeFileUnusedIncludeList) {
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
    public JSONObject getFileUnusedIncludeJson(Long projectId, String smellName) {
        Smell smell = smellRepository.findProjectSmellsByName(projectId, smellName);
        return getFileUnusedIncludeJson(smell);
    }

    @Override
    public JSONObject getFileUnusedIncludeJson(Long smellId) {
        Smell smell = smellRepository.findSmell(smellId);
        return getFileUnusedIncludeJson(smell);
    }

    @Override
    public Boolean exportUnusedInclude(Project project) {
        Map<Long, List<UnusedInclude>> fileUnusedIncludeMap = queryFileUnusedInclude();
        List<UnusedInclude> fileUnusedIncludeList = fileUnusedIncludeMap.getOrDefault(project.getId(), new ArrayList<>());
        try {
            exportUnusedInclude(project, fileUnusedIncludeList);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void exportUnusedInclude(Project project, List<UnusedInclude> fileUnusedIncludeList) {
        Workbook workbook = new XSSFWorkbook();
        exportFileUnusedInclude(workbook, fileUnusedIncludeList);
        OutputStream outputStream = null;
        try {
            String fileName = SmellType.UNUSED_INCLUDE + "_" + project.getName() + "(" + project.getLanguage() + ")" + ".xlsx";
            outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void exportFileUnusedInclude(Workbook workbook, List<UnusedInclude> fileUnusedIncludeList) {
        Sheet sheet = workbook.createSheet(SmellLevel.FILE);
        ThreadLocal<Integer> rowKey = new ThreadLocal<>();
        rowKey.set(0);
        Row row = sheet.createRow(rowKey.get());
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Cell cell;
        cell = row.createCell(0);
        cell.setCellValue("Index");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue("CoreFile");
        cell.setCellStyle(style);
        cell = row.createCell(2);
        cell.setCellValue("Number");
        cell.setCellStyle(style);
        cell = row.createCell(3);
        cell.setCellValue("UnusedIncludeFiles");
        cell.setCellStyle(style);
        int startRow;
        int endRow;
        int index = 1;
        for (UnusedInclude fileUnusedInclude : fileUnusedIncludeList) {
            startRow = rowKey.get() + 1;
            ProjectFile coreFile = fileUnusedInclude.getCoreFile();
            Set<ProjectFile> unusedIncludeFiles = new HashSet<>(fileUnusedInclude.getUnusedIncludeFiles());
            for (ProjectFile unusedIncludeFile : unusedIncludeFiles) {
                rowKey.set(rowKey.get() + 1);
                row = sheet.createRow(rowKey.get());
                cell = row.createCell(0);
                cell.setCellValue(index);
                style.setAlignment(HorizontalAlignment.CENTER);
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                cell.setCellStyle(style);
                cell = row.createCell(1);
                cell.setCellValue(coreFile.getPath());
                style.setAlignment(HorizontalAlignment.LEFT);
                cell = row.createCell(2);
                cell.setCellValue(unusedIncludeFiles.size());
                cell.setCellStyle(style);
                cell = row.createCell(3);
                cell.setCellValue(unusedIncludeFile.getPath());
                style.setAlignment(HorizontalAlignment.LEFT);
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                cell.setCellStyle(style);
            }
            endRow = rowKey.get();
            if (endRow - startRow > 0) {
                CellRangeAddress indexRegion = new CellRangeAddress(startRow, endRow, 0, 0);
                sheet.addMergedRegion(indexRegion);
                CellRangeAddress coreFileRegion = new CellRangeAddress(startRow, endRow,  1, 1);
                sheet.addMergedRegion(coreFileRegion);
                CellRangeAddress numberRegion = new CellRangeAddress(startRow, endRow,  2, 2);
                sheet.addMergedRegion(numberRegion);
            }
            index ++;
        }
    }

    private JSONObject getFileUnusedIncludeJson(Smell smell) {
        JSONObject result = new JSONObject();
        JSONArray nodesJson = new JSONArray();
        JSONArray edgesJson = new JSONArray();
        JSONArray smellsJson = new JSONArray();
        List<ProjectFile> files = new ArrayList<>();
        Long coreFileId = 0L;
        if (smell != null) {
            ProjectFile coreFile = projectFileRepository.findFileById(smell.getCoreNodeId());
            if (coreFile != null) {
                String key = smell.getId().toString();
                if (cache.get(getClass(), key) != null) {
                    return cache.get(getClass(), key);
                }
                files.add(coreFile);
                coreFileId = coreFile.getId();
                JSONObject smellJson = new JSONObject();
                smellJson.put("name", smell.getName());
                Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
                List<ProjectFile> smellFiles = new ArrayList<>();
                for (Node containedNode : containedNodes) {
                    smellFiles.add((ProjectFile) containedNode);
                }
                files.addAll(smellFiles);
                JSONArray smellNodesJson = new JSONArray();
                for (ProjectFile smellFile : smellFiles) {
                    if (!files.contains(smellFile)) {
                        files.add(smellFile);
                    }
                    JSONObject smellNodeJson = new JSONObject();
                    smellNodeJson.put("index", files.indexOf(smellFile) + 1);
                    smellNodeJson.put("path", smellFile.getPath());
                    smellNodesJson.add(smellNodeJson);
                }
                smellJson.put("nodes", smellNodesJson);
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
        }
        result.put("smellType", SmellType.UNUSED_INCLUDE);
        result.put("coreNode", coreFileId.toString());
        result.put("nodes", nodesJson);
        result.put("edges", edgesJson);
        result.put("smells", smellsJson);
        if (smell != null) {
            ProjectFile coreFile = projectFileRepository.findFileById(smell.getCoreNodeId());
            if (coreFile != null) {
                String key = smell.getId().toString();
                cache.cache(getClass(), key, result);
            }
        }
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
