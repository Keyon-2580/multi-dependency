package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.smell.data.SimilarComponents;

public interface SimilarComponentsDetector {

	/**
	 * 检测文件的相似构件
	 */
	Map<Long, List<SimilarComponents<ProjectFile>>> getFileSimilarComponents();

	/**
	 * 检测包的相似构件
	 */
	Map<Long, List<SimilarComponents<Package>>> getPackageSimilarComponents();

	/**
	 * 检测文件的相似构件
	 */
	Map<Long, List<SimilarComponents<ProjectFile>>> detectFileSimilarComponents();

	/**
	 * 检测包的相似构件
	 */
	Map<Long, List<SimilarComponents<Package>>> detectPackageSimilarComponents();
}
