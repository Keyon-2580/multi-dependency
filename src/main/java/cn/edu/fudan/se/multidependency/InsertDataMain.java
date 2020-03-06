package cn.edu.fudan.se.multidependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.service.FeatureAndTestCaseInserter;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4jServiceFactory;
import cn.edu.fudan.se.multidependency.service.RepositoryService;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractorImpl;
import cn.edu.fudan.se.multidependency.service.dynamic.DynamicInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.dynamic.JavassistDynamicInserter;
import cn.edu.fudan.se.multidependency.service.dynamic.StubJavaForJaegerDynamicInserter;
import cn.edu.fudan.se.multidependency.service.microservice.jaeger.JaegerTraceInserterFromJSONFile;
import cn.edu.fudan.se.multidependency.utils.FileUtils;
import cn.edu.fudan.se.multidependency.utils.YamlUtils;
import depends.entity.repo.EntityRepo;

public class InsertDataMain {

	private static final Logger LOGGER = LoggerFactory.getLogger(InsertDataMain.class);

	public static void main(String[] args) throws Exception {
		insert(args);
	}
	
	public static void insert(String[] args) {
		try {
			YamlUtils.YamlObject yaml = null;
			if (args == null || args.length == 0) {
				yaml = YamlUtils.getDataBasePathDefault("src/main/resources/application.yml");
			} else {
				yaml = YamlUtils.getDataBasePath(args[0]);
			}
			InserterForNeo4j repository = RepositoryService.getInstance();
			repository.setDatabasePath(yaml.getNeo4jDatabasePath());
			repository.setDelete(yaml.isDeleteDatabase());
			
			/**
			 * 静态分析
			 */
			String projectsConfig = yaml.getProjectsConfig();
			StringBuilder projectsJson = new StringBuilder();
			try(BufferedReader reader = new BufferedReader(new FileReader(new File(projectsConfig)))) {
				String line = "";
				while((line = reader.readLine()) != null) {
					projectsJson.append(line);
				}
			}
			JSONArray projectsArray = JSONObject.parseArray(projectsJson.toString());
			
			for(int i = 0; i < projectsArray.size(); i++) {
				JSONObject projectJson = projectsArray.getJSONObject(i);
				Language language = Language.valueOf(projectJson.getString("language"));
				String projectPath = projectJson.getString("path");
				boolean isMicroservice = projectJson.getBooleanValue("isMicroservice");
				String serviceGroupName = projectJson.getString("serviceGroupName");
				serviceGroupName = serviceGroupName == null ? "" : serviceGroupName;
				String projectName = projectJson.getString("project");
				
				System.out.println("静态分析，项目：" + projectName + "，语言：" + language);
				LOGGER.info("静态分析，项目：" + projectName + "，语言：" + language);
				
				DependsEntityRepoExtractor extractor = DependsEntityRepoExtractorImpl.getInstance();
				extractor.setLanguage(language);
				extractor.setProjectPath(projectPath);
				EntityRepo entityRepo = extractor.extractEntityRepo();
				if (extractor.getEntityCount() > 0) {
					InserterForNeo4jServiceFactory.getInstance()
							.createCodeInserterService(projectPath, projectName, entityRepo, language, isMicroservice, serviceGroupName)
							.addNodesAndRelations();
				}
			}
			
			/**
			 * 构建分析
			 */
			/*if (yaml.isAnalyseBuild()) {
				System.out.println("构建分析");
				insertBuildInfo(yaml);
			}*/

			/*boolean extractFromMicroService = true;
			if(extractFromMicroService) {
				ExtractorForNodesAndRelationsImpl jaegerExtractor = null;
				File callBetweenServiceDirectory = new File("src/main/resources/dynamic/microservice/train-ticket");
				if(callBetweenServiceDirectory.exists() && callBetweenServiceDirectory.isDirectory()) {
					for(File f : callBetweenServiceDirectory.listFiles()) {
						if(f.isFile()) {
							jaegerExtractor = new JaegerTraceInserterFromJSONFile(f.getAbsolutePath());
							jaegerExtractor.addNodesAndRelations();
						}
					}
				}
				String featuresJsonPath = "src/main/resources/features/train-ticket/features.json";
				ExtractorForNodesAndRelationsImpl featureExtractor = new FeatureAndTestCaseInserter(featuresJsonPath);
				featureExtractor.addNodesAndRelations();
			}*/

			/**
			 * 动态分析
			 */
			if (yaml.isAnalyseDynamic()) {
				System.out.println("动态分析");
				insertDynamicFromJavassist(yaml);
			}
			String featuresJsonPath = "src/main/resources/features/train-ticket/features-javassist.json";
			ExtractorForNodesAndRelationsImpl featureExtractor = new FeatureAndTestCaseInserter(featuresJsonPath);
			featureExtractor.addNodesAndRelations();
			/// FIXME
			// 其它

			// 最后统一插入数据库
			repository.insertToNeo4jDataBase();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void insertDynamicFromJavassist(YamlUtils.YamlObject yaml) throws Exception {
		String[] dynamicFileSuffixes = null;
		for (Language language : Language.values()) {
			if (language == Language.java) {
				dynamicFileSuffixes = new String[yaml.getDynamicJavaFileSuffix().size()];
				yaml.getDynamicJavaFileSuffix().toArray(dynamicFileSuffixes); // 后缀为.log
				File dynamicDirectory = new File(yaml.getDynamicDirectoryRootPath());
				List<File> result = new ArrayList<>();
				FileUtils.listFiles(dynamicDirectory, result, dynamicFileSuffixes);
				File[] files = new File[result.size()];
				result.toArray(files);
				DynamicInserterForNeo4jService stubJavaInserter = new JavassistDynamicInserter();
				stubJavaInserter.setDynamicFunctionCallFiles(files);
				stubJavaInserter.addNodesAndRelations();
			} else {
				///FIXME
			}
		}
	}

	public static void insertDynamicFromStub(YamlUtils.YamlObject yaml) throws Exception {
		String[] dynamicFileSuffixes = null;
		for (Language language : Language.values()) {
			if (language == Language.java) {
				dynamicFileSuffixes = new String[yaml.getDynamicJavaFileSuffix().size()];
				yaml.getDynamicJavaFileSuffix().toArray(dynamicFileSuffixes); // 后缀为.log
				File dynamicDirectory = new File(yaml.getDynamicDirectoryRootPath());
				List<File> result = new ArrayList<>();
				FileUtils.listFiles(dynamicDirectory, result, dynamicFileSuffixes);
				File[] files = new File[result.size()];
				result.toArray(files);
				DynamicInserterForNeo4jService stubJavaInserter = new StubJavaForJaegerDynamicInserter();
				stubJavaInserter.setDynamicFunctionCallFiles(files);
				stubJavaInserter.addNodesAndRelations();
			} else {
				///FIXME
			}
		}
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
