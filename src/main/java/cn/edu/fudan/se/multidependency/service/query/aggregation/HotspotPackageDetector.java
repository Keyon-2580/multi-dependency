package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackage;

import java.io.OutputStream;
import java.util.Collection;

public interface HotspotPackageDetector {

	Collection<HotspotPackage> detectHotspotPackages();

	Collection<HotspotPackage> detectHotspotPackagesByFileClone();

	Collection<HotspotPackage> detectHotspotPackagesByFileCloneLoc();

	Collection<HotspotPackage> detectHotspotPackagesByFileCoChange();

	Collection<HotspotPackage> detectHotspotPackagesByFileCoChangeTimes();

	void exportHotspotPackages(OutputStream stream);
}
