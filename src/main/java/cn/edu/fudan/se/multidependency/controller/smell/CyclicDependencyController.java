package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.query.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/cyclic")
public class CyclicDependencyController {

	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("")
	public String cyclicHierarchy(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("types", cyclicDependencyDetector.cycleTypes());
		request.setAttribute("files", cyclicDependencyDetector.cycleFiles());
		request.setAttribute("packages", cyclicDependencyDetector.cyclePackages());
		request.setAttribute("modules", cyclicDependencyDetector.cycleModules());
		return "as/cyclic";
	}
	
	
}
