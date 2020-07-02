package cn.edu.fudan.se.multidependency.service.spring.history;

import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;

public interface GitAnalyseService {

	Iterable<Commit> findAllCommits();
	
	Map<Developer, Map<Project, Integer>> calCntOfDevUpdPro();
	
	Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsList();
	
	Map<ProjectFile, Integer> calCntOfFileBeUpd();
	
	Map<ProjectFile, Integer> getTopKFileBeUpd(int k);
	
	Map<ProjectFile, Map<ProjectFile, Integer>> calCntOfFileCoChange();
	
	Map<ProjectFile, Map<ProjectFile, Integer>> getTopKFileCoChange(int k);
	
}
