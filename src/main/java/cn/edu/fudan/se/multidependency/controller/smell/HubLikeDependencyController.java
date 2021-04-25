package cn.edu.fudan.se.multidependency.controller.smell;

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
import cn.edu.fudan.se.multidependency.service.query.smell.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/hublike")
public class HubLikeDependencyController {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private HubLikeComponentDetector hubLikeComponentDetector;
	
	@GetMapping("/query")
	public String queryHubLike(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileHubLikeDependencyMap", hubLikeComponentDetector.queryFileHubLike());
		request.setAttribute("packageHubLikeDependencyMap", hubLikeComponentDetector.queryPackageHubLike());
		return "as/hublikedependency";
	}

	@GetMapping("/detect")
	public String detectHubLike(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileHubLikeDependencyMap", hubLikeComponentDetector.detectFileHubLike());
		request.setAttribute("packageHubLikeDependencyMap", hubLikeComponentDetector.detectPackageHubLike());
		return "as/hublikedependency";
	}
	
	@GetMapping("/fanio/{projectId}")
	@ResponseBody
	public int[] minFanIOs(@PathVariable("projectId") long projectId) {
		Project project = nodeService.queryProject(projectId);
		int[] result = new int[4];
		if(project == null) {
			return result;
		}
		int[] minFileIO = hubLikeComponentDetector.getProjectMinFileFanIO(project);
		int[] minPackageIO = hubLikeComponentDetector.getProjectMinPackageFanIO(project);
		result[0] = minFileIO[0];
		result[1] = minFileIO[1];
		result[2] = minPackageIO[0];
		result[3] = minPackageIO[1];
		return result;
	}

	@PostMapping("/fanio/{projectId}")
	@ResponseBody
	public boolean hublike(@PathVariable("projectId") long projectId, 
			@RequestParam("hubLikeMinFileFanIn") int hubLikeMinFileFanIn, @RequestParam("hubLikeMinFileFanOut") int hubLikeMinFileFanOut, 
			@RequestParam("hubLikeMinPackageFanIn") int hubLikeMinPackageFanIn, @RequestParam("hubLikeMinPackageFanOut") int hubLikeMinPackageFanOut) {
		Project project = nodeService.queryProject(projectId);
		if(project == null) {
			return false;
		}
		hubLikeComponentDetector.setProjectMinFileFanIO(project, hubLikeMinFileFanIn, hubLikeMinFileFanOut);
		hubLikeComponentDetector.setProjectMinPackageFanIO(project, hubLikeMinPackageFanIn, hubLikeMinPackageFanOut);
		return true;
	}
	
}
