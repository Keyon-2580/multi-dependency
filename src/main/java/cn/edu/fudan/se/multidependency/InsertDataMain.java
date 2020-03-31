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

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelations;
import cn.edu.fudan.se.multidependency.service.nospring.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.nospring.InserterForNeo4jServiceFactory;
import cn.edu.fudan.se.multidependency.service.nospring.RepositoryService;
import cn.edu.fudan.se.multidependency.service.nospring.code.BasicCodeInserterForNeo4jServiceImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.nospring.code.DependsEntityRepoExtractorImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.RestfulAPIFileExtractor;
import cn.edu.fudan.se.multidependency.service.nospring.code.RestfulAPIFileExtractorImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.SwaggerJSON;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.CppDynamicInserter;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.FeatureAndTestCaseFromJSONFileForMicroserviceInserter;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.JavassistDynamicInserter;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.TraceStartExtractor;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import depends.entity.repo.EntityRepo;

public class InsertDataMain {

	private static final Logger LOGGER = LoggerFactory.getLogger(InsertDataMain.class);

	public static void main(String[] args) throws Exception {
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
			InserterForNeo4j repository = RepositoryService.getInstance();
			repository.setDatabasePath(yaml.getNeo4jDatabasePath());
			repository.setDelete(yaml.isDeleteDatabase());
			
			/**
			 * 静态分析
			 */
			String projectsConfig = yaml.getProjectsConfig();
			StringBuilder projectsJson = new StringBuilder();
			try(BufferedReader reader = new BufferedReader(new FileReader(new File(projectsConfig)))) {
				LOGGER.info("读入项目配置：" + projectsConfig);
				String line = "";
				while((line = reader.readLine()) != null) {
					projectsJson.append(line);
				}
			}
			
			JSONArray projectsArray = JSONObject.parseArray(projectsJson.toString());
			for(int i = 0; i < projectsArray.size(); i++) {
				// 对每一个项目静态分析
				JSONObject projectJson = projectsArray.getJSONObject(i);
				Language language = Language.valueOf(projectJson.getString("language"));
				String projectPath = projectJson.getString("path");
				boolean isMicroservice = projectJson.getBooleanValue("isMicroservice");
				String serviceGroupName = projectJson.getString("serviceGroupName");
				serviceGroupName = serviceGroupName == null ? "" : serviceGroupName;
				String projectName = projectJson.getString("project");
				JSONArray excludesArray = projectJson.getJSONArray("excludes");
				int excludesSize = excludesArray == null ? 0 : excludesArray.size();
				String[] excludes = new String[excludesSize];
				for(int j = 0; j < excludesSize; j++) {
					excludes[j] = excludesArray.getString(j);
				}
				JSONObject restfulAPIs = projectJson.getJSONObject("restfulAPIs");
				
				LOGGER.info("静态分析，项目：" + projectName + "，语言：" + language);
				
				LOGGER.info("使用depends解析项目，项目路径：" + projectPath);
				DependsEntityRepoExtractor extractor = DependsEntityRepoExtractorImpl.getInstance();
				extractor.setExcludes(excludes);
				extractor.setLanguage(language);
				extractor.setProjectPath(projectPath);
				EntityRepo entityRepo = extractor.extractEntityRepo();
				if (extractor.getEntityCount() > 0) {
					BasicCodeInserterForNeo4jServiceImpl inserter = InserterForNeo4jServiceFactory.getInstance()
							.createCodeInserterService(projectPath, projectName, entityRepo, language, isMicroservice, serviceGroupName);
					if(restfulAPIs != null) {
						if("swagger".equals(restfulAPIs.getString("framework"))) {
							SwaggerJSON swagger = new SwaggerJSON();
							swagger.setPath(restfulAPIs.getString("path"));
							JSONArray excludeTags = restfulAPIs.getJSONArray("excludeTags");
							for(int j = 0; j < excludeTags.size(); j++) {
								swagger.addExcludeTag(excludeTags.getString(j));
							}
							RestfulAPIFileExtractor restfulAPIFileExtractorImpl = new RestfulAPIFileExtractorImpl(swagger);
							inserter.setRestfulAPIFileExtractor(restfulAPIFileExtractorImpl);
						}
					}
					inserter.addNodesAndRelations();
				}
			}
			
			ExtractorForNodesAndRelations inserter = null;
			/**
			 * 动态分析
			 */
			if (yaml.isAnalyseDynamic()) {
				LOGGER.info("动态运行分析");
				File[] dynamicLogs = analyseDynamicLogs(yaml);
				
				LOGGER.info("输出trace，只输出日志中记录的trace，不做数据库操作");
				new TraceStartExtractor(dynamicLogs).addNodesAndRelations();

				for (Language language : Language.values()) {
					inserter = insertDynamic(language, dynamicLogs);
					inserter.addNodesAndRelations();
				}
				
				LOGGER.info("引入特性与测试用例，对应到trace");
				inserter = insertFeatureAndTestCaseByJSONFile(yaml.getFeaturesPath());
				inserter.addNodesAndRelations();
			}
			
			/**
			 * 构建分析
			 */
			/*if (yaml.isAnalyseBuild()) {
				System.out.println("构建分析");
				insertBuildInfo(yaml);
			}*/
			
			/// FIXME
			// 其它

			// 最后统一插入数据库
			repository.insertToNeo4jDataBase();
		} catch (Exception e) {
			// 所有步骤中有一个出错，都会终止执行
			e.printStackTrace();
		}
	}
	
	/**
	 * 通过feature的json文件引入
	 * @param featuresJsonPath
	 * @throws Exception
	 */
	public static ExtractorForNodesAndRelations insertFeatureAndTestCaseByJSONFile(String featuresJsonPath) throws Exception {
		return new FeatureAndTestCaseFromJSONFileForMicroserviceInserter(featuresJsonPath);
	}
	
	public static ExtractorForNodesAndRelations insertDynamic(Language language, File[] dynamicLogFiles) throws Exception {
		switch(language) {
		case java:
			return new JavassistDynamicInserter(dynamicLogFiles);
		case cpp:
			return new CppDynamicInserter(dynamicLogFiles);
		}
		throw new LanguageErrorException(language.toString());
	}
	
	private static File[] analyseDynamicLogs(YamlUtil.YamlObject yaml) {
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
