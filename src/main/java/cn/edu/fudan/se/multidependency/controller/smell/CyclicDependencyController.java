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
		request.setAttribute("typeCyclicDependencyMap", cyclicDependencyDetector.queryTypeCyclicDependency());
		request.setAttribute("fileCyclicDependencyMap", cyclicDependencyDetector.queryFileCyclicDependency());
		request.setAttribute("packageCyclicDependencyMap", cyclicDependencyDetector.queryPackageCyclicDependency());
		return "as/cyclic";
	}

	@GetMapping("/detect")
	public String detectCyclicDependency(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("typeCyclicDependencyMap", cyclicDependencyDetector.detectTypeCyclicDependency());
		request.setAttribute("fileCyclicDependencyMap", cyclicDependencyDetector.detectFileCyclicDependency());
		request.setAttribute("packageCyclicDependencyMap", cyclicDependencyDetector.detectPackageCyclicDependency());
		return "as/cyclic";
	}
}
