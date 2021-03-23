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
}
