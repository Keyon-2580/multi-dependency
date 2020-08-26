package cn.edu.fudan.se.multidependency.service.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.config.PropertyConfig;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;

@Component
public class BeanCreator {
	
	@Bean
	public int setCommitSize(CommitUpdateFileRepository commitUpdateFileRepository) {
		System.out.println("设置commit update文件数");
		return commitUpdateFileRepository.setCommitFilesSize();
	}
    
	@Bean("createCoChanges")
	public List<CoChange> createCoChanges(PropertyConfig propertyConfig, CoChangeRepository cochangeRepository) {
		if(propertyConfig.isCalculateCoChange()) {
			System.out.println("创建cochange关系");
			cochangeRepository.deleteAll();
			return cochangeRepository.createCoChanges(Constant.COUNT_OF_MIN_COCHANGE);
		}
		return new ArrayList<>();
	}

	@Bean("createDependsOn")
	public List<DependsOn> createDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository, ProjectFileRepository fileRepository) {
		if(propertyConfig.isCalculateDependsOn()) {
			System.out.println("创建Depends On关系");
			dependsOnRepository.deleteAll();
			
			dependsOnRepository.createDependsOnWithCallInTypes();
			dependsOnRepository.createDependsOnWithCreateInTypes();
			dependsOnRepository.createDependsOnWithCastInTypes();
			dependsOnRepository.createDependsOnWithThrowInTypes();
			dependsOnRepository.createDependsOnWithParameterInTypes();
			dependsOnRepository.createDependsOnWithVariableTypeInTypes();
			dependsOnRepository.createDependsOnWithAccessInTypes();
			dependsOnRepository.createDependsOnWithAnnotationInTypes();
			dependsOnRepository.createDependsOnWithTimesInTypes();
			dependsOnRepository.deleteNullTimesDependsOnInTypes();
			
			dependsOnRepository.createDependsOnWithExtendsInFiles();
			dependsOnRepository.createDependsOnWithImplementsInFiles();
			dependsOnRepository.createDependsOnWithCallInFiles();
			dependsOnRepository.createDependsOnWithCreateInFiles();
			dependsOnRepository.createDependsOnWithCastInFiles();
			dependsOnRepository.createDependsOnWithThrowInFiles();
			dependsOnRepository.createDependsOnWithParameterInFiles();
			dependsOnRepository.createDependsOnWithVariableTypeInFiles();
			dependsOnRepository.createDependsOnWithAccessInFiles();
			dependsOnRepository.createDependsOnWithImpllinkInFiles();
			dependsOnRepository.createDependsOnWithAnnotationInFiles();
			dependsOnRepository.createDependsOnWithTimesInFiles();
			dependsOnRepository.deleteNullTimesDependsOnInFiles();
			
			dependsOnRepository.createDependsOnInPackages();
			dependsOnRepository.addTimesOnDependsOnInPackages();
			dependsOnRepository.deleteNullTimesDependsOnInPackages();
			
			fileRepository.pageRank(20, 0.85);
		}
		return new ArrayList<>();
	}
	
	@Bean
	public boolean setPackageLoSc(PackageRepository packageRepository) {
		System.out.println("计算Package总代码行");
		packageRepository.setEmptyPackageLocAndLines();
		packageRepository.setPackageLoc();
		packageRepository.setPackageLines();
		return true;
	}
}
