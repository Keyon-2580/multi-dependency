package cn.edu.fudan.se.multidependency.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {
	
	@Autowired
	private InterceptorForProject interceptorForProject;
	
	@Autowired
	private InterceptorForFeature interceptorForFeature;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		InterceptorRegistration registrationForProject = registry.addInterceptor(interceptorForProject);
		InterceptorRegistration registrationForFeature = registry.addInterceptor(interceptorForFeature);
		
        registrationForProject.addPathPatterns("/**");
        registrationForFeature.addPathPatterns("/feature/**");
	}
	
}
