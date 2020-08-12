package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculator;

public class UnstableFileDetectStrategyUsingInstability implements UnstableFileDetectStrategy {
	
	private MetricCalculator metricCalculator;
	
	private StaticAnalyseService staticAnalyseService;
	
	private double unstableThreshold;
	
	public UnstableFileDetectStrategyUsingInstability(MetricCalculator metricCalculator,
			StaticAnalyseService staticAnalyseService, double unstableThreshold) {
		super();
		this.metricCalculator = metricCalculator;
		this.staticAnalyseService = staticAnalyseService;
		this.unstableThreshold = unstableThreshold;
	}

	@Override
	public UnstableFile isUnstableFile(Project project, FileMetrics metrics) {
		List<FileMetrics> fileMetrics = metricCalculator.calculateFileMetrics().get(project.getId());
		Map<ProjectFile, FileMetrics> metricsMap = new HashMap<>();
		for(FileMetrics fileMetric : fileMetrics) {
			metricsMap.put(fileMetric.getFile(), fileMetric);
		}
		Map<ProjectFile, List<DependsOn>> pckToDependsOns = staticAnalyseService.findFileDependsOn(project);
		for(Map.Entry<ProjectFile, List<DependsOn>> entry : pckToDependsOns.entrySet()) {
			ProjectFile file = entry.getKey();
			List<DependsOn> badDependencies = new ArrayList<>();
			List<DependsOn> totalDependencies = new ArrayList<>();
			List<DependsOn> dependsOns = entry.getValue();
			for(DependsOn dependsOn : dependsOns) {
				ProjectFile dependsOnFile = (ProjectFile) dependsOn.getEndNode();
				if(metricsMap.get(file).getInstability() < metricsMap.get(dependsOnFile).getInstability()) {
					badDependencies.add(dependsOn);
				}
				totalDependencies.add(dependsOn);
			}
			if(totalDependencies.isEmpty()) {
				continue;
			}
			double doUD = badDependencies.size() / (totalDependencies.size() + 0.0);
			if(doUD >= unstableThreshold) {
				UnstableFile unstableFile = new UnstableFile();
				unstableFile.setFile(file);
				unstableFile.setFanIn(metricsMap.get(file).getFanIn());
				return unstableFile;
			}
		}
		return null;
	}

}
