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
	public String queryHubLikeDependency(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileHubLikeDependencyMap", hubLikeComponentDetector.queryFileHubLikeDependency());
		request.setAttribute("packageHubLikeDependencyMap", hubLikeComponentDetector.queryPackageHubLikeDependency());
		return "as/hublikedependency";
	}

	@GetMapping("/detect")
	public String detectHubLikeDependency(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("fileHubLikeDependencyMap", hubLikeComponentDetector.detectFileHubLikeDependency());
		request.setAttribute("packageHubLikeDependencyMap", hubLikeComponentDetector.detectPackageHubLikeDependency());
		return "as/hublikedependency";
	}
	
	@GetMapping("/fanio/{projectId}")
	@ResponseBody
	public Integer[] getMinFanIO(@PathVariable("projectId") long projectId) {
		Integer[] result = new Integer[4];
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			Integer[] minFileIO = hubLikeComponentDetector.getProjectMinFileFanIO(project.getId());
			Integer[] minPackageIO = hubLikeComponentDetector.getProjectMinPackageFanIO(project.getId());
			result[0] = minFileIO[0];
			result[1] = minFileIO[1];
			result[2] = minPackageIO[0];
			result[3] = minPackageIO[1];
		}
		return result;
	}

	@PostMapping("/fanio/{projectId}")
	@ResponseBody
	public boolean setMinFanIO(@PathVariable("projectId") long projectId,
								@RequestParam("minFileFanIn") int minFileFanIn, @RequestParam("minFileFanOut") int minFileFanOut,
								@RequestParam("minPackageFanIn") int minPackageFanIn, @RequestParam("minPackageFanOut") int minPackageFanOut) {
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			hubLikeComponentDetector.setProjectMinFileFanIO(project.getId(), minFileFanIn, minFileFanOut);
			hubLikeComponentDetector.setProjectMinPackageFanIO(project.getId(), minPackageFanIn, minPackageFanOut);
			return true;
		}
		return false;
	}
}
