package cn.edu.fudan.se.multidependency.controller;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.spring.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.NodeService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.clone.CloneValueServiceImpl;
import cn.edu.fudan.se.multidependency.service.spring.data.FileGraph;
import cn.edu.fudan.se.multidependency.service.spring.data.PackageGraph;

@Controller
@RequestMapping("/graph")
public class GraphController {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private CloneValueServiceImpl cloneValueService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@RequestMapping("/file")
	@ResponseBody
	public JSONObject fileClones() {
		JSONObject result = new JSONObject();
		FileGraph fileGraph = new FileGraph();
//		fileGraph.setFiles(nodeService.queryAllFiles());
		Collection<Clone> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		fileGraph.setFileClones(fileClones);
		fileGraph.setContainRelationService(containRelationService);
//		return fileGraph.matrix();
		return fileGraph.matrixForClone();
	}
	
	@RequestMapping("/package")
	@ResponseBody
	public JSONObject packageGraph() {
		JSONObject result = new JSONObject();
		try {
			PackageGraph graph = new PackageGraph();
			Map<Long, Package> pcks = nodeService.queryAllPackages();
			graph.setIdToPackage(pcks);
			for(Package pck : pcks.values()) {
				graph.add(pck, containRelationService.findPackageBelongToProject(pck));
			}
			Collection<Clone> fileClones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
			graph.setPackageCloneValues(cloneValueService.queryPackageCloneFromFileClone(fileClones, true));
			result.put("result", "success");
			result.put("data", graph.changeToEChartsGraph());
		} catch (Exception e) {
			
		}
		return result;
	}

	@RequestMapping("/")
	public String graph() {
		return "graph";
	}
	
}
