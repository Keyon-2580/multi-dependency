package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.repository.smell.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.UnutilizedAbstractionDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnutilizedAbstraction;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class UnutilizedAbstractionDetectorImpl implements UnutilizedAbstractionDetector {
	
	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;

	@Override
	public Map<Long, List<UnutilizedAbstraction<Type>>> typeUnutilizeds() {
		String key = "typeUnutilizeds";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<UnutilizedAbstraction<Type>>> result = new HashMap<>();
		List<Type> types = asRepository.unutilizedTypes();
		Collection<Project> projects = nodeService.allProjects();
		
		for(Project project : projects) {
			result.put(project.getId(), new ArrayList<>());
		}
		
		for(Type type : types) {
			Project project = containRelationService.findCodeNodeBelongToProject(type);
			result.get(project.getId()).add(new UnutilizedAbstraction<>(type));
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<UnutilizedAbstraction<ProjectFile>>> fileUnutilizeds() {
		String key = "fileUnutilizeds";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> result = new HashMap<>();
		Map<Long, List<UnutilizedAbstraction<Type>>> types = typeUnutilizeds();
		for(Map.Entry<Long, List<UnutilizedAbstraction<Type>>> entry : types.entrySet()) {
			long projectId = entry.getKey();
			Set<ProjectFile> files = new HashSet<>();
			for(UnutilizedAbstraction<Type> type : entry.getValue()) {
				ProjectFile file = containRelationService.findTypeBelongToFile(type.getComponent());
				files.add(file);
			}
			List<UnutilizedAbstraction<ProjectFile>> unutilizedFiles = new ArrayList<>();
			for(ProjectFile file : files) {
				unutilizedFiles.add(new UnutilizedAbstraction<ProjectFile>(file));
			}
			result.put(projectId, unutilizedFiles);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

}
