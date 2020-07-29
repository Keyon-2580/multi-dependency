package cn.edu.fudan.se.multidependency.service.query.as.data;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogicCoupling {
	
	private ProjectFile file1;
	
	private ProjectFile file2;
	
	private int cochangeTimes;
}
