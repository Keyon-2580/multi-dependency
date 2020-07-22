package cn.edu.fudan.se.multidependency.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependOn;
import cn.edu.fudan.se.multidependency.service.spring.as.impl.ArchitectureSmellDetectorImpl;

@Controller
@RequestMapping("/as")
public class ArchitectureSmellController {
	
	@Autowired
	private ArchitectureSmellDetectorImpl detector;
	
	@GetMapping("/cycle/package")
	@ResponseBody
	public Collection<Collection<DependOn>>  cyclePackages() {
		return detector.findCyclePackages();
	}
	
	@GetMapping("/unused/package")
	@ResponseBody
	public Collection<Package> unusdPackages() {
		return detector.unusedPackages();
	}
}
