package cn.edu.fudan.se.multidependency.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.service.spring.ProjectOrganizationService;

@Controller
@RequestMapping("/project")
public class ProjectController {
	
	@Autowired
	private ProjectOrganizationService projectOrganizationService;

	@GetMapping(value = {"/", "/index"})
	public String index() {
		return "project";
	}
	
	@GetMapping("/dynamiccall")
	@ResponseBody
	public void dynamicCall() {
		System.out.println("/project/dynamiccall");
	}
	
	@GetMapping("/treeview")
	@ResponseBody
	public JSONObject projectToTreeView() {
		System.out.println("/project/treeview");
		JSONObject result = new JSONObject();
		try {
			result.put("result", "success");
			result.put("value", projectOrganizationService.projectsToTreeView());
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	
}
