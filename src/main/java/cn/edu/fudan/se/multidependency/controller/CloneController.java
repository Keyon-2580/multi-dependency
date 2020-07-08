package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.spring.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.spring.NodeService;
import cn.edu.fudan.se.multidependency.service.spring.clone.CloneValueService;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneValue;
import cn.edu.fudan.se.multidependency.service.spring.data.PackageCloneValueWithFileCoChange;

@Controller
@RequestMapping("/clone")
public class CloneController {

	@Autowired
	private CloneValueService cloneValueService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private NodeService nodeService;
	
	@GetMapping("/package")
	@ResponseBody
	public Collection<CloneValue<Package>> cloneInPackages() {
		return cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), true);
	}
	
	/**
	 * 两个包之间的文件依赖
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double")
	@ResponseBody
	public CloneValue<Package> cloneInPackage(@RequestParam("package1") long package1Id,
			@RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		return cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), true, pck1, pck2);
	}
	
	/**
	 * 两个包之间的文件依赖加上cochange次数
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double/cochange")
	@ResponseBody
	public PackageCloneValueWithFileCoChange cloneInPackageWithCoChange(@RequestParam("package1") long package1Id,
			@RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		try {
			return cloneValueService.queryPackageCloneWithFileCoChange(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), true, pck1, pck2);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@PostMapping("/package/multiple")
	@ResponseBody
	public Collection<CloneValue<Package>> cloneInMultiplePackages(@RequestBody Map<String, Object> params) {
		List<Long> pckIds = (List<Long>) params.getOrDefault("ids", new ArrayList<>());
		List<Package> pcks = new ArrayList<>();
		for(Long pckId : pckIds) {
			Package pck = nodeService.queryPackage(pckId);
			if(pck != null) {
				pcks.add(pck);
			}
		}
		return cloneValueService.queryPackageCloneFromFileClone(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), true, pcks);
	}
}
