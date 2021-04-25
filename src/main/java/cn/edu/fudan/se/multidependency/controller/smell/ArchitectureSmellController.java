package cn.edu.fudan.se.multidependency.controller.smell;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.MultipleArchitectureSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.CirclePacking;
import cn.edu.fudan.se.multidependency.service.query.smell.data.MultipleAS;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as")
public class ArchitectureSmellController {
	
	@Autowired
	private MultipleArchitectureSmellDetector detector;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("")
	public String index(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		return "as/as";
	}
	
	@GetMapping("/multiple/query")
	public String queryMultipleSmell(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("files", detector.queryMultipleSmellASFiles(false));
		return "as/multiple";
	}

	@GetMapping("/multiple/detect")
	public String detectMultipleSmell(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("files", detector.detectMultipleSmellASFiles(false));
		return "as/multiple";
	}
	
	@GetMapping("/multiple/histogram")
	@ResponseBody
	public Object histogram() {
		return detector.projectHistogramOnVersion();
	}
	
	@GetMapping("/multiple/project/{projectId}")
	public String projectMultiple(HttpServletRequest request, @PathVariable("projectId") long projectId,
			@RequestParam(name="cycle", required=false, defaultValue="true") boolean cycle,
			@RequestParam(name="hublike", required=false, defaultValue="true") boolean hublike,
			@RequestParam(name="logicCoupling", required=false, defaultValue="true") boolean logicCoupling,
			@RequestParam(name="similar", required=false, defaultValue="true") boolean similar,
			@RequestParam(name="unstable", required=false, defaultValue="true") boolean unstable,
			@RequestParam(name="hierarchy", required=false, defaultValue="true") boolean hierarchy,
			@RequestParam(name="godComponent", required=false, defaultValue="true") boolean godComponent,
			@RequestParam(name="unused", required=false, defaultValue="true") boolean unused,
			@RequestParam(name="unutilized", required=false, defaultValue="true") boolean unutilized) {
		Project project = nodeService.queryProject(projectId);
		request.setAttribute("project", project);
		Map<Long, List<CirclePacking>> circlePackings = detector.circlePacking(new MultipleAS() {
			@Override
			public boolean isCycle() {
				return cycle;
			}
			@Override
			public boolean isHublike() {
				return hublike;
			}
			@Override
			public boolean isLogicCoupling() {
				return logicCoupling;
			}
			@Override
			public boolean isSimilar() {
				return similar;
			}
			@Override
			public boolean isUnstable() {
				return unstable;
			}
			/*@Override
			public boolean isCyclicHierarchy() {
				return hierarchy;
			}
			@Override
			public boolean isGod() {
				return godComponent;
			}*/
			@Override
			public boolean isUnused() {
				return unused;
			}
			@Override
			public boolean isUnutilized() {
				return unutilized;
			}
		});
		List<CirclePacking> circlePacking = circlePackings.getOrDefault(project.getId(), new ArrayList<>());
		request.setAttribute("circlePacking", circlePacking);
		return "as/multipleproject";
	}

	@GetMapping("/issue/circle")
	@ResponseBody
	public Object circle(
			@RequestParam(name="cycle", required=false, defaultValue="true") boolean cycle,
			@RequestParam(name="hublike", required=false, defaultValue="true") boolean hublike,
			@RequestParam(name="logicCoupling", required=false, defaultValue="true") boolean logicCoupling,
			@RequestParam(name="similar", required=false, defaultValue="true") boolean similar,
			@RequestParam(name="unstable", required=false, defaultValue="true") boolean unstable,
			@RequestParam(name="hierarchy", required=false, defaultValue="true") boolean hierarchy,
			@RequestParam(name="godComponent", required=false, defaultValue="true") boolean godComponent,
			@RequestParam(name="unused", required=false, defaultValue="true") boolean unused,
			@RequestParam(name="unutilized", required=false, defaultValue="true") boolean unutilized) {
		return detector.circlePacking(new MultipleAS() {
			@Override
			public boolean isCycle() {
				return cycle;
			}
			@Override
			public boolean isHublike() {
				return hublike;
			}
			@Override
			public boolean isLogicCoupling() {
				return logicCoupling;
			}
			@Override
			public boolean isSimilar() {
				return similar;
			}
			@Override
			public boolean isUnstable() {
				return unstable;
			}
			/*@Override
			public boolean isCyclicHierarchy() {
				return hierarchy;
			}
			@Override
			public boolean isGod() {
				return godComponent;
			}*/
			@Override
			public boolean isUnused() {
				return unused;
			}
			@Override
			public boolean isUnutilized() {
				return unutilized;
			}
		});
	}
	
	@GetMapping("/issue/pie")
	@ResponseBody
	public Object issue(
			@RequestParam(name="cycle", required=false, defaultValue="true") boolean cycle,
			@RequestParam(name="hublike", required=false, defaultValue="true") boolean hublike,
			@RequestParam(name="logicCoupling", required=false, defaultValue="true") boolean logicCoupling,
			@RequestParam(name="similar", required=false, defaultValue="true") boolean similar,
			@RequestParam(name="unstable", required=false, defaultValue="true") boolean unstable,
			@RequestParam(name="hierarchy", required=false, defaultValue="true") boolean hierarchy,
			@RequestParam(name="godComponent", required=false, defaultValue="true") boolean godComponent,
			@RequestParam(name="unused", required=false, defaultValue="true") boolean unused,
			@RequestParam(name="unutilized", required=false, defaultValue="true") boolean unutilized) {
		return detector.smellAndIssueFiles(new MultipleAS() {
			@Override
			public boolean isCycle() {
				return cycle;
			}
			@Override
			public boolean isHublike() {
				return hublike;
			}
			@Override
			public boolean isLogicCoupling() {
				return logicCoupling;
			}
			@Override
			public boolean isSimilar() {
				return similar;
			}
			@Override
			public boolean isUnstable() {
				return unstable;
			}
			/*@Override
			public boolean isCyclicHierarchy() {
				return hierarchy;
			}
			@Override
			public boolean isGod() {
				return godComponent;
			}*/
			@Override
			public boolean isUnused() {
				return unused;
			}
			@Override
			public boolean isUnutilized() {
				return unutilized;
			}
		});
	}
	
	@GetMapping("/multiple/excel")
    @ResponseBody
    public void printPackageMetric(HttpServletRequest request, HttpServletResponse response) {
		try {
	        response.addHeader("Content-Disposition", "attachment;filename=multiple_as.xlsx");  
	        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); 
			OutputStream stream = response.getOutputStream();

			detector.printMultipleASFiles(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
