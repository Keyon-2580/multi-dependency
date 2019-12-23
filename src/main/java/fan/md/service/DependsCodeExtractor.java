package fan.md.service;

import depends.entity.repo.EntityRepo;
import fan.md.model.Language;

public interface DependsCodeExtractor {
	
	EntityRepo extractEntityRepo(String src, Language language) throws Exception;
	
}
