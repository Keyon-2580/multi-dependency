package cn.edu.fudan.se.multidependency.controller;

import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.service.query.smell.BasicSmellQueryService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
@RequestMapping("/smell")
public class SmellController {
    @Autowired
    private BasicSmellQueryService basicSmellQueryService;

    @PostMapping("/treemap")
    @ResponseBody
    public JSONArray  smellsToTreemap() {
        return basicSmellQueryService.smellsToTreemap();
    }

    @GetMapping("/get_metric")
    @ResponseBody
    public Metric cytoscape(@RequestParam("smellId") long smellId) {
        return basicSmellQueryService.findMetricBySmellId(smellId);
    }

}
