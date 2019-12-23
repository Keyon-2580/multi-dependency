package fan.md.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import depends.entity.repo.EntityRepo;
import fan.md.config.PropertyConfig;
import fan.md.model.Language;
import fan.md.model.node.code.CodeFile;
import fan.md.service.DependsCodeExtractor;
import fan.md.service.StaticCodeService;

@Controller
public class MDController {
	
	@Autowired
	private DependsCodeExtractor codeExtractor;
	
	@Autowired
	private StaticCodeService staticCodeService;
	
	
	@Autowired
	private PropertyConfig conf;

//	@Bean
	public EntityRepo getRepo() {
		try {
			return codeExtractor.extractEntityRepo(conf.getCodePath(), Language.valueOf(conf.getLanguage()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
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
