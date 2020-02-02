package cn.edu.fudan.se.multidependency.stub;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class StubMain {

	public static void main(String[] args) throws Exception {
//		String filePath = "D:\\git\\multi-dependency\\src\\main\\java\\cn\\edu\\fudan\\se\\multidependency\\stub\\StubMain.java";
//		StubUtil.stubSingleFileForJava(filePath, "D:\\test\\test.java", "cn.edu.fudan.se.multidependency.stub.StubMain",
//				"D:\\stub.log");
//		// stubSingleFileForJava("D:\\git\\SimpleTest\\src\\main\\java\\fan\\SimpleTest\\App2.java",
//		// "D:\\test\\test.java", "fan.SimpleTest.App2", "D:\\stub.log");
//		// stubDirectoryForJava("D:\\git\\multi-dependency", "D:\\projectPath",
//		// "cn.edu.fudan.se.multidependency.stub.StubMain", "D:\\stub.log");
//		StubUtil.stubDirectoryForJava("D:\\multiple-dependency-project\\depends", "D:\\projectPath", "depends.entity.FileEntity",
//				"D:\\stub.log");
		JSONObject result = StubUtil.extractConfig("src/main/resources/dynamic/stub/train-ticket/config.json");
		JSONArray projects = result.getJSONArray("projects");
		projects.forEach(project -> {
			String projectPath = ((JSONObject) project).getString("projectPath");
			String outputProjectPath = ((JSONObject) project).getString("outputProjectPath");
			String language = ((JSONObject) project).getString("language");
			String globalVariableLocation = ((JSONObject) project).getString("globalVariableLocation");
			String outputStubLogFilePath = ((JSONObject) project).getString("outputStubLogFilePath");
			if("java".equals(language)) {
				try {
					StubUtil.stubDirectoryForJava(projectPath, outputProjectPath, globalVariableLocation, outputStubLogFilePath);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

}
