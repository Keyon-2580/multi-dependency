package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.GodComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.GodFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.GodPackage;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculator;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
@Scope(WebApplicationContext.SCOPE_SESSION)
public class GodComponentDetectorImpl implements GodComponentDetector {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private MetricCalculator metricCalculator;

	@Override
	public Map<Long, List<GodFile>> godFiles() {
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, List<GodFile>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), godFiles(project));
		}
		return result;
	}

	@Override
	public Map<Long, List<GodPackage>> godPackages() {
		Collection<Project> projects = nodeService.allProjects();
		Map<Long, List<GodPackage>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), godPackages(project));
		}
		return result;
	}
	
	private Map<Project, Integer> projectToMinFileLoc = new ConcurrentHashMap<>();
	
	private Map<Project, Integer> projectToMinFileCountInPackage = new ConcurrentHashMap<>();
	
	private List<GodFile> godFiles(Project project) {
		Collection<FileMetrics> metrics = metricCalculator.calculateFileMetrics(project);
		List<GodFile> result = new ArrayList<>();
		for(FileMetrics metric : metrics) {
			if(isGodFile(project, metric)) {
				result.add(new GodFile(metric.getFile(), metric));
			}
		}
		return result;
	}
	
	protected boolean isGodFile(Project project, FileMetrics metrics) {
		return metrics.getLoc() >= getProjectMinFileLoc(project);
	}
	
	private List<GodPackage> godPackages(Project project) {
		Collection<PackageMetrics> metrics = metricCalculator.calculatePackageMetrics(project);
		List<GodPackage> result = new ArrayList<>();
		for(PackageMetrics metric : metrics) {
			if(isGodPackage(project, metric)) {
				result.add(new GodPackage(metric.getPck(), metric));
			}
		}
		return result;
	}
	
	protected boolean isGodPackage(Project project, PackageMetrics metrics) {
		return metrics.getNof() >= getProjectMinFileCountInPackage(project);
	}
	
	private int defaultProjectMinFileLoc(Project project) {
		return 1000;
	}
	
	private int defaultProjectMinFileCountInPackage(Project project) {
		return 30;
	}

	@Override
	public int getProjectMinFileLoc(Project project) {
		if(projectToMinFileLoc.get(project) == null) {
			projectToMinFileLoc.put(project, defaultProjectMinFileLoc(project));
		}
		return projectToMinFileLoc.get(project);
	}

	@Override
	public void setProjectMinFileLoc(Project project, int minFileLoc) {
		projectToMinFileLoc.put(project, minFileLoc);
	}

	@Override
	public int getProjectMinFileCountInPackage(Project project) {
		if(projectToMinFileCountInPackage.get(project) == null) {
			projectToMinFileCountInPackage.put(project, defaultProjectMinFileCountInPackage(project));
		}
		return projectToMinFileCountInPackage.get(project);
	}

	@Override
	public void setProjectMinFileCountInPackage(Project project, int minFileCountInPackage) {
		projectToMinFileCountInPackage.put(project, minFileCountInPackage);
	}
	
}
