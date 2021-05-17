package cn.edu.fudan.se.multidependency.controller.smell;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.MultipleSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/as/multiplesmell")
public class MultipleSmellController {
    @Autowired
    private MultipleSmellDetector multipleSmellDetector;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/query")
    public String queryMultipleSmell(HttpServletRequest request, @RequestParam("projectid") Long projectId) {
        Project project = projectRepository.findProjectById(projectId);
        if (project != null) {
            request.setAttribute("project", project);
            request.setAttribute("files", multipleSmellDetector.queryMultipleSmellASFiles(false));
        }
        return "as/multiplesmell";
    }

    @GetMapping("/detect")
    public String detectMultipleSmell(HttpServletRequest request, @RequestParam("projectid") Long projectId) {
        Project project = projectRepository.findProjectById(projectId);
        if (project != null) {
            request.setAttribute("project", project);
            request.setAttribute("files", multipleSmellDetector.detectMultipleSmellASFiles(false));
        }
        return "as/multiplesmell";
    }
}
