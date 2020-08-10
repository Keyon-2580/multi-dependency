package cn.edu.fudan.se.multidependency.service.query.metric;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@QueryResult
public class PackageMetrics extends FanIOMetric implements Serializable {

	private static final long serialVersionUID = 1749274223605679116L;

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

	public double getInstability() {
		return getFanOut() / (getFanOut() + getFanIn() + 0.0);
	}
	
}
