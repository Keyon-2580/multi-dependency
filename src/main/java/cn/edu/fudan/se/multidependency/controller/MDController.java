package cn.edu.fudan.se.multidependency.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MDController {
	
	@RequestMapping("/")
	@ResponseBody
	public String index() {
		return "hello";
	}
	
}
