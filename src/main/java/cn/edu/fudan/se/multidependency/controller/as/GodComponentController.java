package cn.edu.fudan.se.multidependency.controller.as;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/as/god")
public class GodComponentController {
	
	@GetMapping("")
	public String godComponent() {
		return "as/god";
	}

}
