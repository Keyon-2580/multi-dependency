package cn.edu.fudan.se.multidependency.service.query.structure;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.repository.relation.HasRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import cn.edu.fudan.se.multidependency.service.query.data.ProjectStructure;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class HasRelationServiceImpl implements HasRelationService {
    @Autowired
    HasRepository hasRepository;

    @Autowired
    CacheService cache;

    @Autowired
    private HasRelationService hasRelationService;

    @Autowired
    private ContainRelationService containRelationService;

    Map<Project, Collection<Package>> projectHasPakcagesCache = new ConcurrentHashMap<>();
    @Override
    public Collection<Package> findProjectHasPackages(Project project) {
        Collection<Package> result = projectHasPakcagesCache.getOrDefault(project, hasRepository.findProjectHasPackages(project.getId()));

        return result;
    }

    Map<Project, Collection<Package>> packageHasPakcagesCache = new ConcurrentHashMap<>();
    @Override
    public Collection<Package> findPackageHasPackages(Package pck) {
        Collection<Package> result = packageHasPakcagesCache.getOrDefault(pck, hasRepository.findPackageHasPackages(pck.getId()));
        return result;
    }

    @Override
    public Package findPackageInPackage(Package pck) {
        return hasRepository.findPackageInPackage(pck.getId());
    }

    @Override
    public ProjectStructure projectHasInitialize(Project project) {
        ProjectStructure result = new ProjectStructure(project);
//        System.out.println(hasRepository.findProjectHasPackages(project.getId()));
        List<Package> containNoHasPackages = hasRepository.findProjectHasPackages(project.getId());
        for(Package pck : containNoHasPackages) {
            result.addChild(new PackageStructure(pck));
        }
        return result;
    }

    @Override
    public PackageStructure packageHasInitialize(Package pck) {
        if(pck == null) {
            return null;
        }
        PackageStructure result = new PackageStructure(pck);

        Collection<ProjectFile> files = new ArrayList<>(containRelationService.findPackageContainFiles(pck)); // contain关系的file
//        result.addAllFiles(files);

        Collection<Package> childrenPackage = new ArrayList<>(findPackageHasPackages(pck)); // has关系的package
        for(Package child : childrenPackage) {
            result.addChildPackage(packageHasInitialize(child));
        }

        if(childrenPackage.size() >0 && files.size() > 0){
            Package tmpPck = new Package();
            tmpPck.setId(0 - pck.getId());
            tmpPck.setEntityId(pck.getEntityId());
            tmpPck.setLanguage(pck.getLanguage());
            tmpPck.setDirectoryPath(pck.getDirectoryPath());
            tmpPck.setLines(0);
            tmpPck.setLoc(0);
            tmpPck.setName(pck.getName());
            PackageStructure resultTmp = new PackageStructure(tmpPck);
            resultTmp.addAllFiles(files);
            result.addChildPackage(resultTmp);
        } else {
            result.addAllFiles(files);
        }

        return result;

    }
}
