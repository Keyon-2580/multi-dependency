package cn.edu.fudan.se.multidependency.service.query.as.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;
import lombok.Data;

@Data
public class UnstablePackage {

	private Package pck;
	
	private PackageMetrics metrics;
	
	private List<DependsOn> totalDependsOns = new ArrayList<>();
	
	private List<DependsOn> badDependsOns = new ArrayList<>();
	
	public void addAllBadDependencies(Collection<DependsOn> badDependsOns) {
		this.badDependsOns.addAll(badDependsOns);
	}
	
	public void addAllTotalDependencies(Collection<DependsOn> totalDependsOns) {
		this.totalDependsOns.addAll(totalDependsOns);
	}
	
}
