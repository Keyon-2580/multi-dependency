package cn.edu.fudan.se.multidependency.service.query.metric;

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
	 * 协同修改次数，与其它文件共同修改的commit次数
	 */
	private int cochangeTimes;
	
	/**
	 * 方法数
	 */
	private int nom;
	
	/**
	 * 代码行
	 */
	private int loc;
	
	/**
	 * PageRank Score
	 */
	private double score;

	@Override
	public Node getComponent() {
		return file;
	}
	
}
