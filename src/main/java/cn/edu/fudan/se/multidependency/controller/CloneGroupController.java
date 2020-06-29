package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;

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

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.spring.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.spring.NodeService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.clone.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.clone.CloneShowService;
import cn.edu.fudan.se.multidependency.service.spring.clone.PredicateForDataFile;
import cn.edu.fudan.se.multidependency.service.spring.clone.PredicateForLanguage;
import cn.edu.fudan.se.multidependency.service.spring.data.HistogramWithProjectsSize;

@Controller
@RequestMapping("/clonegroup")
public class CloneGroupController {
	
	@Autowired
	private CloneAnalyseService cloneAnalyse;

	@Autowired
	private CloneShowService cloneShow;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private MicroserviceService msService;
	
	@Autowired
	private NodeService nodeService;

	@GetMapping(value = {"", "/", "/index"})
	public String index(HttpServletRequest request) {
		request.setAttribute("cloneRelationTypes", CloneRelationType.values());
		request.setAttribute("search", false);
		return "clonegroup";
	}
	
	@PostMapping("/projects")
	@ResponseBody
	public Collection<? extends Node> projects(@RequestBody Map<String, Object> params) {
		List<String> languages = (List<String>) params.getOrDefault("language", new ArrayList<>());
		Collection<? extends Node> result = new ArrayList<>();
		if(languages.size() != 1) {
			result = msService.findAllMicroService();
		} else {
			result = nodeService.queryProjects(Language.valueOf(languages.get(0)));
		}
		return result;
	}
	
