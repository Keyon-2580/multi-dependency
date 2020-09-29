package cn.edu.fudan.se.multidependency.service.query.as.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import lombok.Data;

@Data
public class UnstableFile {

	private ProjectFile file;
	
	private int fanIn;
	
	private Set<ProjectFile> cochangeFiles = new HashSet<>();
	
	private Map<Long, CoChange> cochangeTimesWithFile = new HashMap<>();
	
	public void addAllCoChanges(Collection<CoChange> cochanges) {
		for(CoChange cochange : cochanges) {
			addCoChange(cochange);
		}
	}
	
	public void addCoChange(CoChange cochange) {
		ProjectFile cochangeFile = (ProjectFile)cochange.getNode1();
		if(cochangeFile.equals(file)) {
			cochangeFile = (ProjectFile)cochange.getNode2();
		}
		cochangeFiles.add(cochangeFile);
		cochangeTimesWithFile.put(cochangeFile.getId(), cochange);
	}
	
}
