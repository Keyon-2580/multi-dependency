package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.service.spring.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.spring.CacheService;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneValue;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneValueCalculatorForMicroService;

@Service
public class CloneValueServiceImpl implements CloneValueService {
	
    @Autowired
    CloneRepository cloneRepository;
    
    @Autowired
    ContainRelationService containRelationService;
    
    @Autowired
    StaticAnalyseService staticAnalyseService;
    
    @Autowired
    CacheService cacheService;
    
    @Autowired
    MicroserviceService msService;
	
    @Autowired
    BasicCloneQueryService basicCloneQueryService;

	@Override
	public Collection<CloneValue<Project>> queryProjectCloneFromFileClone(Collection<Clone> fileClones,
			boolean removeSameNode) {
		List<CloneValue<Project>> result = new ArrayList<>();
		Map<Project, Map<Project, CloneValue<Project>>> projectToProjectClones = new HashMap<>();
		for(Clone clone : fileClones) {
			CodeNode node1 = clone.getCodeNode1();
			CodeNode node2 = clone.getCodeNode2();
			if(!(node1 instanceof ProjectFile) || !(node2 instanceof ProjectFile)) {
				continue;
			}
			ProjectFile file1 = (ProjectFile) node1;
			ProjectFile file2 = (ProjectFile) node2;
			if(file1.equals(file2)) {
				continue;
			}
			Project project1 = containRelationService.findFileBelongToProject(file1);
			Project project2 = containRelationService.findFileBelongToProject(file2);
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			CloneValue<Project> cloneValue = hasFileCloneInProject(projectToProjectClones, project1, project2);
			if(cloneValue == null) {
				cloneValue = new CloneValue<Project>();
				cloneValue.setNode1(project1);
				cloneValue.setNode2(project2);
				result.add(cloneValue);
			}
			cloneValue.addChild(clone);
			
			Map<Project, CloneValue<Project>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
			project1ToClones.put(project2, cloneValue);
			projectToProjectClones.put(project1, project1ToClones);
		}
		return result;
	}
	
	private CloneValue<Project> hasFunctionCloneInProject(
			Map<Project, Map<Project, CloneValue<Project>>> projectToProjectClones, 
			Project project1, Project project2) {
		Map<Project, CloneValue<Project>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
		CloneValue<Project> cloneValue = project1ToClones.get(project2);
		if(cloneValue != null) {
			return cloneValue;
		}
		Map<Project, CloneValue<Project>> project2ToClones = projectToProjectClones.getOrDefault(project2, new HashMap<>());
		cloneValue = project2ToClones.get(project1);
		return cloneValue;
	}
	
	private CloneValue<Project> hasFileCloneInProject(
			Map<Project, Map<Project, CloneValue<Project>>> projectToProjectClones, 
			Project project1, Project project2) {
		Map<Project, CloneValue<Project>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
		CloneValue<Project> clone = project1ToClones.get(project2);
		if(clone != null) {
			return clone;
		}
		Map<Project, CloneValue<Project>> project2ToClones = projectToProjectClones.getOrDefault(project2, new HashMap<>());
		clone = project2ToClones.get(project1);
		return clone;
	}

	@Override
	public Collection<CloneValue<Project>> findProjectCloneFromFunctionClone(Collection<Clone> functionClones, boolean removeSameNode) {
		List<CloneValue<Project>> result = new ArrayList<>();
		Map<Project, Map<Project, CloneValue<Project>>> projectToProjectClones = new HashMap<>();
		for(Clone functionCloneFunction : functionClones) {
			CodeNode node1 = functionCloneFunction.getCodeNode1();
			CodeNode node2 = functionCloneFunction.getCodeNode2();
			if(!(node1 instanceof Function) || !(node2 instanceof Function)) {
				continue;
			}
			Function function1 = (Function) node1;
			Function function2 = (Function) node2;
			if(function1.equals(function2)) {
				continue;
			}
			Project project1 = containRelationService.findFunctionBelongToProject(function1);
			Project project2 = containRelationService.findFunctionBelongToProject(function2);
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			CloneValue<Project> clone = hasFunctionCloneInProject(projectToProjectClones, project1, project2);
			if(clone == null) {
				clone = new CloneValue<Project>();
				clone.setNode1(project1);
				clone.setNode2(project2);
				result.add(clone);
			}
			// 函数间的克隆作为Children
			clone.addChild(functionCloneFunction);
			
			Map<Project, CloneValue<Project>> project1ToClones 
				= projectToProjectClones.getOrDefault(project1, new HashMap<>());
			project1ToClones.put(project2, clone);
			projectToProjectClones.put(project1, project1ToClones);
		}
		return result;
	}
	
