package fan.md.service;

import depends.entity.repo.EntityRepo;

public interface DependsCodeExtractor {
	
	EntityRepo extractEntityRepo(String src, String language) throws Exception;
	
}
