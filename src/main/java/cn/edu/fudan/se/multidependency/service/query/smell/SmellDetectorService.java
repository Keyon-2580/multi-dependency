package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.repository.smell.ModuleRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmellDetectorService {
	
	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;

	@Autowired
	private SmellRepository smellRepository;

	public void createCloneSmells(){
		smellRepository.createCloneSmells();
		smellRepository.createSmellContains();
		smellRepository.setSmellProject();
	}

}
