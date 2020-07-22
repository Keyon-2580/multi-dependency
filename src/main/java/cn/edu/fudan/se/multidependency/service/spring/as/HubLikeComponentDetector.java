package cn.edu.fudan.se.multidependency.service.spring.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Package;

public interface HubLikeComponentDetector {

	Collection<Package> hubLikePackages();
}
