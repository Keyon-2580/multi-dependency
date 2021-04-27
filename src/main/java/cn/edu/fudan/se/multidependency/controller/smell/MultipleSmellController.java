package cn.edu.fudan.se.multidependency.controller.smell;

import cn.edu.fudan.se.multidependency.service.query.smell.MultipleArchitectureSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/as/multiple")
public class MultipleSmellController {
    @Autowired
    private MultipleArchitectureSmellDetector detector;

    @Autowired
    private NodeService nodeService;

    @GetMapping("/query")
    public String queryMultipleSmell(HttpServletRequest request) {
        request.setAttribute("projects", nodeService.allProjects());
        request.setAttribute("files", detector.queryMultipleSmellASFiles(false));
        return "as/multiple";
    }

    @GetMapping("/detect")
    public String detectMultipleSmell(HttpServletRequest request) {
        request.setAttribute("projects", nodeService.allProjects());
        request.setAttribute("files", detector.detectMultipleSmellASFiles(false));
        return "as/multiple";
    }
}
