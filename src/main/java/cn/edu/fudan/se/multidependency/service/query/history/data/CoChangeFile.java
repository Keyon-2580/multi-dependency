package cn.edu.fudan.se.multidependency.service.query.history.data;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import lombok.Data;

@Data
public class CoChangeFile {
	
	public CoChangeFile(ProjectFile mainFile, CoChange cochange) {
		this.mainFile = mainFile;
		this.cochange = cochange;
		this.times = cochange.getTimes();
		if(cochange.getFile1().equals(mainFile)) {
			otherFile = cochange.getFile2();
		} else {
			otherFile = cochange.getFile1();
		}
	}
	
	ProjectFile mainFile;

	ProjectFile otherFile;
	
	int times;
	
	CoChange cochange;
	
}
