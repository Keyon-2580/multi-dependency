package cn.edu.fudan.se.multidependency.service.query.as.data;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HubLikeFile {

	private ProjectFile file;
	
	private int fanOut;
	
	private int fanIn;
	
	private int loc;
}
