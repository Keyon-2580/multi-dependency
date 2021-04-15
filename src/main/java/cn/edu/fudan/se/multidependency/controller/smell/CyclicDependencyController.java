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

	@GetMapping("/query")
	public String queryCyclicDependency(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("types", cyclicDependencyDetector.getTypeCyclicDependency());
		request.setAttribute("files", cyclicDependencyDetector.getFileCyclicDependency());
		request.setAttribute("packages", cyclicDependencyDetector.getPackageCyclicDependency());
		request.setAttribute("modules", cyclicDependencyDetector.getModuleCyclicDependency());
		return "as/cyclic";
	}

	@GetMapping("/detect")
	public String reDetectCyclicDependency(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("types", cyclicDependencyDetector.detectTypeCyclicDependency());
		request.setAttribute("files", cyclicDependencyDetector.detectFileCyclicDependency());
		request.setAttribute("packages", cyclicDependencyDetector.detectPackageCyclicDependency());
		request.setAttribute("modules", cyclicDependencyDetector.detectModuleCyclicDependency());
		return "as/cyclic";
	}
}
