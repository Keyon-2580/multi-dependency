package cn.edu.fudan.se.multidependency.config;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneRelationNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;

public class Constant {
	public static final int SIZE_OF_PAGE = 15;

	private static final Map<String, Class<? extends CloneRelationNode>> CLONE_CLASS_MAP = new HashMap<>();
	
	static {
		CLONE_CLASS_MAP.put("file", ProjectFile.class);
		CLONE_CLASS_MAP.put("function", Function.class);
	}
	
	public static Class<? extends CloneRelationNode> cloneLevelToClass(String level) {
		return CLONE_CLASS_MAP.getOrDefault(level, ProjectFile.class);
	}
}
