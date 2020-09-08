package cn.edu.fudan.se.multidependency.controller.as;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.query.as.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/similar")
public class SimilarComponentController {

	@Autowired
	private SimilarComponentsDetector similarComponentsDetector;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("")
	public String similar(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("files", similarComponentsDetector.similarFiles());
		request.setAttribute("packages", similarComponentsDetector.similarPackages());
		return "as/similar";
	}
	
}
