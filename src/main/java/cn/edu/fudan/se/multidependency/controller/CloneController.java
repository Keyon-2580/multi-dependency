package cn.edu.fudan.se.multidependency.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.spring.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;

@Controller
@RequestMapping("/clone")
public class CloneController {
	
	@Autowired
	private CloneAnalyseService cloneAnalyse;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@GetMapping("/index")
	public String index() {
		return "clone";
	}
	
	@GetMapping("/file/group/histogram")
	@ResponseBody
	public JSONObject fileCloneGroupToHistogram() {
		JSONObject result = new JSONObject();
		try {
			JSONObject histograms = new JSONObject();
			JSONArray fileSizeArray = new JSONArray();
			JSONArray projectSizeArray = new JSONArray();
			Collection<Collection<? extends Node>> nodeGroups = cloneAnalyse.groupFileCloneNode();
			int size = 0;
			for(Collection<? extends Node> nodes : nodeGroups) {
				size++;
				int fileSize = 0, projectSize = 0;
				Set<Project> projects = new HashSet<>();
				for(Node node : nodes) {
					fileSize++;
					projects.add(containRelationService.findFileBelongToProject((ProjectFile) node));
				}
				projectSize = projects.size();
				fileSizeArray.add(fileSize);
				projectSizeArray.add(projectSize);
			}
			histograms.put("fileSize", fileSizeArray);
			histograms.put("projectSize", projectSizeArray);
			result.put("result", "success");
			result.put("value", histograms);
			result.put("size", size);
		} catch (Exception e) {
			
		}
		return result;
	}
	
	@GetMapping("/file/group/cytoscape")
	@ResponseBody
	public JSONObject fileCloneGroupToCytoscape(@RequestParam("top") int top) {
		JSONObject result = new JSONObject();
		try {
			JSONArray cytoscapeArray = new JSONArray();
			JSONArray zTreeArray = new JSONArray();
			int i = 0;
			for(Collection<? extends CloneRelation> relations : cloneAnalyse.groupFileCloneRelation()) {
				if(i++ >= top) {
					break;
				}
				cytoscapeArray.add(cloneAnalyse.fileCloneFilesToCytoscape((Collection<FileCloneFile>) relations));
				
			}
			result.put("size", i - 1);
			result.put("result", "success");
			result.put("value", cytoscapeArray);
			result.put("ztree", zTreeArray);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	@GetMapping("/file/group/node")
	@ResponseBody
	public Object testCloneFileGroupNode() {
		return cloneAnalyse.groupFileCloneNode();
	}
	
	@GetMapping("/file/group/relation")
	@ResponseBody
	public Object testCloneFileGroupRelation() {
		return cloneAnalyse.groupFileCloneRelation();
	}
	
	@GetMapping("/function/group/node")
	@ResponseBody
	public Object testCloneFunctionGroupNode() {
		return cloneAnalyse.groupFunctionCloneNode();
	}

	@GetMapping("/function/group/relation")
	@ResponseBody
	public Object testCloneFunctionGroupRelation() {
		return cloneAnalyse.groupFunctionCloneRelation();
	}

	@GetMapping("/clones")
	@ResponseBody
	public JSONObject findProjectClones() {
		JSONObject result = new JSONObject();
		Iterable<FunctionCloneFunction> allClones = cloneAnalyse.findAllFunctionCloneFunctions();
		Iterable<Clone<Project, FunctionCloneFunction>> clones = cloneAnalyse.findProjectCloneFromFunctionClone(allClones, true);
		result.put("result", "success");
		result.put("projectValues", clones);
		Iterable<Clone<MicroService,  FunctionCloneFunction>> msClones = cloneAnalyse.findMicroServiceCloneFromFunctionClone(allClones, true);
		result.put("msValues", msClones);
		return result;
	}
}
