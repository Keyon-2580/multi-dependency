package cn.edu.fudan.se.multidependency.utils.clone.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CloneResultWithLocFromCsv {
	private int start;
	private int end;
	private double value;
	private String type;
	private int linesSize1;
	private int linesSize2;
	private int loc1;
	private int loc2;
}
