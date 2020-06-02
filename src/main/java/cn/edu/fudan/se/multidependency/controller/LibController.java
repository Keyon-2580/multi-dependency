package cn.edu.fudan.se.multidependency.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

@Controller
@RequestMapping("/lib")
public class LibController {
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private MicroserviceService msService;
	
	@GetMapping("/apis")
	@ResponseBody
	public JSONObject findProjectCallAPIs() {
		JSONObject result = new JSONObject();
		Iterable<Project> projects = staticAnalyseService.allProjects();
		JSONObject values = new JSONObject();
		for(Project project : projects) {
			CallLibrary<Project> call = staticAnalyseService.findProjectCallLibraries(project);
			System.out.println(call.getCallAPITimes());
			values.put(project.getName(), call);
		}
		result.put("result", "success");
		result.put("projectValues", values);
		
		values = new JSONObject();
		Iterable<MicroService> mss = msService.findAllMicroService();
		for(MicroService ms : mss) {
			CallLibrary<MicroService> call = msService.findMicroServiceCallLibraries(ms);
			values.put(ms.getName(), call);
		}
		result.put("msValues", values);
		return result;
	}
}
