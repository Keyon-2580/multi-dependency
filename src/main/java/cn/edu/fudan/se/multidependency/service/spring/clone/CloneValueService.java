package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.Collection;

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
	
}
