package cn.edu.fudan.se.multidependency.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MDController {
	
	@GetMapping(value= {"/", "/index"})
	public String index(HttpServletRequest request) {
		return "index";
	}
	
}
