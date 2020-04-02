package cn.edu.fudan.se.multidependency.service.nospring.code;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.repo.EntityRepo;

/**
 * 调用depends的API提取代码entity
 * @author fan
 *
 */
public interface DependsEntityRepoExtractor {
	
	EntityRepo extractEntityRepo() throws Exception;
	
	void setLanguage(Language language);
	
	void setProjectPath(String projectPath);
	
	void setExcludes(Collection<String> excludes);

	int getEntityCount();
	
	void setAutoInclude(boolean autoInclude);
}
