package cn.edu.fudan.se.multidependency.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.repository.relation.HasRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackage;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import cn.edu.fudan.se.multidependency.service.query.data.ProjectStructure;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
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

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCall;
import cn.edu.fudan.se.multidependency.service.query.DependencyOrganizationService;
import cn.edu.fudan.se.multidependency.service.query.MultipleService;
import cn.edu.fudan.se.multidependency.service.query.ProjectService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.dynamic.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.metric.Fan_IO;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import cn.edu.fudan.se.multidependency.utils.query.ZTreeUtil.ZTreeNode;

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
	private ContainRelationService containRelationService;
	
	@Autowired
	private NodeService nodeService;

	@Autowired
	private HasRepository hasRepository;


	@Autowired
	private HasRelationService hasRelationService;

	@Autowired
	private MultipleService multipleService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private ProjectService projectService;

	@Autowired
	private HotspotPackageDetector hotspotPackageDetector;

	@GetMapping("/graph")
	public String index() {
//		request.setAttribute("cloneRelationTypes", CloneRelationType.values());
//		request.setAttribute("cloneLevels", CloneLevel.values());
//		request.setAttribute("search", false);
		return "projectgraph";
	}

	@GetMapping("/tree")
	public String tree() {
		return "projecttree";
	}

	@GetMapping("/all")
	@ResponseBody
	public Map<Long, Project> allProjects() {
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, Project> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), project);
		}
		return result;
	}
	
	@GetMapping(value = "/fanIO/file/{projectId}")
	@ResponseBody
	public Collection<Fan_IO<ProjectFile>> calculateFanIOs(@PathVariable("projectId") long id) {
		Project project = nodeService.queryProject(id);
		List<Fan_IO<ProjectFile>> result = staticAnalyseService.queryAllFileFanIOs(project);
		int minSize = 35;
		result = result.subList(0, result.size() < minSize ? result.size() : minSize);
		return result;
	}
	
	@GetMapping(value = "/all/{page}")
	@ResponseBody
	public List<Project> allProjectsByPage(@PathVariable("page") int page) {
		List<Project> result = staticAnalyseService.queryAllProjectsByPage(page, Constant.SIZE_OF_PAGE, "name");
		return result;
	}
	
	@GetMapping(value = "/pages/count")
	@ResponseBody
	public long queryMicroServicePagesCount() {
		long count = nodeService.allProjects().size();
		long pageCount = count % Constant.SIZE_OF_PAGE == 0 ? 
				count / Constant.SIZE_OF_PAGE : count / Constant.SIZE_OF_PAGE + 1;
		return pageCount;
	}

    private static final Executor executor = Executors.newCachedThreadPool();
    
    @GetMapping(value = "/ztree/function/variable")
    @ResponseBody
    public JSONObject functionContainNodesToZTree(@RequestParam("functionId") long id) {
    	JSONObject result = new JSONObject();
		try {
			Function function = nodeService.queryFunction(id);
			if(function == null) {
				throw new Exception("file is null, fileId: " + id);
			}
			JSONArray values = new JSONArray();
			Collection<Variable> variables = containRelationService.findFunctionDirectlyContainVariables(function);
			if(!variables.isEmpty()) {
				ZTreeNode variableNodes = new ZTreeNode("变量 (" + variables.size() + ")", true);
				for(Variable variable : variables) {
					ZTreeNode node = new ZTreeNode(variable, false);
					variableNodes.addChild(node);
				}
				values.add(variableNodes.toJSON());
			}
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/type/{childrenType}")
    @ResponseBody
    public JSONObject typeContainNodesToZTree(@RequestParam("typeId") long id, @PathVariable("childrenType") String childrenType) {
    	JSONObject result = new JSONObject();
		try {
			Type type = nodeService.queryType(id);
			if(type == null) {
				throw new Exception("file is null, fileId: " + id);
			}
			JSONArray values = new JSONArray();
			childrenType = childrenType.toLowerCase();
			switch(childrenType) {
			case "function":
				Collection<Function> functions = containRelationService.findTypeDirectlyContainFunctions(type);
				if(!functions.isEmpty()) {
					ZTreeNode functionNodes = new ZTreeNode("方法 (" + functions.size() + ")", true);
					for(Function function : functions) {
						ZTreeNode node = new ZTreeNode(function.getId(), function.getName() + function.getParametersIdentifies(), false, function.getNodeType().toString(), true);
						functionNodes.addChild(node);
					}
					values.add(functionNodes.toJSON());
				}
				break;
			case "variable":
				Collection<Variable> variables = containRelationService.findTypeDirectlyContainFields(type);
				if(!variables.isEmpty()) {
					ZTreeNode variableNodes = new ZTreeNode("属性 (" + variables.size() + ")", true);
					for(Variable variable : variables) {
						ZTreeNode node = new ZTreeNode(variable, false);
						variableNodes.addChild(node);
					}
					values.add(variableNodes.toJSON());
				}
				break;
			}
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/namespace/{childrenType}")
    @ResponseBody
    public JSONObject namespaceContainNodesToZTree(@RequestParam("namespaceId") long id, @PathVariable("childrenType") String childrenType) {
    	JSONObject result = new JSONObject();
		try {
			Namespace namespace = nodeService.queryNamespace(id);
			if(namespace == null) {
				throw new Exception("namespace is null, namespaceId: " + id);
			}
			JSONArray values = new JSONArray();
			childrenType = childrenType.toLowerCase();
			switch(childrenType) {
			case "type":
				Collection<Type> types = containRelationService.findNamespaceDirectlyContainTypes(namespace);
				if(!types.isEmpty()) {
					ZTreeNode typeNodes = new ZTreeNode("类型 (" + types.size() + ")", true);
					for(Type type : types) {
						ZTreeNode node = new ZTreeNode(type, true);
						typeNodes.addChild(node);
					}
					values.add(typeNodes.toJSON());
				}
				break;
			case "function":
				Collection<Function> functions = containRelationService.findNamespaceDirectlyContainFunctions(namespace);
				if(!functions.isEmpty()) {
					ZTreeNode functionNodes = new ZTreeNode("方法 (" + functions.size() + ")", true);
					for(Function function : functions) {
						ZTreeNode node = new ZTreeNode(function.getId(), function.getName() + function.getParametersIdentifies(), false, function.getNodeType().toString(), true);
						functionNodes.addChild(node);
					}
					values.add(functionNodes.toJSON());
				}
				break;
			case "variable":
				Collection<Variable> variables = containRelationService.findNamespaceDirectlyContainVariables(namespace);
				if(!variables.isEmpty()) {
					ZTreeNode variableNodes = new ZTreeNode("变量 (" + variables.size() + ")", true);
					for(Variable variable : variables) {
						ZTreeNode node = new ZTreeNode(variable, false);
						variableNodes.addChild(node);
					}
					values.add(variableNodes.toJSON());
				}
				break;
			}
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/file/{childrenType}")
    @ResponseBody
    public JSONObject fileContainNodesToZTree(@RequestParam("fileId") long id, @PathVariable("childrenType") String childrenType) {
    	JSONObject result = new JSONObject();
		try {
			ProjectFile file = nodeService.queryFile(id);
			if(file == null) {
				throw new Exception("file is null, fileId: " + id);
			}
			JSONArray values = new JSONArray();
			childrenType = childrenType.toLowerCase();
			switch(childrenType) {
			case "namespace":
				Collection<Namespace> namespaces = containRelationService.findFileContainNamespaces(file);
				if(!namespaces.isEmpty()) {
					ZTreeNode filesNodes = new ZTreeNode("命名空间 (" + namespaces.size() + ")", true);
					for(Namespace namespace : namespaces) {
						ZTreeNode node = new ZTreeNode(namespace, true);
						filesNodes.addChild(node);
					}
					values.add(filesNodes.toJSON());
				}
				break;
			case "type":
				Collection<Type> types = containRelationService.findFileDirectlyContainTypes(file);
				if(!types.isEmpty()) {
					ZTreeNode typeNodes = new ZTreeNode("类型 (" + types.size() + ")", true);
					for(Type type : types) {
						ZTreeNode node = new ZTreeNode(type, true);
						typeNodes.addChild(node);
					}
					values.add(typeNodes.toJSON());
				}
				break;
			case "function":
				Collection<Function> functions = containRelationService.findFileDirectlyContainFunctions(file);
				if(!functions.isEmpty()) {
					ZTreeNode functionNodes = new ZTreeNode("方法 (" + functions.size() + ")", true);
					for(Function function : functions) {
						ZTreeNode node = new ZTreeNode(function, true);
						functionNodes.addChild(node);
					}
					values.add(functionNodes.toJSON());
				}
				break;
			case "variable":
				Collection<Variable> variables = containRelationService.findFileDirectlyContainVariables(file);
				if(!variables.isEmpty()) {
					ZTreeNode variableNodes = new ZTreeNode("变量 (" + variables.size() + ")", true);
					for(Variable variable : variables) {
						ZTreeNode node = new ZTreeNode(variable, true);
						variableNodes.addChild(node);
					}
					values.add(variableNodes.toJSON());
				}
				break;
			}
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/file")
    @ResponseBody
    public JSONObject packageContainFilesToZTree(@RequestParam("packageId") long id) {
    	JSONObject result = new JSONObject();
		try {
			Package pck = nodeService.queryPackage(id);
			if(pck == null) {
				throw new Exception("package is null, pckId: " + id);
			}
			Collection<ProjectFile> files = containRelationService.findPackageContainFiles(pck);
			ZTreeNode filesNodes = new ZTreeNode("文件 (" + files.size() + ")", true);
			for(ProjectFile file : files) {
				ZTreeNode node = new ZTreeNode(file, true);
				filesNodes.addChild(node);
			}
			JSONArray values = new JSONArray();
			values.add(filesNodes.toJSON());
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/ztree/package")
    @ResponseBody
    public JSONObject allProjectContainPackagesByPageToZTree(@RequestParam("projectId") long projectId) {
    	JSONObject result = new JSONObject();
		try {
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				throw new Exception("project is null, projectId: " + projectId);
			}
			Collection<Package> pcks = containRelationService.findProjectContainPackages(project);
			ZTreeNode pckNodes = new ZTreeNode("目录 / 包 (" + pcks.size() + ")", true);
			for(Package pck : pcks) {
				String name = pck.getName().equals(pck.getDirectoryPath()) ? pck.getDirectoryPath() : String.join(":", pck.getName(), pck.getDirectoryPath());
				ZTreeNode node = new ZTreeNode(pck.getId(), name, false, pck.getNodeType().toString(), true);
				pckNodes.addChild(node);
			}
			JSONArray values = new JSONArray();
			values.add(pckNodes.toJSON());
			result.put("result", "success");
			result.put("value", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
    @GetMapping(value = "/all/ztree/project/{page}")
    @ResponseBody
    public JSONObject allProjectsByPageToZtree(@PathVariable("page") int page) {
    	JSONObject result = new JSONObject();
		try {
			Collection<Project> projects = staticAnalyseService.queryAllProjectsByPage(page, Constant.SIZE_OF_PAGE, "name");
			JSONArray values = new JSONArray();
			for(Project project : projects) {
				ZTreeNode node = new ZTreeNode(project, true);
				values.add(node.toJSON());
			}
			System.out.println(values);
			result.put("result", "success");
			result.put("values", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
    }
    
	@GetMapping(value = "/all/ztree/structure/{page}")
	@ResponseBody
	public JSONObject allProjectsContainStructureByPage(@PathVariable("page") int page) {
		JSONObject result = new JSONObject();
		try {
			Collection<Project> projects = staticAnalyseService.queryAllProjectsByPage(page, Constant.SIZE_OF_PAGE, "name");
			List<ZTreeNode> nodes = new ArrayList<>();
			DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			LOGGER.info("开始时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
			CountDownLatch latch = new CountDownLatch(projects.size());
			List<FutureTask<ZTreeNode>> list = new ArrayList<>();
			for(Project project : projects) {
				FutureTask<ZTreeNode> s = new FutureTask<>(new ProjectStructureExtractor(project, multipleService, latch));
				list.add(s);
				executor.execute(s);
			}
			latch.await();
			for(FutureTask<ZTreeNode> t : list) {
				nodes.add(t.get());
			}
			LOGGER.info("结束时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
			JSONArray values = new JSONArray();
			for(ZTreeNode node : nodes) {
				values.add(node.toJSON());
			}
			result.put("result", "success");
			result.put("values", values);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}

	@GetMapping(value = {"/", "/index"})
	public String index(HttpServletRequest request, @RequestParam(required=true, value="id") long id) {
		Project project = nodeService.queryProject(id);
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
	
	@PostMapping("/absolute")
	@ResponseBody
	public JSONObject setAbsolutePath(@RequestBody Map<String, Object>[] params) {
    	int len = params.length;
		long projectId;
		String absolutePath;
		JSONObject result = new JSONObject();
		int i;
    	for(i = 0; i < len; i ++) {
			projectId = Long.parseLong(params[i].get("id").toString());
			absolutePath = (String) params[i].get("path");
			absolutePath = absolutePath.replace("\\", "/");
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				result.put("result", "fail");
				result.put("msg", "没找到Project");
				break;
			}
			projectService.setAbsolutePath(project, absolutePath);
			result.put("project", project);
			result.put("path", absolutePath);
		}
    	if(i == len) {
			result.put("result", "success");
		}
		return result;
	}
	
	@GetMapping("/absolute")
	@ResponseBody
	public JSONObject queryAbsolutePath(@RequestParam("id") long projectId) {
		JSONObject result = new JSONObject();
		Project project = nodeService.queryProject(projectId);
		if(project == null) {
			result.put("result", "fail");
			result.put("msg", "没找到Project");
			return result;
		}
		result.put("result", "success");
		result.put("project", project);
		result.put("path", projectService.getAbsolutePath(project));
		return result;
	}
	
	/*@GetMapping("/absolute")
	@ResponseBody
	public JSONObject setAbsolutePath(@RequestParam("id") long projectId, @RequestParam("path") String absolutePath) {
//		long projectId = (long) params.get("id");
//		String absolutePath = (String) params.get("path");
		JSONObject result = new JSONObject();
		Project project = nodeService.queryProject(projectId);
		if(project == null) {
			result.put("result", "fail");
			result.put("msg", "没找到Project");
			return result;
		}
		projectService.setAbsolutePath(project, absolutePath);
		result.put("result", "success");
		result.put("project", project);
		result.put("path", absolutePath);
		return result;
	}*/
	
	@GetMapping("/cytoscape")
	@ResponseBody
	public JSONObject cytoscape(
			@RequestParam("projectId") Long projectId,
			@RequestParam("dependency") String dependency,
			@RequestParam("level") String level) {
		System.out.println("/project/cytoscape");
		JSONObject result = new JSONObject();
		try {
			Project project = nodeService.queryProject(projectId);
			if(project == null) {
				throw new Exception("没有找到id为 " + projectId + " 的项目");
			}
			if("dynamic".equals(dependency)) {
				List<DynamicCall> calls = dynamicAnalyseService.findFunctionDynamicCallsByProject(project);
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
				List<DynamicCall> calls = dynamicAnalyseService.findFunctionDynamicCallsByProject(project);
				System.out.println(calls.size());
				System.out.println("start to find clones");
//				Iterable<Clone> clones = basicCloneQueryService.queryProjectContainFunctionCloneFunctions(project);
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

	private final JSONObject resultHas = new JSONObject();
	private final List<PackageStructure> childrenall = new ArrayList<>();
	@GetMapping("/has")
	@ResponseBody
	public JSONArray projectHas(@RequestParam("projectId") long projectId, @RequestParam("showType") String showType) {
		JSONArray result = new JSONArray();
		JSONArray clone = new JSONArray();
		JSONArray links = new JSONArray();

		Project project = nodeService.queryProject(projectId);
		ProjectStructure projectStructure = hasRelationService.projectHasInitialize(project);

		List<PackageStructure> childrenPackages = projectStructure.getChildren();
		List<PackageStructure> childrenPackagesnew = new ArrayList<>();
		JSONObject nodeJSON = new JSONObject();
		JSONObject nodeJSON2 = new JSONObject();
		JSONObject nodeJSON3 = new JSONObject();
		JSONObject nodeJSON4 = new JSONObject();

		for(PackageStructure pckstru : childrenPackages){
			PackageStructure pcknew = hasRelationService.packageHasInitialize(pckstru.getPck());
			childrenPackagesnew.add(pcknew);
		}

		nodeJSON.put("name", project.getName());
		nodeJSON.put("id", "id_" + project.getId().toString());
		Collection<ProjectFile> clonefiles = basicCloneQueryService.findProjectContainCloneFiles(project);
		Collection<Clone> cloneList = basicCloneQueryService.findClonesInProject(project);
		clone = basicCloneQueryService.ClonesInProject(cloneList);
		nodeJSON.put("children",getHasJson(clonefiles,childrenPackagesnew, showType));

		nodeJSON2.put("result",nodeJSON);
		nodeJSON3.put("clone",clone);

		result.add(nodeJSON2);
		result.add(nodeJSON3);

		if(showType.equals("graph")) {
			Collection<HotspotPackage> hotspotPackages = hotspotPackageDetector.detectHotspotPackages();
			for (HotspotPackage hotspotPackage : hotspotPackages) {
				JSONObject link = new JSONObject();
				link.put("source_id", "id_" + hotspotPackage.getPackage1().getId().toString());
				link.put("target_id", "id_" + hotspotPackage.getPackage2().getId().toString());
				link.put("source_projectBelong", "id_" + containRelationService.findPackageBelongToProject(hotspotPackage.getPackage1()).getId());
				link.put("target_projectBelong", "id_" + containRelationService.findPackageBelongToProject(hotspotPackage.getPackage2()).getId());
				links.add(link);
			}
			nodeJSON4.put("links",links);
			result.add(nodeJSON4);
		}

		return result;
	}

	/**
	 * 递归遍历项目中所有package的包含关系
	 */
	public JSONArray getHasJson(Collection<ProjectFile> clonefiles,List<PackageStructure> childrenPackages, String showType){
		JSONArray rtJA = new JSONArray();
		for(PackageStructure pckstru :childrenPackages){
			List<PackageStructure> pckList = pckstru.getChildrenPackages();
			List<ProjectFile> fileList = pckstru.getChildrenFiles();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name",pckstru.getPck().getName());
			jsonObject.put("id","id_" + pckstru.getPck().getId().toString());
			JSONArray jsonArray = new JSONArray();
			if(fileList.size() > 0){
				for(ProjectFile profile : fileList){
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("size",1000);
					jsonObject2.put("long_name",profile.getPath());
					if(clonefiles.contains(profile)){
						jsonObject2.put("clone",true);
					}else{
						jsonObject2.put("clone",false);
					}
					jsonObject2.put("name",profile.getName());
					jsonObject2.put("id","id_" + profile.getId().toString());
					jsonArray.add(jsonObject2);
				}
			}

			if(jsonArray.size() > 0){
				if(showType.equals("graph")){
					jsonObject.put("children",jsonArray);
				}else{
					jsonObject.put("collapse_children",jsonArray);
				}
			}

			if(pckList.size()>0){//如果该属性还有子属性,继续做查询,直到该属性没有孩子,也就是最后一个节点
				jsonObject.put("children", getHasJson(clonefiles,pckList, showType));
			}
//			System.out.println(pckList.size());
			rtJA.add(jsonObject);
		}
		return rtJA;
	}
}
