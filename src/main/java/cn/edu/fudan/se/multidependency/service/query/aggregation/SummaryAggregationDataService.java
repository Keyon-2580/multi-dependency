package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.RelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChange;

import java.util.*;

public interface SummaryAggregationDataService extends AggregationDataService{

    /**
     * 根据文件间的克隆找出包间的克隆
     * @param fileClones
     * @return
     */
    Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> queryPackageCloneFromFileClone(Collection<? extends Relation> fileClones);

    /**
     * 根据文件间的克隆找出包间的克隆，排序
     * @param fileClones
     * @return
     */
    Collection<RelationDataForDoubleNodes<Node, Relation>> queryPackageCloneFromFileCloneSort(Collection<? extends Relation> fileClones);

//
//    default Collection<RelationDataForDoubleNodes<Node, Relation>> queryPackageCloneFromFileClone(Collection<? extends Relation> fileClones, List<? extends Node> pcks) {
//        if(pcks == null || pcks.isEmpty()) {
//            return new ArrayList<>();
//        }
//        List<RelationDataForDoubleNodes<Node, Relation>> result = new ArrayList<>();
//        for(int i = 0; i < pcks.size(); i++) {
//            for(int j = i + 1; j < pcks.size(); j++) {
//                RelationDataForDoubleNodes<Node, Relation> queryResult = queryPackageCloneFromFileCloneSort(fileClones, (Package)pcks.get(i), (Package)pcks.get(j));
//                if(queryResult != null){
//                    result.add(queryResult);
//                }
//            }
//        }
//
//        return result;
//    }

//    /**
//     * 两个包之间的文件级克隆的聚合，两个包之间不分先后顺序
//     * @param fileClones
//     * @param pck1
//     * @param pck2
//     * @return
//     */
//    default RelationDataForDoubleNodes<Node, Relation> queryPackageCloneFromFileCloneSort(Collection<? extends Relation> fileClones, Package pck1, Package pck2) {
//        Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> packageClones = queryPackageCloneFromFileClone(fileClones);
//        Map<Node, RelationDataForDoubleNodes<Node, Relation>> map = packageClones.getOrDefault(pck1, new HashMap<>());
//        RelationDataForDoubleNodes<Node, Relation> result = map.get(pck2);
//        if(result == null) {
//            map = packageClones.getOrDefault(pck2, new HashMap<>());
//            result = map.get(pck1);
//        }
//        if(result != null) {
//            result.sortChildren();
//        }
//        return result;
//    }

    /**
     * 列出包克隆中，包中克隆文件的co-change情况
     * @param fileClones
     * @param pck1
     * @param pck2
     * @return
     * @throws Exception
     */
    PackageCloneValueWithFileCoChange queryPackageCloneWithFileCoChange(Collection<? extends Relation> fileClones, Package pck1, Package pck2) throws Exception;

    /**
     * 根据文件间的克隆找出包间的克隆
     * @param fileCoChanges
     * @return
     */
    Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> queryPackageCoChangeFromFileCoChange(Collection<? extends Relation> fileCoChanges);

    /**
     * 根据文件间的co-chage找出包间的co-change，排序
     * @param fileCoChanges
     * @return
     */
    Collection<RelationDataForDoubleNodes<Node, Relation>> queryPackageCoChangeFromFileCoChangeSort(Collection<? extends Relation> fileCoChanges);


//    default Collection<RelationDataForDoubleNodes<Node, Relation>> queryPackageCoChangeFromFileCoChange(Collection<? extends Relation> fileCoChanges, List<Node> pcks) {
//        if(pcks == null || pcks.isEmpty()) {
//            return new ArrayList<>();
//        }
//        List<RelationDataForDoubleNodes<Node, Relation>> result = new ArrayList<>();
//        for(int i = 0; i < pcks.size(); i++) {
//            for(int j = i + 1; j < pcks.size(); j++) {
//                RelationDataForDoubleNodes<Node, Relation> queryResult = queryPackageCoChangeFromFileCoChangeSort(fileCoChanges, pcks.get(i), pcks.get(j));
//                if(queryResult != null){
//                    result.add(queryResult);
//                }
//            }
//        }
//
//        return result;
//    }

//    /**
//     * 两个节点（包）之间的关系（cochange）聚合，两个节点之间不分先后顺序
//     * @param fileCoChanges
//     * @param pck1
//     * @param pck2
//     * @return
//     */
//    default RelationDataForDoubleNodes<Node, Relation> queryPackageCoChangeFromFileCoChangeSort(Collection<? extends Relation> fileCoChanges, Node pck1, Node pck2) {
//        Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> packageClones = queryPackageCoChangeFromFileCoChange(fileCoChanges);
//        Map<Node, RelationDataForDoubleNodes<Node, Relation>> map = packageClones.getOrDefault(pck1, new HashMap<>());
//        RelationDataForDoubleNodes<Node, Relation> result = map.get(pck2);
//        if(result == null) {
//            map = packageClones.getOrDefault(pck2, new HashMap<>());
//            result = map.get(pck1);
//        }
//        if(result != null) {
//            result.sortChildren();
//        }
//        return result;
//    }





    default Collection<RelationDataForDoubleNodes<Node, Relation>> querySuperNodeRelationFromSubNodeRelation(Collection<Relation> subNodeRelations, List<Node> pcks) {
        if(pcks == null || pcks.isEmpty()) {
            return new ArrayList<>();
        }
        List<RelationDataForDoubleNodes<Node, Relation>> result = new ArrayList<>();
        for(int i = 0; i < pcks.size(); i++) {
            for(int j = i + 1; j < pcks.size(); j++) {
                RelationDataForDoubleNodes<Node, Relation> queryResult = querySuperNodeRelationFromSubNodeRelationSort(subNodeRelations, pcks.get(i), pcks.get(j));
                if(queryResult != null){
                    result.add(queryResult);
                }
            }
        }

        return result;
    }

    /**
     * 两个节点（包）之间的关系（cochange）聚合，两个节点之间不分先后顺序
     * @param subNodeRelations
     * @param pck1
     * @param pck2
     * @return
     */
    default RelationDataForDoubleNodes<Node, Relation> querySuperNodeRelationFromSubNodeRelationSort(Collection<? extends Relation> subNodeRelations, Node pck1, Node pck2) {
        Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> packageClones = new HashMap<>();
        if(subNodeRelations == null || subNodeRelations.isEmpty())
            return null;
        if(((List<Relation>)subNodeRelations).get(0) instanceof Clone)
            packageClones = queryPackageCloneFromFileClone(subNodeRelations);
        else if(((List<Relation>)subNodeRelations).get(0) instanceof CoChange){
            packageClones = queryPackageCoChangeFromFileCoChange(subNodeRelations);
        }else
            return null;
        Map<Node, RelationDataForDoubleNodes<Node, Relation>> map = packageClones.getOrDefault(pck1, new HashMap<>());
        RelationDataForDoubleNodes<Node, Relation> result = map.get(pck2);
        if(result == null) {
            map = packageClones.getOrDefault(pck2, new HashMap<>());
            result = map.get(pck1);
        }
        if(result != null) {
            result.sortChildren();
        }
        return result;
    }

}
