package cn.edu.fudan.se.multidependency;

import cn.edu.fudan.se.multidependency.model.node.*;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.service.insert.RepositoryService;
import cn.edu.fudan.se.multidependency.service.insert.ThreadService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: keyon
 * @time: 2023/4/21 16:59
 */

public class ConstractData {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertStaticData.class);

    public static void main(String[] args) {
        LOGGER.info("InsertStaticData");
        insert(args);
    }

    public static void insert(String[] args) {
        try {
            YamlUtil.YamlObject yaml = YamlUtil.getYaml(args);

            ThreadService ts = new ThreadService(yaml);

            ts.staticAnalyse();
            ts.javaCcnAnalyse();

            RepositoryService service = RepositoryService.getInstance();


            RepositoryService serializedService =
                    (RepositoryService) FileUtil.readObject(yaml.getSerializePath());
            Nodes preNodes = serializedService.getNodes();
            Relations preRelations = serializedService.getRelations();

            Nodes curNodes = service.getNodes();
            Relations curRelations = service.getRelations();

            Map<String, Map<String, Map<RelationType, RelationWithTimes>>> startNodesIdentifierToNodeRelations = new ConcurrentHashMap<>();
            Map<Node, Map<Node, Map<RelationType, RelationWithTimes>>> startNodesToNodeRelations = preRelations.getStartNodesToNodeRelations();




            //新增Node
            Set<String> identifiersSet = new HashSet<>();
            Map<NodeLabelType, List<Node>> nodeTypeToNodes = preNodes.getAllNodes();
            for(Map.Entry<NodeLabelType, List<Node>> entry : nodeTypeToNodes.entrySet()){
                if(entry.getKey() == NodeLabelType.Package ){
                    for( Node packageNode : entry.getValue()){
                        identifiersSet.add(((Package) packageNode).getDirectoryPath());
                    }
                }else{
                    for(Node node : entry.getValue()){
                        identifiersSet.add(((CodeNode) node).getIdentifier());
                    }
                }
            }

            Map<NodeLabelType, List<Node>> curNodeTypeToNodes = preNodes.getAllNodes();
            List<Node> newNodes = new ArrayList<>();


            for(Map.Entry<NodeLabelType, List<Node>> entry : curNodeTypeToNodes.entrySet()){
                if(entry.getKey() == NodeLabelType.Package ){
                    for( Node packageNode : entry.getValue()){
                        if(!identifiersSet.contains(((Package) packageNode).getDirectoryPath())){
                            newNodes.add(packageNode);
                        }
                    }
                }else{
                    for(Node node : entry.getValue()){
                        if(!identifiersSet.contains(((CodeNode) node).getIdentifier())){
                            newNodes.add(node);
                        }
                    }
                }
            }






            //不变的节点







            LOGGER.info("静态分析节点数：" + service.getNodes().size());
            LOGGER.info("静态分析关系数：" + service.getRelations().size());

            FileUtil.writeObject(yaml.getSerializePath(), service);
            System.exit(0);
        } catch (Exception e) {
            // 所有步骤中有一个出错，都会终止执行
            e.printStackTrace();
            LOGGER.error("插入出错：" + e.getMessage());
        }
    }
}
