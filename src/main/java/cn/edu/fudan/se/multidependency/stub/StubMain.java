package cn.edu.fudan.se.multidependency.stub;

public class StubMain {

	public static void main(String[] args) throws Exception {
//		String filePath = "D:\\git\\multi-dependency\\src\\main\\java\\cn\\edu\\fudan\\se\\multidependency\\stub\\StubMain.java";
//		StubUtil.stubSingleFileForJava("test", filePath, "D:\\test\\test.java", "cn.edu.fudan.se.multidependency.stub.StubMain",
//				"D:\\stub.log", "{\"featureId\" : \"123\", \"scenarioId\" : \"123\"}");
//		// stubSingleFileForJava("D:\\git\\SimpleTest\\src\\main\\java\\fan\\SimpleTest\\App2.java",
//		// "D:\\test\\test.java", "fan.SimpleTest.App2", "D:\\stub.log");
//		// stubDirectoryForJava("D:\\git\\multi-dependency", "D:\\projectPath",
//		// "cn.edu.fudan.se.multidependency.stub.StubMain", "D:\\stub.log");
//		StubUtil.stubDirectoryForJava("D:\\multiple-dependency-project\\depends", "D:\\projectPath", "depends.entity.FileEntity",
//				"D:\\stub.log");
		if(args.length == 0) {
			StubUtil.stubByConfig("src/main/resources/dynamic/stub/train-ticket/config.json");
		} else {
			StubUtil.stubByConfig(args[0]);
		}
		
	}

}
