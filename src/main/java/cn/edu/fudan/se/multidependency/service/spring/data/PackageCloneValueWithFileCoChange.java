package cn.edu.fudan.se.multidependency.service.spring.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Data;

@Data
public class PackageCloneValueWithFileCoChange implements Serializable {

	private static final long serialVersionUID = 1287543734251562271L;
	
	private Package pck1;
	
	private Package pck2;
	
	// 两个克隆节点内部的克隆关系
	protected List<FileCloneWithCoChange> children = new ArrayList<>();
	
	public void sortChildren() {
		children.sort((clone1, clone2) -> {
			return clone2.getCochangeTimes() - clone1.getCochangeTimes();
		});
	}
	
	public void addChild(FileCloneWithCoChange child) {
		this.children.add(child);
	}
	
}
