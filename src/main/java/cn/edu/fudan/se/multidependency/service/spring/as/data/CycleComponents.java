package cn.edu.fudan.se.multidependency.service.spring.as.data;

import java.util.Collection;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;

@Data
@QueryResult
public class CycleComponents<T extends Node> {
	
	int partition;
	
	Collection<T> components;
	
}
