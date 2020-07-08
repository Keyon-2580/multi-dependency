package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneValue;
import cn.edu.fudan.se.multidependency.service.spring.data.PackageCloneValueWithFileCoChange;

public interface CloneValueService {
	
	/**
	 * 根据函数间的克隆找出微服务间的克隆
	 * @param functionClones
	 * @return
	 */
	Collection<CloneValue<MicroService>> findMicroServiceCloneFromFunctionClone(Collection<Clone> functionClones, boolean removeSameNode);
	
	/**
	 * 根据文件间的克隆找出微服务间的克隆
	 * @param fileClones
	 * @param removeSameNode
	 * @return
	 */
	Collection<CloneValue<MicroService>> findMicroServiceCloneFromFileClone(Collection<Clone> fileClones, boolean removeSameNode);
	
	/**
	 * 根据函数间的克隆找出项目间的克隆
	 * @param functionClones
	 * @return
	 */
	Collection<CloneValue<Project>> findProjectCloneFromFunctionClone(Collection<Clone> functionClones, boolean removeSameNode);
	
	/**
	 * 根据文件间的克隆找出项目间的克隆
	 * @param fileClones
	 * @param removeSameNode
	 * @return
	 */
	Collection<CloneValue<Project>> queryProjectCloneFromFileClone(Collection<Clone> fileClones, boolean removeSameNode);
	
	Map<Package, Map<Package, CloneValue<Package>>> queryPackageCloneFromFileClone(Collection<Clone> fileClones, boolean removeSameNode);
	
	Collection<CloneValue<Package>> queryPackageCloneFromFileCloneSort(Collection<Clone> fileClones, boolean removeSameNode);
	
	default Collection<CloneValue<Package>> queryPackageCloneFromFileClone(Collection<Clone> fileClones, boolean removeSameNode, List<Package> pcks) {
		if(pcks == null || pcks.isEmpty()) {
			return new ArrayList<>();
		}
		List<CloneValue<Package>> result = new ArrayList<>();
		for(int i = 0; i < pcks.size(); i++) {
			for(int j = i + 1; j < pcks.size(); j++) {
				result.add(queryPackageCloneFromFileCloneSort(fileClones, removeSameNode, pcks.get(i), pcks.get(j)));
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
	default CloneValue<Package> queryPackageCloneFromFileCloneSort(Collection<Clone> fileClones, boolean removeSameNode, Package pck1, Package pck2) {
		Map<Package, Map<Package, CloneValue<Package>>> packageClones = queryPackageCloneFromFileClone(fileClones, removeSameNode);
		Map<Package, CloneValue<Package>> map = packageClones.getOrDefault(pck1, new HashMap<>());
		CloneValue<Package> value = map.get(pck2);
		if(value == null) {
			map = packageClones.getOrDefault(pck2, new HashMap<>());
			value = map.get(pck1);
		}
		value.sortChildren();
		return value;
	}
	
	PackageCloneValueWithFileCoChange queryPackageCloneWithFileCoChange(Collection<Clone> fileClones, boolean removeSameNode, Package pck1, Package pck2) throws Exception;
	
}
