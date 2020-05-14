package cn.edu.fudan.se.multidependency.service.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
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
//				ZTreeNode projectNode = projectToZTreeNode(project);
				ZTreeNode projectNode = nodeToZTreeNode(project);
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
//			ZTreeNode node = projectToZTreeNode(project);
			ZTreeNode node = nodeToZTreeNode(project);
			result.add(node.toJSON());
		}
		
		Iterable<Library> libraries = staticAnalyseService.findAllLibraries();
		
		for(Library library : libraries) {
			ZTreeNode libNode = new ZTreeNode(library.getId(), String.join(":", library.getNodeType().toString(), library.getFullName()), false);
			
			Iterable<LibraryAPI> apis = containRelationService.findLibraryContainAPIs(library);
			for(LibraryAPI api : apis) {
				ZTreeNode apiNode = new ZTreeNode(api, false);
				libNode.addChild(apiNode);
			}
			
			result.add(libNode.toJSON());
		}
		System.out.println(result);
		return result;
	}
	
	private void addNodesToZTreeNode(ZTreeNode parentZTreeNode, Iterable<? extends Node> nodes) {
		for(Node node : nodes) {
			parentZTreeNode.addChild(nodeToZTreeNode(node));
		}
	}
	
	public ZTreeNode nodeToZTreeNode(Node node) {
		ZTreeNode result = null;
		if(node instanceof Variable) {
			result = new ZTreeNode(node);
		} else if(node instanceof Function) {
			result = new ZTreeNode(node);
			addNodesToZTreeNode(result, containRelationService.findFunctionDirectlyContainVariables((Function) node));
		} else if(node instanceof Type) {
			result = new ZTreeNode(node);
			addNodesToZTreeNode(result, containRelationService.findTypeDirectlyContainFunctions((Type) node));
			addNodesToZTreeNode(result, containRelationService.findTypeDirectlyContainFields((Type) node));
		} else if(node instanceof Namespace) {
			result = new ZTreeNode(node);
			addNodesToZTreeNode(result, containRelationService.findNamespaceDirectlyContainTypes((Namespace) node));
			addNodesToZTreeNode(result, containRelationService.findNamespaceDirectlyContainFunctions((Namespace) node));
			addNodesToZTreeNode(result, containRelationService.findNamespaceDirectlyContainVariables((Namespace) node));
		} else if(node instanceof ProjectFile) {
			result = new ZTreeNode(node);
			addNodesToZTreeNode(result, containRelationService.findFileContainNamespaces((ProjectFile) node));
			addNodesToZTreeNode(result, containRelationService.findFileDirectlyContainTypes((ProjectFile) node));
			addNodesToZTreeNode(result, containRelationService.findFileDirectlyContainFunctions((ProjectFile) node));
			addNodesToZTreeNode(result, containRelationService.findFileDirectlyContainVariables((ProjectFile) node));
		} else if(node instanceof Package) {
			result = new ZTreeNode(node);
			addNodesToZTreeNode(result, containRelationService.findPackageContainFiles((Package) node));
		} else if(node instanceof Project) {
			Project project = (Project) node;
			result = new ZTreeNode(project.getId(), String.join(":", project.getNodeType().toString(), project.getName() + "(" + project.getLanguage() + ")"), false);
			addNodesToZTreeNode(result, containRelationService.findProjectContainPackages(project));
		} else {
			return null;
		}
		return result;
	}
	
}
