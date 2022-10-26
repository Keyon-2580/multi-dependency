package cn.edu.fudan.se.multidependency.service.insert.javancss;

import cn.edu.fudan.se.multidependency.model.MethodMetric;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelations;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.DirExplorer;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import javancss.Javancss;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: keyon
 * @time: 2022/10/26 10:03
 */

public class JavaCcnExtractor extends ExtractorForNodesAndRelationsImpl {

    private final String repoPath;

    private List<MethodMetric> methodMetricList = new ArrayList<>();

    public JavaCcnExtractor(String repoPath){
        this.repoPath = repoPath;

    }

    @Override
    public void addNodesAndRelations() throws Exception {
        getAllMethodMetric(repoPath);
        extractNodesAndRelations();
    }
    public void getAllMethodMetric(String repoPath) {
        List<MethodMetric> allMethodMetric = new LinkedList<>();
        new DirExplorer((level, path, file) -> !DirExplorer.fileFilter(path), (level, path, file) -> {
            allMethodMetric.addAll((new Javancss(new File(file.getAbsolutePath()))).getFunctionMetrics().stream().map((f) -> new MethodMetric(path, f.name, f.ccn, f.ncss)).collect(Collectors.toList()));
        }).explore(new File(repoPath));
        this.methodMetricList = allMethodMetric;
    }
    public void extractNodesAndRelations(){
        DecimalFormat df = new DecimalFormat("#.00");
        Map<String, List<MethodMetric>> groupByFiles;
        groupByFiles = this.methodMetricList.stream().collect(Collectors.groupingBy(MethodMetric::getRelativeFilePath));
        Map<String, List<Double>> fileToCcn = new HashMap<>();
        //文件聚合
        for(Map.Entry<String, List<MethodMetric>> entry : groupByFiles.entrySet()){
            List<Double> list = new ArrayList<>();
            int totalCcn = entry.getValue().stream().mapToInt(MethodMetric::getMethodCcn).sum();
            int count = entry.getValue().size();
            list.add((double) totalCcn);
            list.add( Double.valueOf(df.format((double) totalCcn / count)));
            String prefix = FileUtil.extractFilePathName(repoPath);
            String filePath = "/" + prefix + entry.getKey();
            fileToCcn.put(filePath, list);
        }
        fileToCcn.forEach((key, value)->{
            ProjectFile file = this.getNodes().findFileByPathRecursion(key);
            if(file != null){
                file.setWmc(value.get(0));
                file.setAmc(value.get(1));
            }
        });
    }
}
