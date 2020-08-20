package cn.edu.fudan.se.multidependency.utils.clone.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CloneResultWithLocFromCsv extends CloneResultFromCsv {
	public CloneResultWithLocFromCsv(int start, int end, double value, String type, int linesSize1, int linesSize2,
		int loc1, int loc2) {
	super(start, end, value, type);
	this.linesSize1 = linesSize1;
	this.linesSize2 = linesSize2;
	this.loc1 = loc1;
	this.loc2 = loc2;
}
	private int linesSize1;
	private int linesSize2;
	private int loc1;
	private int loc2;
}
