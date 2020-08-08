package cn.edu.fudan.se.multidependency.controller.as;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.service.query.as.ArchitectureSmellDetector;

@Controller
@RequestMapping("/as")
public class ArchitectureSmellController {
	
	@Autowired
	private ArchitectureSmellDetector detector;
	
	@Autowired
	private ProjectFileRepository fileRepository;
	
	@GetMapping("/excel/multiple")
    @ResponseBody
    public void printPackageMetric(HttpServletRequest request, HttpServletResponse response) {
		try {
	        response.addHeader("Content-Disposition", "attachment;filename=multiple_as.xlsx");  
	        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); 
			OutputStream stream = response.getOutputStream();

			detector.printMultipleASFiles(stream, 4);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping("")
	public String index() {
		return "as/as";
	}
	
	@GetMapping("/icd")
	public String icd() {
		return "as/icd";
	}
	
	@GetMapping("/cycle")
	public String cycle() {
		return "as/cycle";
	}
	
	@GetMapping("/hublike")
	public String hublike() {
		return "as/hublike";
	}
	
	@GetMapping("/unstable")
	public String unstable() {
		return "as/unstable";
	}
	
	@GetMapping("/similar")
	public String similar() {
		return "as/similar";
	}

	@GetMapping("/multiple")
	public String multiple() {
		return "as/multiple";
	}
	
	@GetMapping("/api/icd/{times}")
	@ResponseBody
	public Object icd(@PathVariable("times") int times) {
		return detector.cochangesInDifferentModule(times);
	}
	
	@GetMapping("/api/pagerank/file")
	@ResponseBody
	public Object pagerankFile() {
		return fileRepository.pageRank(20, 0.85);
	}
	
	@GetMapping("/api/cycle/package")
	@ResponseBody
	public Object cyclePackages(@RequestParam(required=false, name="relation", defaultValue="false") boolean relation) {
		detector.setCyclePackagesWithRelation(relation);
		return detector.cyclePackages();
	}
	
	@GetMapping("/api/cycle/file")
	@ResponseBody
	public Object cycleFiles(@RequestParam(required=false, name="relation", defaultValue="false") boolean relation) {
		detector.setCycleFilesWithRelation(relation);
		return detector.cycleFiles();
	}
	
	@GetMapping("/api/unused/package")
	@ResponseBody
	public Object unusdPackages() {
		return detector.unusedPackages();
	}
	
	@GetMapping("/api/hublike/package")
	@ResponseBody
	public Object hubLikePackages() {
		return detector.hubLikePackages();
	}
	
	@GetMapping("/api/hublike/file")
	@ResponseBody
	public Object hubLikeFiles() {
		return detector.hubLikeFiles();
	}
	
	@GetMapping("/api/unstable/file")
	@ResponseBody
	public Object unstableFiles() {
		return detector.unstableFiles();
	}
	
	@GetMapping("/api/similar/file")
	@ResponseBody
	public Object similarFiles() {
		return detector.similarFiles();
	}
	
	@GetMapping("/api/similar/package")
	@ResponseBody
	public Object similarPackages() {
		return detector.similarPackages();
	}
	
	@GetMapping("/api/cyclic/hierarchy")
	@ResponseBody
	public Object cyclicHierarchies() {
		return detector.cyclicHierarchies();
	}
	
	@GetMapping("/api/multiple")
	@ResponseBody
	public Object multipleAS() {
		return detector.multipleASFiles(4, false);
	}

}
