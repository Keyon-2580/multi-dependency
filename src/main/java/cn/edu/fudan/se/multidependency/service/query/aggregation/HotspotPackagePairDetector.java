package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackagePair;

import java.util.List;

public interface HotspotPackagePairDetector {

	List<HotspotPackagePair> detectHotspotPackagePairWithDependsOnByProjectId(long projectId);

	HotspotPackagePair detectHotspotPackagePairWithDependsOnByPackageId(long pck1Id, long pck2Id);

}
