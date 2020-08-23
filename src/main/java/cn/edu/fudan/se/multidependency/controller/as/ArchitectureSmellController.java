package cn.edu.fudan.se.multidependency.controller.as;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.service.query.as.impl.ArchitectureSmellDetectorImpl;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/as")
public class ArchitectureSmellController {
	
	@Autowired
	private ArchitectureSmellDetectorImpl detector;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/issue/pie")
	@ResponseBody
	public Object issue() {
		return detector.smellAndIssueFiles();
	}
	
	@GetMapping("")
	public String index(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		return "as/as";
	}
	
	@GetMapping("/multiple")
	public String multiple() {
		return "as/multiple";
	}
	
	@GetMapping("/api/multiple")
	@ResponseBody
	public Object multipleAS() {
		return detector.multipleASFiles(false);
	}
	
	@GetMapping("/excel/multiple")
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
