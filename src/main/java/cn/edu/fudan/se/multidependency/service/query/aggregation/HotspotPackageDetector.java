package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackage;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

public interface HotspotPackageDetector {

	Collection<HotspotPackage> detectHotspotPackages();

	Collection<HotspotPackage> detectHotspotPackagesByFileClone();

	Collection<HotspotPackage> detectHotspotPackagesByFileCloneLoc();

	Collection<HotspotPackage> detectHotspotPackagesByFileCoChange();

	Collection<HotspotPackage> detectHotspotPackagesByFileCoChangeTimes();

	List<AggregationClone> quickDetectHotspotPackages(long parent1Id, long parent2Id);

	void exportHotspotPackages(OutputStream stream);
}
