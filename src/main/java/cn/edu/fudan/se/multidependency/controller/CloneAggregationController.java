package cn.edu.fudan.se.multidependency.controller;

import java.util.*;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    @GetMapping("/package")
    @ResponseBody
    public Collection<HotspotPackage> hotspotPackages(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
        return hotspotPackageDetector.detectHotspotPackages();
    }
}
