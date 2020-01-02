package cn.edu.fudan.se.multidependency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = {"cn.edu.fudan.se.multidependency.repository"})
public class MultipleDependencyApp {
	
	public static void main(String[] args) {
//		InsertDataMain.insert();
		SpringApplication.run(MultipleDependencyApp.class, args);
	}
	
}
