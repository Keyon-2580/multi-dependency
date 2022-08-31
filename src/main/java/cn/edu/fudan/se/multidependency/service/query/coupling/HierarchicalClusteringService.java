package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.node.hierarchical_clustering.HierarchicalCluster;
import com.alibaba.fastjson.JSONObject;

public interface HierarchicalClusteringService {
    double calPackageComplexityByCluster(long PackageId);

    JSONObject getPackageClusteringOverview(long packageId);
}
