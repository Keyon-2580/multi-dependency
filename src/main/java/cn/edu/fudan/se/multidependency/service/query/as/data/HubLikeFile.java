package cn.edu.fudan.se.multidependency.service.query.as.data;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;

@Data
@QueryResult
public class HubLikeFile {

	private ProjectFile file;
	
	private int fanIn;
	
	private int fanOut;
	
	public HubLikeFile(ProjectFile file, int fanIn, int fanOut) {
		this.file = file;
		this.fanIn = fanIn;
		this.fanOut = fanOut;
	}
	
	public int getLoc() {
		return file.getEndLine();
	}
}
