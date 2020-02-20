package cn.edu.fudan.se.multidependency.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

@Controller
public class MDController {
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@GetMapping(value= {"/", "/index"})
	public String index(HttpServletRequest request) {
		return "index";
	}
	
}
