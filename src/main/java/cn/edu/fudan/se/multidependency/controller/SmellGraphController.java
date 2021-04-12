package cn.edu.fudan.se.multidependency.controller;

import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.UnusedIncludeDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/smellGraph")
public class SmellGraphController {

	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;

	@Autowired
	private UnusedIncludeDetector unusedIncludeDetector;

	@GetMapping("")
	public String cyclicHierarchy(HttpServletRequest request, @RequestParam("smellType") String smellType, @RequestParam("fileId") long fileId) {
		switch (smellType){
			case SmellType.CYCLIC_DEPENDENCY:
				request.setAttribute("json_data", cyclicDependencyDetector.getCyclicDependencyJson(fileId));
				break;
			case SmellType.UNUSED_INCLUDE:
				request.setAttribute("json_data", unusedIncludeDetector.getUnusedIncludeJson(fileId));
				break;
			default:
				break;
		}
		return "as/smellGraph";
	}
}
