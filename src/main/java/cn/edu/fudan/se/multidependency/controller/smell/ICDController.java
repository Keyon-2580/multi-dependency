package cn.edu.fudan.se.multidependency.controller.smell;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.service.query.smell.ImplicitCrossModuleDependencyDetector;

@Controller
@RequestMapping("/as/icd")
public class ICDController {

	@Autowired
	private ImplicitCrossModuleDependencyDetector icdDetector;
	
	@GetMapping("")
	public String icd(HttpServletRequest request) {
		request.setAttribute("files", icdDetector.cochangesInDifferentFile());
		return "as/icd";
	}
	
	@GetMapping("/cochange")
	@ResponseBody
	public int setMinCoChange() {
		return icdDetector.getFileMinCoChange();
	}
	
	@PostMapping("/cochange")
	@ResponseBody
	public boolean setMinCoChange(@RequestParam("minCoChange") int minCoChange) {
		icdDetector.setFileMinCoChange(minCoChange);
		return true;
	}
	
}
