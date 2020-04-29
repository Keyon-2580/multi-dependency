package cn.edu.fudan.se.multidependency.service.nospring.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.Entity;
import depends.entity.repo.EntityRepo;
import depends.extractor.AbstractLangProcessor;
import depends.util.FileUtil;
import depends.util.FolderCollector;
import edu.emory.mathcs.backport.java.util.Arrays;
import lombok.Setter;

public class Depends096Extractor implements DependsEntityRepoExtractor {
	private Depends096Extractor() {}
	private static Depends096Extractor instance = new Depends096Extractor();
	public static Depends096Extractor getInstance() {
		return instance;
	}
	
	private EntityRepo executeCommand(Language language, String inputDir, 
			String[] includeDir, boolean autoInclude) throws Exception {
		inputDir = FileUtil.uniqFilePath(inputDir);
		boolean supportImplLink = false;
		AbstractLangProcessor langProcessor = null;
		switch(language) {
		case cpp:
			supportImplLink = true;
			langProcessor = new depends.extractor.cpp.CppProcessor();
			break;
		case java:
			langProcessor = new depends.extractor.java.JavaProcessor();
			break;
		default:
			throw new LanguageErrorException(language.toString());
		}
		
		if (autoInclude) {
			FolderCollector includePathCollector = new FolderCollector();
			List<String> additionalIncludePaths = includePathCollector.getFolders(inputDir);
			additionalIncludePaths.addAll(Arrays.asList(includeDir));
			includeDir = additionalIncludePaths.toArray(new String[] {});
		}
		langProcessor.buildDependencies(inputDir, includeDir, new ArrayList<>(), supportImplLink,false);
		EntityRepo repo = langProcessor.getEntityRepo();
		return repo;
	}
	

	@Setter
	private Language language;
	@Setter
	private String projectPath;
	@Setter
	private Collection<String> excludes;
	@Setter
	private String[] includeDirs;
	@Setter
	private boolean autoInclude;

	private EntityRepo entityRepo ;

	@Override
	public int getEntityCount() {
		if(entityRepo == null) {
			return 0;
		}
		List<Entity> entities = new ArrayList<>();
		Iterator<Entity> iterator = entityRepo.entityIterator();
		iterator.forEachRemaining(entity -> {
			entities.add(entity);
		});
		return entities.size();
	}

	@Override
	public EntityRepo extractEntityRepo() throws Exception {
		return entityRepo = executeCommand(language, projectPath, new String[] {}, autoInclude);
	}

}
