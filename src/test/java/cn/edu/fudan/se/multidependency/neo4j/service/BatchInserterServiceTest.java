package cn.edu.fudan.se.multidependency.neo4j.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import cn.edu.fudan.se.multidependency.model.node.code.CodeFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Package;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsType;
import cn.edu.fudan.se.multidependency.model.relation.code.PackageContainsFile;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsVariable;
import cn.edu.fudan.se.multidependency.utils.YamlUtils;

public class BatchInserterServiceTest {

	@Test
	public void test() {
		YamlUtils.YamlObject yaml = null;
		try {
			yaml = YamlUtils.getDataBasePath("src/main/resources/application.yml");
			String test = yaml.getForTest();
			assertTrue("this property is for YamlUtilsTest".equals(test));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try(BatchInserterService inserter = BatchInserterService.getInstance();){
			inserter.init(yaml.getNeo4jDatabasePath(), true);
			
			Package pck = new Package();
			pck.setPackageName("test");
			inserter.insertNode(pck);
			
			CodeFile file = new CodeFile();
			file.setFileName("src/test/Test.java");
			file.setPath("src/test/Test.java");
			inserter.insertNode(file);
			
			Type type = new Type();
			type.setTypeName("test.Test");
			inserter.insertNode(type);
			
			Function function = new Function();
			function.setFunctionName("test.Test.test");
			inserter.insertNode(function);
			
			Variable variable = new Variable();
			variable.setVariableName("test.Test.vtest");
			inserter.insertNode(variable);
			
			PackageContainsFile packageContainsFile = new PackageContainsFile(pck, file);
			inserter.insertRelation(packageContainsFile);
			
			FileContainsType fileContainsType = new FileContainsType(file, type);
			inserter.insertRelation(fileContainsType);
			
			TypeContainsFunction typeContainsFunction = new TypeContainsFunction(type, function);
			inserter.insertRelation(typeContainsFunction);
			
			TypeContainsVariable typeContainsVariable = new TypeContainsVariable(type, variable);
			inserter.insertRelation(typeContainsVariable);
			
			List<Long> ids = inserter.getRelationshipIds(type.getId());
			assertTrue(ids.size() == 3);
			
			Map<String, Object> properties = inserter.getNodeProperties(type.getId());
			assertEquals(properties.get("typeName"), "test.Test");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

}
