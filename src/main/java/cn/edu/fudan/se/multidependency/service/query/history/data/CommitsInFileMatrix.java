package cn.edu.fudan.se.multidependency.service.query.history.data;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommitsInFileMatrix {
	
	ProjectFile file;
	
	List<ProjectFile> files;
	
	List<Commit> commits;
	
	boolean[][] update;
	
	Map<Long, Integer> commitTimes;

	public void sortCommitByTime(){
		this.commits.sort(new Comparator<Commit>() {
			@Override
			public int compare(Commit o1, Commit o2) {
				return o1.getAuthoredDate().compareTo(o2.getAuthoredDate());
			}
		});

	}
}
