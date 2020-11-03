package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.AggregationCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.*;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.sonatype.guice.bean.binders.ParameterKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HotspotPackagePairDetectorImpl<ps> implements HotspotPackagePairDetector {

	@Autowired
	private DependsOnRepository dependsOnRepository;

	@Autowired
	private NodeService nodeService;

	@Override
	public List<HotspotPackagePair> detectHotspotPackagePairWithDependsOnByProjectId(long projectId) {
		List<HotspotPackagePair> result = new ArrayList<>();
		List<DependsOn> projectDependsOn = dependsOnRepository.findPackageDependsInProject(projectId);
		if(projectDependsOn != null && !projectDependsOn.isEmpty()){
			Map<Package, Map<Package, List<DependsOn>>> packageDependsPackage = new HashMap<>();
			for (DependsOn dependsOn : projectDependsOn){
				Package startNode = (Package) dependsOn.getStartNode();
				Package endNode = (Package) dependsOn.getEndNode();
				Package pck1 = startNode.getId() < endNode.getId() ? startNode : endNode;
				Package pck2 = startNode.getId() < endNode.getId() ? endNode : startNode;
				Map<Package, List<DependsOn>> dependsPackage = packageDependsPackage.getOrDefault(pck1, new HashMap<>());
				List<DependsOn> dependsOns = dependsPackage.getOrDefault(pck2, new ArrayList<>());
				dependsOns.add(dependsOn);
				dependsPackage.put(pck2, dependsOns);
				packageDependsPackage.put(pck1, dependsPackage);
			}
			for(Map.Entry<Package, Map<Package, List<DependsOn>>> entry : packageDependsPackage.entrySet()){
				Package pck1 = entry.getKey();
				Map<Package, List<DependsOn>> dependsPackage = entry.getValue();
				for(Map.Entry<Package, List<DependsOn>> entryKey : dependsPackage.entrySet()){
					Package pck2 = entryKey.getKey();
					List<DependsOn> dependsOns = dependsPackage.getOrDefault(pck2, new ArrayList<>());
					HotspotPackagePair hotspotPackagePair = createHotspotPackagePairWithDepends(pck1, pck2, dependsOns);
					result.add(hotspotPackagePair);
				}
			}
		}
		return result;
	}

	@Override
	public HotspotPackagePair detectHotspotPackagePairWithDependsOnByPackageId(long pck1Id, long pck2Id) {
		List<DependsOn> packageDependsOnList = dependsOnRepository.findPackageDependsByPackageId(pck1Id, pck2Id);
		HotspotPackagePair hotspotPackagePair = null;
		if(packageDependsOnList != null && !packageDependsOnList.isEmpty()){
			Package tmp1 = (Package) packageDependsOnList.get(0).getStartNode();
			Package tmp2 = (Package) packageDependsOnList.get(0).getEndNode();
			Package pck1 = tmp1.getId() == pck1Id ? tmp1 : tmp2;
			Package pck2 = tmp2.getId() == pck2Id ? tmp2 : tmp1;
			hotspotPackagePair = createHotspotPackagePairWithDepends(pck1, pck2, packageDependsOnList);
		}
		return hotspotPackagePair;
	}

	private HotspotPackagePair createHotspotPackagePairWithDepends(Package pck1, Package pck2, List<DependsOn> packageDependsOnList) {
		String dependsOnStr = "DependsOn: ";
		String dependsByStr = "DependsBy: ";
		int dependsOnTimes = 0;
		int dependsByTimes = 0;
		for (DependsOn dependsOn : packageDependsOnList){
			if(dependsOn.getStartNode().getId() == pck1.getId()){
				dependsOnStr += dependsOn.getDependsOnType();
				dependsOnTimes += dependsOn.getTimes();
			}else {
				dependsByStr += dependsOn.getDependsOnType();
				dependsByTimes += dependsOn.getTimes();
			}
		}
		DependsRelationDataForDoubleNodes<Node, Relation> dependsRelationDataForDoubleNodes = new DependsRelationDataForDoubleNodes(pck1, pck2);
		dependsRelationDataForDoubleNodes.setDependsOnTypes(dependsOnStr);
		dependsRelationDataForDoubleNodes.setDependsByTypes(dependsByStr);
		dependsRelationDataForDoubleNodes.setDependsOnTimes(dependsOnTimes);
		dependsRelationDataForDoubleNodes.setDependsByTimes(dependsByTimes);
		HotspotPackagePair hotspotPackagePair = new HotspotPackagePair(pck1, pck2, dependsRelationDataForDoubleNodes);
		return hotspotPackagePair;
	}

	@Override
	public List<HotspotPackagePair> detectHotspotPackagesByDependsOnInAllProjects(){
		Collection<Project> projects = nodeService.allProjects();
		List<HotspotPackagePair> result = new ArrayList<>();
		for(Project project : projects) {
			result.addAll(detectHotspotPackagePairWithDependsOnByProjectId(project.getId()));
		}
		return result;
	}

}
