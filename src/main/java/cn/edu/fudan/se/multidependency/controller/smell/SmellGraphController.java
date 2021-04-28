package cn.edu.fudan.se.multidependency.controller.smell;

import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.UnusedIncludeDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/as/smellgraph")
public class SmellGraphController {

	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;

	@Autowired
	private UnusedIncludeDetector unusedIncludeDetector;

	@GetMapping("")
	public String cyclicHierarchy(HttpServletRequest request, @RequestParam("smelltype") String smellType, @RequestParam("fileid") long fileId) {
		switch (smellType){
			case SmellType.CYCLIC_DEPENDENCY:
				request.setAttribute("json_data", cyclicDependencyDetector.getFileCyclicDependencyJson(fileId));
				break;
			case SmellType.UNUSED_INCLUDE:
				request.setAttribute("json_data", unusedIncludeDetector.getFileUnusedIncludeJson(fileId));
				break;
			default:
				break;
		}
		return "as/smellgraph";
	}

	@GetMapping("/{projectId}")
	public String getSmellJson(HttpServletRequest request, @PathVariable("projectId") Long projectId, @RequestParam("smelltype") String smellType, @RequestParam("smelllevel") String smellLevel, @RequestParam("smellindex") int smellIndex) {
		String smellName = smellLevel + "_" + smellType + "_" + smellIndex;
		if (smellType.equals(SmellType.CYCLIC_DEPENDENCY)) {
			if (smellLevel.equals(SmellLevel.TYPE)) {
				request.setAttribute("json_data", cyclicDependencyDetector.getTypeCyclicDependencyJson(projectId, smellName));
			}
			else if (smellLevel.equals(SmellLevel.FILE)) {
				request.setAttribute("json_data", cyclicDependencyDetector.getFileCyclicDependencyJson(projectId, smellName));
			}
			else if (smellLevel.equals(SmellLevel.PACKAGE)) {
				request.setAttribute("json_data", cyclicDependencyDetector.getPackageCyclicDependencyJson(projectId, smellName));
			}
		}
		else if (smellType.equals(SmellType.UNUSED_INCLUDE)) {
			if (smellLevel.equals(SmellLevel.FILE)) {
				request.setAttribute("json_data", unusedIncludeDetector.getFileUnusedIncludeJson(projectId, smellName));
			}
		}
		return "as/smellgraph";
	}
}
