package cn.edu.fudan.se.multidependency.service.spring.as;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.relation.DependOn;

@Service
public class ArchitectureSmellDetectorImpl implements ArchitectureSmellDetector {
	
	@Autowired
	private CycleASDetector cycleASDetector;

	@Override
	public Collection<Collection<DependOn>> findCyclePackages() {
		return cycleASDetector.findCyclePackages();
	}

	
}
