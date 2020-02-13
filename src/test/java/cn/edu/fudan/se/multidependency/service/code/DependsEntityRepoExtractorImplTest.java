package cn.edu.fudan.se.multidependency.service.code;

import static org.junit.Assert.*;

import org.junit.Test;

import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.repo.EntityRepo;

public class DependsEntityRepoExtractorImplTest {

	@Test
	public void test() {
		DependsEntityRepoExtractor extractor = DependsEntityRepoExtractorImpl.getInstance();
		extractor.setLanguage(Language.java);
		extractor.setProjectPath("D:\\git\\SimpleTest");
		try {
			EntityRepo entityRepo = extractor.extractEntityRepo();
//			System.out.println(entityRepo.get);
			System.out.println(extractor.getEntityCount());
			entityRepo.entityIterator().forEachRemaining(entity -> {
				System.out.println(entity.getClass() + " " + entity);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}