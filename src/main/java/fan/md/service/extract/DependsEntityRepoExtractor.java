package fan.md.service.extract;

import depends.entity.repo.EntityRepo;
import fan.md.model.Language;

public interface DependsEntityRepoExtractor {
	
	EntityRepo extractEntityRepo(String src, Language language) throws Exception;
	
}
