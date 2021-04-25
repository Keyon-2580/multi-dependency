package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import cn.edu.fudan.se.multidependency.service.query.smell.ImplicitCrossModuleDependencyDetector;

@Controller
@RequestMapping("/as/icd")
public class ICDController {

	@Autowired
	private ImplicitCrossModuleDependencyDetector icdDetector;

	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/query")
	public String queryImplicitCrossModuleDependency(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileImplicitCrossModuleDependencyMap", icdDetector.queryFileImplicitCrossModuleDependency());
		request.setAttribute("packageImplicitCrossModuleDependencyMap", icdDetector.queryPackageImplicitCrossModuleDependency());
		return "as/icd";
	}

	@GetMapping("/detect")
	public String detectImplicitCrossModuleDependency(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileImplicitCrossModuleDependencyMap", icdDetector.detectFileImplicitCrossModuleDependency());
		request.setAttribute("packageImplicitCrossModuleDependencyMap", icdDetector.detectPackageImplicitCrossModuleDependency());
		return "as/icd";
	}
	
	@GetMapping("/cochange/{projectId}")
	@ResponseBody
	public int[] getMinCoChange(@PathVariable("projectId") Long projectId) {
		int[] result = new int[2];
		Integer minFileCoChange = icdDetector.getFileMinCoChange(projectId);
		Integer minPackageCpChange = icdDetector.getPackageMinCoChange(projectId);
		result[0] = minFileCoChange;
		result[1] = minPackageCpChange;
		return result;
	}
	
	@PostMapping("/cochange/{projectId}")
	@ResponseBody
	public boolean setMinCoChange(@PathVariable("projectId") Long projectId, @RequestParam("icdMinFileCoChange") int minFileCoChange, @RequestParam("icdMinPackageCoChange") int minPackageCoChange) {
		icdDetector.setProjectFileMinCoChange(projectId, minFileCoChange);
		icdDetector.setProjectPackageMinCoChange(projectId, minPackageCoChange);
		return true;
	}
	
}
