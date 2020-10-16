package cn.edu.fudan.se.multidependency.controller;

import java.io.OutputStream;
import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.clone.AggregationCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackage;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.RelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/cloneaggregation")
public class CloneAggregationController {

    @Autowired
    private HotspotPackageDetector hotspotPackageDetector;

    @Autowired
    private AggregationCloneRepository aggregationCloneRepository;

    @Autowired
    private HasRelationService hasRelationService;

    @Autowired
    private CoChangeRepository  coChangeRepository;

    @Autowired
    private ModuleCloneRepository moduleCloneRepository;

    @GetMapping(value = {""})
    public String graph() {
        return "cloneaggregation";
    }

//    /**
//     * 两个包之间的克隆聚合
//     * @param threshold
//     * @param percentage
//     * @return
//     */
//    @GetMapping("/analysis")
//    @ResponseBody
//    public int analysisHotspotPackages(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
//        Collection<HotspotPackage> result = hotspotPackageDetector.detectHotspotPackages();
//        return result.size();
//    }

    /**
     * 两个包之间的克隆聚合
     * @param threshold
     * @param percentage
     * @return
     */
//    @GetMapping("/show")
//    @ResponseBody
//    public Collection<HotspotPackage> showHotspotPackages(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
//        return hotspotPackageDetector.detectHotspotPackages();
//    }
    @GetMapping("/show")
    @ResponseBody
    public List<HotspotPackage> showHotspotPackages(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
        List<HotspotPackage> result = getHotspotPackages(-1, -1);
        result.sort((d1, d2) -> {
            int allNodes1 = d1.getAllNodes1() + d1.getAllNodes2();
            int cloneNodes1 = d1.getRelationNodes1() + d1.getRelationNodes2();
            double percentageThreshold1 = (cloneNodes1 + 0.0) / allNodes1;
            int allNodes2 = d2.getAllNodes1() + d2.getAllNodes2();
            int cloneNodes2 = d2.getRelationNodes1() + d2.getRelationNodes2();
            double percentageThreshold2 = (cloneNodes2 + 0.0) / allNodes2;
            if(percentageThreshold1 < percentageThreshold2) {
                return 1;
            }
            else if(percentageThreshold1 == percentageThreshold2) {
                return Integer.compare(cloneNodes2, cloneNodes1);
            }
            else {
                return -1;
            }
        });
        return result;
    }

    public List<HotspotPackage> getHotspotPackages(long parent1Id, long parent2Id) {
        List<HotspotPackage> result = new ArrayList<>();
        List<AggregationClone> aggregationClones = aggregationCloneRepository.findAggregationClone(parent1Id, parent2Id);

        for(AggregationClone aggregationClone : aggregationClones) {
            CoChange packageCoChanges = null;
            ModuleClone packageCloneCoChanges = null;
            if(parent1Id > -1 && parent2Id > -1){
                packageCoChanges = coChangeRepository.findModuleCoChange(aggregationClone.getStartNode().getId(), aggregationClone.getEndNode().getId());
                packageCloneCoChanges = moduleCloneRepository.findModuleClone(aggregationClone.getStartNode().getId(), aggregationClone.getEndNode().getId());
            }

            List<HotspotPackage> childrenHotspotPackages = getHotspotPackages(aggregationClone.getNode1().getId(), aggregationClone.getNode2().getId());
            RelationDataForDoubleNodes<Node, Relation> relationDataForDoubleNodes = new RelationDataForDoubleNodes<Node, Relation>(aggregationClone.getNode1(), aggregationClone.getNode2());
            HotspotPackage hotspotPackage = new HotspotPackage(relationDataForDoubleNodes);
            hotspotPackage.setClonePairs(aggregationClone.getClonePairs());
            if(packageCoChanges != null){
                hotspotPackage.setPackageCochangeTimes(packageCoChanges.getTimes());
            }else {
                hotspotPackage.setPackageCochangeTimes(0);
            }

            if(packageCloneCoChanges != null){
                hotspotPackage.setPackageCloneCochangeTimes(packageCloneCoChanges.getModuleCloneCochangeTimes());
            }else {
                hotspotPackage.setPackageCloneCochangeTimes(0);
            }

            hotspotPackage.setData(aggregationClone.getAllNodesInNode1(), aggregationClone.getAllNodesInNode2(), aggregationClone.getNodesInNode1(), aggregationClone.getNodesInNode2());
            Collection<Package> childrenPackage1 = hasRelationService.findPackageHasPackages(hotspotPackage.getPackage1());
            Collection<Package> childrenPackage2 = hasRelationService.findPackageHasPackages(hotspotPackage.getPackage2());
            for(HotspotPackage childHotspotPackage : childrenHotspotPackages) {
                hotspotPackage.addHotspotChild(childHotspotPackage);
                childrenPackage1.remove(childHotspotPackage.getPackage1());
                childrenPackage2.remove(childHotspotPackage.getPackage2());
            }
            for(Package childPackage1 : childrenPackage1) {
                hotspotPackage.addOtherChild1(childPackage1);
            }
            for(Package childPackage2 : childrenPackage2) {
                hotspotPackage.addOtherChild2(childPackage2);
            }
            result.add(hotspotPackage);
        }
        return result;
    }
    @GetMapping("/package/export")
    @ResponseBody
    public void exportSimilarPackages(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.addHeader("Content-Disposition", "attachment;filename=similar_packages.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            OutputStream stream = response.getOutputStream();

            hotspotPackageDetector.exportHotspotPackages(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
