package cn.edu.fudan.se.multidependency.controller;

import cn.edu.fudan.se.multidependency.service.spring.GitAnalyseService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/git")
public class GitController {

    @Autowired
    private GitAnalyseService gitAnalyseService;

    @GetMapping("/developerToProject")
    @ResponseBody
    public JSONObject cntOfDevUpdPro() {
        JSONObject result = new JSONObject();
        try {
            result.put("result", "success");
            result.put("value", gitAnalyseService.cntOfDevUpdPro());
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/developerToMicroservice")
    @ResponseBody
    public JSONObject cntOfDevUpdMs() {
        JSONObject result = new JSONObject();
        try {
            result.put("result", "success");
            result.put("value", gitAnalyseService.cntOfDevUpdMs());
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
