package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.annotation.QueryResult;

@Data
@EqualsAndHashCode(callSuper=false)
public class SmellMetric {

	private Smell smell;

	/**
	 * 结构性度量指标
	 */
	private StructureMetric structureMetric;

	/**
	 * 演化性度量指标
	 */
	private EvolutionMetric evolutionMetric;

	/**
	 * 演化性度量指标
	 */
	private CoChangeMetric coChangeMetric;

	/**
	 * 债务性度量指标
	 */
	private DebtMetric debtMetric;

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class StructureMetric {
		private Smell smell;

		private int size;
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
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class EvolutionMetric{

		private Smell smell;
		/**
		 * 修改次数
		 */
		private int commits;

		/**
		 * 开发者数
		 */
		private int developers;

//		/**
//		 * 协同修改的文件数量
//		 */
//		private int coChangeFileCount;
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class CoChangeMetric{

		private Smell smell;
		/**
		 * 修改次数
		 */
		private int coChangeCommits;

		/**
		 * 协同修改的文件数量
		 */
		private int coChangeFiles;
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class DebtMetric{

		private Smell smell;

		/**
		 * Issue总数数量
		 */
		private int issues;
		/**
		 * Bug Issue总数数量
		 */
		private int bugIssues;
		/**
		 * New feature Issue总数数量
		 */
		private int newFeatureIssues;
		/**
		 * Improvement feature Issue总数数量
		 */
		private int improvementIssues;
	}
}
