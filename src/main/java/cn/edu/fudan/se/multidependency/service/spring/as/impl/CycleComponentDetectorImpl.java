package cn.edu.fudan.se.multidependency.service.spring.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependOn;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependOnRepository;
import cn.edu.fudan.se.multidependency.service.spring.as.CycleComponentDetector;
import cn.edu.fudan.se.multidependency.service.spring.as.data.CyclePackages;

@Service
public class CycleComponentDetectorImpl implements CycleComponentDetector {

	@Autowired
	private PackageRepository packageRepository;
	
	@Autowired
	private DependOnRepository dependOnRepository;
	
	@SuppressWarnings("unused")
	private Collection<DependOn> findCyclePackageRelationsByIds(CyclePackages cycle) {
		long[] ids = new long[cycle.getPackages().size()];
		int i = 0;
		for(Package pck : cycle.getPackages()) {
			ids[i] = pck.getId();
		}
		return dependOnRepository.cyclePackagesByIds(ids);
	}
	
	private Collection<DependOn> findCyclePackageRelationsBySCC(CyclePackages cycle) {
		return dependOnRepository.cyclePackagesBySCC(cycle.getPartition());
	}
	
	@Override
	public Collection<Collection<DependOn>> cyclePackages() {
		Collection<CyclePackages> cyclePackages = packageRepository.cyclePackages();
		List<Collection<DependOn>> result = new ArrayList<>();
		for(CyclePackages cycle : cyclePackages) {
			result.add(findCyclePackageRelationsBySCC(cycle));
		}
		return result;
	}
}
