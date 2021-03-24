package cn.edu.fudan.se.multidependency.service.query;

import java.util.HashMap;
import java.util.Map;

public class ComboPcks {

    public static Map<String, Boolean> listOfPcks(){
        Map<String, Boolean> selectedPcks = new HashMap<>();
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/service/", true);
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/cql3/", false);
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/cql3/functions/", true);
        selectedPcks.put("/cassandra/src/java/org/apache/cassandra/cql3/statements/", true);
        return selectedPcks;
    }

    public static Map<String, Boolean> listOfPcksForAtlas(){
        Map<String, Boolean> selectedPcks = new HashMap<>();
        selectedPcks.put("/atlas/addons/", true);
        selectedPcks.put("/atlas/authorization/src/main/java/org/apache/atlas/authorize/", true);
        selectedPcks.put("/atlas/client/", true);
        selectedPcks.put("/atlas/common/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/graphdb/", true);
        selectedPcks.put("/atlas/intg/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/notification/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/plugin-classloader/src/main/java/org/apache/atlas/plugin/classloader/", true);
        selectedPcks.put("/atlas/repository/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/server-api/src/main/java/org/apache/atlas/", true);
        selectedPcks.put("/atlas/tools/", true);
        selectedPcks.put("/atlas/webapp/src/main/java/org/apache/atlas/", true);
        return selectedPcks;
    }
}
