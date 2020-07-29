package cn.edu.fudan.se.multidependency.controller.as;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.service.query.as.impl.ArchitectureSmellDetectorImpl;

@Controller
@RequestMapping("/as")
public class ArchitectureSmellController {
	
	@Autowired
	private ArchitectureSmellDetectorImpl detector;
	
	@Autowired
	private ProjectFileRepository fileRepository;
	
	@GetMapping("/icd/{times}")
	@ResponseBody
	public Object icd(@PathVariable("times") int times) {
		return detector.cochangesInDifferentModule(times);
	}
	
	@GetMapping("/pagerank/file")
	@ResponseBody
	public Object pagerankFile() {
		return fileRepository.pageRank(20, 0.85);
	}
	
	@GetMapping("/cycle/package")
	@ResponseBody
	public Object cyclePackages(@RequestParam(required=false, name="relation", defaultValue="false") boolean relation) {
		return detector.cyclePackages(relation);
	}
	
	@GetMapping("/cycle/file")
	@ResponseBody
	public Object cycleFiles(@RequestParam(required=false, name="relation", defaultValue="false") boolean relation) {
		return detector.cycleFiles(relation);
	}
	
	@GetMapping("/unused/package")
	@ResponseBody
	public Object unusdPackages() {
		return detector.unusedPackages();
	}
	
	@GetMapping("/hublike/package")
	@ResponseBody
	public Object hubLikePackages() {
		return detector.hubLikePackages();
	}
	
	@GetMapping("/hublike/file")
	@ResponseBody
	public Object hubLikeFiles() {
		return detector.hubLikeFiles();
	}
	
	@GetMapping("/unstable/file")
	@ResponseBody
	public Object unstableFiles() {
		return detector.unstableFiles();
	}
	
	@GetMapping("/similar/file")
	@ResponseBody
	public Object similarFiles() {
		return detector.similarFiles();
	}
	
	@GetMapping("/similar/package")
	@ResponseBody
	public Object similarPackages() {
		return detector.similarPackages();
	}

}
