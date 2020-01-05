package cn.edu.fudan.se.multidependency.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class FileUtilsTest {

	@Test
	public void test() {
		String projectPath = "/bash-5.0";
		String filePath = "D:\\multiple-dependency-project\\bash-5.0\\lib\\malloc\\malloc.c";
		String directoryPath = "D:\\multiple-dependency-project\\bash-5.0\\lib\\malloc";
		filePath = filePath.replace("\\", "/");
		filePath = filePath.substring(filePath.indexOf(projectPath + "/"));
		directoryPath = directoryPath.replace("\\", "/");
		directoryPath = directoryPath.substring(directoryPath.indexOf(projectPath + "/"));
		assertEquals(filePath, "/bash-5.0/lib/malloc/malloc.c");
		assertEquals(directoryPath, "/bash-5.0/lib/malloc");
	}
	

}
