package cn.edu.fudan.se.multidependency.controller.ar;

import cn.edu.fudan.se.multidependency.service.query.ar.ClusterService;
import cn.edu.fudan.se.multidependency.service.query.ar.ShowService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/ar")
public class ARController {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ShowService showService;

    @GetMapping("/exportNeo4jCSV")
    @ResponseBody
    public void exportNeo4jCSV() {}

    @GetMapping("/staticDependGraph")
    public String staticDependGraph() {
        System.out.println("test staticDependGraph");
        return "ar/static";
    }

    @GetMapping("/dynamicDependGraph")
    public String dynamicDependGraph() {
        System.out.println("test dynamicDependGraph");
        return "ar/dynamic";
    }

    @GetMapping("/cochangeDependGraph")
    public String cochangeDependGraph() {
        System.out.println("test cochangeDependGraph");
        return "ar/cochange";
    }

    @GetMapping("/json/static")
    @ResponseBody
    public JSONObject staicDependJson() {
        JSONObject result = new JSONObject();
        System.out.println("test staicDependJson");
        result.put("result", showService.staticDependGraph());
        return result;
    }

    @GetMapping("/json/dynamic")
    @ResponseBody
    public JSONObject dynamicDependJson() {
        JSONObject result = new JSONObject();
        System.out.println("test dynamicDependJson");
        result.put("result", showService.dynamicDependGraph());
        return result;
    }

    @GetMapping("/json/cochange")
    @ResponseBody
    public JSONObject cochangeDependJson() {
        JSONObject result = new JSONObject();
        System.out.println("test cochangedependJson");
        result.put("result", showService.cochangeDependGraph());
        return result;
    }
}
