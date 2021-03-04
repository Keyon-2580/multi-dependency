package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.service.query.smell.SmellDetectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/cyclic")
public class CyclicDependencyController {

	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;

	@Autowired
	private NodeService nodeService;

	@Autowired
	SmellDetectorService smellDetectorService;

	@GetMapping("")
	public String cyclicHierarchy(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("types", cyclicDependencyDetector.typeCycles());
		request.setAttribute("files", cyclicDependencyDetector.fileCycles());
		request.setAttribute("packages", cyclicDependencyDetector.packageCycles());
		request.setAttribute("modules", cyclicDependencyDetector.moduleCycles());
		return "as/cyclic";
	}
	
	
}
