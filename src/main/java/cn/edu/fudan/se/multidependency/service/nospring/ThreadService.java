package cn.edu.fudan.se.multidependency.service.nospring;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.nospring.clone.CloneInserterForFile;
import cn.edu.fudan.se.multidependency.service.nospring.clone.CloneInserterForMethod;
import cn.edu.fudan.se.multidependency.service.nospring.code.Depends096Extractor;
import cn.edu.fudan.se.multidependency.service.nospring.code.DependsCodeInserterForNeo4jServiceImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.nospring.code.RestfulAPIFileExtractor;
import cn.edu.fudan.se.multidependency.service.nospring.code.RestfulAPIFileExtractorImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.SwaggerJSON;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.FeatureAndTestCaseFromJSONFileForMicroserviceInserter;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.TraceStartExtractor;
import cn.edu.fudan.se.multidependency.service.nospring.git.GitInserter;
import cn.edu.fudan.se.multidependency.service.nospring.lib.LibraryInserter;
import cn.edu.fudan.se.multidependency.service.nospring.structure.MicroServiceArchitectureInserter;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import cn.edu.fudan.se.multidependency.utils.config.CloneConfig;
import cn.edu.fudan.se.multidependency.utils.config.DynamicConfig;
import cn.edu.fudan.se.multidependency.utils.config.GitConfig;
import cn.edu.fudan.se.multidependency.utils.config.JSONConfigFile;
import cn.edu.fudan.se.multidependency.utils.config.LibConfig;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfigUtil;
import cn.edu.fudan.se.multidependency.utils.config.RestfulAPIConfig;

public class ThreadService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadService.class);
	private static YamlUtil.YamlObject yaml;
	private static JSONConfigFile config;
	private static final Executor executor = Executors.newCachedThreadPool();
	private static CountDownLatch latchOfStatic, latchOfOthers;

	public ThreadService(YamlUtil.YamlObject yaml, CountDownLatch latchOfStatic, CountDownLatch latchOfOthers) throws Exception {
		ThreadService.yaml = yaml;
		config = ProjectConfigUtil.extract(JSONUtil.extractJSONObject(new File(yaml.getProjectsConfig())));
		ThreadService.latchOfStatic = latchOfStatic;
		ThreadService.latchOfOthers = latchOfOthers;
	}

	public void staticAnalyse() {
		Collection<ProjectConfig> projectsConfig = config.getProjectsConfig();
		CountDownLatch latchOfProjects = new CountDownLatch(projectsConfig.size());
		LOGGER.info("项目结构存储线程开始，数量：" + latchOfProjects.getCount());
		for (ProjectConfig projectConfig : projectsConfig) {
			executor.execute(() -> {
				try {
					staticAnalyseCore(projectConfig);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					LOGGER.info("项目结构存储线程结束，线程-1：" + (latchOfProjects.getCount() - 1));
					latchOfProjects.countDown();
				}
			});
		}
		try {
			latchOfProjects.await();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			latchOfStatic.countDown();
		}
	}

	public void staticAnalyseCore(ProjectConfig projectConfig) throws Exception {
		LOGGER.info(projectConfig.getProject() + " " + projectConfig.getLanguage());
		DependsEntityRepoExtractor extractor = new Depends096Extractor();
		extractor.setIncludeDirs(projectConfig.includeDirsArray());
		extractor.setExcludes(projectConfig.getExcludes());
		extractor.setLanguage(projectConfig.getLanguage());
		extractor.setProjectPath(projectConfig.getPath());
		extractor.setAutoInclude(projectConfig.isAutoInclude());
		DependsCodeInserterForNeo4jServiceImpl inserter = InserterForNeo4jServiceFactory.getInstance()
				.createCodeInserterService(extractor.extractEntityRepo(), projectConfig);
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

	public void msDependAnalyse() {
		try {
			if (config.getMicroServiceDependencies() != null) {
				LOGGER.info("微服务依赖存储");
				new MicroServiceArchitectureInserter(config.getMicroServiceDependencies()).addNodesAndRelations();
				LOGGER.info("微服务依赖存储添加结束");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("微服务结构依赖提取出错：" + e.getMessage());
		} finally {
			LOGGER.info("微服务依赖存储线程结束，线程-1：" + (latchOfOthers.getCount() - 1));
			latchOfOthers.countDown();
		}
	}

	public void dynamicAnalyse() {
		try {
			if (yaml.isAnalyseDynamic()) {
				LOGGER.info("动态运行分析");
				DynamicConfig dynamicConfig = config.getDynamicsConfig();
				File[] dynamicLogs = InserterForNeo4jServiceFactory.analyseDynamicLogs(dynamicConfig);

				LOGGER.info("输出trace，只输出日志中记录的trace，不做数据库操作");
				new TraceStartExtractor(dynamicLogs).addNodesAndRelations();

				for (Language language : Language.values()) {
					InserterForNeo4jServiceFactory.insertDynamic(language, dynamicLogs).addNodesAndRelations();
				}

				LOGGER.info("引入特性与测试用例，对应到trace");
				new FeatureAndTestCaseFromJSONFileForMicroserviceInserter(dynamicConfig.getFeaturesPath()).addNodesAndRelations();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			LOGGER.info("动态分析线程结束，线程-1：" + (latchOfOthers.getCount() - 1));
			latchOfOthers.countDown();
		}
	}

	public void gitAnalyse() {
		try {
			if (yaml.isAnalyseGit()) {
				LOGGER.info("Git库分析");
				Collection<GitConfig> gitsConfig = config.getGitsConfig();
				CountDownLatch latchOfGits = new CountDownLatch((gitsConfig).size());
				for (GitConfig gitConfig : gitsConfig) {
					executor.execute(() -> {
						try {
							LOGGER.info(gitConfig.getPath());
							new GitInserter(gitConfig).addNodesAndRelations();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							latchOfGits.countDown();
						}
					});
				}
				latchOfGits.await();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			LOGGER.info("git分析线程结束，线程-1：" + (latchOfOthers.getCount() - 1));
			latchOfOthers.countDown();
		}
	}

	public void cloneAnalyse() {
		try {
			if (yaml.isAnalyseClone()) {
				LOGGER.info("克隆依赖分析");
				for (CloneConfig cloneConfig : config.getClonesConfig()) {
					switch (cloneConfig.getGranularity()) {
						case function:
							new CloneInserterForMethod(cloneConfig.getNamePath(), cloneConfig.getResultPath(), cloneConfig.getGroupPath(), cloneConfig.getLanguage()).addNodesAndRelations();
							break;
						case file:
							new CloneInserterForFile(cloneConfig.getNamePath(), cloneConfig.getResultPath(), cloneConfig.getGroupPath(), cloneConfig.getLanguage()).addNodesAndRelations();
							break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			LOGGER.info("克隆分析线程结束，线程-1：" + (latchOfOthers.getCount() - 1));
			latchOfOthers.countDown();
		}

	}

	public void libAnalyse() {
		try {
			if (yaml.isAnalyseLib()) {
				LOGGER.info("三方依赖分析");
				for (LibConfig libConfig : config.getLibsConfig()) {
					switch(libConfig.getLanguage()) {
						case java:
							new LibraryInserter(libConfig.getPath()).addNodesAndRelations();
							break;
						case cpp:
							break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			LOGGER.info("lib分析线程结束，线程-1：" + (latchOfOthers.getCount() - 1));
			latchOfOthers.countDown();
		}
	}
}
