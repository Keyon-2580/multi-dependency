package cn.edu.fudan.se.multidependency.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.JaegerService;
import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

@Controller
@RequestMapping("/dynamic")
public class DynamicController {
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;

	@Autowired
	private JaegerService jaegerService;

	@Autowired
	private FeatureOrganizationService organizationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	

}
