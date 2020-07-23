package cn.edu.fudan.se.multidependency.service.spring.metric;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Project;
import lombok.Data;

@Data
@QueryResult
public class ProjectMetrics {
	
	private Project project;
	
	/**
	 * 包数
	 */
	private int nop;
	
	/**
	 * 文件数
	 */
	private int nof;
	
	/**
	 * 方法数
	 */
	private int nom;
	
	/**
	 * 代码行
	 */
	private int loc;
	
	
}
