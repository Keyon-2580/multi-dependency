package cn.edu.fudan.se.multidependency.service.extract;

import depends.entity.repo.EntityRepo;

public interface InsertDependsCodeToNeo4j extends InsertCodeToNeo4j {
	
	public EntityRepo getEntityRepo();
	
}
