package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.coupling.HierarchicalClusteringServiceImpl;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/hierarchical_clustering")
public class HierarchicalClusteringController {
    @Autowired
    HierarchicalClusteringServiceImpl hierarchicalClusteringService;

    @GetMapping("/package/{packageId}")
    public String index(HttpServletRequest request, @PathVariable("packageId") long packageId) {
        request.setAttribute("packageId", packageId);
        return "hierarchical_clustering/package_view";
    }

    @GetMapping("/get_package_view")
    @ResponseBody
    public JSONObject containType(@RequestParam("packageId") long packageId) {
        return hierarchicalClusteringService.getPackageClusteringOverview(packageId);
    }
}
