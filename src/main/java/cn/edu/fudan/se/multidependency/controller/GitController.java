package cn.edu.fudan.se.multidependency.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.spring.history.GitAnalyseService;

@Controller
@RequestMapping("/git")
public class GitController {

    @Autowired
    private GitAnalyseService gitAnalyseService;

    @GetMapping("/developerToMicroservice")
    @ResponseBody
    public JSONObject cntOfDevUpdMs() {
        JSONObject result = new JSONObject();
        try {
            result.put("result", "success");
            result.put("value", gitAnalyseService.cntOfDevUpdMsList());
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }
    @GetMapping("/topKFileBeUpd")
    @ResponseBody
    public JSONObject topKFileBeUpd() {
        JSONObject result = new JSONObject();
        try {
            result.put("result", "success");
            result.put("value", gitAnalyseService.getTopKFileBeUpd(10));
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }
    @GetMapping("/topKFileCoChange")
    @ResponseBody
    public JSONObject topKFileCoChange(@RequestParam(name="k", required=false, defaultValue="10") int k) {
        JSONObject result = new JSONObject();
        try {
            result.put("result", "success");
            Collection<CoChange> value = null;
            if(k > 0) {
            	value = gitAnalyseService.getTopKFileCoChange(k);
            } else {
            	value = gitAnalyseService.calCntOfFileCoChange();
            }
            result.put("value", value);
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
