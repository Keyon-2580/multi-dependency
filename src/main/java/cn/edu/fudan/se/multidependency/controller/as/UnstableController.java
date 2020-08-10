package cn.edu.fudan.se.multidependency.controller.as;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.UnstableDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/unstable")
public class UnstableController {
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private UnstableDependencyDetector unstableDependencyDetector;
	
	@GetMapping("")
	public String hubLike(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("files", unstableDependencyDetector.unstableFiles());
		request.setAttribute("packages", unstableDependencyDetector.unstablePackages());
		return "as/unstable";
	}
	
	@GetMapping("/threshold/{projectId}")
	@ResponseBody
	public int[] minFanIOs(@PathVariable("projectId") long projectId) {
		Project project = nodeService.queryProject(projectId);
		int[] result = new int[3];
		if(project == null) {
			return result;
		}
		result[0] = unstableDependencyDetector.getFanInThreshold(project);
		result[1] = unstableDependencyDetector.getCoChangeTimesThreshold(project);
		result[2] = unstableDependencyDetector.getCoChangeFilesThreshold(project);
		return result;
	}
	
	@PostMapping("/threshold/{projectId}")
	@ResponseBody
	public boolean minFanIOs(@PathVariable("projectId") long projectId, 
			@RequestParam("fanInThreshold") int fanInThreshold,
			@RequestParam("cochangeTimesThreshold") int cochangeTimesThreshold,
			@RequestParam("cochangeFilesThreshold") int cochangeFilesThreshold) {
		Project project = nodeService.queryProject(projectId);
		if(project == null) {
			return false;
		}
		unstableDependencyDetector.setFanInThreshold(project, fanInThreshold);
		unstableDependencyDetector.setCoChangeTimesThreshold(project, cochangeTimesThreshold);
		unstableDependencyDetector.setCoChangeFilesThreshold(project, cochangeFilesThreshold);
		return true;
	}

}