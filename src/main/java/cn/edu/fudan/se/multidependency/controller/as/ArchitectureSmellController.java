package cn.edu.fudan.se.multidependency.controller.as;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.service.query.as.MultipleArchitectureSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.MultipleAS;
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
	
	@GetMapping("/multiple")
	public String multiple(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("files", detector.multipleASFiles(false));
		return "as/multiple";
	}
	
	@GetMapping("/multiple/histogram")
	@ResponseBody
	public Object histogram() {
		return detector.projectHistogramOnVersion();
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
			@RequestParam(name="unused", required=false, defaultValue="true") boolean unused) {
//		boolean cycle = (boolean) params.getOrDefault("cycle", true);
//		boolean hublike = (boolean) params.getOrDefault("hublike", true);
//		boolean logicCoupling = (boolean) params.getOrDefault("logicCoupling", true);
//		boolean similar = (boolean) params.getOrDefault("similar", true);
//		boolean unstable = (boolean) params.getOrDefault("unstable", true);
//		boolean hierarchy = (boolean) params.getOrDefault("hierarchy", true);
//		boolean godComponent = (boolean) params.getOrDefault("godComponent", true);
//		boolean unused = (boolean) params.getOrDefault("unused", true);
		System.out.println(cycle + " " + hublike + " " + logicCoupling + " " + similar + " " + unstable + " " + hierarchy + " " + godComponent + " " + unused);
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
			@Override
			public boolean isCyclicHierarchy() {
				return hierarchy;
			}
			@Override
			public boolean isGod() {
				return godComponent;
			}
			@Override
			public boolean isUnused() {
				return unused;
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
