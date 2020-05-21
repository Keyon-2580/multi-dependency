package cn.edu.fudan.se.multidependency.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.service.nospring.RepositoryService;
import cn.edu.fudan.se.multidependency.service.spring.DependencyOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.MultipleService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.utils.ZTreeUtil.ZTreeNode;

@Controller
@RequestMapping("/project")
public class ProjectController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;

	@Autowired
	private DependencyOrganizationService dependencyOrganizationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private MultipleService multipleService;
	
	@GetMapping(value = "/all/{page}")
	@ResponseBody
	public List<Project> allProjectsByPage(@PathVariable("page") int page) {
		List<Project> result = staticAnalyseService.queryAllProjectsByPage(page, Constant.SIZE_OF_PAGE, "name");
		return result;
	}
	
	@GetMapping(value = "/pages/count")
	@ResponseBody
	public long queryMicroServicePagesCount() {
		long count = staticAnalyseService.countOfAllProjects();
		long pageCount = count % Constant.SIZE_OF_PAGE == 0 ? 
				count / Constant.SIZE_OF_PAGE : count / Constant.SIZE_OF_PAGE + 1;
		return pageCount;
	}
	@GetMapping(value = "/all/ztree/structure/{page}")
	@ResponseBody
	public JSONObject allMicroServicesContainProjectsByPage(@PathVariable("page") int page) {
		JSONObject result = new JSONObject();
		Iterable<Project> projects = staticAnalyseService.queryAllProjectsByPage(page, Constant.SIZE_OF_PAGE, "name");
		List<ZTreeNode> nodes = new ArrayList<>();
//		List<Thread> threads = new ArrayList<>();
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		LOGGER.info("开始时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
		for(Project project : projects) {
			ZTreeNode node = multipleService.projectToZTree(project);
			nodes.add(node);
//			Thread projectThread = new Thread(() -> {
//				ZTreeNode node = multipleService.projectToZTree(project);
//				nodes.add(node);
//			});
//			threads.add(projectThread);
//			projectThread.start();
		}
//		for(Thread thread : threads) {
//			try {
//				thread.join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		LOGGER.info("结束时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
		JSONArray values = new JSONArray();
		for(ZTreeNode node : nodes) {
			values.add(node.toJSON());
		}
		result.put("result", "success");
		result.put("values", values);
		return result;
	}

	@GetMapping(value = {"/", "/index"})
	public String index(HttpServletRequest request, @RequestParam(required=true, value="id") long id) {
		Project project = staticAnalyseService.queryProject(id);
		if(project == null) {
			request.setAttribute("error", "没有找到id为 " + id + " 的Project");
			return "error";
		} else {
			request.setAttribute("project", project);
			return "project";
		}
	}
	
	@GetMapping("/dynamiccall")
	@ResponseBody
	public void dynamicCall() {
		System.out.println("/project/dynamiccall");
	}
	
	@GetMapping("/cytoscape")
	@ResponseBody
	public JSONObject cytoscape(
			@RequestParam("projectId") Long projectId,
			@RequestParam("dependency") String dependency,
			@RequestParam("level") String level) {
		System.out.println("/project/cytoscape");
		JSONObject result = new JSONObject();
		try {
			Project project = staticAnalyseService.queryProject(projectId);
			if(project == null) {
				throw new Exception("没有找到id为 " + projectId + " 的项目");
			}
			if("dynamic".equals(dependency)) {
				List<FunctionDynamicCallFunction> calls = dynamicAnalyseService.findFunctionDynamicCallsByProject(project);
				System.out.println(calls.size());
				dependencyOrganizationService.dynamicCallDependency(calls);
				if("file".equals(level)) {
					result.put("value", dependencyOrganizationService.fileCallToCytoscape());
				} else if("directory".equals(level)) {
					result.put("value", dependencyOrganizationService.directoryCallToCytoscape());
				}
			}
			if("static".equals(dependency)) {
				result.put("value", dependencyOrganizationService.projectToCytoscape(project));
			}
			if("all".equals(dependency)) {
				List<FunctionDynamicCallFunction> calls = dynamicAnalyseService.findFunctionDynamicCallsByProject(project);
				System.out.println(calls.size());
				System.out.println("start to find clones");
				Iterable<FunctionCloneFunction> clones = staticAnalyseService.findProjectContainFunctionCloneFunctions(project);
//				result.put("value", dependencyOrganizationService.projectStaticAndDynamicToCytoscape(project, calls));
				System.out.println("end finding clones");
//				result.put("value", dependencyOrganizationService.projectToCytoscape(project, calls, clones));
				result.put("value", dependencyOrganizationService.projectToCytoscape(project));
				result.put("ztreenode", multipleService.projectToZTree(project));
			}
			System.out.println(result.get("value"));
			if(result.get("value") == null) {
				throw new Exception("结果暂无");
			}
			result.put("result", "success");
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
}