	@GetMapping("/cytoscape/{name}")
	@ResponseBody
	public JSONObject cloneGroupByIndexToCytoscape(
			@PathVariable("name") String name,
			@RequestParam("singleLanguage") boolean singleLanguage) {
		JSONObject result = new JSONObject();
		try {
			List<CloneGroup> groups = new ArrayList<>();
			CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
			cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
			if(cloneGroup != null) {
				groups.add(cloneGroup);
			}
			JSONObject value = cloneShow.clonesGroupsToCytoscape(groups, false, singleLanguage);
			System.out.println(value);
			result.put("result", "success");
			result.put("value", value);
			result.put("group", cloneGroup);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	private Collection<CloneGroup> selectGroups(Map<String, Object> params) throws Exception {
		List<String> cloneRelationTypes = (List<String>) params.getOrDefault("searchCloneRelationTypeSelect", new ArrayList<>());
		List<String> languages = (List<String>) params.getOrDefault("language", new ArrayList<>());
		List<String> filters = (List<String>) params.getOrDefault("filter", new ArrayList<>());
		if(cloneRelationTypes.isEmpty()) {
			throw new Exception();
		}
		CloneRelationType cloneRelationType = CloneRelationType.valueOf(cloneRelationTypes.get(0));
		cloneRelationType = cloneRelationType == null ? CloneRelationType.FILE_CLONE_FILE : cloneRelationType;
		List<Predicate<CloneGroup>> predicates = new ArrayList<>();
		if(languages.size() == 1) {
			predicates.add(new PredicateForLanguage(Language.valueOf(languages.get(0))));
		}
		for(String filter : filters) {
			if("dataclass".equals(filter)) {
				predicates.add(new PredicateForDataFile(staticAnalyseService));
			}
			if("fileclone".equals(filter)) {
				
			}
		}
		return cloneAnalyse.group(cloneRelationType, predicates);
	}
	
	@PostMapping("/histogram")
	@ResponseBody
	public JSONObject cloneGroupToHistogram(
			@RequestParam(name="sort", required=false, defaultValue="nodes") String sort,
			@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			JSONObject histograms = new JSONObject();
			JSONArray nodeSizeArray = new JSONArray();
			JSONArray projectSizeArray = new JSONArray();
			List<String> languages = (List<String>) params.getOrDefault("language", new ArrayList<>());
			Collection<CloneGroup> selectGroups = selectGroups(params);
			List<CloneGroup> sortGroups = new ArrayList<>(selectGroups);
			sortGroups.sort((group1, group2) -> {
				if("nodes".equals(sort)) {
					int s = group2.sizeOfNodes() - group1.sizeOfNodes();
					if(s == 0) {
						if(languages.size() == 1) {
							return cloneAnalyse.cloneGroupContainProjects(group2).size() - cloneAnalyse.cloneGroupContainProjects(group1).size();
						} else {
							return cloneAnalyse.cloneGroupContainMicroServices(group2).size() - cloneAnalyse.cloneGroupContainMicroServices(group1).size();
						}
					}
					return s;
				} else {
					int s = 0;
					if(languages.size() == 1) {
						s = cloneAnalyse.cloneGroupContainProjects(group2).size() - cloneAnalyse.cloneGroupContainProjects(group1).size();
					} else {
						s = cloneAnalyse.cloneGroupContainMicroServices(group2).size() - cloneAnalyse.cloneGroupContainMicroServices(group1).size();
					}
					if(s == 0) {
						return group2.sizeOfNodes() - group1.sizeOfNodes();
					}
					return s;
				}
			});
			for(CloneGroup group : sortGroups) {
				nodeSizeArray.add(group.getNodes().size());
				if(languages.size() == 1) {
					projectSizeArray.add(cloneAnalyse.cloneGroupContainMicroServices(group).size());
				} else {
					projectSizeArray.add(cloneAnalyse.cloneGroupContainProjects(group).size());
				}
			}
			histograms.put("nodeSize", nodeSizeArray);
			histograms.put("projectSize", projectSizeArray);
			result.put("result", "success");
			result.put("value", histograms);
			result.put("groups", sortGroups);
		} catch (Exception e) {
			
		}
		return result;
	}
	
	@PostMapping("/histogram/projects/size")
	@ResponseBody
	public List<HistogramWithProjectsSize> cloneGroupWithProjectsSizeToHistogram(
			@RequestParam(name="sort", required=false, defaultValue="nodes") String sort,
			@RequestBody Map<String, Object> params) throws Exception {
		Collection<CloneGroup> selectGroups = selectGroups(params);
		List<String> languages = (List<String>) params.getOrDefault("language", new ArrayList<>());
		List<HistogramWithProjectsSize> histograms = 
				new ArrayList<>(cloneShow.withProjectsSizeToHistogram(selectGroups, languages.size() == 1));
		if("nodes".equals(sort)) {
			histograms.sort((h1, h2) -> {
				return h2.getNodesSize() - h1.getNodesSize();
			});
		} else if("groups".equals(sort)) {
			histograms.sort((h1, h2) -> {
				return h2.getGroupsSize() - h1.getGroupsSize();
			});
		} else if("ratio".equals(sort)) {
			histograms.sort((h1, h2) -> {
				if(h2.getRatio() > h1.getRatio()) {
					return 1;
				} else if(h2.getRatio() == h1.getRatio()) {
					return 0;
				} else {
					return -1;
				}
			});
		}
		return histograms;
	}

	
	@PostMapping("/cytoscape")
	@ResponseBody
	public JSONObject cloneGroupByGroupsToCytoscape(@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		List<String> languages = (List<String>) params.getOrDefault("language", new ArrayList<>());
		try {
			String searchWhat = (String) params.get("search");
			Collection<CloneGroup> selectedGroups = new ArrayList<>();
			if("groups".equals(searchWhat)) {
				List<String> groupsStr = (List<String>) params.get("groups");
				for(String idStr : groupsStr) {
					CloneGroup group = nodeService.queryCloneGroup(Long.valueOf(idStr));
					group = cloneAnalyse.addNodeAndRelationToCloneGroup(group);
					if(group != null) {
						selectedGroups.add(group);
					}
				}
			} else if("projects".equals(searchWhat)) {
				List<String> projectsStr = (List<String>) params.get("projects");
				Collection<CloneGroup> cloneGroups = this.selectGroups(params);
				if(languages.size() != 1) {
					List<MicroService> mss = new ArrayList<>();
					for(String idStr : projectsStr) {
						MicroService ms = msService.findMicroServiceById(Long.valueOf(idStr));
						if(ms != null) {
							mss.add(ms);
						}
					}
					selectedGroups = cloneAnalyse.findGroupsContainMicroServices(cloneGroups, mss);
				} else {
					Collection<Project> projects = nodeService.queryProjects(Language.valueOf(languages.get(0)));
					selectedGroups = cloneAnalyse.findGroupsContainProjects(cloneGroups, projects);
				}
			}
			JSONArray cytoscapeArray = new JSONArray();
			for(CloneGroup group : selectedGroups) {
				List<CloneGroup> groups = new ArrayList<>();
				groups.add(group);
				cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(groups, false, languages.size() == 1));
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
			result.put("groupValue", cloneShow.clonesGroupsToCytoscape(groups, true, languages.size() == 1));
			result.put("result", "success");
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	@PostMapping("/cytoscape/count")
	@ResponseBody
	public JSONObject cloneGroupToCytoscape(
			@RequestBody Map<String, Object> params,
			@RequestParam(name="minProjectsCount", required=false, defaultValue="-1") int minProjectsCount,
			@RequestParam(name="maxProjectsCount", required=false, defaultValue="-1") int maxProjectsCount) {
		JSONObject result = new JSONObject();
		try {
			List<String> languages = (List<String>) params.getOrDefault("language", new ArrayList<>());
			Collection<CloneGroup> selectGroups = this.selectGroups(params);
			JSONArray cytoscapeArray = new JSONArray();
			if(minProjectsCount >= 0 || maxProjectsCount >= 0) {
				List<CloneGroup> groups = new ArrayList<>();
				for(CloneGroup group : selectGroups) {
					if(languages.size() != 1) {
						Collection<MicroService> mss = cloneAnalyse.cloneGroupContainMicroServices(group);
						if(isCountIn(mss.size(), minProjectsCount, maxProjectsCount)) {
							groups.add(group);
						}
					} else {
						Collection<Project> projects = cloneAnalyse.cloneGroupContainProjects(group);
						if(isCountIn(projects.size(), minProjectsCount, maxProjectsCount)) {
							groups.add(group);
						}
					}
				}
				for(int i = 0; i < groups.size(); i++) {
					List<CloneGroup> group = new ArrayList<>();
					group.add(groups.get(i));
					cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(group, false, languages.size() == 1));
				}
				result.put("result", "success");
				result.put("value", cytoscapeArray);
				result.put("groups", groups);
				// 合并
				result.put("groupValue", cloneShow.clonesGroupsToCytoscape(groups, true, languages.size() == 1));
				return result;
			}
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
