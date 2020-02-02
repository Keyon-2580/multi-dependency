package cn.edu.fudan.se.multidependency.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.config.YamlConfig;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.RepositoryService;
import cn.edu.fudan.se.multidependency.utils.FileUtils;

@Controller
@RequestMapping("/insert")
public class InsertController {
	
	@Autowired
	private YamlConfig yaml;
	
	@Autowired
	private InserterForNeo4j repositorySerivce;
	
	@Bean
	private InserterForNeo4j getRepository(YamlConfig yaml) throws Exception {
		InserterForNeo4j repositoryService = RepositoryService.getInstance();
		repositoryService.setDatabasePath(yaml.getNeo4jPath());
		repositoryService.setDelete(yaml.isNeo4jDelete());
		return repositoryService;
	}
	
	@GetMapping({"/index", "/"})
	public String index(HttpServletRequest request, HttpServletResponse response) {
		String rootDirectoryPath = yaml.getRootPath();
		File rootDirectory = new File(rootDirectoryPath);
		List<File> projectDirectories = new ArrayList<>();
		FileUtils.listDirectories(rootDirectory, yaml.getDepth(), projectDirectories);
		System.out.println(projectDirectories.size());
		
		return "index";
	}
	
	
	@GetMapping("")
	public void test() {
		
	}
	
}
