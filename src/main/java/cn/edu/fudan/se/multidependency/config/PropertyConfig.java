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
	
}
