package cn.edu.fudan.se.multidependency.controller.relation;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/relation/file/{fileId}")
public class FileRelationController {

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private GitAnalyseService gitAnalyseService;

	@GetMapping("")
	public String index(HttpServletRequest request, @PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		request.setAttribute("file", file);
		request.setAttribute("pck", containRelationService.findFileBelongToPackage(file));
		request.setAttribute("project", containRelationService.findFileBelongToProject(file));
		return "relation/file";
	}
	
	@GetMapping("/contain/type")
	@ResponseBody
	public Object contain(HttpServletRequest request, @PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return containRelationService.findFileDirectlyContainTypes(file);
	}
	
	@GetMapping("/import/type")
	@ResponseBody
	public Object importType(HttpServletRequest request, @PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return staticAnalyseService.findProjectContainFileImportTypeRelations(containRelationService.findFileBelongToProject(file));
	}
	
	@GetMapping("/import/function")
	@ResponseBody
	public Object importFunction(HttpServletRequest request, @PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return staticAnalyseService.findProjectContainFileImportFunctionRelations(containRelationService.findFileBelongToProject(file));
	}
	
	@GetMapping("/import/variable")
	@ResponseBody
	public Object importVariable(HttpServletRequest request, @PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		return staticAnalyseService.findProjectContainFileImportVariableRelations(containRelationService.findFileBelongToProject(file));
	}
	
	@GetMapping("/dependsOn")
	@ResponseBody
	public Object dependsOn(HttpServletRequest request, @PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);
		Project project = containRelationService.findFileBelongToProject(file);
		return staticAnalyseService.findFileDependsOn(project).get(file);
	}
	
	@GetMapping("/cochange")
	@ResponseBody
	public Object cochange(HttpServletRequest request, @PathVariable("fileId") long id) {
		ProjectFile file = nodeService.queryFile(id);

		return null;
		
	}
}
