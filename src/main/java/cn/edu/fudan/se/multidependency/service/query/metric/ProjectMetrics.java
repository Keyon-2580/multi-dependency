package cn.edu.fudan.se.multidependency.service.query.metric;

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
	 * 类数
	 */
	private int noc;
	
	/**
	 * 方法数
	 */
	private int nom;
	
	/**
	 * 代码行
	 */
	private int loc;
	
	/**
	 * 文件总行数
	 */
	private int lines;
	
	/**
	 * 与该项目相关的commit次数
	 */
	private int commits = -1;
	
	/**
	 * 模块度
	 */
	private double modularity = -1;

	/**
	 * 该项目所有文件入度中位数
	 */
	private int fanIn = -1;

	/**
	 * 该项目所有文件出度中位数
	 */
	private int fanOut = -1;
}
