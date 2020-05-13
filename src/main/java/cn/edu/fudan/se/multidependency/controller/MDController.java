package cn.edu.fudan.se.multidependency.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import cn.edu.fudan.se.multidependency.service.spring.MultipleService;

@Controller
public class MDController {
	
	@Autowired
	private MultipleService multipleService;

	@GetMapping(value= {"/", "/index"})
	public String index(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
		return "index";
	}
	
	@GetMapping("/multiple")
	public String multiple(HttpServletRequest request) {
		request.setAttribute("ztree", multipleService.allNodesToZTree());
		return "multiple";
	}
	
}
