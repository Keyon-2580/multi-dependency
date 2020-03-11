package cn.edu.fudan.se.multidependency.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.service.spring.DependencyOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.ProjectOrganizationService;

@Controller
@RequestMapping("/project")
public class ProjectController {
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;

	@Autowired
	private ProjectOrganizationService projectOrganizationService;

	@Autowired
	private DependencyOrganizationService dependencyOrganizationService;

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
	
	@GetMapping("/cytoscape")
	@ResponseBody
	public JSONObject cytoscape(
			@RequestParam("projectId") Long projectId,
			@RequestParam("dependency") String dependency,
			@RequestParam("level") String level) {
		System.out.println("/project/cytoscape");
		JSONObject result = new JSONObject();
		try {
			Project project = projectOrganizationService.findProjectById(projectId);
			if(project == null) {
				throw new Exception("没有找到id为 " + projectId + " 的项目");
			}
			if("dynamic".equals(dependency)) {
				List<FunctionDynamicCallFunction> calls = dynamicAnalyseService.findFunctionDynamicCallsByProject(project);
				System.out.println(calls.size());
				dependencyOrganizationService.dynamicCallDependency(calls);
				if("file".equals(level)) {
					result.put("value", dependencyOrganizationService.fileCallToCytoscape());
				} else if("directory".equals(level)) {
					result.put("value", dependencyOrganizationService.directoryCallToCytoscape());
				}
			}
			System.out.println(result.get("value"));
			if(result.get("value") == null) {
				throw new Exception("结果暂无");
			}
			result.put("result", "success");
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
}
