package cn.edu.fudan.se.multidependency;

import cn.edu.fudan.se.multidependency.service.nospring.RepositoryService;
import cn.edu.fudan.se.multidependency.service.nospring.ThreadService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InsertStaticData {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertStaticData.class);

    private static final Executor executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        LOGGER.info("InsertStaticData");
        insert(args);
    }

    public static void insert(String[] args) {
        try {
            YamlUtil.YamlObject yaml = YamlUtil.getYaml(args);

            CountDownLatch latchOfStatic = new CountDownLatch(1);
            CountDownLatch latchOfOthers = new CountDownLatch(0);
            ThreadService ts = new ThreadService(yaml, latchOfStatic, latchOfOthers);

            executor.execute(ts::staticAnalyse);
            latchOfStatic.await();
            RepositoryService service = RepositoryService.getInstance();
            LOGGER.info("静态分析节点数：" + service.getNodes().size());
            LOGGER.info("静态分析关系数：" + service.getRelations().size());

            FileUtil.writeObject(yaml.getSerializePath(), service);
        } catch (Exception e) {
            // 所有步骤中有一个出错，都会终止执行
            e.printStackTrace();
        }
        System.exit(0);
    }
}
