package cn.edu.fudan.se.multidependency.service.query.data;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.repository.relation.HasRepository;
import cn.edu.fudan.se.multidependency.service.query.structure.HasRelationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProjectStructure {

    @Autowired
    private HasRelationService hasRelationService;

    @Setter
    @Getter
    private Project project;

    @Setter
    @Getter
    private List<PackageStructure> children = new ArrayList<>();

    public ProjectStructure(Project project, List<PackageStructure> children) {
        this.project = project;
        this.children = children;
    }

    public ProjectStructure(Project project) {
        this.project = project;
    }

    public void addChild(PackageStructure child) {
        this.children.add(child);
    }


}
