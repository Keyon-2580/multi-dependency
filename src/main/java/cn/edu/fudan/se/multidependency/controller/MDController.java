package cn.edu.fudan.se.multidependency.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

@Controller
public class MDController {
	
	
	@Autowired
	private StaticAnalyseService staticCodeService;
	
//	@Bean
//	private DependsEntityRepoExtractor getEntityRepoExtractor() {
//		
//		return null;
//	}
	
//	@Autowired
//	private PropertyConfig conf;

	@RequestMapping("/")
	@ResponseBody
	public String index() {
		return "hello";
	}
	
	@RequestMapping("/test")
	@ResponseBody
	public void test() {
		ProjectFile file = new ProjectFile();
		file.setId(1270L);
		staticCodeService.findTypesInFile(file);
	}
	
	@Bean
	public String testBean() {
		ProjectFile file = new ProjectFile();
		file.setId(1270L);
		staticCodeService.findTypesInFile(file);
		staticCodeService.findAllExtends();
		return "";
	}
	
	
}
