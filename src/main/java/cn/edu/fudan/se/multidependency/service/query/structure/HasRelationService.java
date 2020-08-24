package cn.edu.fudan.se.multidependency.service.query.structure;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import cn.edu.fudan.se.multidependency.service.query.data.ProjectStructure;

import java.util.Collection;

public interface HasRelationService {
    Collection<Package> findProjectHasPackages(Project project);

    Collection<Package> findPackageHasPackages(Package pck);

    ProjectStructure projectHasInitialize(Project project);

    PackageStructure packageHasInitialize(Package pck);
}
