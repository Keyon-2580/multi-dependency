package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikePackage;

@Service
public class SimpleHubLikeComponentDetectorImpl implements HubLikeComponentDetector {

	@Override
	public Map<Project, List<HubLikePackage>> hubLikePackages() {
		return null;
	}

	@Override
	public Map<Project, List<HubLikeFile>> hubLikeFiles() {
		return null;
	}

}
