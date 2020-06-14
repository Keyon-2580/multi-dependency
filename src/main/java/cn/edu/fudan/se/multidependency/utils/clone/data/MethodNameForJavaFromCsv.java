package cn.edu.fudan.se.multidependency.utils.clone.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Deprecated
public class MethodNameForJavaFromCsv extends FilePathFromCsv {
	private String projectName;
	private String packageName;
	private String className;
	private String functionSimpleName;
	private List<String> parameterTypes = new ArrayList<>();
	public void addParameterType(String type) {
		this.parameterTypes.add(type);
	}
	private String getFunctionName() {
		StringBuilder builder = new StringBuilder();
		if(!StringUtils.isBlank(packageName)) {
			builder.append(packageName);
			builder.append(".");
		}
		builder.append(className);
		builder.append(".");
		builder.append(functionSimpleName);
		return builder.toString();
	}
	public int countOfParameterTypes() {
		return parameterTypes.size();
	}
	public String getFunctionFullName() {
		StringBuilder builder = new StringBuilder();
		builder.append(getFunctionName());
		builder.append("(");
		for(int i = 0; i < this.parameterTypes.size(); i++) {
			if(i != 0) {
				builder.append(",");
			}
			builder.append(parameterTypes.get(i));
		}
		builder.append(")");
		return builder.toString();
	}
}