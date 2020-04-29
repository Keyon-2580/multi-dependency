package cn.edu.fudan.se.multidependency.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

@Controller
@RequestMapping("/clone")
public class CloneController {
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private MicroserviceService msService;

	@GetMapping("/clones")
	@ResponseBody
	public JSONObject findProjectClones() {
		JSONObject result = new JSONObject();
		Iterable<FunctionCloneFunction> allClones = staticAnalyseService.findAllFunctionCloneFunctions();
		Iterable<Clone<Project>> clones = staticAnalyseService.findProjectClone(allClones, true);
		result.put("result", "success");
		result.put("projectValues", clones);
		Iterable<Clone<MicroService>> msClones = msService.findMicroServiceClone(allClones, true);
		result.put("msValues", msClones);
		return result;
	}
}