package fan.md.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fan.md.model.node.code.CodeFile;
import fan.md.service.StaticCodeService;

@Controller
public class MDController {
	
	
	@Autowired
	private StaticCodeService staticCodeService;
	
//	@Bean
//	private DependsEntityRepoExtractor getEntityRepoExtractor() {
//		
//		return null;
//	}
	
//	@Autowired
//	private PropertyConfig conf;

	@RequestMapping("/")
	@ResponseBody
	public String index() {
		return "hello";
	}
	
	@RequestMapping("/test")
	@ResponseBody
	public void test() {
		CodeFile file = new CodeFile();
		file.setId(1270L);
		staticCodeService.findTypesInFile(file);
	}
	
	@Bean
	public String testBean() {
		CodeFile file = new CodeFile();
		file.setId(1270L);
		staticCodeService.findTypesInFile(file);
		staticCodeService.findAllExtends();
		return "";
	}
	
	
}
