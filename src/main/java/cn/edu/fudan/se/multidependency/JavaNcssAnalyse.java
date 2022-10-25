package cn.edu.fudan.se.multidependency;
import cn.edu.fudan.se.multidependency.model.MethodMetric;
import cn.edu.fudan.se.multidependency.utils.CsvExportUtil;
import cn.edu.fudan.se.multidependency.utils.DirExplorer;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import cn.edu.fudan.se.multidependency.utils.config.JSONConfigFile;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfigUtil;
import javancss.Javancss;
import org.springframework.beans.factory.annotation.Value;


import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @description:
 * @author: keyon
 * @time: 2022/10/21 09:13
 */

public class JavaNcssAnalyse {

  //  @Value("${packages}")
    private static String packages = "/java/org/apache/coyote";



    public static void main(String[] args) throws Exception {
        YamlUtil.YamlObject yaml = YamlUtil.getYaml(args);
        JSONConfigFile jsonConfigFile = ProjectConfigUtil.extract(JSONUtil.extractJSONObject(new File(yaml.getProjectsConfig())));
        for(ProjectConfig projectConfig : jsonConfigFile.getProjectsConfig()){
            String repoPath = projectConfig.getPath();
            System.out.println(repoPath);
            if (repoPath != null && !"".equals(repoPath)) {
                List<MethodMetric> methodMetricList = getAllMethodMetric(repoPath);
                exportCSV(repoPath + "metric.csv", methodMetricList);
                getPackagesCcn(methodMetricList);
            } else {
                System.out.println("repo path is null");
            }
        }
    }


    public static List<MethodMetric> getAllMethodMetric(String repoPath) {
        List<MethodMetric> allMethodMetric = new LinkedList<>();
        new DirExplorer((level, path, file) -> !DirExplorer.fileFilter(path), (level, path, file) -> {
            allMethodMetric.addAll((new Javancss(new File(file.getAbsolutePath()))).getFunctionMetrics().stream().map((f) -> new MethodMetric(path, f.name, f.ccn, f.ncss)).collect(Collectors.toList()));
        }).explore(new File(repoPath));
        return allMethodMetric;
    }

    public static void exportCSV(String tableName, List<MethodMetric> methodMetrics) throws Exception {
        List<String[]> contents = methodMetrics.stream().map((m) -> new String[]{m.getRelativeFilePath(), m.getMethodFullName(), String.valueOf(m.getMethodCcn()), String.valueOf(m.getNcss())}).collect(Collectors.toList());
        CsvExportUtil.doExport(tableName, contents, "relativeFilePath", "methodFullName", "methodCcn", "ncss");
    }

    //文件 包聚合
    public static Map<String, Map<String, Double>> getPackagesCcn(List<MethodMetric> allMethodMetric){
        allMethodMetric = allMethodMetric.stream().filter(methodMetric -> methodMetric.getNcss() > 5).collect(Collectors.toList());
        DecimalFormat df = new DecimalFormat("#.00");
        Map<String, List<MethodMetric>> groupByFiles;
        groupByFiles = allMethodMetric.stream().collect(Collectors.groupingBy(MethodMetric::getRelativeFilePath));
        Map<String, List<Double>> fileToCcn = new HashMap<>();
        //文件聚合
        for(Map.Entry<String, List<MethodMetric>> entry : groupByFiles.entrySet()){
            List<Double> list = new ArrayList<>();
            int totalCcn = entry.getValue().stream().mapToInt(MethodMetric::getMethodCcn).sum();
            int count = entry.getValue().size();
            list.add((double) totalCcn);
            list.add( Double.valueOf(df.format((double) totalCcn / count)));
            fileToCcn.put(entry.getKey(), list);
        }
        Map<String, Map<String, Double>> packageToCcn = new HashMap<>();
        List<String> packageList = Arrays.stream(packages.split(",")).collect(Collectors.toList());
        //包聚合
        for(String onePackage : packageList){
            Map<String, Double> dataMap = new HashMap<>();
            Double WMC = 0.0;
            Double AMC = 0.0;
            int filesNum = 0;
            for(Map.Entry<String, List<Double>> entry : fileToCcn.entrySet()){
                if(entry.getKey().startsWith(packages)){
                    filesNum++;
                    WMC += entry.getValue().get(0);
                    AMC += entry.getValue().get(1);
                }
            }
            dataMap.put("WMC", WMC);
            dataMap.put("AMC", Double.valueOf(df.format(AMC)));
            dataMap.put("AWMC", Double.valueOf(df.format(WMC / filesNum)));
            dataMap.put("AAMC", Double.valueOf(df.format(AMC /filesNum)));
            packageToCcn.put(onePackage, dataMap);
        }
        System.out.println(packageToCcn);
        return packageToCcn;

    }
}
