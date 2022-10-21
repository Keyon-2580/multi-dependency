package cn.edu.fudan.se.multidependency;
import cn.edu.fudan.se.multidependency.model.MethodMetric;
import cn.edu.fudan.se.multidependency.utils.CsvExportUtil;
import cn.edu.fudan.se.multidependency.utils.DirExplorer;
import javancss.Javancss;


import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @description:
 * @author: keyon
 * @time: 2022/10/21 09:13
 */

public class JavaNcssAnalyse {


    public static void main(String[] args) throws Exception {
        String repoPath = "/Users/keyon/Documents/bigDataPlatform/depend-service/reopFiles/nacos";
        System.out.println(repoPath);
        if (repoPath != null && !"".equals(repoPath)) {
            exportCSV(repoPath + "metric.csv", getAllMethodMetric(repoPath));
        } else {
            System.out.println("repo path is null");
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
}
