package cn.edu.fudan.se.multidependency.controller;

import java.io.OutputStream;
import java.util.*;

import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackagePair;
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
    private HotspotPackagePairDetector hotspotPackagePairDetector;

    @GetMapping(value = {""})
    public String graph() {
        return "cloneaggregation";
    }

    /**
     * java项目两个包之间的克隆聚合
     * @param threshold
     * @param percentage
     * @return
     */
    @GetMapping("/show/java")
    @ResponseBody
    public List<HotspotPackagePair> showHotspotPackagesOfJava(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
        return hotspotPackagePairDetector.detectHotspotPackagePairWithFileCloneByParentId(-1, -1, "java");
    }

    /**
     * cpp项目两个包之间的克隆聚合
     * @param threshold
     * @param percentage
     * @return
     */
    @GetMapping("/show/cpp")
    @ResponseBody
    public List<HotspotPackagePair> showHotspotPackagesOfCpp(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
        return hotspotPackagePairDetector.detectHotspotPackagePairWithFileCloneByParentId(-1, -1, "cpp");
    }

    @GetMapping("/package/export")
    @ResponseBody
    public void exportSimilarPackages(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.addHeader("Content-Disposition", "attachment;filename=similar_packages.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            OutputStream stream = response.getOutputStream();
            hotspotPackagePairDetector.exportHotspotPackages(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/details")
    public String showDetails(@RequestParam("id1") long id1, @RequestParam("id2") long id2, @RequestParam("path1") String path1, @RequestParam("path2") String path2, @RequestParam("cloneNodes1") int cloneNodes1, @RequestParam("allNodes1") int allNodes1, @RequestParam("cloneNodes2") int cloneNodes2, @RequestParam("allNodes2") int allNodes2, @RequestParam("cloneCochangeTimes") int cloneCochangeTimes, @RequestParam("allCochangeTimes") int allCochangeTimes, @RequestParam("clonePairs") int clonePairs) {
        return "details";
    }
}
