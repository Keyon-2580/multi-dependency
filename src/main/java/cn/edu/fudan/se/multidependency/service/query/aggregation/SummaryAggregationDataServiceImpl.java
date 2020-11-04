package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.*;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.BasicDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.CloneRelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.CoChangeRelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.data.FileCloneWithCoChange;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChange;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SummaryAggregationDataServiceImpl implements SummaryAggregationDataService {
    
    @Autowired
    ContainRelationService containRelationService;

	@Autowired
	GitAnalyseService gitAnalyseService;


	private List<BasicDataForDoubleNodes<Node, Relation>> removeSameNodeToCloneValuePackages = null;
    private Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCloneFromFileCloneCache = null;
	public Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCloneFromFileClone(Collection<? extends Relation> fileClones) {
		Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> pckToPackageClones = queryPackageCloneFromFileCloneCache;
		if(pckToPackageClones == null) {
			List<BasicDataForDoubleNodes<Node, Relation>> cache = new ArrayList<>();
			pckToPackageClones = new HashMap<>();
			
			for(Relation clone : fileClones) {
				CodeNode node1 = (CodeNode)clone.getStartNode();
				CodeNode node2 = (CodeNode)clone.getEndNode();
				if(!(node1 instanceof ProjectFile) || !(node2 instanceof ProjectFile)) {
					continue;
				}
				ProjectFile file1 = (ProjectFile) node1;
				ProjectFile file2 = (ProjectFile) node2;
				if(file1.equals(file2)) {
					continue;
				}
				Package pck1 = containRelationService.findFileBelongToPackage(file1);
				Package pck2 = containRelationService.findFileBelongToPackage(file2);
				if(pck1.equals(pck2)) {
					continue;
				}
				BasicDataForDoubleNodes<Node, Relation> cloneValue = getSuperNodeRelationWithSubNodeRelation(pckToPackageClones, pck1, pck2);
				if(cloneValue == null) {
					cloneValue = new CloneRelationDataForDoubleNodes(pck1, pck2);
					cache.add(cloneValue);
				}
				cloneValue.addChild(clone);
				if(pck1.equals(cloneValue.getNode1())) {
					cloneValue.addCodeNodeToNode1(file1);
					cloneValue.addCodeNodeToNode2(file2);
				} else {
					cloneValue.addCodeNodeToNode2(file1);
					cloneValue.addCodeNodeToNode1(file2);
				}
				cloneValue.setAllNodesInNode1(new HashSet<>(containRelationService.findPackageContainFiles((Package)cloneValue.getNode1())));
				cloneValue.setAllNodesInNode2(new HashSet<>(containRelationService.findPackageContainFiles((Package)cloneValue.getNode2())));
				
				Map<Node, BasicDataForDoubleNodes<Node, Relation>> pck1ToClones = pckToPackageClones.getOrDefault(pck1, new HashMap<>());
				pck1ToClones.put(pck2, cloneValue);
				pckToPackageClones.put(pck1, pck1ToClones);
			}
			removeSameNodeToCloneValuePackages = cache;
			queryPackageCloneFromFileCloneCache = pckToPackageClones;
		}
		return pckToPackageClones;
	}

	@Override
	public List<BasicDataForDoubleNodes<Node, Relation>> queryPackageCloneFromFileCloneSort(Collection<? extends Relation> fileClones) {
		List<BasicDataForDoubleNodes<Node, Relation>> cache = removeSameNodeToCloneValuePackages;
		if(cache == null) {
			queryPackageCloneFromFileClone(fileClones);
			cache = removeSameNodeToCloneValuePackages;
		}
		List<BasicDataForDoubleNodes<Node, Relation>> result = new ArrayList<>(cache);
		result.sort((v1, v2) -> {
			return v2.getChildren().size() - v1.getChildren().size();
		});
		return result;
	}

	@Override
	public PackageCloneValueWithFileCoChange queryPackageCloneWithFileCoChange(Collection<? extends Relation> fileClones, Package pck1, Package pck2) throws Exception {
		BasicDataForDoubleNodes<Node, Relation> temp = querySuperNodeRelationFromSubNodeRelationSort(fileClones, pck1, pck2);
		PackageCloneValueWithFileCoChange result = new PackageCloneValueWithFileCoChange();
		result.setPck1((Package)temp.getNode1());
		result.setPck2((Package)temp.getNode2());
		result.addFile1(containRelationService.findPackageContainFiles(pck1));
		result.addFile2(containRelationService.findPackageContainFiles(pck2));
		List<Relation> children = temp.getChildren();
		for(Relation clone : children) {
			ProjectFile file1 = (ProjectFile) clone.getStartNode();
			ProjectFile file2 = (ProjectFile) clone.getEndNode();
			if(containRelationService.findFileBelongToPackage(file1).equals(pck1)) {
				result.addCloneFile1(file1);
				result.addCloneFile2(file2);
			} else {
				result.addCloneFile1(file2);
				result.addCloneFile2(file1);
			}
			CoChange cochange = gitAnalyseService.findCoChangeBetweenTwoFiles(file1, file2);
			result.addChild(new FileCloneWithCoChange((Clone)clone, cochange));
		}
		result.sortChildren();
		return result;
	}

	private List<BasicDataForDoubleNodes<Node, Relation>> removeSameNodeToCoChangeValuePackages = null;
	private Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCoChangeFromFileCoChangeCache = null;
	public Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCoChangeFromFileCoChange(Collection<? extends Relation> fileCoChanges) {
		Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> pckToPackageCoChanges = queryPackageCoChangeFromFileCoChangeCache;
		if(pckToPackageCoChanges == null) {
			List<BasicDataForDoubleNodes<Node, Relation>> cache = new ArrayList<>();
			pckToPackageCoChanges = new HashMap<>();

			for(Relation coChange : fileCoChanges) {
				CodeNode node1 = (CodeNode)coChange.getStartNode();
				CodeNode node2 = (CodeNode)coChange.getEndNode();
				if(!(node1 instanceof ProjectFile) || !(node2 instanceof ProjectFile)) {
					continue;
				}
				ProjectFile file1 = (ProjectFile) node1;
				ProjectFile file2 = (ProjectFile) node2;
				if(file1.equals(file2)) {
					continue;
				}
				Package pck1 = containRelationService.findFileBelongToPackage(file1);
				Package pck2 = containRelationService.findFileBelongToPackage(file2);
				if(pck1.equals(pck2)) {
					continue;
				}
				BasicDataForDoubleNodes<Node, Relation> coChangeTimes = getSuperNodeRelationWithSubNodeRelation(pckToPackageCoChanges, pck1, pck2);
				if(coChangeTimes == null) {
					coChangeTimes = new CoChangeRelationDataForDoubleNodes(pck1, pck2);
					cache.add(coChangeTimes);
				}
				coChangeTimes.addChild(coChange);
				if(pck1.equals(coChangeTimes.getNode1())) {
					coChangeTimes.addCodeNodeToNode1(file1);
					coChangeTimes.addCodeNodeToNode2(file2);
				} else {
					coChangeTimes.addCodeNodeToNode2(file1);
					coChangeTimes.addCodeNodeToNode1(file2);
				}
				coChangeTimes.setAllNodesInNode1(new HashSet<>(containRelationService.findPackageContainFiles((Package)coChangeTimes.getNode1())));
				coChangeTimes.setAllNodesInNode2(new HashSet<>(containRelationService.findPackageContainFiles((Package)coChangeTimes.getNode2())));

				Map<Node, BasicDataForDoubleNodes<Node, Relation>> pck1ToCoChanges = pckToPackageCoChanges.getOrDefault(pck1, new HashMap<>());
				pck1ToCoChanges.put(pck2, coChangeTimes);
				pckToPackageCoChanges.put(pck1, pck1ToCoChanges);
			}
			removeSameNodeToCoChangeValuePackages = cache;
			queryPackageCoChangeFromFileCoChangeCache = pckToPackageCoChanges;
		}
		return pckToPackageCoChanges;
	}

	@Override
	public Collection<BasicDataForDoubleNodes<Node, Relation>> queryPackageCoChangeFromFileCoChangeSort(Collection<? extends Relation> fileCoChanges) {
		Collection<BasicDataForDoubleNodes<Node, Relation>> cache = removeSameNodeToCoChangeValuePackages;
		if(cache == null) {
			queryPackageCoChangeFromFileCoChange(fileCoChanges);
			cache = removeSameNodeToCoChangeValuePackages;
		}
		List<BasicDataForDoubleNodes<Node, Relation>> result = new ArrayList<>(cache);
		result.sort((v1, v2) -> {
			return v2.getChildren().size() - v1.getChildren().size();
		});
		return result;
	}
}
