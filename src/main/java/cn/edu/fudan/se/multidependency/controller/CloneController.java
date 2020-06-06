package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.service.spring.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;
import cn.edu.fudan.se.multidependency.service.spring.show.CloneShowService;

@Controller
@RequestMapping("/clone")
public class CloneController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneController.class);
	
	@Autowired
	private CloneAnalyseService cloneAnalyse;

	@Autowired
	private CloneShowService cloneShow;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private MicroserviceService msService;
	
	@GetMapping("/{level}")
	public String cloneIndex(@PathVariable("level") String level, 
			HttpServletRequest request,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass);
		request.setAttribute("level", level);
		request.setAttribute("removeFileClone", removeFileClone);
		request.setAttribute("removeDataClass", removeDataClass);
		return "clone";
	}
	
	@GetMapping("/{level}/table/microservice")
	@ResponseBody
	public JSONObject cloneMicroService(@PathVariable("level") String level,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass);
		JSONObject result = new JSONObject();
		Collection<MicroService> mss = msService.findAllMicroService();
		if("function".equals(level)) {
			LOGGER.info("function level table, get data");
			Map<Integer, Map<Long, CloneLineValue<MicroService>>> data = cloneAnalyse.msCloneLineValuesCalculateGroupByFunction(mss, removeFileClone);
			result.put("data", data);
			LOGGER.info("get microservices");
			Collection<MicroService> microservices = cloneAnalyse.msSortByMsCloneLineCount(mss, CloneLevel.function, removeFileClone, removeDataClass);
			result.put("microservices", microservices);
		} else if("file".equals(level)) {
			LOGGER.info("file level table, get data");
			Map<Integer, Map<Long, CloneLineValue<MicroService>>> data = cloneAnalyse.msCloneLineValuesCalculateGroupByFile(mss, removeFileClone);
			result.put("data", data);
			LOGGER.info("get microservices");
			Collection<MicroService> microservices = cloneAnalyse.msSortByMsCloneLineCount(mss, CloneLevel.file, removeFileClone, removeDataClass);
			result.put("microservices", microservices);
		}
		return result;
	}
	
	@GetMapping("/{level}/group/histogram")
	@ResponseBody
	public JSONObject cloneGroupToHistogram(@PathVariable("level") String level,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass);
		JSONObject result = new JSONObject();
		try {
			JSONObject histograms = new JSONObject();
			JSONArray nodeSizeArray = new JSONArray();
			JSONArray projectSizeArray = new JSONArray();
			Collection<Collection<? extends Node>> nodeGroups = new ArrayList<>();
			if(CloneLevel.function.toString().equals(level)) {
				LOGGER.info("function level histogram");
				nodeGroups = cloneAnalyse.groupFunctionCloneNode(removeFileClone);
			} else {
				LOGGER.info("file level histogram");
				nodeGroups = cloneAnalyse.groupFileCloneNode(removeDataClass);
			}
			LOGGER.info("克隆组数：" + nodeGroups.size());
			int size = 0;
			for(Collection<? extends Node> nodes : nodeGroups) {
				size++;
				int nodeSize = 0, projectSize = 0;
				Set<Node> projects = new HashSet<>();
				for(Node node : nodes) {
					nodeSize++;
					if(node instanceof Function) {
						ProjectFile belongToFile = containRelationService.findFunctionBelongToFile((Function) node);
						Project belongToProject = containRelationService.findFileBelongToProject(belongToFile);
						MicroService belongToMS = containRelationService.findProjectBelongToMicroService(belongToProject);
						if(belongToMS == null) {
							projects.add(belongToProject);
						} else {
							projects.add(belongToMS);
						}
					} else if(node instanceof ProjectFile) {
						Project belongToProject = containRelationService.findFileBelongToProject((ProjectFile) node);
						MicroService belongToMS = containRelationService.findProjectBelongToMicroService(belongToProject);
						if(belongToMS == null) {
							projects.add(belongToProject);
						} else {
							projects.add(belongToMS);
						}
					}
				}
				projectSize = projects.size();
				nodeSizeArray.add(nodeSize);
				projectSizeArray.add(projectSize);
			}
			histograms.put("nodeSize", nodeSizeArray);
			histograms.put("projectSize", projectSizeArray);
			result.put("result", "success");
			result.put("value", histograms);
			result.put("size", size);
		} catch (Exception e) {
			
		}
		return result;
	}
	
	@GetMapping("/{level}/group/cytoscape/{index}")
	@ResponseBody
	public JSONObject cloneGroupByIndexToCytoscape(@PathVariable("level") String level, 
			@PathVariable("index") int index,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass);
		JSONObject result = new JSONObject();
		try {
			List<Integer> groups = new ArrayList<>();
			groups.add(index);
			JSONObject value = cloneShow.clonesGroupsToCytoscape(groups, CloneLevel.valueOf(level), false, removeFileClone, removeDataClass);
			result.put("result", "success");
			result.put("value", value);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	@GetMapping("/{level}/group/cytoscape")
	@ResponseBody
	public JSONObject cloneGroupToCytoscape(@PathVariable("level") String level, 
			@RequestParam("top") int top,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		JSONObject result = new JSONObject();
		try {
			JSONArray cytoscapeArray = new JSONArray();
			for(int i = 0; i < top; i++) {
				List<Integer> groups = new ArrayList<>();
				groups.add(i);
				cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(groups, CloneLevel.valueOf(level), false, removeFileClone, removeDataClass));
			}
			result.put("size", top);
			result.put("result", "success");
			result.put("value", cytoscapeArray);
			List<Integer> groups = new ArrayList<>();
			for(int j = 0; j < top; j++) {
				groups.add(j);
			}
			result.put("groupValue", cloneShow.clonesGroupsToCytoscape(groups, CloneLevel.valueOf(level), true, removeFileClone, removeDataClass));
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
//	@GetMapping("/microservice/line")
//	@ResponseBody
//	public Object msCloneLineValue() {
//		Map<Long, CloneLineValue<MicroService>> result = new HashMap<>();
//		for(Map.Entry<MicroService, CloneLineValue<MicroService>> entry : cloneAnalyse.msCloneLineValues(msService.findAllMicroService()).entrySet()) {
//			result.put(entry.getKey().getId(), entry.getValue());
//		}
//		return result;
//	}
	
//	@GetMapping("/{level}/microservice/{group}")
//	@ResponseBody
//	public Object cloneMicroService(@PathVariable("level") String level, @PathVariable("group") int group, @RequestParam("microserviceId") long id) {
//		MicroService ms = msService.findMicroServiceById(id);
//		if(ms == null) {
//			return "";
//		}
//		return cloneAnalyse.msCloneLineValuesGroup(ms, group, CloneLevel.valueOf(level));
//	}
	
//	@GetMapping("/{level}/project")
//	@ResponseBody
//	public Object cloneProject(@PathVariable("level") String level) {
//		if("function".equals(level)) {
//			return cloneAnalyse.projectCloneLineValuesCalculateGroupByFunction();
//		} else if("file".equals(level)) {
//			return cloneAnalyse.projectCloneLineValuesCalculateGroupByFile();
//		}
//		return "";
//	}
	
//	@GetMapping("/file/group/node")
//	@ResponseBody
//	public Object testCloneFileGroupNode() {
//		return cloneAnalyse.groupFileCloneNode();
//	}
//	
//	@GetMapping("/file/group/relation")
//	@ResponseBody
//	public Object testCloneFileGroupRelation() {
//		return cloneAnalyse.groupFileCloneRelation();
//	}
//	
//	@GetMapping("/function/group/node")
//	@ResponseBody
//	public Object testCloneFunctionGroupNode() {
//		return cloneAnalyse.groupFunctionCloneNode();
//	}
//
//	@GetMapping("/function/group/relation")
//	@ResponseBody
//	public Object testCloneFunctionGroupRelation() {
//		return cloneAnalyse.groupFunctionCloneRelation();
//	}
//
//	@GetMapping("/clones")
//	@ResponseBody
//	public JSONObject findProjectClones() {
//		JSONObject result = new JSONObject();
//		Iterable<FunctionCloneFunction> allClones = cloneAnalyse.findAllFunctionCloneFunctions();
//		Iterable<Clone<Project, FunctionCloneFunction>> clones = cloneAnalyse.findProjectCloneFromFunctionClone(allClones, true);
//		result.put("result", "success");
//		result.put("projectValues", clones);
//		Iterable<Clone<MicroService,  FunctionCloneFunction>> msClones = cloneAnalyse.findMicroServiceCloneFromFunctionClone(allClones, true);
//		result.put("msValues", msClones);
//		return result;
//	}
}
