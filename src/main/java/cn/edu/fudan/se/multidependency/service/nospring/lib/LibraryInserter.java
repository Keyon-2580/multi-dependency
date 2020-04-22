package cn.edu.fudan.se.multidependency.service.nospring.lib;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.InsertDataMain;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.FunctionUtil;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import cn.edu.fudan.se.multidependency.utils.LibraryUtil;

public class LibraryInserter extends ExtractorForNodesAndRelationsImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryInserter.class);
	
	public LibraryInserter(String libraryJsonPath) {
		this.libraryJsonPath = libraryJsonPath;
	}
	
	private String libraryJsonPath;

	@Override
	public void addNodesAndRelations() throws Exception {
		JSONObject libJson = JSONUtil.extractJSONObject(new File(libraryJsonPath));
		
		for(String projectName : libJson.keySet()) {
			Project project = null;
			if(StringUtils.isBlank(projectName)) {
				if(this.getNodes().findAllProjects().size() > 1) {
					throw new Exception("不知道三方依赖的项目是啥 " + libraryJsonPath);
				} else {
					project = this.getNodes().findAllProjects().get(0);
				}
			} else {
				project = this.getNodes().findProject(projectName, Language.java);
			}
			if(project == null) {
//				throw new Exception("不知道三方依赖的项目是啥 " + libraryJsonPath + " " + projectName);
				LOGGER.warn(String.join(" ", "不知道三方依赖的项目是啥 ", libraryJsonPath, projectName));
				continue;
			}
//			this.getNodes().findFileByPath(path)
//			List<Function>this.getNodes().findFunctionsInProject(project)
			JSONObject projectJson = libJson.getJSONObject(projectName);
			for(String fileName : projectJson.keySet()) {
				JSONObject fileJson = projectJson.getJSONObject(fileName);
				for(String functionFullName : fileJson.keySet()) {
					List<String> functionNameAndParameters = FunctionUtil.extractFunctionNameAndParameters(functionFullName);
					if(functionNameAndParameters.size() < 1) {
						throw new Exception("函数名和变量数量小于1");
					}
					String functionName = functionNameAndParameters.get(0);
					List<Function> functions = this.getNodes().findFunctionsInProject(project).get(functionName);
					if(functions == null) {
//						System.out.println(functionName);
						LOGGER.warn(String.join(" ", "没有函数名为", functionName, "的函数"));
					}
					Function givenFunction = null;
					for(Function function : functions) {
						if(function.getParameters().size() != (functionNameAndParameters.size() - 1)) {
							continue;
						}
						/// FIXME
						givenFunction = function;
					}
					if(givenFunction == null) {
						throw new Exception("函数为null，函数名：" + functionName);
					}
					JSONObject functionJson = fileJson.getJSONObject(functionFullName);
					for(String apiName : functionJson.keySet()) {
						Library library = LibraryUtil.extract(apiName);
						Library libraryInDataBase = this.getNodes().findLibrary(library.getGroupId(), library.getName(), library.getVersion());
						if(libraryInDataBase != null) {
							// 数据库里有
							library = libraryInDataBase;
						} else {
							library.setEntityId(generateEntityId());
							addNode(library, null);
						}
						JSONObject libraryAPIJson = functionJson.getJSONObject(apiName);
						for(String apiFunctionName : libraryAPIJson.keySet()) {
							LibraryAPI api = this.getNodes().findLibraryAPIInLibraryByAPIName(apiFunctionName, library);
							if(api == null) {
								api = new LibraryAPI();
								api.setName(apiFunctionName);
								this.getNodes().addLibraryAPINode(api, library);
								Contain contain = new Contain(library, api);
								addRelation(contain);
							}
							int times = libraryAPIJson.getIntValue(apiFunctionName);
							FunctionCallLibraryAPI call = new FunctionCallLibraryAPI(givenFunction, api);
							call.setTimes(times);
							addRelation(call);
						}
					}
				}
			}
		}
	}
	
}