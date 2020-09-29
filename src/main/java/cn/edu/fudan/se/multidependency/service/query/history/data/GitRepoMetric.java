package cn.edu.fudan.se.multidependency.service.query.history.data;

import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.service.query.metric.ProjectMetrics;
import lombok.Data;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Collection;


@Data
@QueryResult
public class GitRepoMetric {
	
	private GitRepository gitRepository;

	private Collection<ProjectMetrics> projectMetricsList;

	private int numOfCommits = -1;

	private int numOfIssues = -1;
}
