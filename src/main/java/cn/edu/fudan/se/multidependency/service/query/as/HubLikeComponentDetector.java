package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikePackage;

public interface HubLikeComponentDetector {

	Map<Project, List<HubLikePackage>> hubLikePackages();
	
	Map<Project, List<HubLikeFile>> hubLikeFiles();
}
