package cn.edu.fudan.se.multidependency.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.MetricCalculator;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.ModularityCalculatorImplForFieldMethodLevel;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.ProjectMetrics;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/metric")
public class MeasureController {

    @Autowired
    ModularityCalculatorImplForFieldMethodLevel modularityCalculator;

    @Autowired
    MetricCalculator metricCalculator;
    
    @Autowired
    NodeService nodeService;
    
    @GetMapping(value= {"", "/", "/index"})
    public String index() {
    	return "metric";
    }
    
    
    @GetMapping("/file")
    @ResponseBody
    public Collection<FileMetrics> calculateFileMetrics() {
    	return metricCalculator.calculateFileMetrics();
    }
    
    @GetMapping("/project")
    @ResponseBody
    public Collection<ProjectMetrics> calculateProjectMetrics() {
    	return metricCalculator.calculateProjectMetrics();
    }
    
    @GetMapping("/package")
    @ResponseBody
    public Collection<PackageMetrics> calculatePackageMetrics() {
    	return metricCalculator.calculatePackageMetrics();
    }

    @GetMapping("/modularityMetricQ")
    @ResponseBody
    public JSONObject modularityMetricQ() {
        JSONObject result = new JSONObject();

        Map<String, Double> metricQs = new HashMap<>();
        for (Project project:nodeService.allProjects()){
            metricQs.put(project.getName(),modularityCalculator.calculate(project).getValue());
        }

        try {
            result.put("result", "success");
            result.put("value", metricQs);
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
