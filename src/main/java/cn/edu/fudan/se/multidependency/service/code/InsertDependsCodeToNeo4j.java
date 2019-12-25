package cn.edu.fudan.se.multidependency.service.code;

import depends.entity.repo.EntityRepo;

public interface InsertDependsCodeToNeo4j extends InsertCodeToNeo4j {
	
	public EntityRepo getEntityRepo();
	
}
