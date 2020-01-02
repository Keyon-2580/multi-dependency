package cn.edu.fudan.se.multidependency.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicTestCaseToFileDependency;
import cn.edu.fudan.se.multidependency.repository.node.code.FunctionRepository;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

@Controller
public class MDController {
	
	
	@Autowired
	private StaticAnalyseService staticCodeService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
	
	@Autowired
	private FunctionRepository functionRepository;	
	
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
	
	@RequestMapping("/dynamic/{featureName}")
	@ResponseBody
	public void dynamic(@PathVariable("featureName") String featureName) {
		System.out.println(featureName);
		List<DynamicTestCaseToFileDependency> result = dynamicAnalyseService.findDependencyFiles(featureName);
		System.out.println(result.size());
		for(DynamicTestCaseToFileDependency r : result) {
			System.out.println(r.getTestCase().getTestCaseName() + " " + r.getProjectFiles().size());
		}
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
		System.out.println(functionRepository.findAll());
		ProjectFile file = new ProjectFile();
		file.setId(1270L);
		staticCodeService.findTypesInFile(file);
		staticCodeService.findAllExtends();
		for(Function function : functionRepository.findAll()) {
			System.out.println(function.getParametersIdentifies());
		}
		return "";
	}
	
	
}
