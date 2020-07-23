package cn.edu.fudan.se.multidependency.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependOn;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.service.spring.as.impl.ArchitectureSmellDetectorImpl;
import cn.edu.fudan.se.multidependency.service.spring.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.spring.metric.PackageMetrics;

@Controller
@RequestMapping("/as")
public class ArchitectureSmellController {
	
	@Autowired
	private ArchitectureSmellDetectorImpl detector;
	
	@Autowired
	private ProjectFileRepository fileRepository;
	
	@GetMapping("pagerank/file")
	@ResponseBody
	public Collection<ProjectFile> pagerankFile() {
		return fileRepository.pageRank(20, 0.85);
	}
	
	@GetMapping("/cycle/package")
	@ResponseBody
	public Collection<Collection<DependOn>> cyclePackages() {
		return detector.cyclePackages();
	}
	
	@GetMapping("/unused/package")
	@ResponseBody
	public Collection<Package> unusdPackages() {
		return detector.unusedPackages();
	}
	
	@GetMapping("/hublike/package")
	@ResponseBody
	public Collection<PackageMetrics> hubLikePackages() {
		return detector.hubLikePackages();
	}
	
	@GetMapping("/hublike/file")
	@ResponseBody
	public Collection<FileMetrics> hubLikeFiles() {
		return detector.hubLikeFiles();
	}

}
