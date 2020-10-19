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

	List<HotspotPackage> quickDetectHotspotPackages(long parent1Id, long parent2Id);

	void exportHotspotPackages(OutputStream stream);
}
