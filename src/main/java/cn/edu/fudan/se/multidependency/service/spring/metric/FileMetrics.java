package cn.edu.fudan.se.multidependency.service.spring.metric;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;

@Data
@QueryResult
public class FileMetrics implements FanIOMetric {

	private ProjectFile file;
	
	/**
	 * fanIn
	 */
	private int fanIn;
	
	/**
	 * fanOut
	 */
	private int fanOut;
	
	/**
	 * 修改次数
	 */
	private int changeTimes;
	
	/**
	 * 方法数
	 */
	private int nom;
	
	/**
	 * 代码行
	 */
	private int loc;

	@Override
	public Node getComponent() {
		return file;
	}
	
}
