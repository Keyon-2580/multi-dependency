package cn.edu.fudan.se.multidependency.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;

@Configuration
public class InterceptorForTestCase implements HandlerInterceptor {
	
	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
//		request.setAttribute("testCases", featureOrganizationService.allTestCasesGroupByTestCaseGroup());
//		request.setAttribute("features", featureOrganizationService.allFeatures());
//		request.setAttribute("testCaseNoGroup", featureOrganizationService.allTestCases());
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}

}
