package cn.edu.fudan.se.multidependency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
//(exclude = {Neo4jDataAutoConfiguration.class, Neo4jRepositoriesAutoConfiguration.class})
@EnableNeo4jRepositories(basePackages = {"cn.edu.fudan.se.multidependency.repository"})
public class MultipleDependencyApp {
	
	public static void main(String[] args) {
//		InsertDataMain.insert(args);
		SpringApplication.run(MultipleDependencyApp.class, args);
	}
	
}
