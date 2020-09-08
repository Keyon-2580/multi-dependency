package cn.edu.fudan.se.multidependency.service.query.as;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.repository.as.ModuleRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;

@Service
public class ModuleService {
	
	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private CacheService cache;
	
	public Module findFileBelongToModule(ProjectFile file) {
		if(cache.findNodeBelongToNode(file, NodeLabelType.Module) != null) {
			return (Module) cache.findNodeBelongToNode(file, NodeLabelType.Module);
		}
		Module result = moduleRepository.findFileBelongToModule(file.getId());
		cache.cacheNodeBelongToNode(file, result);
		return result;
	}
	
	public boolean isInDifferentModule(ProjectFile file1, ProjectFile file2) {
		return !findFileBelongToModule(file1).equals(findFileBelongToModule(file2));
	}
	
	/**
	 * 两个文件所在的模块之间是否没有依赖关系，彼此独立
	 * @param file1
	 * @param file2
	 * @return
	 */
	public boolean isInDependence(ProjectFile file1, ProjectFile file2) {
		return isInDependence(findFileBelongToModule(file1), findFileBelongToModule(file2));
	}
	
	public boolean isInDependence(Module m1, Module m2) {
		if(m1.equals(m2)) {
			return false;
		}
		return !(isDependsOn(m1, m2) || isDependsOn(m2, m1));
	}
	
	public boolean isDependsOn(Module m1, Module m2) {
		String key = "IsDependesOn_" + m1.getId() + " " + m2.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		boolean result = moduleRepository.isDependsOnBetweenModules(m1.getId(), m2.getId());
		cache.cache(getClass(), key, result);
		return result;
	}
	

}
