package cn.edu.fudan.se.multidependency.service.query.ar;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DependencyMatrix {

    @Getter
    private Map<String, Map<String, Double>> adjacencyList;

    @Getter
    private Map<String, Map<String, Double>> inverseAdjacencyList;

    @Autowired
    private ProjectFileRepository fileRepository;

    private List<ProjectFile> projectFiles;

    //静态
    private static final double IMPORT_BTW_FILE = 1.0;
    private static final double INCLUDE_BTW_FILE = 1.0;
    private static final double EXTENDS_BTW_TYPE = 3.0;
    private static final double IMPLEMENTS_BTW_TYPE = 3.0;
    private static final double IMPLEMENTS_BTW_FUNC = 3.0;
    private static final double CREATE_BTW_TYPE = 1.0;
    private static final double CREATE_FROM_FUNC_TO_TYPE = 1.0;
    private static final double IMPLLINK_BTW_FUNC = 3.0;
    private static final double CALL_BTW_FUNC = 1.0;
    private static final double CALL_FROM_TYPE_TO_FUNC = 1.0;
    private static final double ACCESS_FROM_FUNC_TO_VAR = 1.0;
    private static final double MEMBER_VAR_TYPE_FROM_VAR_TO_TYPE = 1.0;
    private static final double LOCAL_VAR_TYPE_FROM_VAR_TO_TYPE = 1.0;
    private static final double PARA_From_Var_To_Type = 1.0;
    private static final double RETURN_FROM_FUNC_TO_TYPE = 1.0;
    private static final double THROW_FROM_FUNC_TO_TYPE = 1.0;
    private static final double CAST_FROM_FUNC_TO_TYPE = 1.0;


    //动态
    private static final double DYNAMIC_CALL_BTW_FUNC = 1.0;

    //演化
    private static final double CO_CHANGE = 1.0;

    private boolean flag = true;

    public void init() {
        adjacencyList = new HashMap<>();
        inverseAdjacencyList = new HashMap<>();
        projectFiles = (List<ProjectFile>) fileRepository.findAll();
        calculate();
    }

    private void calculate() {
        for (ProjectFile file : projectFiles) {
            if (!adjacencyList.containsKey(file.getPath())) {
                adjacencyList.put(file.getPath(), new HashMap<>());
            }
            if (!inverseAdjacencyList.containsKey(file.getPath())) {
                inverseAdjacencyList.put(file.getPath(), new HashMap<>());
            }
            Map<String, Double> edges = adjacencyList.get(file.getPath());

            flag = false;
            //静态
            calculate(file, edges, fileRepository.getImportBtwFile(file.getId()), IMPORT_BTW_FILE);
            calculate(file, edges, fileRepository.getIncludeBtwFile(file.getId()), INCLUDE_BTW_FILE);
            calculate(file, edges, fileRepository.getExtendBtwType(file.getId()), EXTENDS_BTW_TYPE);
            calculate(file, edges, fileRepository.getImplementBtwType(file.getId()), IMPLEMENTS_BTW_TYPE);
            calculate(file, edges, fileRepository.getImplementBtwFunc(file.getId()), IMPLEMENTS_BTW_FUNC);
            calculate(file, edges, fileRepository.getCreateBtwType(file.getId()), CREATE_BTW_TYPE);
            calculate(file, edges, fileRepository.getCreateFromFuncToType(file.getId()), CREATE_FROM_FUNC_TO_TYPE);
            calculate(file, edges, fileRepository.getImpllinkBtwFunc(file.getId()), IMPLLINK_BTW_FUNC);
            calculate(file, edges, fileRepository.getCallBtwFunc(file.getId()), CALL_BTW_FUNC);
            calculate(file, edges, fileRepository.getCallFromTypeToFunc(file.getId()), CALL_FROM_TYPE_TO_FUNC);
            calculate(file, edges, fileRepository.getAccessFromFuncToVar(file.getId()), ACCESS_FROM_FUNC_TO_VAR);
            calculate(file, edges, fileRepository.getMemberVarTypeFromVarToType(file.getId()), MEMBER_VAR_TYPE_FROM_VAR_TO_TYPE);
            calculate(file, edges, fileRepository.getLocalVarTypeFromVarToType(file.getId()), LOCAL_VAR_TYPE_FROM_VAR_TO_TYPE);
            calculate(file, edges, fileRepository.getParaFromVarToType(file.getId()), PARA_From_Var_To_Type);
            calculate(file, edges, fileRepository.getReturnFromFuncToType(file.getId()), RETURN_FROM_FUNC_TO_TYPE);
            calculate(file, edges, fileRepository.getThrowFromFuncToType(file.getId()), THROW_FROM_FUNC_TO_TYPE);
            calculate(file, edges, fileRepository.getCastFromFuncToType(file.getId()), CAST_FROM_FUNC_TO_TYPE);

            //动态
            calculate(file, edges, fileRepository.getDynamicCallBtwFunc(file.getId()), DYNAMIC_CALL_BTW_FUNC);
            flag = true;
            //演化
            calculate(file, edges, fileRepository.getCoChangeFiles(file.getId()), CO_CHANGE);
        }
    }

    private void calculate(ProjectFile from, Map<String, Double> edges, List<DependencyPair> pairs, double weight) {
        if (pairs == null || pairs.size() == 0) return;
        for (DependencyPair pair : pairs) {
            ProjectFile to = pair.getProjectFile();
            double val = weight * pair.getCount() / from.getEndLine();
            edges.put(to.getPath(), edges.getOrDefault(to.getPath(), 0.0) + val);
            if (flag) {
                if (!adjacencyList.containsKey(to.getPath())) {
                    adjacencyList.put(to.getPath(), new HashMap<>());
                }
                Map<String, Double> tmp = adjacencyList.get(to.getPath());
                tmp.put(from.getPath(), tmp.getOrDefault(from.getPath(), 0.0) + val);
            }
            if (!inverseAdjacencyList.containsKey(to.getPath())) {
                inverseAdjacencyList.put(to.getPath(), new HashMap<>());
            }
            Map<String, Double> inverseEdges = inverseAdjacencyList.get(to.getPath());
            inverseEdges.put(from.getPath(), inverseEdges.getOrDefault(from.getPath(), 0.0) + val);
            if (flag) {
                if (!inverseAdjacencyList.containsKey(from.getPath())) {
                    inverseAdjacencyList.put(from.getPath(), new HashMap<>());
                }
                Map<String, Double> tmp = inverseAdjacencyList.get(from.getPath());
                tmp.put(to.getPath(), tmp.getOrDefault(to.getPath(), 0.0) + val);
            }
        }
    }

    public void exportMatrix(String filePath, Map<String, Map<String, Double>> map) {
        StringBuffer buf = new StringBuffer();
        DecimalFormat df = new DecimalFormat("0.00000");
        for (String file1 : map.keySet()) {
            buf.append(file1 + "\n");
            for (Map.Entry<String, Double> entry : map.get(file1).entrySet()) {
                buf.append("-----" + entry.getKey() + ":" + df.format(entry.getValue()) + "\n");
            }
        }
        FileUtil.exportToFile(filePath, buf.toString());
    }

    public void exportNeo4jCSV(String dirPath) {
        String nodeFileName = "nodes.csv";
        String relationFileName = "relations.csv";
        final String CSV_COL_SPR = ",";
        final String CSV_ROW_SPR = "\r\n";

        StringBuffer nodeBuf = new StringBuffer();
        nodeBuf.append("file_id" + CSV_COL_SPR + "name" + CSV_ROW_SPR);

        Map<String, Integer> map = new HashMap<>();
        int i = 0;
        for (ProjectFile projectFile : projectFiles) {
            nodeBuf.append(i + CSV_COL_SPR + projectFile.getPath() + CSV_ROW_SPR);
            map.put(projectFile.getPath(), i);
            i++;
        }
        FileUtil.exportToFile(dirPath + nodeFileName, nodeBuf.toString());

        StringBuffer relationBuf = new StringBuffer();
        relationBuf.append("file1_id" + CSV_COL_SPR + "file2_id" + CSV_COL_SPR + "weight" + CSV_ROW_SPR);

        for (String file1 : adjacencyList.keySet()) {
            for (Map.Entry<String, Double> entry : adjacencyList.get(file1).entrySet()) {
                relationBuf.append(map.get(file1) + CSV_COL_SPR + map.get(entry.getKey())
                        + CSV_COL_SPR + entry.getValue() + CSV_ROW_SPR);
            }
        }
        FileUtil.exportToFile(dirPath + relationFileName, relationBuf.toString());
    }
}
