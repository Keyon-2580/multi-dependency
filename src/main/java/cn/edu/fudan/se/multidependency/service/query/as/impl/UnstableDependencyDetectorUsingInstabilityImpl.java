package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.as.UnstableDependencyDetectorUsingInstability;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableComponentByInstability;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class UnstableDependencyDetectorUsingInstabilityImpl implements UnstableDependencyDetectorUsingInstability {

	@Autowired
	private CacheService cache;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ASRepository asRepository;
	
	public static final int DEFAULT_THRESHOLD_FILE_FANOUT = 15;
	public static final int DEFAULT_THRESHOLD_MODULE_FANOUT = 1;
	public static final double DEFAULT_THRESHOLD_RATIO = 0.3;
	
	private Map<Project, Integer> projectToFileFanOutThreshold = new ConcurrentHashMap<>();
	private Map<Project, Integer> projectToModuleFanOutThreshold = new ConcurrentHashMap<>();
	private Map<Project, Double> projectToRatioThreshold = new ConcurrentHashMap<>();
	
	@Override
	public Map<Long, List<UnstableComponentByInstability<Module>>> unstableModule() {
		return null;
	}

	@Override
	public Map<Long, List<UnstableComponentByInstability<ProjectFile>>> unstableFiles() {
		String key = "unstableFiles";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, List<UnstableComponentByInstability<ProjectFile>>> result = new HashMap<>();
		for(Project project : projects) {
			List<UnstableComponentByInstability<ProjectFile>> temp = asRepository.unstableFilesByInstability(project.getId(), getFileFanOutThreshold(project), getRatioThreshold(project));
			result.put(project.getId(), temp);
		}
		
		cache.cache(getClass(), key, result);
		return result;
	}
	
	public void setRatio(Project project, double threshold) {
		this.projectToRatioThreshold.put(project, threshold);
		cache.remove(getClass());
	}
	
	public void setFileFanOutThreshold(Project project, int threshold) {
		this.projectToFileFanOutThreshold.put(project, threshold);
		cache.remove(getClass());
	}
	
	public void setModuleFanOutThreshold(Project project, int threshold) {
		this.projectToModuleFanOutThreshold.put(project, threshold);
		cache.remove(getClass());
	}
	
	public int getFileFanOutThreshold(Project project) {
		if(projectToFileFanOutThreshold.get(project) == null) {
			projectToFileFanOutThreshold.put(project, DEFAULT_THRESHOLD_FILE_FANOUT);
		}
		return projectToFileFanOutThreshold.get(project);
	}
	
	public int getModuleFanOutThreshold(Project project) {
		if(projectToModuleFanOutThreshold.get(project) == null) {
			projectToModuleFanOutThreshold.put(project, DEFAULT_THRESHOLD_MODULE_FANOUT);
		}
		return projectToModuleFanOutThreshold.get(project);
	}
	
	public double getRatioThreshold(Project project) {
		if(projectToRatioThreshold.get(project) == null) {
			projectToRatioThreshold.put(project, DEFAULT_THRESHOLD_RATIO);
		}
		return projectToRatioThreshold.get(project);
	}

}
