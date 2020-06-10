package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.service.spring.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.spring.NodeService;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;
import cn.edu.fudan.se.multidependency.service.spring.data.FileCloneGroup;
import cn.edu.fudan.se.multidependency.service.spring.data.FunctionCloneGroup;
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
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/{level}")
	public String cloneIndex(@PathVariable("level") String level, 
			HttpServletRequest request,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass);
		request.setAttribute("microservicesCount", msService.findAllMicroService().size());
		request.setAttribute("microservices", msService.findAllMicroService());
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
		if(CloneLevel.valueOf(level) == CloneLevel.function) {
			Map<String, Map<Long, CloneLineValue<MicroService>>> data = cloneAnalyse.msCloneLineValuesCalculateGroupByFunction(mss, removeFileClone);
			result.put("data", data);
			Collection<MicroService> microservices = cloneAnalyse.msSortByMsCloneLineCount(mss, CloneLevel.function, removeFileClone, removeDataClass);
			result.put("microservices", microservices);
		} else {
			Map<String, Map<Long, CloneLineValue<MicroService>>> data = cloneAnalyse.msCloneLineValuesCalculateGroupByFile(mss, removeFileClone);
			result.put("data", data);
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
		System.out.println("histogram: " + removeFileClone + " " + removeDataClass);
		JSONObject result = new JSONObject();
		try {
			JSONObject histograms = new JSONObject();
			JSONArray nodeSizeArray = new JSONArray();
			JSONArray projectSizeArray = new JSONArray();
			List<CloneGroup> groups = new ArrayList<>();
			Collection<Collection<? extends Node>> nodeGroups = new ArrayList<>();
			if(CloneLevel.valueOf(level) == CloneLevel.function) {
				Collection<FunctionCloneGroup> functionGroups = cloneAnalyse.groupFunctionClones(removeFileClone);
				for(FunctionCloneGroup group : functionGroups) {
					Collection<? extends Node> nodes = group.getFunctions();
					nodeGroups.add(nodes);
					groups.add(group.getGroup());
				}
			} else {
				Collection<FileCloneGroup> fileGroups = cloneAnalyse.groupFileClones(removeDataClass);
				for(FileCloneGroup group : fileGroups) {
					Collection<? extends Node> nodes = group.getFiles();
					nodeGroups.add(nodes);
					groups.add(group.getGroup());
				}
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
			result.put("groups", groups);
			result.put("size", size);
		} catch (Exception e) {
			
		}
		return result;
	}
	
	@PostMapping("/{level}/group/cytoscape")
	@ResponseBody
	public JSONObject cloneGroupByGroupsToCytoscape(@PathVariable("level") String level, 
			@RequestBody Map<String, Object> params, 
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass);
		System.out.println(params);
		CloneLevel cloneLevel = CloneLevel.valueOf(level);
		cloneLevel = cloneLevel == null ? CloneLevel.file : cloneLevel;
		JSONObject result = new JSONObject();
		try {
			String searchWhat = (String) params.get("search");
			List<CloneGroup> selectedGroups = new ArrayList<>();
			if("groups".equals(searchWhat)) {
				List<String> groupsStr = (List<String>) params.get("groups");
				for(String idStr : groupsStr) {
					CloneGroup group = nodeService.queryCloneGroup(Long.valueOf(idStr));
					if(group != null) {
						selectedGroups.add(group);
					}
				}
			} else if("projects".equals(searchWhat)) {
				List<String> projectsStr = (List<String>) params.get("projects");
				List<MicroService> mss = new ArrayList<>();
				for(String idStr : projectsStr) {
					MicroService ms = msService.findMicroServiceById(Long.valueOf(idStr));
					if(ms != null) {
						mss.add(ms);
					}
				}
				if(cloneLevel == CloneLevel.function) {
					Collection<FunctionCloneGroup> functionCloneGroups = cloneAnalyse.groupFunctionClonesContainMSs(cloneAnalyse.groupFunctionClones(removeFileClone), mss);
					for(FunctionCloneGroup g : functionCloneGroups) {
						selectedGroups.add(g.getGroup());
					}
				} else {
					Collection<FileCloneGroup> fileCloneGroups = cloneAnalyse.groupFileClonesContainMSs(cloneAnalyse.groupFileClones(removeDataClass), mss);
					for(FileCloneGroup g : fileCloneGroups) {
						selectedGroups.add(g.getGroup());
					}
				}
			}
			System.out.println(selectedGroups);
			JSONArray cytoscapeArray = new JSONArray();
			for(CloneGroup group : selectedGroups) {
				List<CloneGroup> groups = new ArrayList<>();
				groups.add(group);
				cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(groups, cloneLevel, false, removeFileClone, removeDataClass));
			}
			result.put("size", selectedGroups.size());
			result.put("groups", selectedGroups);
			result.put("result", "success");
			result.put("value", cytoscapeArray);
			List<CloneGroup> groups = new ArrayList<>();
			for(CloneGroup group : selectedGroups) {
				groups.add(group);
			}
			// 合并
			result.put("groupValue", cloneShow.clonesGroupsToCytoscape(groups, cloneLevel, true, removeFileClone, removeDataClass));
			result.put("result", "success");
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	@GetMapping("/{level}/group/cytoscape/{name}")
	@ResponseBody
	public JSONObject cloneGroupByIndexToCytoscape(@PathVariable("level") String level, 
			@PathVariable("name") String name,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass + " " + name);
		JSONObject result = new JSONObject();
		try {
			List<CloneGroup> groups = new ArrayList<>();
			CloneGroup cloneGroup = nodeService.queryCloneGroup(CloneLevel.valueOf(level), name);
			if(cloneGroup != null) {
				groups.add(cloneGroup);
			}
			JSONObject value = cloneShow.clonesGroupsToCytoscape(groups, CloneLevel.valueOf(level), false, removeFileClone, removeDataClass);
			result.put("result", "success");
			result.put("value", value);
			result.put("group", cloneGroup);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	@GetMapping("/{level}/group/cytoscape")
	@ResponseBody
	public JSONObject cloneGroupToCytoscape(@PathVariable("level") String level, 
			@RequestParam(name="top", required=false, defaultValue="-1") int top,
			@RequestParam(name="minProjectsCount", required=false, defaultValue="-1") int minProjectsCount,
			@RequestParam(name="maxProjectsCount", required=false, defaultValue="-1") int maxProjectsCount,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		JSONObject result = new JSONObject();
		try {
			JSONArray cytoscapeArray = new JSONArray();
			CloneLevel cloneLevel = CloneLevel.valueOf(level);
			cloneLevel = cloneLevel == null ? CloneLevel.file : cloneLevel;
			if(top >= 0) {
				List<CloneGroup> groups = new ArrayList<>();
				int count = 0;
				if(cloneLevel == CloneLevel.function) {
					Collection<FunctionCloneGroup> functionCloneGroups = cloneAnalyse.groupFunctionClones(removeFileClone);
					for(FunctionCloneGroup group : functionCloneGroups) {
						List<CloneGroup> singleGroup = new ArrayList<>();
						singleGroup.add(group.getGroup());
						groups.add(group.getGroup());
						cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(singleGroup, cloneLevel, false, removeFileClone, removeDataClass));
						count++;
						if(count >= top) {
							break;
						}
					}
				} else {
					Collection<FileCloneGroup> fileCloneGroups = cloneAnalyse.groupFileClones(removeDataClass);
					for(FileCloneGroup group : fileCloneGroups) {
						List<CloneGroup> singleGroup = new ArrayList<>();
						singleGroup.add(group.getGroup());
						groups.add(group.getGroup());
						cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(singleGroup, cloneLevel, false, removeFileClone, removeDataClass));
						count++;
						if(count >= top) {
							break;
						}
					}
				}
				result.put("result", "success");
				result.put("value", cytoscapeArray);
				result.put("groups", groups);
				// 合并
				result.put("groupValue", cloneShow.clonesGroupsToCytoscape(groups, CloneLevel.valueOf(level), true, removeFileClone, removeDataClass));
				return result;
			} 
			if(minProjectsCount >= 0 || maxProjectsCount >= 0) {
				Collection<MicroService> mss = msService.findAllMicroService();
				Map<String, Map<Long, CloneLineValue<MicroService>>> data = new HashMap<>();
				if(cloneLevel == CloneLevel.function) {
					data = cloneAnalyse.msCloneLineValuesCalculateGroupByFunction(mss, removeFileClone);
				} else {
					data = cloneAnalyse.msCloneLineValuesCalculateGroupByFile(mss, removeDataClass);
				}
				List<CloneGroup> groups = new ArrayList<>();
				for(Map.Entry<String, Map<Long, CloneLineValue<MicroService>>> entry : data.entrySet()) {
					String group = entry.getKey();
					CloneGroup cloneGroup = nodeService.queryCloneGroup(cloneLevel, group);
					if(cloneGroup == null) {
						continue;
					}
					int count = 0;
					Map<Long, CloneLineValue<MicroService>> value = entry.getValue();
					for(Map.Entry<Long, CloneLineValue<MicroService>> msEntry : value.entrySet()) {
						CloneLineValue<MicroService> msValue = msEntry.getValue();
						if(CloneLevel.function == cloneLevel && !msValue.getCloneFunctions().isEmpty()) {
							count++;
						} 
						if(CloneLevel.file == cloneLevel && !msValue.getCloneFiles().isEmpty()) {
							count++;
						}
					}
					if(isCountIn(count, minProjectsCount, maxProjectsCount)) {
						groups.add(nodeService.queryCloneGroup(cloneLevel, group));
					}
				}
				
				for(int i = 0; i < groups.size(); i++) {
					List<CloneGroup> group = new ArrayList<>();
					group.add(groups.get(i));
					cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(group, CloneLevel.valueOf(level), false, removeFileClone, removeDataClass));
				}
				result.put("result", "success");
				result.put("value", cytoscapeArray);
				result.put("groups", groups);
				// 合并
				result.put("groupValue", cloneShow.clonesGroupsToCytoscape(groups, CloneLevel.valueOf(level), true, removeFileClone, removeDataClass));
				return result;
			}
			throw new Exception("参数错误，top：" + top + "，projectsCount：" + minProjectsCount);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	private boolean isCountIn(int count, int min, int max) {
		if(min < 0 && max < 0) {
			return false;
		} else if(min < 0) {
			return count <= max;
		} else if(max < 0) {
			return count >= min;
		} else {
			return count <= max && count >= min;
		}
		
	}
	
}
