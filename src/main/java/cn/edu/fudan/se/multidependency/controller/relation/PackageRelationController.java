package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculator;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/relation/package/{packageId}")
public class PackageRelationController {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private MetricCalculator metricCalculator;

    @Autowired
    private StaticAnalyseService staticAnalyseService;

    @GetMapping("")
    public String index(HttpServletRequest request, @PathVariable("packageId") long id){
        Package pck = nodeService.queryPackage(id);
        request.setAttribute("pck",pck);
        request.setAttribute("project",containRelationService.findPackageBelongToProject(pck));
        return"relation/package";
    }

    @GetMapping("/metric")
    @ResponseBody
    public Object metric(@PathVariable("packageId") long id) {
        Package pck = nodeService.queryPackage(id);
        return metricCalculator.calculatePackageMetric(pck);
    }

    @GetMapping("/contain/file")
    @ResponseBody
    public Object contain(@PathVariable("packageId") long id) {
        Package pck = nodeService.queryPackage(id);
        return containRelationService.findPackageContainFiles(pck);
    }

    @GetMapping("/dependsOn")
    @ResponseBody
    public Object dependsOn(@PathVariable("packageId") long id) {
        Package pck = nodeService.queryPackage(id);
        return staticAnalyseService.findPackageDependsOn(pck);
    }

    @GetMapping("/dependedBy")
    @ResponseBody
    public Object dependedOn(@PathVariable("packageId") long id) {
        Package pck = nodeService.queryPackage(id);
        return staticAnalyseService.findPackageDependedOnBy(pck);
    }
}
