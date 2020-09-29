package cn.edu.fudan.se.multidependency.service.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.config.PropertyConfig;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.clone.CloneGroupRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneValueService;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;

@Component
public class BeanCreator {

	@Autowired
	private CloneValueService cloneValueService;

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Bean
	public int setCommitSize(CommitUpdateFileRepository commitUpdateFileRepository) {
		System.out.println("设置commit update文件数...");
		return commitUpdateFileRepository.setCommitFilesSize();
	}
    
	@Bean("createCoChanges")
	public List<CoChange> createCoChanges(PropertyConfig propertyConfig, CoChangeRepository cochangeRepository) {
		if(propertyConfig.isCalculateCoChange()) {
			System.out.println("创建cochange关系...");
			cochangeRepository.deleteAll();
			return cochangeRepository.createCoChanges(Constant.COUNT_OF_MIN_COCHANGE);
		}
		return new ArrayList<>();
	}

	@Bean("createDependsOn")
	public List<DependsOn> createDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository, ProjectFileRepository fileRepository) {
		if(propertyConfig.isCalculateDependsOn()) {
			System.out.println("创建Depends On关系...");
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

	@Bean("createCloneGroup")
	public List<DependsOn> createCloneGroup(PropertyConfig propertyConfig, CloneGroupRepository cloneGroupRepository) {
		if(propertyConfig.isCalculateCloneGroup()) {
			System.out.println("创建Clone Group关系...");
			cloneGroupRepository.setJavaLanguageBySuffix();
			cloneGroupRepository.setCppLanguageBySuffix();
			cloneGroupRepository.deleteCloneGroupContainRelations();
			cloneGroupRepository.deleteCloneGroupRelations();

			cloneGroupRepository.setFileGroup();
			cloneGroupRepository.createCloneGroupRelations();
			cloneGroupRepository.createCloneGroupContainRelations();
			cloneGroupRepository.setCloneGroupContainSize();
			cloneGroupRepository.setCloneGroupLanguage();
		}
		return new ArrayList<>();
	}
	
	@Bean
	public boolean setPackageLoSc(PackageRepository packageRepository) {
		System.out.println("计算Package总代码行...");
		packageRepository.setEmptyPackageLocAndLines();
		packageRepository.setPackageLoc();
		packageRepository.setPackageLines();
		return true;
	}

	@Bean("setModuleClone")
	public List<ModuleClone> setModuleClone(PropertyConfig propertyConfig, ModuleCloneRepository moduleCloneRepository) {
		if(propertyConfig.isSetModuleClone()) {
			System.out.println("设置Module Clone信息...");
			if(moduleCloneRepository.getNumberOfModuleClone() == 0) {
				Collection<CloneValueForDoubleNodes<Package>> result = cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE));
				for(CloneValueForDoubleNodes<Package> moduleClone : result) {
					moduleCloneRepository.createModuleClone(moduleClone.getNode1().getId(), moduleClone.getNode2().getId(), moduleClone.getChildren().size(), moduleClone.getAllNodesInNode1().size(), moduleClone.getAllNodesInNode2().size(), moduleClone.getNodesInNode1().size(), moduleClone.getNodesInNode2().size());
				}
			}
			return moduleCloneRepository.getAllModuleClone();
		}
		return new ArrayList<>();
	}
}
