package cn.edu.fudan.se.multidependency.controller.relation;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Controller
@RequestMapping("/relation/function/{functionId}")
public class FunctionRelationController {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;

	@GetMapping("")
	public String index(HttpServletRequest request, @PathVariable("functionId") long id) {
		Function function = nodeService.queryFunction(id);
		ProjectFile file = containRelationService.findFunctionBelongToFile(function);
		request.setAttribute("function", function);
		request.setAttribute("file", file);
		request.setAttribute("pck", containRelationService.findFileBelongToPackage(file));
		request.setAttribute("project", containRelationService.findFileBelongToProject(file));
		return "relation/file";
	}
	
	@GetMapping("/call")
	@ResponseBody
	public Object call(HttpServletRequest request, @PathVariable("functionId") long id) {
		Function function = nodeService.queryFunction(id);
		return staticAnalyseService.findAllFunctionCallRelationsGroupByCaller().getOrDefault(function, new ArrayList<>());
	}
	
}
