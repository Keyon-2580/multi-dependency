package cn.edu.fudan.se.multidependency.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class YamlConfig {

	@Value(value = "${data.code.rootPath}")
	private String rootPath;
	
	@Value(value = "${data.delete}")
	private boolean neo4jDelete;
	
	@Value(value = "${data.code.depth}")
	private int depth;

	@Value(value = "${data.neo4j.path}")
	private String neo4jPath;
	
}
