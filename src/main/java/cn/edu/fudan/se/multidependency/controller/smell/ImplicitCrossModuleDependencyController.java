package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import cn.edu.fudan.se.multidependency.service.query.smell.ImplicitCrossModuleDependencyDetector;

@Controller
@RequestMapping("/as/icd")
public class ImplicitCrossModuleDependencyController {

	@Autowired
	private ImplicitCrossModuleDependencyDetector icdDetector;

	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/query")
	public String queryImplicitCrossModuleDependency(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileImplicitCrossModuleDependencyMap", icdDetector.queryFileImplicitCrossModuleDependency());
		request.setAttribute("packageImplicitCrossModuleDependencyMap", icdDetector.queryPackageImplicitCrossModuleDependency());
		return "as/implicitcrossmoduledependency";
	}

	@GetMapping("/detect")
	public String detectImplicitCrossModuleDependency(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileImplicitCrossModuleDependencyMap", icdDetector.detectFileImplicitCrossModuleDependency());
		request.setAttribute("packageImplicitCrossModuleDependencyMap", icdDetector.detectPackageImplicitCrossModuleDependency());
		return "as/implicitcrossmoduledependency";
	}
	
	@GetMapping("/cochange/{projectId}")
	@ResponseBody
	public Integer[] getProjectMinCoChange(@PathVariable("projectId") Long projectId) {
		Integer[] result = new Integer[2];
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			result[0] = icdDetector.getFileMinCoChange(projectId);
			result[1] = icdDetector.getPackageMinCoChange(projectId);
		}
		return result;
	}
	
	@PostMapping("/cochange/{projectId}")
	@ResponseBody
	public boolean setProjectMinCoChange(@PathVariable("projectId") Long projectId, @RequestParam("minFileCoChange") int minFileCoChange, @RequestParam("minPackageCoChange") int minPackageCoChange) {
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			icdDetector.setProjectFileMinCoChange(projectId, minFileCoChange);
			icdDetector.setProjectPackageMinCoChange(projectId, minPackageCoChange);
			return true;
		}
		return false;
	}
}
