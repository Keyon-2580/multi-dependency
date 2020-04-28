package cn.edu.fudan.se.multidependency.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class MDController {

	@GetMapping(value= {"/", "/index"})
	public String index(HttpServletRequest request, @RequestHeader HttpHeaders headers) {
		return "index";
	}
	
}
