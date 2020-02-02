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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.google.common.io.Files;

import cn.edu.fudan.se.multidependency.utils.FileUtils;
import depends.extractor.java.JavaLexer;
import depends.extractor.java.JavaParser;

public class StubUtil {
	
	public static JSONObject extractConfig(String configPath) {
		JSONObject result = new JSONObject();
		try(JSONReader reader = new JSONReader(new FileReader(new File(configPath)));){
			result = (JSONObject) reader.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void stubSingleFileForJava(String filePath, String outputFilePath, String className,
			String outputStubLogFilePath) throws Exception {
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
		JavaStubListener stubListener = new JavaStubListenerUsingTryFinally(tokens, listenFile, input, className,
				outputStubLogFilePath);
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

	public static void stubDirectoryForJava(String directoryPath, String outputDirectoryPath, String className,
			String outputStubLogFilePath) throws Exception {
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
		String projectName = FileUtils.extractFileName(directoryPath);
		result.forEach(file -> {
			String outputFilePath = file.getAbsolutePath().replace(directory.getAbsolutePath(),
					outputDirectory.getAbsolutePath() + "\\" + projectName);
			if (".java".equals(FileUtils.extractSuffix(file.getAbsolutePath()))) {
				try {
					stubSingleFileForJava(file.getAbsolutePath(), outputFilePath, className, outputStubLogFilePath);
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
