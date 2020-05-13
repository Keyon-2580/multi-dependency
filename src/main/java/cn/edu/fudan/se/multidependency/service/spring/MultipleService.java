package cn.edu.fudan.se.multidependency.service.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.utils.ZTreeUtil.ZTreeNode;

@Service
public class MultipleService {
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private MicroserviceService msService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	public JSONArray allNodesToZTree() {
		JSONArray result = new JSONArray();
		
		
		Iterable<MicroService> allMicroServices = msService.findAllMicroService().values();
		
		Iterable<Project> allProjects = staticAnalyseService.allProjects().values();
		
		Map<Project, Boolean> isProjectAddToNodes = new HashMap<>();
		
		for(MicroService ms : allMicroServices) {
			ZTreeNode node = new ZTreeNode(ms, false);
			
			Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
			for(Project project : projects) {
				isProjectAddToNodes.put(project, true);
//				ZTreeNode projectNode = new ZTreeNode(project.getName(), false);
				ZTreeNode projectNode = projectToZTreeNode(project);
				node.addChild(projectNode);
			}
			
			Iterable<RestfulAPI> apis = containRelationService.findMicroServiceContainRestfulAPI(ms);
			for(RestfulAPI api : apis) {
				ZTreeNode apiNode = new ZTreeNode(api, false);
				node.addChild(apiNode);
			}
			
			result.add(node.toJSON());
		}
		
		for(Project project : allProjects) {
			if(isProjectAddToNodes.get(project) != null && isProjectAddToNodes.get(project)) {
				continue;
			}
			ZTreeNode node = projectToZTreeNode(project);
			result.add(node.toJSON());
		}
		
		Iterable<Library> libraries = staticAnalyseService.findAllLibraries();
		
		for(Library library : libraries) {
			ZTreeNode libNode = new ZTreeNode(String.join(":", library.getNodeType().toString(), library.getFullName()), false);
			
			Iterable<LibraryAPI> apis = containRelationService.findLibraryContainAPIs(library);
			for(LibraryAPI api : apis) {
				ZTreeNode apiNode = new ZTreeNode(api, false);
				libNode.addChild(apiNode);
			}
			
			result.add(libNode.toJSON());
		}
		
		return result;
	}
	
