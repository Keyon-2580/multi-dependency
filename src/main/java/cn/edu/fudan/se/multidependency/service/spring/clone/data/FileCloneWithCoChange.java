package cn.edu.fudan.se.multidependency.service.spring.clone.data;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import lombok.Data;

@Data
public class FileCloneWithCoChange {

	private ProjectFile file1;
	
	private ProjectFile file2;
	
	private Clone fileClone;
	
	private CoChange cochange;

	private int cochangeTimes;
	
	public FileCloneWithCoChange(Clone fileClone, CoChange cochange) throws Exception {
		if(fileClone.getCodeNode1().getClass() != ProjectFile.class || fileClone.getCodeNode2().getClass() != ProjectFile.class) {
			throw new Exception();
		}
		this.fileClone = fileClone;
		ProjectFile cloneFile1 = (ProjectFile) fileClone.getCodeNode1();
		ProjectFile cloneFile2 = (ProjectFile) fileClone.getCodeNode2();
		if(cochange == null) {
			this.file1 = cloneFile1;
			this.file2 = cloneFile2;
			cochangeTimes = 0;
			this.cochange = cochange;
		} else {
			ProjectFile cochangeFile1 = cochange.getFile1();
			ProjectFile cochangeFile2 = cochange.getFile2();
			if((cloneFile1.equals(cochangeFile1) && cloneFile2.equals(cochangeFile2)) 
					|| (cloneFile2.equals(cochangeFile1) && cloneFile1.equals(cochangeFile2))) {
				this.file1 = cloneFile1;
				this.file2 = cloneFile2;
				cochangeTimes = cochange.getTimes();
				this.cochange = cochange;
			} else {
				throw new Exception();
			}
		}
	}
	
}
