package cn.edu.fudan.se.multidependency.service.query.as;

import java.io.OutputStream;

public interface ArchitectureSmellDetector {
	
	Object multipleASFiles(boolean removeNoASFile);
	
	void printMultipleASFiles(OutputStream stream);
}
