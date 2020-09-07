package cn.edu.fudan.se.multidependency.service.query.ar.impl;


import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.ar.ClusterService;
import cn.edu.fudan.se.multidependency.service.query.ar.DependencyMatrix;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private DependencyMatrix dependencyMatrix;

    public void exportNeo4jCSV(String dirPath) {
        String nodeFileName = "nodes.csv";
        String relationFileName = "relations.csv";
        final String CSV_COL_SPR = ",";
        final String CSV_ROW_SPR = "\r\n";

        StringBuffer nodeBuf = new StringBuffer();
        nodeBuf.append("file_id" + CSV_COL_SPR + "name" + CSV_ROW_SPR);

        Map<String, Integer> map = new HashMap<>();
        int i = 0;
        for (ProjectFile projectFile : dependencyMatrix.getProjectFiles()) {
            nodeBuf.append(i + CSV_COL_SPR + projectFile.getPath() + CSV_ROW_SPR);
            map.put(projectFile.getPath(), i);
            i++;
        }
        FileUtil.exportToFile(dirPath + nodeFileName, nodeBuf.toString());

        StringBuffer relationBuf = new StringBuffer();
        relationBuf.append("file1_id" + CSV_COL_SPR + "file2_id" + CSV_COL_SPR + "weight" + CSV_ROW_SPR);

        Map<String, Map<String, Double>> adjacencyList = dependencyMatrix.getAdjacencyList();
        for (String file1 : adjacencyList.keySet()) {
            for (Map.Entry<String, Double> entry : adjacencyList.get(file1).entrySet()) {
                relationBuf.append(map.get(file1) + CSV_COL_SPR + map.get(entry.getKey())
                        + CSV_COL_SPR + entry.getValue() + CSV_ROW_SPR);
            }
        }
        FileUtil.exportToFile(dirPath + relationFileName, relationBuf.toString());
    }
}
