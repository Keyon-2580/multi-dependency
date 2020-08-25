package cn.edu.fudan.se.multidependency.service.query.history.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class IssueFile {
	
	public IssueFile(@NonNull ProjectFile file) {
		this.file = file;
	}

	@Setter
	@Getter
	ProjectFile file;
	
	@Getter
	List<Issue> issues = new ArrayList<>();
	
	public void addAll(Collection<Issue> issues) {
		this.issues.addAll(issues);
	}
	
	@Override
	public boolean equals(Object obj) {
		return file.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return file.hashCode();
	}
	
}
