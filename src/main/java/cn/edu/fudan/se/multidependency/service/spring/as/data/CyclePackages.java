package cn.edu.fudan.se.multidependency.service.spring.as.data;

import java.util.Collection;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Data;

@Data
@QueryResult
public class CyclePackages {
	
	int partition;
	
	Collection<Package> packages;
	
}
