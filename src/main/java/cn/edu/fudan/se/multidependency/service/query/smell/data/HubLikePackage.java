package cn.edu.fudan.se.multidependency.service.query.smell.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import cn.edu.fudan.se.multidependency.model.node.Package;

@Data
@AllArgsConstructor
public class HubLikePackage {
	
	private Package pck;
	
	private int fanOut;
	
	private int fanIn;
	
	private int loc;
	
}
