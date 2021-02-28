package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;

public class UnstableFileInHistory extends UnstableComponent<ProjectFile> {

	private int fanIn;
	
	private Set<ProjectFile> cochangeFiles = new HashSet<>();
	
	private Map<Long, CoChange> cochangeTimesWithFile = new HashMap<>();
	
	public void addAllCoChanges(Collection<CoChange> cochanges) {
		for(CoChange cochange : cochanges) {
			addCoChange(cochange);
		}
	}
	
	public void addCoChange(CoChange cochange) {
		ProjectFile cochangeFile = (ProjectFile) cochange.getNode1();
		if(cochangeFile.equals(super.getComponent())) {
			cochangeFile = (ProjectFile) cochange.getNode2();
		}
		cochangeFiles.add(cochangeFile);
		cochangeTimesWithFile.put(cochangeFile.getId(), cochange);
	}

	public int getFanIn() {
		return fanIn;
	}

	public void setFanIn(int fanIn) {
		this.fanIn = fanIn;
	}

	public Set<ProjectFile> getCochangeFiles() {
		return cochangeFiles;
	}

	public Map<Long, CoChange> getCochangeTimesWithFile() {
		return cochangeTimesWithFile;
	}

}
