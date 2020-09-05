package cn.edu.fudan.se.multidependency.controller.ar;

import cn.edu.fudan.se.multidependency.service.query.ar.DependencyMatrix;
import cn.edu.fudan.se.multidependency.service.query.ar.KMeans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/ar")
public class ARController {

    @Autowired
    private KMeans kMeans;

    @Autowired
    private DependencyMatrix dependencyMatrix;

    @GetMapping("/kmeans")
    @ResponseBody
    public void kmeans() {
        kMeans.init();
        List<List<String>> res = kMeans.clustering();
        kMeans.exportClusterRes("C:\\Users\\SongJee\\Desktop\\res\\output.txt", res);
    }

    @GetMapping("/exportNeo4jCSV")
    @ResponseBody
    public void exportNeo4jCSV() {
        dependencyMatrix.init();
        dependencyMatrix.exportNeo4jCSV("C:\\Users\\SongJee\\Desktop\\Dissertation\\cluster\\import\\");
    }
}