	public ZTreeNode projectToZTreeNode(Project project) {
		ZTreeNode projectNode = new ZTreeNode(String.join(":", project.getNodeType().toString(), project.getName() + "(" + project.getLanguage() + ")"), false);
		Iterable<Package> packages = containRelationService.findProjectContainPackages(project);
		
		for(Package pck : packages) {

			ZTreeNode pckNode = new ZTreeNode(pck, false);
			projectNode.addChild(pckNode);
			
			Iterable<ProjectFile> files = containRelationService.findPackageContainFiles(pck);
			for(ProjectFile file : files) {
				
				ZTreeNode fileNode = new ZTreeNode(file, false);
				pckNode.addChild(fileNode);
				
				Iterable<Type> types = containRelationService.findFileContainTypes(file);
				for(Type type : types) {
					ZTreeNode typeNode = new ZTreeNode(type, false);
					fileNode.addChild(typeNode);
					
					Iterable<Function> functions = containRelationService.findTypeDirectlyContainFunctions(type);
					for(Function function : functions) {
						ZTreeNode functionNode = new ZTreeNode(function, false);
						typeNode.addChild(functionNode);
						
						Iterable<Variable> variables = containRelationService.findFunctionDirectlyContainVariables(function);
						for(Variable variable : variables) {
							ZTreeNode variableNode = new ZTreeNode(variable, false);
							functionNode.addChild(variableNode);
						}
					}
					
					Iterable<Variable> variables = containRelationService.findTypeDirectlyContainFields(type);
					for(Variable variable : variables) {
						ZTreeNode variableNode = new ZTreeNode(variable, false);
						typeNode.addChild(variableNode);
					}
				}
				
				Iterable<Function> functions = containRelationService.findFileDirectlyContainFunctions(file);
				for(Function function : functions) {
					ZTreeNode functionNode = new ZTreeNode(function, false);
					fileNode.addChild(functionNode);
					
					Iterable<Variable> variables = containRelationService.findFunctionDirectlyContainVariables(function);
					for(Variable variable : variables) {
						ZTreeNode variableNode = new ZTreeNode(variable, false);
						functionNode.addChild(variableNode);
					}
				}
			}
		}
		return projectNode;
	}
	
	
	/*public JSONArray projectNodesToZTree(Project project) {
		JSONArray result = new JSONArray();
		
		Iterable<Package> packages = containRelationService.findProjectContainPackages(project);
		Map<String, Package> pathToPackages = new HashMap<>();
		for(Package pck : packages) {
			pathToPackages.put(pck.getDirectoryPath(), pck);
		}
		
		for(Package pck : packages) {
			JSONObject pckNode = CytoscapeUtil.toCytoscapeNode(pck, "Package: " + pck.getName(), "Package");
			String parentPckPath = FileUtil.extractDirectoryFromFile(pck.getDirectoryPath().substring(0, pck.getDirectoryPath().length() - 1));
			Package parentPck = pathToPackages.get(parentPckPath + "/");
			if(parentPck != null) {
				System.out.println("not null");
				pckNode.getJSONObject("data").put("parent", String.valueOf(parentPck.getId()));
			}
			nodes.add(pckNode);
			
			if(level < LEVEL_FILE) {
				continue;
			}
			Iterable<ProjectFile> files = containRelationService.findPackageContainFiles(pck);
			for(ProjectFile file : files) {
				JSONObject fileJson = CytoscapeUtil.toCytoscapeNode(file, "File: " + file.getName(), "File");
				fileJson.getJSONObject("data").put("parent", pck.getId() + "");
				nodes.add(fileJson);
				
				if(level < LEVEL_TYPE) {
					continue;
				}
				Iterable<Type> types = containRelationService.findFileContainTypes(file);
				for(Type type : types) {
					JSONObject typeJson = CytoscapeUtil.toCytoscapeNode(type, "Type: " + type.getName(), "Type");
					typeJson.getJSONObject("data").put("parent", file.getId() + "");
					nodes.add(typeJson);
					
					if(level < LEVEL_FUNCTION) {
						continue;
					}
					Iterable<Function> functions = containRelationService.findTypeContainFunctions(type);
					for(Function function : functions) {
						JSONObject functionJson = CytoscapeUtil.toCytoscapeNode(function, "Function: " + function.getName(), "Function");
						functionJson.getJSONObject("data").put("parent", type.getId() + "");
						nodes.add(functionJson);
						
						if(level < LEVEL_VARIABLE) {
							continue;
						}
						Iterable<Variable> variables = containRelationService.findFunctionContainVariables(function);
						for(Variable variable : variables) {
							JSONObject variableJson = CytoscapeUtil.toCytoscapeNode(variable, "Variable: " + variable.getName(), "Variable");
							variableJson.getJSONObject("data").put("parent", function.getId() + "");
							nodes.add(variableJson);
						}

					}
					
					if(level < LEVEL_VARIABLE) {
						continue;
					}
					Iterable<Variable> variables = containRelationService.findTypeContainVariables(type);
					for(Variable variable : variables) {
						JSONObject variableJson = CytoscapeUtil.toCytoscapeNode(variable, "Variable: " + variable.getName(), "Variable");
						variableJson.getJSONObject("data").put("parent", type.getId() + "");
						nodes.add(variableJson);
					}
				}
				
				if(level < LEVEL_FUNCTION) {
					continue;
				}
				Iterable<Function> functions = containRelationService.findFileContainFunctions(file);
				for(Function function : functions) {
					JSONObject functionJson = CytoscapeUtil.toCytoscapeNode(function, "Function: " + function.getName(), "Function");
					functionJson.getJSONObject("data").put("parent", file.getId() + "");
					nodes.add(functionJson);
					
					if(level < LEVEL_VARIABLE) {
						continue;
					}
					Iterable<Variable> variables = containRelationService.findFunctionContainVariables(function);
					for(Variable variable : variables) {
						JSONObject variableJson = CytoscapeUtil.toCytoscapeNode(variable, "Variable: " + variable.getName(), "Variable");
						variableJson.getJSONObject("data").put("parent", function.getId() + "");
						nodes.add(variableJson);
					}
				}
			}
		}
		
		return result;
	}*/
}
