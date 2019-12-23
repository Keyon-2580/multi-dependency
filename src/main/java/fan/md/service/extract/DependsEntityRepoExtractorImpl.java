package fan.md.service.extract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
import depends.relations.Inferer;
import depends.util.FileTraversal;
import depends.util.FileUtil;
import depends.util.TemporaryFile;
import fan.md.model.Language;

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
	
	private Language language;
	private String projectPath;
	
	@Override
	public EntityRepo extractEntityRepo() throws Exception {
		if(Language.java == language) {
			initJavaExtractor();
			return extractJava(this.projectPath);
		} else {
			initCppExtractor(this.projectPath);
			return extractCpp(this.projectPath);
		}
	}

	@Override
	public String getProjectPath() {
		return this.projectPath;
	}

	@Override
	public Language getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(Language language) {
		this.language = language;
	}

	@Override
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
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
	
	private EntityRepo extractJava(String directory) throws Exception {
	    fileTransversal = new FileTraversal(new FileTraversal.IFileVisitor(){
			@Override
			public void visit(File file) {
				String fileFullPath = file.getAbsolutePath();
				fileFullPath = FileUtil.uniqFilePath(fileFullPath);
				if (!fileFullPath.startsWith(directory)) {
					return;
				}
	            FileParser fileParser = new JavaFileParser(fileFullPath, entityRepo, inferer);
	            try {
	                System.out.println("parsing " + fileFullPath 
	                		+ "...");		
	                fileParser.parse();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }	
			}
    		
    	});
    	fileTransversal.extensionFilter(new String[] {".java"});
		fileTransversal.travers(directory);
	    inferer.resolveAllBindings();
		return entityRepo;
	}

	private EntityRepo extractCpp(String directory) throws Exception {
		FileTraversal fileTransversal = new FileTraversal(new FileTraversal.IFileVisitor(){
			@Override
			public void visit(File file) {
				String fileFullPath = file.getAbsolutePath();
				fileFullPath = FileUtil.uniqFilePath(fileFullPath);
				if (!fileFullPath.startsWith(directory)) {
					return;
				}
	            FileParser fileParser = new CdtCppFileParser(fileFullPath,entityRepo,preprocessorHandler,inferer, macroRepo);
	            try {
	                System.out.println("parsing " + fileFullPath 
	                		+ "...");
	                fileParser.parse();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }	
			}
    		
    	});
    	fileTransversal.extensionFilter(new CppProcessor().fileSuffixes());
		fileTransversal.travers(directory);
		inferer.resolveAllBindings();
		return entityRepo;
	}
	
}
