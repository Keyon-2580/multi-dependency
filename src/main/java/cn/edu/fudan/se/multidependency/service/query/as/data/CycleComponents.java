package cn.edu.fudan.se.multidependency.service.query.as.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import lombok.Data;

@Data
@QueryResult
public class CycleComponents<T extends Node> {
	
	private int partition;
	
	private Collection<T> components;
	
	private List<Commit> commits = new ArrayList<>();
	
	public void addCommit(Commit commit) {
		this.commits.add(commit);
	}
	
	public void addAllCommits(Collection<Commit> commits) {
		this.commits.addAll(commits);
	}
	
}
