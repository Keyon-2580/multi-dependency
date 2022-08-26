package cn.edu.fudan.se.multidependency.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
public class PropertyConfig {

	@Getter
	@Value("${config.starter.insert_nodes}")
	private boolean insertNodes;

	@Getter
	@Value("${config.starter.insert_relations}")
	private boolean insertRelations;

	@Getter
	@Value("${config.starter.cochange}")
	private boolean setCoChange;

	@Getter
	@Value("${config.starter.depends_on}")
	private boolean setDependsOn;

	@Getter
	@Value("${config.starter.clone_group}")
	private boolean setCloneGroup;

	@Getter
	@Value("${config.starter.module_clone}")
	private boolean setModuleClone;

	@Getter
	@Value("${config.starter.aggregation_clone}")
	private boolean setAggregationClone;

	@Getter
	@Value("${config.starter.coupling}")
	private boolean setCoupling;

	@Getter
	@Value("${config.starter.hierarchical_cluster}")
	private boolean setHierarchicalCluster;

	@Getter
	@Value("${config.starter.loose_degree}")
	private boolean setLooseDegree;

	@Getter
	@Value("${config.starter.package_depth}")
	private boolean setPackageDepth;

	@Getter
	@Value("${data.serialize_path}")
	private String serializePath;

	@Getter
	@Value("${data.neo4j.data_path}")
	private String databaseDir;

	@Getter
	@Value("${data.neo4j.database_name}")
	private String databaseName;

//	@Getter
//	@Value("${config.starter.co-change.co-change}")
//	private boolean setCoChange;
//
//	@Getter
//	@Value("${config.starter.co-change.module_co-change}")
//	private boolean setModuleCoChange;
//
//	@Getter
//	@Value("${config.starter.co-change.aggregation_co-change}")
//	private boolean setAggregationCoChange;
//
//	@Getter
//	@Value("${config.starter.depends_on.depends_on}")
//	private boolean setDependsOn;
//
//	@Getter
//	@Value("${config.starter.depends_on.module_depends_on}")
//	private boolean setModuleDependsOn;
//
//	@Getter
//	@Value("${config.starter.depends_on.aggregation_depends_on}")
//	private boolean setAggregationDependsOn;
//
//	@Getter
//	@Value("${config.starter.clone.clone_group}")
//	private boolean setCloneGroup;
//
//	@Getter
//	@Value("${config.starter.clone.module_clone}")
//	private boolean setModuleClone;
//
//	@Getter
//	@Value("${config.starter.clone.aggregation_clone}")
//	private boolean setAggregationClone;

	@Getter
	@Value("${config.starter.smell}")
	private boolean detectAS;

	@Getter
	@Value("${config.starter.modularity}")
	private boolean calculateModularity;

	@Getter
	@Value("${config.starter.export_cyclic_dependency}")
	private boolean exportCyclicDependency;
}
