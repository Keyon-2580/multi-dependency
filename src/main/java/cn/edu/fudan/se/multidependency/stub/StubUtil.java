package cn.edu.fudan.se.multidependency.stub;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.google.common.io.Files;

import cn.edu.fudan.se.multidependency.utils.FileUtils;
import depends.extractor.java.JavaLexer;
import depends.extractor.java.JavaParser;

public class StubUtil {

	/**
	 * 通過配置文件解析多個項目
	 * 
	 * @param configPath
	 */
	public static void stubByConfig(String configPath) {
		JSONObject result = StubUtil.extractConfig(configPath);
		JSONArray projects = result.getJSONArray("projects");
		projects.forEach(project -> {
			String name = ((JSONObject) project).getString("name");
			String path = ((JSONObject) project).getString("path");
			String outputPath = ((JSONObject) project).getString("outputPath");
			String language = ((JSONObject) project).getString("language");
			String globalVariableLocation = ((JSONObject) project).getString("globalVariableLocation");
			String logPath = ((JSONObject) project).getString("logPath");
			JSONObject remarks = ((JSONObject) project).getJSONObject("remarks");
			if ("java".equals(language)) {
				try {
					if(remarks == null) {
						StubUtil.stubDirectoryForJava(name, path, outputPath, globalVariableLocation, logPath, null);
					} else {
						StubUtil.stubDirectoryForJava(name, path, outputPath, globalVariableLocation, logPath, remarks.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static JSONObject extractConfig(String configPath) {
		JSONObject result = new JSONObject();
		try (JSONReader reader = new JSONReader(new FileReader(new File(configPath)));) {
			result = (JSONObject) reader.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 解析單個java文件
	 * 
	 * @param projectName
	 * @param filePath
	 * @param outputFilePath
	 * @param className
	 * @param outputStubLogFilePath
	 * @throws Exception
	 */
	public static void stubSingleFileForJava(String projectName, String filePath, String outputFilePath,
			String className, String outputStubLogFilePath, String remarks) throws Exception {
		File listenFile = new File(filePath);
		CharStream input = CharStreams.fromFileName(filePath);
		Lexer lexer = new JavaLexer(input);
		lexer.setInterpreter(new LexerATNSimulator(lexer, lexer.getATN(), lexer.getInterpreter().decisionToDFA,
				new PredictionContextCache()));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaParser parser = new JavaParser(tokens);
		ParserATNSimulator interpreter = new ParserATNSimulator(parser, parser.getATN(),
				parser.getInterpreter().decisionToDFA, new PredictionContextCache());
		parser.setInterpreter(interpreter);
		JavaStubListener stubListener = new JavaStubListenerUsingTryFinally(tokens, projectName, listenFile, input,
				className, outputStubLogFilePath, remarks);
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(stubListener, parser.compilationUnit());
		File outputFile = new File(outputFilePath);
		if (!outputFile.exists()) {
			String directory = FileUtils.extractDirectoryFromFile(outputFilePath);
			new File(directory).mkdirs();
		}
		PrintWriter writer = new PrintWriter(outputFile);
		writer.append(stubListener.getRewriter().getText());
		writer.close();
	}

	/**
	 * 解析java語言的單個項目
	 * 
	 * @param projectName
	 * @param directoryPath
	 * @param outputDirectoryPath
	 * @param className
	 * @param outputStubLogFilePath
	 * @throws Exception
	 */
	public static void stubDirectoryForJava(String projectName, String directoryPath, String outputDirectoryPath,
			String className, String outputStubLogFilePath, String remarks) throws Exception {
		if (directoryPath.equals(outputDirectoryPath)) {
			return;
		}
		System.out.println("start to stub java project");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));

		File directory = new File(directoryPath);
		List<File> result = new ArrayList<>();
		FileUtils.listFiles(directory, result);
		File outputDirectory = new File(outputDirectoryPath);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		String directoryName = FileUtils.extractFileName(directoryPath);
		result.forEach(file -> {
			String outputFilePath = file.getAbsolutePath().replace(directory.getAbsolutePath(),
					outputDirectory.getAbsolutePath() + "\\" + directoryName);
			if (".java".equals(FileUtils.extractSuffix(file.getAbsolutePath()))) {
				try {
					stubSingleFileForJava(projectName, file.getAbsolutePath(), outputFilePath, className,
							outputStubLogFilePath, remarks);
				} catch (Exception e) {
					System.err.println(file.getAbsolutePath());
					e.printStackTrace();
					try {
						File outputFile = new File(outputFilePath);
						if (!outputFile.exists()) {
							String temp = FileUtils.extractDirectoryFromFile(outputFilePath);
							new File(temp).mkdirs();
						}
						Files.copy(file, outputFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			} else {
				try {
					File outputFile = new File(outputFilePath);
					if (!outputFile.exists()) {
						String temp = FileUtils.extractDirectoryFromFile(outputFilePath);
						new File(temp).mkdirs();
					}
					Files.copy(file, outputFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
	}
}
