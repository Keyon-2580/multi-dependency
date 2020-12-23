package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;

import java.util.Map;

public interface DependsOnPackagePairDetector {
    Map<Package, Map<Package, DependsOn>> detectDependsOnPackagePairs();
}
