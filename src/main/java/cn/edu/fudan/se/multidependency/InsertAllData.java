package cn.edu.fudan.se.multidependency;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.service.nospring.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.nospring.InserterForNeo4jServiceFactory;
import cn.edu.fudan.se.multidependency.service.nospring.RepositoryService;
import cn.edu.fudan.se.multidependency.service.nospring.ThreadService;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.TraceStartExtractor;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import cn.edu.fudan.se.multidependency.utils.config.JSONConfigFile;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfigUtil;

public class InsertAllData {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertAllData.class);

    private static final Executor executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {
        LOGGER.info("InsertAllData");
        insert(args);
//		checkMicroserviceTrace(args);
    }

    public static void checkMicroserviceTrace(String[] args) throws Exception {
        YamlUtil.YamlObject yaml = YamlUtil.getYaml(args);
        JSONConfigFile config = ProjectConfigUtil.extract(JSONUtil.extractJSONObject(new File(yaml.getProjectsConfig())));
        LOGGER.info("输出trace");
        new TraceStartExtractor(InserterForNeo4jServiceFactory.analyseDynamicLogs(config.getDynamicsConfig())).addNodesAndRelations();
    }

    public static void insert(String[] args) {
        try {
            YamlUtil.YamlObject yaml = YamlUtil.getYaml(args);

            CountDownLatch latchOfStatic = new CountDownLatch(1);
            CountDownLatch latchOfOthers = new CountDownLatch(5);
            ThreadService ts = new ThreadService(yaml, latchOfStatic, latchOfOthers);

            executor.execute(ts::staticAnalyse);
            latchOfStatic.await();
            RepositoryService service = RepositoryService.getInstance();
            LOGGER.info("静态分析节点数：" + service.getNodes().size());
            LOGGER.info("静态分析关系数：" + service.getRelations().size());
            executor.execute(ts::msDependAnalyse);
            executor.execute(ts::dynamicAnalyse);
            executor.execute(ts::gitAnalyse);
            executor.execute(ts::cloneAnalyse);
            executor.execute(ts::libAnalyse);

            latchOfOthers.await();
            InserterForNeo4j repository = RepositoryService.getInstance();
            repository.setDatabasePath(yaml.getNeo4jDatabasePath());
            repository.setDelete(yaml.isDeleteDatabase());
            repository.insertToNeo4jDataBase();
        } catch (Exception e) {
            // 所有步骤中有一个出错，都会终止执行
            e.printStackTrace();
        }
        System.exit(0);
    }

}
