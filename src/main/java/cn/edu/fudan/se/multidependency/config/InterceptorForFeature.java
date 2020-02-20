package cn.edu.fudan.se.multidependency.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import cn.edu.fudan.se.multidependency.service.spring.OrganizationService;

@Configuration
public class InterceptorForFeature implements HandlerInterceptor {

	@Autowired
	private OrganizationService organizationService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		request.setAttribute("features", organizationService.allFeatures());
		request.setAttribute("testcases", organizationService.allTestCases());
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}
}
