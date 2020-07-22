package cn.edu.fudan.se.multidependency.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.service.spring.as.ArchitectureSmellDetectorImpl;

@Controller
@RequestMapping("/as")
public class ArchitectureSmellController {
	
	
	@Autowired
	private ArchitectureSmellDetectorImpl detector;

	@GetMapping("/cycle/package")
	@ResponseBody
	public Object cyclePackages() {
		return detector.findCyclePackages();
	}
	
}
