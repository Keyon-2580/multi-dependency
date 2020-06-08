package cn.edu.fudan.se.multidependency.service.nospring;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import lombok.Getter;
import lombok.Setter;

public final class RepositoryService implements InserterForNeo4j, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryService.class);

    private static final long serialVersionUID = 4968121323297165683L;

    protected transient BatchInserterService batchInserterService = BatchInserterService.getInstance();

    private static RepositoryService repository = new RepositoryService();

    @Setter
    private String databasePath;

    @Setter
    private boolean delete;

    private RepositoryService() {
    }

    public static RepositoryService getInstance() {
        return repository;
    }

    @Getter
    @Setter
    private Nodes nodes = new Nodes();

    @Getter
    @Setter
    private Relations relations = new Relations();

    @Override
    public void insertToNeo4jDataBase() throws Exception {
        LOGGER.info("start to store datas to database");
        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        LOGGER.info("总计节点数：" + nodes.size());
        LOGGER.info("总计关系数：" + relations.size());
        LOGGER.info("开始时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
        batchInserterService.init(databasePath, delete);
        batchInserterService.insertNodes(nodes);
        batchInserterService.insertRelations(relations);
        closeBatchInserter();
        LOGGER.info("结束时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
    }

    @Override
    public boolean addRelation(Relation relation) {
        this.relations.addRelation(relation);
        return true;
    }

    private void closeBatchInserter() {
        if (this.batchInserterService != null) {
            this.batchInserterService.close();
        }
    }

    @Override
    public boolean addNode(Node node, Project inProject) {
        this.nodes.addNode(node, inProject);
        return true;
    }

    @Override
    public boolean existNode(Node node) {
        return this.nodes.existNode(node);
    }

    @Override
    public boolean existRelation(Relation relation) {
        return this.relations.existRelation(relation);
    }
}
