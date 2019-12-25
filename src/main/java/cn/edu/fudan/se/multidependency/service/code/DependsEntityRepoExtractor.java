package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.repo.EntityRepo;

public interface DependsEntityRepoExtractor {
	
	EntityRepo extractEntityRepo() throws Exception;
	
	String getProjectPath();
	
	Language getLanguage();
	
	void setLanguage(Language language);
	
	void setProjectPath(String projectPath);
}
