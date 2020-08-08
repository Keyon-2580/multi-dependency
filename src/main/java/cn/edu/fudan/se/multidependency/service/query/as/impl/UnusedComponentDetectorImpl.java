package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.as.UnusedComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class UnusedComponentDetectorImpl implements UnusedComponentDetector {
	
	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private ContainRelationService containRelationService;

	@Override
	public Map<Project, List<Package>> unusedPackage() {
		Map<Project, List<Package>> result = new HashMap<>();
		Collection<Package> pcks = asRepository.unusedPackages();
		for(Package pck : pcks) {
			Project project = containRelationService.findPackageBelongToProject(pck);
			List<Package> temp = result.getOrDefault(project, new ArrayList<>());
			temp.add(pck);
			result.put(project, temp);
		}
		return result;
	}

}