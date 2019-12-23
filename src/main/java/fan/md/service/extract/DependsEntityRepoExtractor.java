package fan.md.service.extract;

import depends.entity.repo.EntityRepo;
import fan.md.model.Language;

public interface DependsEntityRepoExtractor {
	
	EntityRepo extractEntityRepo() throws Exception;
	
	String getProjectPath();
	
	Language getLanguage();
	
	void setLanguage(Language language);
	
	void setProjectPath(String projectPath);
}
