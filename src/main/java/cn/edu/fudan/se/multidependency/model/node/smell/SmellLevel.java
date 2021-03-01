package cn.edu.fudan.se.multidependency.model.node.smell;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.node.code.Type;

public enum SmellLevel {
	Module(SmellLevel.str_Module),
	Package(SmellLevel.str_Package),
	File(SmellLevel.str_File),
	Function(SmellLevel.str_Function),
	Type(SmellLevel.str_Type),
	Snippet(SmellLevel.str_Snippet),
	MultipleLevel(SmellLevel.str_MultipleLevel);
	
	public static SmellLevel getNodeSmellLevel(Node node) {
		if(node instanceof ProjectFile) {
			return File;
		} else if(node instanceof Function) {
			return Function;
		} else if(node instanceof Type) {
			return Type;
		} else if(node instanceof Snippet) {
			return Snippet;
		}else if(node instanceof Package) {
			return Package;
		}
		return null;
	}

	public static final String str_Module = "Module";
	public static final String str_Package = "Package";
	public static final String str_File = "File";
	public static final String str_Function = "Function";
	public static final String str_Type = "Type";
	public static final String str_Snippet = "Snippet";
	public static final String str_MultipleLevel = "MultipleLevel";

	private String name;

	SmellLevel(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}

}
