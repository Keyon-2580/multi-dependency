package cn.edu.fudan.se.multidependency.controller;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.service.query.smell.BasicSmellQueryService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

}
