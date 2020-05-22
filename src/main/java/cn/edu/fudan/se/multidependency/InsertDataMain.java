package cn.edu.fudan.se.multidependency;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelations;
import cn.edu.fudan.se.multidependency.service.nospring.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.nospring.InserterForNeo4jServiceFactory;
import cn.edu.fudan.se.multidependency.service.nospring.RepositoryService;
import cn.edu.fudan.se.multidependency.service.nospring.clone.CloneInserter;
import cn.edu.fudan.se.multidependency.service.nospring.code.BasicCodeInserterForNeo4jServiceImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.nospring.code.RestfulAPIFileExtractor;
import cn.edu.fudan.se.multidependency.service.nospring.code.RestfulAPIFileExtractorImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.SwaggerJSON;
import cn.edu.fudan.se.multidependency.service.nospring.code.Depends096Extractor;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.CppDynamicInserter;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.FeatureAndTestCaseFromJSONFileForMicroserviceInserter;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.JavassistDynamicInserter;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.TraceStartExtractor;
import cn.edu.fudan.se.multidependency.service.nospring.git.GitInserter;
import cn.edu.fudan.se.multidependency.service.nospring.lib.LibraryInserter;
import cn.edu.fudan.se.multidependency.service.nospring.structure.MicroServiceArchitectureInserter;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import cn.edu.fudan.se.multidependency.utils.ProjectConfigUtil;
import cn.edu.fudan.se.multidependency.utils.ProjectConfigUtil.JSONConfigFile;
import cn.edu.fudan.se.multidependency.utils.ProjectConfigUtil.ProjectConfig;
import cn.edu.fudan.se.multidependency.utils.ProjectConfigUtil.RestfulAPIConfig;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import depends.entity.repo.EntityRepo;

