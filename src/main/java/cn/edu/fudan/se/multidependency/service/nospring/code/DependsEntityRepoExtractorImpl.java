package cn.edu.fudan.se.multidependency.service.nospring.code;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.Entity;
import depends.entity.repo.EntityRepo;
import depends.entity.repo.InMemoryEntityRepo;
import depends.extractor.FileParser;
import depends.extractor.cpp.CppBuiltInType;
import depends.extractor.cpp.CppImportLookupStrategy;
import depends.extractor.cpp.CppProcessor;
import depends.extractor.cpp.MacroFileRepo;
import depends.extractor.cpp.MacroRepo;
import depends.extractor.cpp.cdt.CdtCppFileParser;
import depends.extractor.cpp.cdt.PreprocessorHandler;
import depends.extractor.java.JavaBuiltInType;
import depends.extractor.java.JavaFileParser;
import depends.extractor.java.JavaImportLookupStrategy;
import depends.extractor.java.JavaProcessor;
import depends.relations.Inferer;
import depends.util.FileTraversal;
import depends.util.FileUtil;
import depends.util.TemporaryFile;
import lombok.Setter;

/**
 * 调用depends的API提取代码entity
 * @author fan
 *
 */
public class DependsEntityRepoExtractorImpl implements DependsEntityRepoExtractor {
	private DependsEntityRepoExtractorImpl() {}
	private static DependsEntityRepoExtractorImpl instance = new DependsEntityRepoExtractorImpl();
	public static DependsEntityRepoExtractorImpl getInstance() {
		return instance;
	}
	
	private EntityRepo entityRepo ;
	private Inferer inferer;
    private PreprocessorHandler preprocessorHandler;
    private FileTraversal fileTransversal;
	private MacroRepo macroRepo;
	@Setter
	private Language language;
	@Setter
	private String projectPath;
	@Setter
	private String[] excludes;
	
	private String slash;

	private boolean isExclude(String filePath) {
		for(String exclude : excludes) {
			if(isExcludeDirectory(exclude)) {
				exclude = exclude.replace("**", "");
				if(filePath.startsWith(exclude)) {
					return true;
				}
			} else {
				if(exclude.equals(filePath)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isExcludeDirectory(String exclude) {
		return exclude.endsWith(slash + "**");
	}
	
	@Override
	public EntityRepo extractEntityRepo() throws Exception {
		slash = projectPath.contains("\\") ? "\\" : "/";
		for(int i = 0; i < excludes.length; i++) {
			excludes[i] = projectPath.concat(excludes[i].replace("/", slash));
		}
		if(Language.java == language) {
			initJavaExtractor();
		} else {
			initCppExtractor(this.projectPath);
		}
		return extract();
	}

	private void initJavaExtractor() {
		entityRepo = new InMemoryEntityRepo();
		inferer = new Inferer(entityRepo,new JavaImportLookupStrategy(),new JavaBuiltInType(),false);
    	TemporaryFile.reset();
	}
	
	private void initCppExtractor(String src) {
		entityRepo = new InMemoryEntityRepo();
    	inferer = new Inferer(entityRepo,new CppImportLookupStrategy(),new CppBuiltInType(),false);
    	preprocessorHandler = new PreprocessorHandler(src, new ArrayList<>());
    	TemporaryFile.reset();
    	macroRepo = new MacroFileRepo(entityRepo);
	}
	
	private EntityRepo extract() throws Exception {
		fileTransversal = new FileTraversal(new MyFileVisitor());
    	fileTransversal.extensionFilter(fileSuffixes.get(language));
		fileTransversal.travers(this.projectPath);
		inferer.resolveAllBindings();
		return entityRepo;
	}

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

	private Map<Language, String[]> fileSuffixes = new HashMap<>();
	{
		fileSuffixes.put(Language.java, new JavaProcessor().fileSuffixes());
		fileSuffixes.put(Language.cpp, new CppProcessor().fileSuffixes());
	}
	
	private class MyFileVisitor implements FileTraversal.IFileVisitor {
		@Override
		public void visit(File file) {
			String fileFullPath = file.getAbsolutePath();
			fileFullPath = FileUtil.uniqFilePath(fileFullPath);
			if (!fileFullPath.startsWith(projectPath) || isExclude(fileFullPath)) {
				return;
			}
            try {
            	FileParser fileParser = null;
            	switch(language) {
            	case java:
    	            fileParser = new JavaFileParser(fileFullPath, entityRepo, inferer);
    	            break;
            	case cpp:
            		fileParser = new CdtCppFileParser(fileFullPath,entityRepo,preprocessorHandler,inferer, macroRepo);
            		break;
            	default:
            		throw new LanguageErrorException(language.toString());
            	}
                System.out.println("parsing " + fileFullPath 
                		+ "...");
                fileParser.parse();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LanguageErrorException e) {
				e.printStackTrace();
			}	
		}
		
	}

}
