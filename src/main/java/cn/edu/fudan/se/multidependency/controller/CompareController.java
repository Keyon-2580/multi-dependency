package cn.edu.fudan.se.multidependency.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CompareController {

    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    public String index() {
        return "compare";
    }
}
