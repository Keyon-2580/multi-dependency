package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.query.smell.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/similarcomponents")
public class SimilarComponentController {

	@Autowired
	private SimilarComponentsDetector similarComponentsDetector;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/query")
	public String querySimilarComponents(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("files", similarComponentsDetector.getFileSimilarComponents());
//		request.setAttribute("packages", similarComponentsDetector.getPackageSimilarComponents());
		return "as/similar";
	}

	@GetMapping("/detect")
	public String detectSimilarComponents(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("files", similarComponentsDetector.detectFileSimilarComponents());
//		request.setAttribute("packages", similarComponentsDetector.detectPackageSimilarComponents());
		return "as/similar";
	}
}
