package cn.edu.fudan.se.multidependency.controller;

import java.io.OutputStream;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.query.ProjectService;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneShowService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneValueService;
import cn.edu.fudan.se.multidependency.service.query.clone.SimilarPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChange;
import cn.edu.fudan.se.multidependency.service.query.clone.data.SimilarPackage;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/clone")
public class CloneController {
	
	@Autowired
	private ProjectService projectService;

	@Autowired
	private CloneValueService cloneValueService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private CloneShowService cloneShowService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private SimilarPackageDetector similarPackageDetector;

	@GetMapping("/packages")
	public String graph() {
		return "clonepackage";
	}


	@GetMapping("/packages/export")
	@ResponseBody
	public void exportSimilarPackages(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.addHeader("Content-Disposition", "attachment;filename=similar_packages.xlsx");
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			OutputStream stream = response.getOutputStream();

			similarPackageDetector.exportSimilarPackages(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@GetMapping("/package/cytoscape")
	@ResponseBody
	public JSONObject packageClonesToCytoscape() {
		JSONObject result = new JSONObject();
		result.put("result", "success");
		result.put("value", cloneShowService.crossPackageCloneToCytoscape(
				cloneValueService.queryPackageCloneFromFileCloneSort(
				basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE))));
		
		return result;
	}
	
	@GetMapping("/package")
	@ResponseBody
	public Collection<CloneValueForDoubleNodes<Package>> cloneInPackages() {
		return cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE));
	}
	
	@GetMapping("/package/duplicated")
	@ResponseBody
	public Collection<SimilarPackage> similarPackages(@RequestParam("threshold") int threshold,
			@RequestParam("percentage") double percentage) {
		return similarPackageDetector.detectSimilarPackages(threshold, percentage);
	}
	
	/**
	 * 两个包之间的文件依赖
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double")
	@ResponseBody
	public CloneValueForDoubleNodes<Package> cloneInPackage(@RequestParam("package1") long package1Id,
			@RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		JSONObject result = new JSONObject();
		CloneValueForDoubleNodes<Package> cloneValue = cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
		JSONObject pck1JSON = new JSONObject();
		pck1JSON.put("package", pck1);
		Collection<ProjectFile> files1 = containRelationService.findPackageContainFiles(pck1);
		pck1JSON.put("files", files1);
		JSONObject pck2JSON = new JSONObject();
		pck2JSON.put("package", pck2);
		Collection<ProjectFile> files2 = containRelationService.findPackageContainFiles(pck2);
		pck2JSON.put("files", files2);
		
		result.put("result", "success");
		result.put("cloneValue", cloneValue);
		result.put("package1", pck1JSON);
		result.put("package2", pck2JSON);
		return cloneValue;
	}
	
	/**
	 * 两个包之间的文件依赖
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double/json")
	@ResponseBody
	public JSONObject cloneInPackageJson(@RequestParam("package1") long package1Id,
			@RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		CloneValueForDoubleNodes<Package> value = cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
		JSONObject result = new JSONObject();
		List<Clone> children = value.getChildren();
		result.put("result", cloneShowService.graphFileClones(children));
		return result;
	}
	
	/**
	 * 两个包之间的文件依赖加上cochange次数
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double/cochange")
	@ResponseBody
	public PackageCloneValueWithFileCoChange clonesInPackageWithCoChange(@RequestParam("package1") long package1Id, @RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		try {
			return cloneValueService.queryPackageCloneWithFileCoChange(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@GetMapping("/package/double/graph")
	@ResponseBody
	public JSONArray clonesInPackageToGraph(@RequestParam("package1") long package1Id,
			@RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		try {
			CloneValueForDoubleNodes<Package> value = cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
			Collection<Clone> clones = value == null ? new ArrayList<>() : value.getChildren();
			return cloneShowService.graphFileClones(clones);
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONArray();
		}
	}
	
	@PostMapping("/package/multiple")
	@ResponseBody
	public Collection<CloneValueForDoubleNodes<Package>> cloneInMultiplePackages(@RequestBody Map<String, Object> params) {
		List<Long> pckIds = (List<Long>) params.getOrDefault("ids", new ArrayList<>());
		List<Package> pcks = new ArrayList<>();
		for(Long pckId : pckIds) {
			Package pck = nodeService.queryPackage(pckId);
			if(pck != null) {
				pcks.add(pck);
			}
		}
		return cloneValueService.queryPackageCloneFromFileClone(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pcks);
	}
	
	@GetMapping("/compare")
	public String compare(@RequestParam("id1") long file1Id, @RequestParam("id2") long file2Id) {
		ProjectFile file1 = nodeService.queryFile(file1Id);
		ProjectFile file2 = nodeService.queryFile(file2Id);
		if(file1 == null || file2 == null) {
			return "error";
		}
		Project project1 = containRelationService.findFileBelongToProject(file1);
		Project project2 = containRelationService.findFileBelongToProject(file2);
		String file1AbsolutePath = projectService.getAbsolutePath(project1) + file1.getPath();
		String file2AbsolutePath = projectService.getAbsolutePath(project2) + file2.getPath();
		
		return "redirect:/compare?leftFilePath=" + file1AbsolutePath + "&rightFilePath=" + file2AbsolutePath;
	}

	/**
	 * 查询一个project中所有的包克隆关系
	 * @return
	 */
	@GetMapping("/packages/all")
	@ResponseBody
	public JSONArray clonesInPackageWithCoChange(/*@RequestParam("project") long projectId*/) {
		Project project = nodeService.queryProject(67728);
		Collection<Package> pckList = containRelationService.findProjectContainPackages(project);
		Collection<Clone> clones = new ArrayList<>();

		List<Package> pcks = new ArrayList<>(pckList);

		int k = 0;

		for(int i = 0; i < pcks.size(); i++) {
			for(int j = i + 1; j < pcks.size(); j++) {
				try {
					CloneValueForDoubleNodes<Package> value = cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pcks.get(i), pcks.get(j));
					if(value != null){
						clones.addAll(value.getChildren());
					}
				} catch (Exception e) {
					e.printStackTrace();
					return new JSONArray();
				}
			}
		}

		assert false;
		return cloneShowService.graphFileClones(clones);
	}

}
