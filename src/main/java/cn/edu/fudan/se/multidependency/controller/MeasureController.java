package cn.edu.fudan.se.multidependency.controller;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.node.code.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.spring.NodeService;
import cn.edu.fudan.se.multidependency.service.spring.metric.ModularityCalculatorImplForFieldMethodLevel;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/metric")
public class MeasureController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeasureController.class);

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ModularityCalculatorImplForFieldMethodLevel modularityCalculator;


    @GetMapping("/modularityMetricQ")
    @ResponseBody
    public JSONObject  modularityMetricQ() {
        JSONObject result = new JSONObject();

        Map<String, Double> metricQs = new HashMap<>();
        for (Project project:projectRepository.findAll()){
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
