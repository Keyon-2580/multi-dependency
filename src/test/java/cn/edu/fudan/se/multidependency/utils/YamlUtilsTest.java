package cn.edu.fudan.se.multidependency.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class YamlUtilsTest {

	@Test
	public void test() {
		YamlUtils.YamlObject yaml;
		try {
			yaml = YamlUtils.getDataBasePath("src/main/resources/application.yml");
			String test = yaml.forTest;
			assertTrue("this property is for YamlUtilsTest".equals(test));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
