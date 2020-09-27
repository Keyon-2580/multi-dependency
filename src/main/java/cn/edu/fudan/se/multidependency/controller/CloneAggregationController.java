package cn.edu.fudan.se.multidependency.controller;

import java.io.OutputStream;
import java.util.*;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackage;
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

    @GetMapping(value = {""})
    public String graph() {
        return "cloneaggregation";
    }

    /**
     * 两个包之间的克隆聚合
     * @param threshold
     * @param percentage
     * @return
     */
    @GetMapping("/analysis")
    @ResponseBody
    public int analysisHotspotPackages(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
        Collection<HotspotPackage> result = hotspotPackageDetector.detectHotspotPackages();
        return result.size();
    }

    /**
     * 两个包之间的克隆聚合
     * @param threshold
     * @param percentage
     * @return
     */
    @GetMapping("/show")
    @ResponseBody
    public Collection<HotspotPackage> showHotspotPackages(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
        return hotspotPackageDetector.detectHotspotPackages();
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
