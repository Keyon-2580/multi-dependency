package cn.edu.fudan.se.multidependency.service.spring.metric;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Data;

@Data
@QueryResult
public class PackageMetrics implements FanIOMetric {

	private Package pck;
	
	/**
	 * 包内文件数
	 */
	private int nof;
	
	/**
	 * 包内方法数
	 */
	private int nom;
	
	/**
	 * 该包依赖其它包的数量
	 */
	private int fanOut;
	
	/**
	 * 该包被其它包依赖的数量
	 */
	private int fanIn;
	
	/**
	 * 代码行
	 */
	private int loc;

	@Override
	public Node getComponent() {
		return pck;
	}
	
}
