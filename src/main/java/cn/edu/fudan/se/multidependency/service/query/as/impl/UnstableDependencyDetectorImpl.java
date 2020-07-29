package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.UnstableDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;

@Service
public class UnstableDependencyDetectorImpl implements UnstableDependencyDetector {

	@Override
	public Map<Project, List<UnstableFile>> unstableFiles() {
		Map<Project, List<UnstableFile>> result = new HashMap<>();
		return result;
	}

}
