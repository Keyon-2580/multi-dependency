package cn.edu.fudan.se.multidependency.controller.smell;

import cn.edu.fudan.se.multidependency.service.query.smell.UnusedIncludeDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/as/unusedinclude")
public class UnusedIncludeController {

	@Autowired
	private UnusedIncludeDetector unusedIncludeDetector;

	@Autowired
	private NodeService nodeService;

	@GetMapping("/query")
	public String queryUnusedInclude(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileUnusedIncludeMap", unusedIncludeDetector.queryFileUnusedInclude());
		return "as/unusedinclude";
	}

	@GetMapping("/detect")
	public String detectUnusedInclude(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileUnusedIncludeMap", unusedIncludeDetector.detectFileUnusedInclude());
		return "as/unusedinclude";
	}
}
