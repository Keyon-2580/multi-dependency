package cn.edu.fudan.se.multidependency.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ZTreeUtil {

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ZTreeNode implements Serializable {
		
		public static final long DEFAULT_ID = -1;
		
		private static final long serialVersionUID = -8811436922800805233L;
		
		public ZTreeNode(Node node) {
			this(node, false);
		}
		
		public ZTreeNode(Node node, Boolean open) {
			this.id = node.getId();
			this.name = String.join(":", node.getNodeType().toString(), node.getName());
			this.open = open;
		}
		
		public ZTreeNode(long id, String name, Boolean open) {
			this.name = name;
			this.open = open;
		}
		
		private long id;
		private String name;
		private Boolean open;
		
		private List<ZTreeNode> children = new ArrayList<>();
		
		public void addChild(Node node) {
			ZTreeNode child = new ZTreeNode(node);
			addChild(child);
		}
		
		public void addChild(ZTreeNode ztree) {
			this.children.add(ztree);
			children.sort(new Comparator<ZTreeNode>() {
				@Override
				public int compare(ZTreeNode o1, ZTreeNode o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
		public JSONObject toJSON() {
			JSONObject result = new JSONObject();
			result.put("name", name);
			result.put("id", id);
			if(open != null) {
				result.put("open", open);
			}
			if(!children.isEmpty()) {
				JSONArray childrenJSON = new JSONArray();
				for(ZTreeNode child : children) {
					childrenJSON.add(child.toJSON());
				}
				result.put("children", childrenJSON);
			}
			return result;
		}
	}
	
}
