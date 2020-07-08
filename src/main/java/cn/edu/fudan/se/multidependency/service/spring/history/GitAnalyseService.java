package cn.edu.fudan.se.multidependency.service.spring.history;

import java.util.Collection;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;

public interface GitAnalyseService {

	Iterable<Commit> findAllCommits();
	
	Map<Developer, Map<Project, Integer>> calCntOfDevUpdPro();
	
	Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsList();
	
	Map<ProjectFile, Integer> calCntOfFileBeUpd();
	
	Map<ProjectFile, Integer> getTopKFileBeUpd(int k);
	
	Collection<CoChange> calCntOfFileCoChange();
	
	Collection<CoChange> getTopKFileCoChange(int k);
	
	CoChange findCoChangeBetweenTwoFiles(ProjectFile file1, ProjectFile file2);
	
}
