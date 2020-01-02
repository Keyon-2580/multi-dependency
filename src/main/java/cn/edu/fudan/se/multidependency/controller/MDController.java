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
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
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
		System.out.println("featureName " + featureName);
		List<TestCase> testCases = dynamicAnalyseService.findTestCasesByFeatureName(featureName);
		System.out.println(testCases.size());
		
		List<DynamicTestCaseToFileDependency> allDependencies = dynamicAnalyseService.findDependencyFilesByFeatureName(featureName);
		System.out.println(featureName + " " + allDependencies.size());
		for(DynamicTestCaseToFileDependency dependencies : allDependencies) {
			System.out.println("测试用例名：" + dependencies.getTestCase().getTestCaseName());
		}
		
		int fileSize = 0;
		for(ProjectFile file : dynamicAnalyseService.findAllDependencyFilesByFeatureName(featureName)) {
			System.out.println(file.getPath());
			fileSize++;
		}
		System.out.println("总数：" + fileSize);
	}
	
	
}
