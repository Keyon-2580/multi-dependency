package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.node.hierarchical_clustering.HierarchicalCluster;

public interface HierarchicalClusteringService {
    double calPackageComplexityByCluster(long PackageId);
}
