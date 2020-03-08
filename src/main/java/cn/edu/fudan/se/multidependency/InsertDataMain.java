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
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelations;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4jServiceFactory;
import cn.edu.fudan.se.multidependency.service.RepositoryService;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractorImpl;
import cn.edu.fudan.se.multidependency.service.dynamic.DynamicInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.dynamic.FeatureAndTestCaseFromCodeForMicroserviceInserter;
import cn.edu.fudan.se.multidependency.service.dynamic.FeatureAndTestCaseFromJSONFileForMicroserviceInserter;
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
			
			ExtractorForNodesAndRelations inserter = null;
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
					inserter = InserterForNeo4jServiceFactory.getInstance()
							.createCodeInserterService(projectPath, projectName, entityRepo, language, isMicroservice, serviceGroupName);
					inserter.addNodesAndRelations();
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
				for (Language language : Language.values()) {
					inserter = insertDynamic(language, yaml);
					if(inserter != null) {
						inserter.addNodesAndRelations();
					}
				}
				
				LOGGER.info("引入特性与测试用例");
				inserter = insertFeatureAndTestCaseByJSONFile("src/main/resources/features/train-ticket/features-javassist.json");
				inserter.addNodesAndRelations();
			}
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
	
	public static ExtractorForNodesAndRelations insertFeatureAndTestCaseByCode() throws Exception {
		FeatureAndTestCaseFromCodeForMicroserviceInserter inserter = new FeatureAndTestCaseFromCodeForMicroserviceInserter();
		Feature feature0 = new Feature(0, "查询车票", "能够根据输入查询所需车票");
		inserter.addFeature(feature0);
		Feature feature1 = new Feature(1, "订票", "能够订购查询的车票");
		inserter.addFeature(feature1);
		Feature feature2 = new Feature(2, "查询所有保险", "能够查询所有保险");
		inserter.addFeature(feature2);
		Feature feature3 = new Feature(3, "查询所有餐品", "能够查询所有餐品");
		inserter.addFeature(feature3);
		Feature feature4 = new Feature(4, "查询账户所有联系方式", "能够查询当前登录账户的所有联系方式");
		inserter.addFeature(feature4);
		Feature feature5 = new Feature(5, "创建新联系方式", "在订票页面填写新的账户联系方式");
		inserter.addFeature(feature5);
		Feature feature6 = new Feature(6, "登录", "输入用户名、密码和验证码，无误后能够使用系统");
		inserter.addFeature(feature6);
		Feature feature7 = new Feature(7, "获取验证码", "获取验证码");
		inserter.addFeature(feature7);
		
		JSONObject input = new JSONObject();
		input.clear();
		input.put("Starting", "Shang Hai");
		input.put("Terminal", "Su Zhou");
		input.put("Date", "0305");
		input.put("Type", "GaoTie Dongche");
		TestCase testcase0 = new TestCase(0, "查票-00", input.toString(), true, "");
		inserter.addTestCase(testcase0);
		inserter.addTestCaseExecuteFeature(testcase0, feature0);
		inserter.addTestCaseRunTrace(testcase0, "0ec6ca77-9daf-4d0a-9170-4e7bf3a5738c");
		
		
		
		return inserter;
	}
	
	public static ExtractorForNodesAndRelations insertDynamic(Language language, YamlUtil.YamlObject yaml) throws Exception {
		switch(language) {
		case java:
			String[] dynamicFileSuffixes = new String[yaml.getDynamicJavaFileSuffix().size()];
			yaml.getDynamicJavaFileSuffix().toArray(dynamicFileSuffixes); // 后缀为.log
			LOGGER.info("分析动态运行文件的后缀为：" + yaml.getDynamicJavaFileSuffix());
			File dynamicDirectory = new File(yaml.getDynamicDirectoryRootPath());
			LOGGER.info("分析动态运行文件的目录为：" + yaml.getDynamicDirectoryRootPath());
			List<File> result = new ArrayList<>();
			FileUtil.listFiles(dynamicDirectory, result, dynamicFileSuffixes);
			File[] files = new File[result.size()];
			result.toArray(files);
			DynamicInserterForNeo4jService stubJavaInserter = new JavassistDynamicInserter();
			stubJavaInserter.setDynamicFunctionCallFiles(files);
			return stubJavaInserter;
		case cpp:
			/// FIXME
		}
		return null;
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
