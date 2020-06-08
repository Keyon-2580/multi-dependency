package cn.edu.fudan.se.multidependency;

import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.service.nospring.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.nospring.RepositoryService;
import cn.edu.fudan.se.multidependency.service.nospring.ThreadService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import org.neo4j.cypher.internal.compiler.v2_3.No;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InsertOtherData {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertOtherData.class);

    private static final Executor executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        LOGGER.info("InsertOtherData");
        insert(args);
    }

    public static void insert(String[] args) {
        try {
            YamlUtil.YamlObject yaml = YamlUtil.getYaml(args);

            CountDownLatch latchOfStatic = new CountDownLatch(0);
            CountDownLatch latchOfOthers = new CountDownLatch(5);
            ThreadService ts = new ThreadService(yaml, latchOfStatic, latchOfOthers);

            RepositoryService service = RepositoryService.getInstance();
            RepositoryService serializedService = (RepositoryService) FileUtil.readObject(yaml.getSerializePath());
            service.setNodes(serializedService.getNodes());
            service.setRelations(serializedService.getRelations());

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
