package cn.edu.fudan.se.multidependency.utils.clone.data;

import cn.edu.fudan.se.multidependency.utils.clone.CloneUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CloneResultFromCsv {
	private int start;
	private int end;
	private double value;
}
