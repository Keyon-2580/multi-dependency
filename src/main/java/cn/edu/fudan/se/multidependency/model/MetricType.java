package cn.edu.fudan.se.multidependency.model;

public enum MetricType {
	NOP(MetricType.str_NOP),
	NOF(MetricType.str_NOF),
	NOC(MetricType.str_NOC),
	NOM(MetricType.str_NOM),
	LOC(MetricType.str_LOC),
	LINES(MetricType.str_LINES),
	FAN_IN(MetricType.str_FAN_IN),
	FAN_OUT(MetricType.str_FAN_OUT),
	INSTABILITY(MetricType.str_INSTABILITY),
	PAGERANK_SCORE(MetricType.str_PAGERANK_SCORE),
	MODULARITY(MetricType.str_MODULARITY),

	COMMITS(MetricType.str_COMMITS),
	CHANGE_TIMES(MetricType.str_CHANGE_TIMES),
	CO_CHANGE_FILE_COUNT(MetricType.str_CO_CHANGE_FILE_COUNT),
	CO_CHANGE_COMMIT_TIMES(MetricType.str_CO_CHANGE_COMMIT_TIMES),

	DEFAULT(MetricType.str_DEFAULT);

	/**
	 * -------------------------------结构性度量值-----------------------------------
	 */

	/**
	 * 包数, Number of Packages
	 */
	public static final String str_NOP = "NOP";
	/**
	 * 文件数, Number of Files
	 */
	public static final String str_NOF = "NOF";
	/**
	 * 类的个数, Number of Classes
	 */
	public static final String str_NOC = "NOC";
	/**
	 * 函数个数, Number of Methods
	 */
	public static final String str_NOM = "NOM";
	/**
	 * 代码行，去除空行和注释
	 */
	public static final String str_LOC = "LOC";
	/**
	 * 代码总规模， 包括空行和注释
	 */
	public static final String str_LINES = "Lines";
	/**
	 * 扇出依赖
	 * 也叫Efferent  Couplings，依赖出度数
	 */
	public static final String str_FAN_OUT = "FanOut";
	/**
	 * 扇入依赖
	 * 也叫Afferent Couplings，依赖入度数
	 */
	public static final String str_FAN_IN = "FanIn";
	/**
	 * 不稳定度
	 * Instability = Ce / (Ce + Ca)
	 * 即Instability = FanOut / (FanOut + FanIn)
	 */
	public static final String str_INSTABILITY = "Instability";
	/**
	 * 根据依赖情况，衡量节点的重要程度
	 * PageRank
	 */
	public static final String str_PAGERANK_SCORE = "PageRankScore";
	/**
	 * 衡量项目的模块性
	 * Modularity
	 */
	public static final String str_MODULARITY = "Modularity";

	/**
	 * --------------------------------演化性度量值------------------------------------------
	 */

	/**
	 * 提交数， Commits
	 */
	public static final String str_COMMITS = "Commits";
	/**
	 * 变更数
	 */
	public static final String str_CHANGE_TIMES = "ChangeTimes";
	/**
	 * 共变文件数coChangeFileCount
	 */
	public static final String str_CO_CHANGE_FILE_COUNT = "coChangeFileCount";
	/**
	 * 共变文件数coChangeFileCount
	 */
	public static final String str_CO_CHANGE_COMMIT_TIMES = "coChangeCommitTimes";




	/**
	 * --------------------------------债务性度量值------------------------------------------
	 */


	public static final String str_DEFAULT = "default";

	private String name;

	MetricType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}
}
