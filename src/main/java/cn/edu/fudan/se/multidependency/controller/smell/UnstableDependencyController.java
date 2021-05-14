package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableDependencyDetectorUsingHistory;
import cn.edu.fudan.se.multidependency.service.query.smell.UnstableDependencyDetectorUsingInstability;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as/unstabledependency")
public class UnstableDependencyController {
	@Autowired
	private NodeService nodeService;

	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private UnstableDependencyDetectorUsingHistory unstableDependencyDetectorUsingHistory;
	
	@Autowired
	private UnstableDependencyDetectorUsingInstability unstableDependencyDetectorUsingInstability;
	
	@GetMapping("/query")
	public String queryUnstableDependency(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileUnstableDependencyMap", unstableDependencyDetectorUsingInstability.queryFileUnstableDependency());
					request.setAttribute("packageUnstableDependencyMap", null);
					break;
				case SmellLevel.PACKAGE:
					request.setAttribute("fileUnstableDependencyMap", null);
					request.setAttribute("packageUnstableDependencyMap", unstableDependencyDetectorUsingInstability.queryPackageUnstableDependency());
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileUnstableDependencyMap", unstableDependencyDetectorUsingInstability.queryFileUnstableDependency());
					request.setAttribute("packageUnstableDependencyMap", unstableDependencyDetectorUsingInstability.queryPackageUnstableDependency());
					break;
			}
		}
		return "as/unstabledependency";
	}
	@GetMapping("/detect")
	public String detectUnstableDependency(HttpServletRequest request, @RequestParam("projectid") Long projectId, @RequestParam("smelllevel") String smellLevel) {
		Project project = projectRepository.findProjectById(projectId);
		if (project != null) {
			request.setAttribute("project", project);
			switch (smellLevel) {
				case SmellLevel.FILE:
					request.setAttribute("fileUnstableDependencyMap", unstableDependencyDetectorUsingInstability.detectFileUnstableDependency());
					request.setAttribute("packageUnstableDependencyMap", null);
					break;
				case SmellLevel.PACKAGE:
					request.setAttribute("fileUnstableDependencyMap", null);
					request.setAttribute("packageUnstableDependencyMap", unstableDependencyDetectorUsingInstability.detectPackageUnstableDependency());
					break;
				case SmellLevel.MULTIPLE_LEVEL:
					request.setAttribute("fileUnstableDependencyMap", unstableDependencyDetectorUsingInstability.detectFileUnstableDependency());
					request.setAttribute("packageUnstableDependencyMap", unstableDependencyDetectorUsingInstability.detectPackageUnstableDependency());
					break;
			}
		}
		return "as/unstabledependency";
	}
	
	@GetMapping("/threshold/instability/{projectId}")
	@ResponseBody
	public Double[] getProjectMinFanOutInstability(@PathVariable("projectId") long projectId) {
		Double[] result = new Double[3];
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			result[0] = Double.valueOf(unstableDependencyDetectorUsingInstability.getProjectMinFileFanOut(projectId));
			result[1] = Double.valueOf(unstableDependencyDetectorUsingInstability.getProjectMinPackageFanOut(projectId));
			result[2] = unstableDependencyDetectorUsingInstability.getProjectMinRatio(projectId);
		}
		return result;
	}
	
	@PostMapping("/threshold/instability/{projectId}")
	@ResponseBody
	public boolean setProjectMinFanOutInstability(@PathVariable("projectId") Long projectId,
												 @RequestParam("minFileFanOut") Integer minFileFanOut,
												 @RequestParam("minPackageFanOut") Integer minPackageFanOut,
												 @RequestParam("minRatio") Double minRatio) {
		Project project = nodeService.queryProject(projectId);
		if(project != null) {
			unstableDependencyDetectorUsingInstability.setProjectMinFileFanOut(project.getId(), minFileFanOut);
			unstableDependencyDetectorUsingInstability.setProjectMinPackageFanOut(project.getId(), minPackageFanOut);
			unstableDependencyDetectorUsingInstability.setProjectMinRatio(project.getId(), minRatio);
			return true;
		}
		return false;
	}
	
	@GetMapping("/threshold/history/{projectId}")
	@ResponseBody
	public int[] getHistory(@PathVariable("projectId") long projectId) {
		Project project = nodeService.queryProject(projectId);
		int[] result = new int[3];
		if(project == null) {
			return result;
		}
		result[0] = unstableDependencyDetectorUsingHistory.getFanInThreshold(project);
		result[1] = unstableDependencyDetectorUsingHistory.getCoChangeTimesThreshold(project);
		result[2] = unstableDependencyDetectorUsingHistory.getCoChangeFilesThreshold(project);
		return result;
	}
	
	@PostMapping("/threshold/history/{projectId}")
	@ResponseBody
	public boolean setHistory(@PathVariable("projectId") long projectId, 
			@RequestParam("fanInThreshold") int fanInThreshold,
			@RequestParam("cochangeTimesThreshold") int cochangeTimesThreshold,
			@RequestParam("cochangeFilesThreshold") int cochangeFilesThreshold) {
		Project project = nodeService.queryProject(projectId);
		if(project == null) {
			return false;
		}
		unstableDependencyDetectorUsingHistory.setFanInThreshold(project, fanInThreshold);
		unstableDependencyDetectorUsingHistory.setCoChangeTimesThreshold(project, cochangeTimesThreshold);
		unstableDependencyDetectorUsingHistory.setCoChangeFilesThreshold(project, cochangeFilesThreshold);
		return true;
	}

}
