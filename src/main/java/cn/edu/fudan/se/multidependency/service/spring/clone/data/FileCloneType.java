package cn.edu.fudan.se.multidependency.service.spring.clone.data;

import lombok.Data;

@Data
public class FileCloneType {
	
	private String type = "";
	
	private boolean differentPackage = false;
	
}