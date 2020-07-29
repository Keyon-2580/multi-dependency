package cn.edu.fudan.se.multidependency.service.query.as.data;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;

@Data
public class UnstableFile {

	private ProjectFile file;
	
	private int fanIn;
	
	private int fanOut;
	
	
	
}
