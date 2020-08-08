package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChange;

public interface CloneValueService {
	
	/**
	 * 根据函数间的克隆找出微服务间的克隆
	 * @param functionClones
	 * @return
	 */
	Collection<CloneValueForDoubleNodes<MicroService>> findMicroServiceCloneFromFunctionClone(Collection<Clone> functionClones);
	
	/**
	 * 根据文件间的克隆找出微服务间的克隆
	 * @param fileClones
	 * @param removeSameNode
	 * @return
	 */
	Collection<CloneValueForDoubleNodes<MicroService>> findMicroServiceCloneFromFileClone(Collection<Clone> fileClones);
	
	/**
	 * 根据函数间的克隆找出项目间的克隆
	 * @param functionClones
	 * @return
	 */
	Collection<CloneValueForDoubleNodes<Project>> findProjectCloneFromFunctionClone(Collection<Clone> functionClones);
	
	/**
	 * 根据文件间的克隆找出项目间的克隆
	 * @param fileClones
	 * @param removeSameNode
	 * @return
	 */
	Collection<CloneValueForDoubleNodes<Project>> queryProjectCloneFromFileClone(Collection<Clone> fileClones);
	
	Map<Package, Map<Package, CloneValueForDoubleNodes<Package>>> queryPackageCloneFromFileClone(Collection<Clone> fileClones);
	
	Collection<CloneValueForDoubleNodes<Package>> queryPackageCloneFromFileCloneSort(Collection<Clone> fileClones);
	
	default Collection<CloneValueForDoubleNodes<Package>> queryPackageCloneFromFileClone(Collection<Clone> fileClones, List<Package> pcks) {
		if(pcks == null || pcks.isEmpty()) {
			return new ArrayList<>();
		}
		List<CloneValueForDoubleNodes<Package>> result = new ArrayList<>();
		for(int i = 0; i < pcks.size(); i++) {
			for(int j = i + 1; j < pcks.size(); j++) {
				result.add(queryPackageCloneFromFileCloneSort(fileClones, pcks.get(i), pcks.get(j)));
			}
		}
		
		return result;
	}
	
	/**
	 * 两个包之间的文件级克隆的聚合，两个包之间不分先后顺序
	 * @param fileClones
	 * @param removeSameNode
	 * @param pck1
	 * @param pck2
	 * @return
	 */
	default CloneValueForDoubleNodes<Package> queryPackageCloneFromFileCloneSort(Collection<Clone> fileClones, Package pck1, Package pck2) {
		Map<Package, Map<Package, CloneValueForDoubleNodes<Package>>> packageClones = queryPackageCloneFromFileClone(fileClones);
		Map<Package, CloneValueForDoubleNodes<Package>> map = packageClones.getOrDefault(pck1, new HashMap<>());
		CloneValueForDoubleNodes<Package> result = map.get(pck2);
		if(result == null) {
			map = packageClones.getOrDefault(pck2, new HashMap<>());
			result = map.get(pck1);
		}
		if(result != null) {
			result.sortChildren();
		}
		return result;
	}
	
	PackageCloneValueWithFileCoChange queryPackageCloneWithFileCoChange(Collection<Clone> fileClones, Package pck1, Package pck2) throws Exception;
	
}