	private CloneValue<MicroService> hasCloneInFunctionClones(
			Map<MicroService, Map<MicroService, CloneValue<MicroService>>> msToMsClones, MicroService ms1, MicroService ms2) {
		Map<MicroService, CloneValue<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
		CloneValue<MicroService> clone = ms1ToClones.get(ms2);
		if(clone != null) {
			return clone;
		}
		Map<MicroService, CloneValue<MicroService>> ms2ToClones = msToMsClones.getOrDefault(ms2, new HashMap<>());
		clone = ms2ToClones.get(ms1);
		return clone;
	}
	
	private CloneValue<MicroService> hasCloneInFileClones(
			Map<MicroService, Map<MicroService, CloneValue<MicroService>>> msToMsClones, MicroService ms1, MicroService ms2) {
		Map<MicroService, CloneValue<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
		CloneValue<MicroService> clone = ms1ToClones.get(ms2);
		if(clone != null) {
			return clone;
		}
		Map<MicroService, CloneValue<MicroService>> ms2ToClones = msToMsClones.getOrDefault(ms2, new HashMap<>());
		clone = ms2ToClones.get(ms1);
		return clone;
	}

	@Override
	public Collection<CloneValue<MicroService>> findMicroServiceCloneFromFunctionClone(Collection<Clone> functionClones, boolean removeSameNode) {
		Collection<CloneValue<Project>> projectClones = findProjectCloneFromFunctionClone(functionClones, removeSameNode);
		List<CloneValue<MicroService>> result = new ArrayList<>();
		Map<MicroService, Map<MicroService, CloneValue<MicroService>>> msToMsClones = new HashMap<>();
		for(CloneValue<Project> projectClone : projectClones) {
			Project project1 = projectClone.getNode1();
			Project project2 = projectClone.getNode2();
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms1 == null || ms2 == null) {
				continue;
			}
			if(removeSameNode && ms1.equals(ms2)) {
				continue;
			}
			CloneValue<MicroService> clone = hasCloneInFunctionClones(msToMsClones, ms1, ms2);
			if(clone == null) {
				clone = new CloneValue<MicroService>();
				clone.setNode1(ms1);
				clone.setNode2(ms2);
				result.add(clone);
				CloneValueCalculatorForMicroService calculator = new CloneValueCalculatorForMicroService();
				Iterable<Function> functions1 = containRelationService.findMicroServiceContainFunctions(ms1);
				Iterable<Function> functions2 = containRelationService.findMicroServiceContainFunctions(ms2);
				calculator.addFunctions(functions1, ms1);
				calculator.addFunctions(functions2, ms2);
				clone.setCalculator(calculator);
			}
			clone.addChildren(projectClone.getChildren());
			Map<MicroService, CloneValue<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
			ms1ToClones.put(ms2, clone);
			msToMsClones.put(ms1, ms1ToClones);
			
		}
		return result;
	}

	@Override
	public Collection<CloneValue<MicroService>> findMicroServiceCloneFromFileClone(
			Collection<Clone> fileClones, boolean removeSameNode) {
		Iterable<CloneValue<Project>> projectClones = queryProjectCloneFromFileClone(fileClones, removeSameNode);
		List<CloneValue<MicroService>> result = new ArrayList<>();
		Map<MicroService, Map<MicroService, CloneValue<MicroService>>> msToMsClones = new HashMap<>();
		for(CloneValue<Project> projectClone : projectClones) {
			Project project1 = projectClone.getNode1();
			Project project2 = projectClone.getNode2();
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms1 == null || ms2 == null) {
				continue;
			}
			if(removeSameNode && ms1.equals(ms2)) {
				continue;
			}
			CloneValue<MicroService> clone = hasCloneInFileClones(msToMsClones, ms1, ms2);
			if(clone == null) {
				clone = new CloneValue<MicroService>();
				clone.setNode1(ms1);
				clone.setNode2(ms2);
				result.add(clone);
				CloneValueCalculatorForMicroService calculator = new CloneValueCalculatorForMicroService();
				Iterable<Function> functions1 = containRelationService.findMicroServiceContainFunctions(ms1);
				Iterable<Function> functions2 = containRelationService.findMicroServiceContainFunctions(ms2);
				calculator.addFunctions(functions1, ms1);
				calculator.addFunctions(functions2, ms2);
				clone.setCalculator(calculator);
			}
			clone.addChildren(projectClone.getChildren());
			Map<MicroService, CloneValue<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
			ms1ToClones.put(ms2, clone);
			msToMsClones.put(ms1, ms1ToClones);
			
		}
		return result;
	}
}
