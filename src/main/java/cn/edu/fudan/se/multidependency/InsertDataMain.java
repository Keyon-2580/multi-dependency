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
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import depends.entity.repo.EntityRepo;

public class InsertDataMain {

	private static final Logger LOGGER = LoggerFactory.getLogger(InsertDataMain.class);

	public static void main(String[] args) throws Exception {
		insert(args);
	}
	
	public static void insert(String[] args) {
		try {
			YamlUtil.YamlObject yaml = null;
			if (args == null || args.length == 0) {
				yaml = YamlUtil.getDataBasePathDefault("src/main/resources/application.yml");
			} else {
				yaml = YamlUtil.getDataBasePath(args[0]);
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
				
				LOGGER.info("静态分析，项目：" + projectName + "，语言：" + language);
				
				LOGGER.info("使用depends解析项目，项目路径：" + projectPath);
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

			/**
			 * 动态分析
			 */
			if (yaml.isAnalyseDynamic()) {
				LOGGER.info("动态运行分析");
				insertDynamic(yaml);
				
				LOGGER.info("引入特性与测试用例");
				String featuresJsonPath = "src/main/resources/features/train-ticket/features-javassist.json";
				ExtractorForNodesAndRelationsImpl featureExtractor = new FeatureAndTestCaseInserter(featuresJsonPath);
				featureExtractor.addNodesAndRelations();
			}
			/// FIXME
			// 其它

			// 最后统一插入数据库
			repository.insertToNeo4jDataBase();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void insertDynamic(YamlUtil.YamlObject yaml) throws Exception {
		String[] dynamicFileSuffixes = null;
		for (Language language : Language.values()) {
			switch(language) {
			case java:
				dynamicFileSuffixes = new String[yaml.getDynamicJavaFileSuffix().size()];
				yaml.getDynamicJavaFileSuffix().toArray(dynamicFileSuffixes); // 后缀为.log
				File dynamicDirectory = new File(yaml.getDynamicDirectoryRootPath());
				List<File> result = new ArrayList<>();
				FileUtil.listFiles(dynamicDirectory, result, dynamicFileSuffixes);
				File[] files = new File[result.size()];
				result.toArray(files);
				DynamicInserterForNeo4jService stubJavaInserter = new JavassistDynamicInserter();
				stubJavaInserter.setDynamicFunctionCallFiles(files);
				stubJavaInserter.addNodesAndRelations();
				break;
			case cpp:
				/// FIXME
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
