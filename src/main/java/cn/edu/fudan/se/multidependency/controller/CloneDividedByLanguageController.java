package cn.edu.fudan.se.multidependency.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.service.spring.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.NodeService;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;
import cn.edu.fudan.se.multidependency.service.spring.data.FileCloneGroup;
import cn.edu.fudan.se.multidependency.service.spring.data.FunctionCloneGroup;
import cn.edu.fudan.se.multidependency.service.spring.data.HistogramWithProjectsSize;
import cn.edu.fudan.se.multidependency.service.spring.show.CloneShowService;

@Controller
@RequestMapping("/language/clone")
public class CloneDividedByLanguageController {
	
	@Autowired
	private CloneAnalyseService cloneAnalyse;

	@Autowired
	private CloneShowService cloneShow;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/{language}/{level}")
	public String cloneIndex(@PathVariable("level") String level, 
			@PathVariable("language") String language,
			HttpServletRequest request,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass);
		CloneLevel cloneLevel = CloneLevel.valueOf(level);
		cloneLevel = cloneLevel == null ? CloneLevel.file : cloneLevel;
		Language lang = Language.valueOf(language);
		lang = lang == null ? Language.java : lang;
		Collection<Project> projects = nodeService.queryProjects(lang);
		request.setAttribute("projectsCount", projects.size());
		request.setAttribute("projects", projects);
		request.setAttribute("level", cloneLevel.toString());
		request.setAttribute("removeFileClone", removeFileClone);
		request.setAttribute("removeDataClass", removeDataClass);
		request.setAttribute("language", lang.toString());
		return "clone";
	}
	
	@GetMapping("/{language}/{level}/table/project")
	@ResponseBody
	public JSONObject cloneProject(@PathVariable("level") String level,
			@PathVariable("language") String language,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass);
		Language lang = Language.valueOf(language);
		lang = lang == null ? Language.java : lang;
		JSONObject result = new JSONObject();
		Collection<Project> projects = nodeService.queryProjects(lang);
		if(CloneLevel.valueOf(level) == CloneLevel.function) {
			Map<String, Map<Long, CloneLineValue<Project>>> data = cloneAnalyse.projectCloneLineValuesCalculateGroupByFunction(removeFileClone, lang);
			result.put("data", data);
			result.put("projects", projects);
		} else {
			Map<String, Map<Long, CloneLineValue<Project>>> data = cloneAnalyse.projectCloneLineValuesCalculateGroupByFile(removeDataClass, lang);
			result.put("data", data);
			result.put("projects", projects);
		}
		return result;
	}

	@GetMapping("/{language}/{level}/table/project/export")
	public void exportCloneProject(@PathVariable("level") String level, 
			@PathVariable("language") String language,
			HttpServletResponse res,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println("导出csv");
		Language lang = Language.valueOf(language);
		lang = lang == null ? Language.java : lang;
		Collection<Project> projects = nodeService.queryProjects(lang);
		Map<String, Map<Long, CloneLineValue<Project>>> data = CloneLevel.valueOf(level) == CloneLevel.function ?
				cloneAnalyse.projectCloneLineValuesCalculateGroupByFunction(removeFileClone, lang) :
				cloneAnalyse.projectCloneLineValuesCalculateGroupByFile(removeDataClass, lang) ;
		String cloneProject = cloneAnalyse.exportCloneProject(data, projects, CloneLevel.valueOf(level));
		try {
			OutputStream os = res.getOutputStream();
			os.write(cloneProject.getBytes("gbk"));
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/{language}/{level}/group/histogram/projects/size")
	@ResponseBody
	public List<HistogramWithProjectsSize> cloneGroupWithProjectsSizeToHistogram(@PathVariable("level") String level,
			@PathVariable("language") String language,
			@RequestParam(name="sort", required=false, defaultValue="nodes") String sort,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println("histogram: " + removeFileClone + " " + removeDataClass + " " + sort);
		Language lang = Language.valueOf(language);
		lang = lang == null ? Language.java : lang;
		CloneLevel cloneLevel = CloneLevel.valueOf(level);
		cloneLevel = cloneLevel == null ? CloneLevel.file : cloneLevel;
		List<HistogramWithProjectsSize> histograms = new ArrayList<>(cloneShow.withProjectsSizeToHistogram(lang, cloneLevel, removeDataClass, removeFileClone));
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
	
	@GetMapping("/{language}/{level}/group/histogram")
	@ResponseBody
	public JSONObject cloneGroupToHistogram(@PathVariable("level") String level,
			@PathVariable("language") String language,
			@RequestParam(name="sort", required=false, defaultValue="nodes") String sort,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println("histogram: " + removeFileClone + " " + removeDataClass + " " + sort);
		Language temp = Language.valueOf(language);
		temp = temp == null ? Language.java : temp;
		final Language lang = temp;
		JSONObject result = new JSONObject();
		try {
			JSONObject histograms = new JSONObject();
			JSONArray nodeSizeArray = new JSONArray();
			JSONArray projectSizeArray = new JSONArray();
			List<CloneGroup> groups = new ArrayList<>();
			if(CloneLevel.valueOf(level) == CloneLevel.function) {
				Collection<FunctionCloneGroup> functionGroups = cloneAnalyse.groupFunctionClones(removeFileClone, lang);
				List<FunctionCloneGroup> sortGroups = new ArrayList<>(functionGroups);
				sortGroups.sort((group1, group2) -> {
					if("nodes".equals(sort)) {
						int s = group2.sizeOfNodes() - group1.sizeOfNodes();
						if(s == 0) {
							return cloneAnalyse.functionCloneGroupContainProjects(group2, lang).size() - cloneAnalyse.functionCloneGroupContainProjects(group1, lang).size();
						}
						return s;
					} else {
						int s = cloneAnalyse.functionCloneGroupContainProjects(group2, lang).size() - cloneAnalyse.functionCloneGroupContainProjects(group1, lang).size();
						if(s == 0) {
							return group2.sizeOfNodes() - group1.sizeOfNodes();
						}
						return s;
					}
				});
				for(FunctionCloneGroup group : sortGroups) {
					groups.add(group.getGroup());
					nodeSizeArray.add(group.getNodes().size());
					projectSizeArray.add(cloneAnalyse.functionCloneGroupContainProjects(group, lang).size());
				}
			} else {
				Collection<FileCloneGroup> fileGroups = cloneAnalyse.groupFileClones(removeDataClass, lang);
				List<FileCloneGroup> sortGroups = new ArrayList<>(fileGroups);
				sortGroups.sort((group1, group2) -> {
					if("nodes".equals(sort)) {
						int s = group2.sizeOfNodes() - group1.sizeOfNodes();
						if(s == 0) {
							return cloneAnalyse.fileCloneGroupContainProjects(group2, lang).size() - cloneAnalyse.fileCloneGroupContainProjects(group1, lang).size();
						}
						return s;
					} else {
						int s = cloneAnalyse.fileCloneGroupContainProjects(group2, lang).size() - cloneAnalyse.fileCloneGroupContainProjects(group1, lang).size();
						if(s == 0) {
							return group2.sizeOfNodes() - group1.sizeOfNodes();
						}
						return s;
					}
				});
				for(FileCloneGroup group : sortGroups) {
					groups.add(group.getGroup());
					nodeSizeArray.add(group.getNodes().size());
					projectSizeArray.add(cloneAnalyse.fileCloneGroupContainProjects(group, lang).size());
				}
			}
			histograms.put("nodeSize", nodeSizeArray);
			histograms.put("projectSize", projectSizeArray);
			result.put("result", "success");
			result.put("value", histograms);
			result.put("groups", groups);
		} catch (Exception e) {
			
		}
		return result;
	}
	
	@PostMapping("/{language}/{level}/group/cytoscape")
	@ResponseBody
	public JSONObject cloneGroupByGroupsToCytoscape(@PathVariable("level") String level, 
			@PathVariable("language") String language,
			@RequestBody Map<String, Object> params, 
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass);
		System.out.println(params);
		Language lang = Language.valueOf(language);
		lang = lang == null ? Language.java : lang;
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
				List<Project> projects = new ArrayList<>();
				for(String idStr : projectsStr) {
					Project project = nodeService.queryProject(Long.valueOf(idStr));
					if(project != null) {
						projects.add(project);
					}
				}
				if(cloneLevel == CloneLevel.function) {
					Collection<FunctionCloneGroup> functionCloneGroups = cloneAnalyse.groupFunctionClonesContainProjects(cloneAnalyse.groupFunctionClones(removeFileClone, lang), projects);
					for(FunctionCloneGroup g : functionCloneGroups) {
						selectedGroups.add(g.getGroup());
					}
				} else {
					Collection<FileCloneGroup> fileCloneGroups = cloneAnalyse.groupFileClonesContainProjects(cloneAnalyse.groupFileClones(removeDataClass, lang), projects);
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
				cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(lang, groups, cloneLevel, false, removeFileClone, removeDataClass));
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
			result.put("groupValue", cloneShow.clonesGroupsToCytoscape(lang, groups, cloneLevel, true, removeFileClone, removeDataClass));
			result.put("result", "success");
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	@GetMapping("/{language}/{level}/group/cytoscape/{name}")
	@ResponseBody
	public JSONObject cloneGroupByIndexToCytoscape(@PathVariable("level") String level, 
			@PathVariable("language") String language,
			@PathVariable("name") String name,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		System.out.println(removeFileClone + " " + removeDataClass + " " + name);
		Language lang = Language.valueOf(language);
		lang = lang == null ? Language.java : lang;
		JSONObject result = new JSONObject();
		try {
			List<CloneGroup> groups = new ArrayList<>();
			CloneGroup cloneGroup = nodeService.queryCloneGroup(CloneLevel.valueOf(level), name);
			if(cloneGroup != null) {
				groups.add(cloneGroup);
			}
			JSONObject value = cloneShow.clonesGroupsToCytoscape(lang, groups, CloneLevel.valueOf(level), false, removeFileClone, removeDataClass);
			result.put("result", "success");
			result.put("value", value);
			result.put("group", cloneGroup);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	@GetMapping("/{language}/{level}/group/cytoscape")
	@ResponseBody
	public JSONObject cloneGroupToCytoscape(@PathVariable("level") String level, 
			@PathVariable("language") String language,
			@RequestParam(name="top", required=false, defaultValue="-1") int top,
			@RequestParam(name="minProjectsCount", required=false, defaultValue="-1") int minProjectsCount,
			@RequestParam(name="maxProjectsCount", required=false, defaultValue="-1") int maxProjectsCount,
			@RequestParam(name="removeFileClone", required=false, defaultValue="false") boolean removeFileClone,
			@RequestParam(name="removeDataClass", required=false, defaultValue="false") boolean removeDataClass) {
		JSONObject result = new JSONObject();
		Language lang = Language.valueOf(language);
		lang = lang == null ? Language.java : lang;
		try {
			JSONArray cytoscapeArray = new JSONArray();
			CloneLevel cloneLevel = CloneLevel.valueOf(level);
			cloneLevel = cloneLevel == null ? CloneLevel.file : cloneLevel;
			if(top >= 0) {
				List<CloneGroup> groups = new ArrayList<>();
				int count = 0;
				if(cloneLevel == CloneLevel.function) {
					Collection<FunctionCloneGroup> functionCloneGroups = cloneAnalyse.groupFunctionClones(removeFileClone, lang);
					for(FunctionCloneGroup group : functionCloneGroups) {
						List<CloneGroup> singleGroup = new ArrayList<>();
						singleGroup.add(group.getGroup());
						groups.add(group.getGroup());
						cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(lang, singleGroup, cloneLevel, false, removeFileClone, removeDataClass));
						count++;
						if(count >= top) {
							break;
						}
					}
				} else {
					Collection<FileCloneGroup> fileCloneGroups = cloneAnalyse.groupFileClones(removeDataClass, lang);
					for(FileCloneGroup group : fileCloneGroups) {
						List<CloneGroup> singleGroup = new ArrayList<>();
						singleGroup.add(group.getGroup());
						groups.add(group.getGroup());
						cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(lang, singleGroup, cloneLevel, false, removeFileClone, removeDataClass));
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
				result.put("groupValue", cloneShow.clonesGroupsToCytoscape(lang, groups, CloneLevel.valueOf(level), true, removeFileClone, removeDataClass));
				return result;
			} 
			if(minProjectsCount >= 0 || maxProjectsCount >= 0) {
				Map<String, Map<Long, CloneLineValue<Project>>> data = new HashMap<>();
				if(cloneLevel == CloneLevel.function) {
					data = cloneAnalyse.projectCloneLineValuesCalculateGroupByFunction(removeFileClone, lang);
				} else {
					data = cloneAnalyse.projectCloneLineValuesCalculateGroupByFile(removeDataClass, lang);
				}
				List<CloneGroup> groups = new ArrayList<>();
				for(Map.Entry<String, Map<Long, CloneLineValue<Project>>> entry : data.entrySet()) {
					String group = entry.getKey();
					CloneGroup cloneGroup = nodeService.queryCloneGroup(cloneLevel, group);
					if(cloneGroup == null) {
						continue;
					}
					int count = 0;
					Map<Long, CloneLineValue<Project>> value = entry.getValue();
					for(Map.Entry<Long, CloneLineValue<Project>> projectEntry : value.entrySet()) {
						CloneLineValue<Project> projectValue = projectEntry.getValue();
						if(CloneLevel.function == cloneLevel && !projectValue.getCloneFunctions().isEmpty()) {
							count++;
						} 
						if(CloneLevel.file == cloneLevel && !projectValue.getCloneFiles().isEmpty()) {
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
					cytoscapeArray.add(cloneShow.clonesGroupsToCytoscape(lang, group, CloneLevel.valueOf(level), false, removeFileClone, removeDataClass));
				}
				result.put("result", "success");
				result.put("value", cytoscapeArray);
				result.put("groups", groups);
				// 合并
				result.put("groupValue", cloneShow.clonesGroupsToCytoscape(lang, groups, CloneLevel.valueOf(level), true, removeFileClone, removeDataClass));
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
