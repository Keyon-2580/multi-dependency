package cn.edu.fudan.se.multidependency.controller.as;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.query.as.UnusedComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@RequestMapping("/as/unused")
@Controller
public class UnusedComponentController {

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private UnusedComponentDetector unusedComponentDetector;
	
	@GetMapping("")
	public String unused(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("packages", unusedComponentDetector.unusedPackage());
		return "as/unused";
	}
	
}
