package cn.edu.fudan.se.multidependency;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelations;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.RepositoryService;
import cn.edu.fudan.se.multidependency.service.build.BuildInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractorImpl;
import cn.edu.fudan.se.multidependency.service.code.InserterForNeo4jServiceFactory;
import cn.edu.fudan.se.multidependency.service.dynamic.DynamicInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.utils.FileUtils;
import cn.edu.fudan.se.multidependency.utils.YamlUtils;
import depends.entity.repo.EntityRepo;

public class InsertDataMain {

    public static void main(String[] args) throws Exception {
    	insert(args);
	}
    
    public static void insert(String[] args) {
		try {
			YamlUtils.YamlObject yaml = null;
			if(args == null || args.length == 0) {
				yaml = YamlUtils.getDataBasePathDefault("src/main/resources/application.yml");
			} else {
				yaml = YamlUtils.getDataBasePath(args[0]);
			}
			String projectPath = yaml.getCodeProjectPath();
			Language language = Language.valueOf(yaml.getCodeLanguage());
	    	DependsEntityRepoExtractor extractor = DependsEntityRepoExtractorImpl.getInstance();
			extractor.setLanguage(language);
			extractor.setProjectPath(projectPath);
			EntityRepo entityRepo = extractor.extractEntityRepo();
			
			InserterForNeo4j repository = RepositoryService.getInstance();
			repository.setDatabasePath(yaml.getNeo4jDatabasePath());
			repository.setDelete(true);
			/**
			 * 静态分析
			 */
			System.out.println("静态分析");
			insertStaticCode(yaml, entityRepo);
			/**
			 * 动态分析
			 */
			if(yaml.isAnalyseDynamic()) {
				System.out.println("动态分析");
				insertDynamic(yaml);
			}
			/**
			 * 构建分析
			 */
			if(yaml.isAnalyseBuild()) {
				System.out.println("构建分析");
				if(language == Language.cpp) {
					insertBuildInfo(yaml);
				}
			}
			
			///FIXME
			//其它
			
			
			//最后统一插入数据库
			repository.insertToNeo4jDataBase();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static Nodes insertStaticCode(YamlUtils.YamlObject yaml, EntityRepo entityRepo) throws Exception {
		ExtractorForNodesAndRelations dependsInserter = InserterForNeo4jServiceFactory.getInstance().createCodeInserterService(yaml, entityRepo);
		dependsInserter.addNodesAndRelations();
		return dependsInserter.getNodes();
    }
    
    /**
     * 添加一个测试用例类的动态分析
     * @param markFile
     * @param files
     * @param language
     * @throws Exception
     */
    public static void insertDynamicCall(File markFile, Language language, File... files) throws Exception {
    	DynamicInserterForNeo4jService kiekerInserter = InserterForNeo4jServiceFactory.getInstance().createDynamicInserterService(language);//根据语言确定服务
    	kiekerInserter.setMarkFile(markFile);
    	kiekerInserter.setDynamicFunctionCallFiles(files);
    	kiekerInserter.addNodesAndRelations();
    }
    
    public static void insertDynamic(YamlUtils.YamlObject yaml) throws Exception {
    	String[] dynamicFileSuffixes = null;
    	Language language = Language.valueOf(yaml.getCodeLanguage());
		if(language == Language.java) {
			dynamicFileSuffixes = new String[yaml.getDynamicJavaFileSuffix().size()];
			yaml.getDynamicJavaFileSuffix().toArray(dynamicFileSuffixes); //后缀为.dat
		} else {
			dynamicFileSuffixes = new String[yaml.getDynamicCppFileSuffix().size()];
			yaml.getDynamicCppFileSuffix().toArray(dynamicFileSuffixes);
		}
		
		File dynamicDirectory = new File(yaml.getDirectoryRootPath());
		for(File javaData : dynamicDirectory.listFiles()) {
			//并非批量插入动态分析的数据，而是挨个测试用例插入的
			if(javaData.isFile()) {
				continue;
			}
			List<File> dynamicFiles = new ArrayList<>();
			FileUtils.listFiles(javaData, dynamicFiles, dynamicFileSuffixes);
			File[] files = new File[dynamicFiles.size()];
			dynamicFiles.toArray(files);
			List<File> markFiles = new ArrayList<>();
			FileUtils.listFiles(javaData, markFiles, yaml.getDynamicMarkSuffix());
			File markFile = markFiles.get(0);
			insertDynamicCall(markFile, language, files); //markFile表示后缀名为.mark的，files表示后缀名为.dat的，一个.mark可能对应多个.dat文件
		}
    }
    
    public static void insertBuildInfo(YamlUtils.YamlObject yaml) throws Exception {
    	BuildInserterForNeo4jService buildInserter = InserterForNeo4jServiceFactory.getInstance().createBuildInserterService(Language.valueOf(yaml.getCodeLanguage()) );
    	File buildInfoFile = new File(yaml.getBuildFilePath());
    	buildInserter.setBuildInfoFile(buildInfoFile);
    	buildInserter.addNodesAndRelations();
    }
}
