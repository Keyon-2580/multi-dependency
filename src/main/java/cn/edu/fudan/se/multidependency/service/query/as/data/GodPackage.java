package cn.edu.fudan.se.multidependency.service.query.as.data;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GodPackage {

	private Package pck;
	
	private PackageMetrics pckMetrics;
	
}
