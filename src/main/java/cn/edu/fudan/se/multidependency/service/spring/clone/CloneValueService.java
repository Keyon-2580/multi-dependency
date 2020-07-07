package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneValue;

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
	
	default CloneValue<Package> queryPackageCloneFromFileCloneSort(Collection<Clone> fileClones, boolean removeSameNode, Package pck1, Package pck2) {
		Map<Package, Map<Package, CloneValue<Package>>> packageClones = queryPackageCloneFromFileClone(fileClones, removeSameNode);
		Map<Package, CloneValue<Package>> map = packageClones.getOrDefault(pck1, new HashMap<>());
		CloneValue<Package> value = map.get(pck2);
		if(value == null) {
			map = packageClones.getOrDefault(pck2, new HashMap<>());
			value = map.get(pck1);
		} 
		return value;
	}
	
}
