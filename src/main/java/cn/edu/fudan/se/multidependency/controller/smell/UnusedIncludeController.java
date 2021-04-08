package cn.edu.fudan.se.multidependency.controller.smell;

import cn.edu.fudan.se.multidependency.service.query.smell.UnusedIncludeDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/as/unusedInclude")
public class UnusedIncludeController {

	@Autowired
	private UnusedIncludeDetector unusedIncludeDetector;

	@Autowired
	private NodeService nodeService;

	@GetMapping("")
	public String showUnusedInclude(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("unusedIncludeMap", unusedIncludeDetector.getUnusedIncludeFromSmell());
		return "as/unusedInclude";
	}
}
