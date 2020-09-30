package cn.edu.fudan.se.multidependency.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
public class PropertyConfig {
	
	@Getter
	@Value("${config.starter.cochange}")
	private boolean calculateCoChange;
	
	@Getter
	@Value("${config.starter.depends_on}")
	private boolean calculateDependsOn;

	@Getter
	@Value("${config.starter.clone_group}")
	private boolean calculateCloneGroup;

	@Getter
	@Value("${config.starter.module_clone}")
	private boolean setModuleClone;

	@Getter
	@Value("${config.starter.aggregation_clone}")
	private boolean setAggregationClone;

}
