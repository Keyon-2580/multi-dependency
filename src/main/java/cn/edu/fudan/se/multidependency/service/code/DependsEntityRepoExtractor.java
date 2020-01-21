package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.repo.EntityRepo;

/**
 * 调用depends的API提取代码entity
 * @author fan
 *
 */
public interface DependsEntityRepoExtractor {
	
	EntityRepo extractEntityRepo() throws Exception;
	
	String getProjectPath();
	
	Language getLanguage();
	
	void setLanguage(Language language);
	
	void setProjectPath(String projectPath);

	int getEntityCount();
}
