package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;

public class UnstableFileDetectStrategyUsingCoChange implements UnstableFileDetectStrategy {
	
	private int fanInThreshold;
	
	private int coChangeTimesThreshold;
	
	private int coChangeFilesThreshold;
	
	private ProjectFileRepository fileRepository;
	
	private GitAnalyseService gitAnalyseService;
	
	public UnstableFileDetectStrategyUsingCoChange(int fanInThreshold, int coChangeTimesThreshold,
			int coChangeFilesThreshold, ProjectFileRepository fileRepository, GitAnalyseService gitAnalyseService) {
		super();
		this.fanInThreshold = fanInThreshold;
		this.coChangeTimesThreshold = coChangeTimesThreshold;
		this.coChangeFilesThreshold = coChangeFilesThreshold;
		this.fileRepository = fileRepository;
		this.gitAnalyseService = gitAnalyseService;
	}

	@Override
	public UnstableFile isUnstableFile(Project project, FileMetrics metrics) {
		if(metrics.getFanIn() < fanInThreshold) {
			return null;
		}
		int coChangeFilesCount = 0;
		ProjectFile file = metrics.getFile();
		Collection<ProjectFile> fanInFiles = fileRepository.calculateFanIn(file.getId());
		List<CoChange> cochanges = new ArrayList<>();
		for(ProjectFile dependedOnFile : fanInFiles) {
			CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(file, dependedOnFile);
			if(cochange != null && cochange.getTimes() >= coChangeTimesThreshold) {
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

}
