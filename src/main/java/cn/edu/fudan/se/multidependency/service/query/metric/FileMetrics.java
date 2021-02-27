package cn.edu.fudan.se.multidependency.service.query.metric;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@QueryResult
public class FileMetrics extends FanIOMetric {

	private ProjectFile file;

	/**
	 * fanIn
	 */
	private int fanIn;


	/**
	 * fanOut
	 */
	private int fanOut;
//
//	/**
//	 * 修改次数
//	 */
//	private int changeTimes;
//
//	/**
//	 * 协同修改次数，与其它文件共同修改的commit次数
//	 */
//	private int cochangeCommitTimes;
//
//	/**
//	 * 与该文件协同修改的文件数量
//	 */
//	private int cochangeFileCount;
//
//	/**
//	 * 方法数
//	 */
//	private int nom;
//
//	/**
//	 * 代码行
//	 */
//	private int loc;

	/**
	 * 结构性度量指标
	 */
	private StructureMetric structureMetric;

	/**
	 * 演化性度量指标
	 */
	private EvolutionMetric evolutionMetric;

	/**
	 * 债务性度量指标
	 */
	private DebtMetric debtMetric;

	/**
	 * 不稳定度
	 * Instability = Ce / (Ce + Ca)
	 */
	private double instability;

	/**
	 * PageRank Score
	 */
	private double pageRankScore;


	@Override
	public Node getComponent() {
		return file;
	}


	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class StructureMetric {

		private ProjectFile file;

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
		 * fanOut
		 */
		private int fanOut;

		/**
		 * fanIn
		 */
		private int fanIn;
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class EvolutionMetric{

		private ProjectFile file;

		/**
		 * 修改次数
		 */
		private int changeTimes;

		/**
		 * 与该文件协同修改的文件数量
		 */
		private int coChangeFileCount;

		/**
		 * 协同修改次数，与其它文件共同修改的commit次数
		 */
//		private int coChangeCommitTimes;
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class DebtMetric{
		private ProjectFile file;
	}

}