public class InsertDataMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertDataMain.class);

	private static final Executor executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {
        LOGGER.info("InsertDataMain");
        insert(args);
//		checkMicroserviceTrace(args);
    }

    public static YamlUtil.YamlObject getYaml(String[] args) throws Exception {
        YamlUtil.YamlObject yaml = null;
        if (args == null || args.length == 0) {
            yaml = YamlUtil.getDataBasePathDefault("src/main/resources/application.yml");
        } else {
            yaml = YamlUtil.getDataBasePath(args[0]);
        }
        return yaml;
    }

    public static void checkMicroserviceTrace(String[] args) throws Exception {
        YamlUtil.YamlObject yaml = getYaml(args);
        LOGGER.info("输出trace");
        new TraceStartExtractor(analyseDynamicLogs(yaml)).addNodesAndRelations();
    }


    public static void insert(String[] args) {
        try {
            YamlUtil.YamlObject yaml = getYaml(args);

			CountDownLatch latchOfStatic = new CountDownLatch(1);
			CountDownLatch latchOfOthers = new CountDownLatch(5);
			ThreadService ts = new ThreadService(yaml, latchOfStatic, latchOfOthers);

			executor.execute(ts::staticAnalyse);
			latchOfStatic.await();
			
			executor.execute(ts::msDependAnalyse);
			executor.execute(ts::dynamicAnalyse);
			executor.execute(ts::gitAnalyse);
			executor.execute(ts::cloneAnalyse);
			executor.execute(ts::libAnalyse);

			latchOfOthers.await();
			InserterForNeo4j repository = RepositoryService.getInstance();
			repository.setDatabasePath(yaml.getNeo4jDatabasePath());
			repository.setDelete(yaml.isDeleteDatabase());
			repository.insertToNeo4jDataBase();
		} catch (Exception e) {
            // 所有步骤中有一个出错，都会终止执行
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * 通过feature的json文件引入
     *
     * @param featuresJsonPath
     * @throws Exception
     */
    public static ExtractorForNodesAndRelations insertFeatureAndTestCaseByJSONFile(String featuresJsonPath) throws Exception {
        return new FeatureAndTestCaseFromJSONFileForMicroserviceInserter(featuresJsonPath);
    }

	public static ExtractorForNodesAndRelations insertDynamic(Language language, File[] dynamicLogFiles) throws Exception {
		switch (language) {
			case java:
				return new JavassistDynamicInserter(dynamicLogFiles);
			case cpp:
				return new CppDynamicInserter(dynamicLogFiles);
		}
		throw new LanguageErrorException(language.toString());
	}

	public static File[] analyseDynamicLogs(YamlUtil.YamlObject yaml) {
		File[] result = null;
		String[] dynamicFileSuffixes = new String[yaml.getDynamicFileSuffix().size()];
		yaml.getDynamicFileSuffix().toArray(dynamicFileSuffixes); // 后缀为.log
		LOGGER.info("分析动态运行文件的后缀为：" + yaml.getDynamicFileSuffix());
		File dynamicDirectory = new File(yaml.getDynamicDirectoryRootPath());
		LOGGER.info("分析动态运行文件的目录为：" + yaml.getDynamicDirectoryRootPath());
		List<File> resultList = new ArrayList<>();
		FileUtil.listFiles(dynamicDirectory, resultList, dynamicFileSuffixes);
		result = new File[resultList.size()];
		resultList.toArray(result);
		return result;
	}
	
	/*public static void insertBuildInfo(YamlUtils.YamlObject yaml) throws Exception {
		for (String l : yaml.getAnalyseLanguages()) {
			Language language = Language.valueOf(l);
			if (language != Language.cpp) {
				continue;
			}
			BuildInserterForNeo4jService buildInserter = InserterForNeo4jServiceFactory.getInstance()
					.createBuildInserterService(language);
			File buildInfoFile = new File(yaml.getBuildFilePath());
			buildInserter.setBuildInfoFile(buildInfoFile);
			buildInserter.addNodesAndRelations();
		}
	}*/
}

class ThreadService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertDataMain.class);
    private static YamlUtil.YamlObject yaml;
    private static JSONConfigFile config;
    private static Collection<ProjectConfig> projectsConfig;
    private static final Executor executor = Executors.newCachedThreadPool();
    private static CountDownLatch latchOfStatic, latchOfOthers;

    ThreadService(YamlUtil.YamlObject yaml, CountDownLatch latchOfStatic, CountDownLatch latchOfOthers) throws Exception {
        ThreadService.yaml = yaml;
        config = ProjectConfigUtil.extract(JSONUtil.extractJSONObject(new File(yaml.getProjectsConfig())));
        projectsConfig = config.getProjectConfigs();
        ThreadService.latchOfStatic = latchOfStatic;
        ThreadService.latchOfOthers = latchOfOthers;
    }

    void staticAnalyse() {
		CountDownLatch latchOfProjects = new CountDownLatch((projectsConfig).size());
		for (ProjectConfig projectConfig : projectsConfig) {
			executor.execute(()->{
				try {
					staticAnalyseCore(projectConfig);
                    latchOfProjects.countDown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		try {
            latchOfProjects.await();
			latchOfStatic.countDown();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    void staticAnalyseCore(ProjectConfig projectConfig) throws Exception{
        DependsEntityRepoExtractor extractor = new Depends096Extractor();
        extractor.setIncludeDirs(projectConfig.includeDirsArray());
        extractor.setExcludes(projectConfig.getExcludes());
        extractor.setLanguage(projectConfig.getLanguage());
        extractor.setProjectPath(projectConfig.getPath());
        extractor.setAutoInclude(projectConfig.isAutoInclude());
        EntityRepo entityRepo = extractor.extractEntityRepo();
        if (extractor.getEntityCount() > 0) {
            BasicCodeInserterForNeo4jServiceImpl inserter = InserterForNeo4jServiceFactory.getInstance()
                    .createCodeInserterService(entityRepo, projectConfig);
            RestfulAPIConfig apiConfig = projectConfig.getApiConfig();
            if (apiConfig != null && RestfulAPIConfig.FRAMEWORK_SWAGGER.equals(projectConfig.getApiConfig().getFramework())) {
                SwaggerJSON swagger = new SwaggerJSON();
                swagger.setPath(apiConfig.getPath());
                swagger.setExcludeTags(apiConfig.getExcludeTags());
                RestfulAPIFileExtractor restfulAPIFileExtractorImpl = new RestfulAPIFileExtractorImpl(swagger);
                inserter.setRestfulAPIFileExtractor(restfulAPIFileExtractorImpl);
            }
            inserter.addNodesAndRelations();
        }
    }

    void msDependAnalyse() {
        try {
            if (config.getMicroServiceDependencies() != null) {
                LOGGER.info("微服务依赖存储");
                new MicroServiceArchitectureInserter(config.getMicroServiceDependencies()).addNodesAndRelations();
            }
            latchOfOthers.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void dynamicAnalyse() {
        try {
            if (yaml.isAnalyseDynamic()) {
                LOGGER.info("动态运行分析");
                File[] dynamicLogs = InsertDataMain.analyseDynamicLogs(yaml);

                LOGGER.info("输出trace，只输出日志中记录的trace，不做数据库操作");
                new TraceStartExtractor(dynamicLogs).addNodesAndRelations();

                for (Language language : Language.values()) {
					InsertDataMain.insertDynamic(language, dynamicLogs).addNodesAndRelations();
                }

                LOGGER.info("引入特性与测试用例，对应到trace");
                new FeatureAndTestCaseFromJSONFileForMicroserviceInserter(yaml.getFeaturesPath()).addNodesAndRelations();
            }
            latchOfOthers.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void gitAnalyse() {
        try {
            if (yaml.isAnalyseGit()) {
                LOGGER.info("Git库分析");
                new GitInserter(yaml.getGitDirectoryRootPath(), yaml.getIssuesPath(),
                        yaml.isGitSelectRange(), yaml.getCommitIdFrom(), yaml.getCommitIdTo()).addNodesAndRelations();
            }
            latchOfOthers.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void cloneAnalyse() {
        try {
            if (yaml.isAnalyseClone()) {
                LOGGER.info("克隆依赖分析");
                new CloneInserter(yaml.getCloneLanguage(), yaml.getMethodNameTablePath(), yaml.getMethodResultPath()).addNodesAndRelations();
            }
            latchOfOthers.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void libAnalyse() {
        try {
            if (yaml.isAnalyseLib()) {
                LOGGER.info("三方依赖分析");
                new LibraryInserter(yaml.getLibsPath()).addNodesAndRelations();
            }
            latchOfOthers.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
