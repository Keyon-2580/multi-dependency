package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.service.query.as.UnstableDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculator;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import lombok.Setter;

@Service
public class UnstableDependencyDetectorImpl implements UnstableDependencyDetector {

	@Setter
	private int fanInThreshold;
	@Setter
	private int coChangeTimesThreshold;
	@Setter
	private int coChangeFilesThreshold;
	
	@Autowired
	private MetricCalculator metricCalculator;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ProjectFileRepository fileRepository;
	
	@Autowired
	private GitAnalyseService gitAnalyseService;

	@Override
	public Map<Project, List<UnstableFile>> unstableFiles() {
		Map<Project, List<UnstableFile>> result = new HashMap<>();
		
		Map<Long, List<FileMetrics>> fileMetrics = metricCalculator.calculateFileMetrics(false);
		for(Map.Entry<Long, List<FileMetrics>> entry : fileMetrics.entrySet()) {
			Project project = nodeService.queryProject(entry.getKey());
			List<UnstableFile> unstableFiles = new ArrayList<>();
			for(FileMetrics metrics : entry.getValue()) {
				UnstableFile unstableFile = isUnstableFile(metrics);
				if(unstableFile != null) {
					unstableFiles.add(unstableFile);
				}
			}
			if(!unstableFiles.isEmpty()) {
				result.put(project, unstableFiles);
			}
		}
		
		return result;
	}
	
	private UnstableFile isUnstableFile(FileMetrics metrics) {
		if(metrics.getFanIn() < fanInThreshold) {
			return null;
		}
		int coChangeFilesCount = 0;
		ProjectFile file = metrics.getFile();
		Collection<ProjectFile> fanInFiles = fileRepository.calculateFanIn(file.getId());
		List<CoChange> cochanges = new ArrayList<>();
		for(ProjectFile dependedOnFile : fanInFiles) {
			CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(file, dependedOnFile);
			if(cochange != null && cochange.getTimes() >= this.coChangeTimesThreshold) {
				coChangeFilesCount++;
				cochanges.add(cochange);
			}
		}
		UnstableFile result = null;
		if(coChangeFilesCount >= coChangeFilesThreshold) {
			result = new UnstableFile();
			result.setFile(file);
			result.setFanIn(metrics.getFanIn());
			result.addAllCoChanges(cochanges);
		}
		return result;
	}

	@Override
	public void initThreshold() {
		fanInThreshold = 20;
		coChangeTimesThreshold = 4;
		coChangeFilesThreshold = 10;
	}


}
