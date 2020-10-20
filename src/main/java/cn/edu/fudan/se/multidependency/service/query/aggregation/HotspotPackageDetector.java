package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackage;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

public interface HotspotPackageDetector {

	List<HotspotPackage> detectHotspotPackages();

	List<HotspotPackage> detectHotspotPackagesByFileClone();

	List<HotspotPackage> detectHotspotPackagesByFileCloneWithoutEmptyPackage();

	Collection<HotspotPackage> detectHotspotPackagesByFileCloneLoc();

	Collection<HotspotPackage> detectHotspotPackagesByFileCoChange();

	Collection<HotspotPackage> detectHotspotPackagesByFileCoChangeTimes();

	HotspotPackage detectHotspotPackagesByPackageId(long pck1Id, long pck2Id);

	List<HotspotPackage> detectHotspotPackagesByParentId(long parent1Id, long parent2Id);

	void exportHotspotPackages(OutputStream stream);
}
