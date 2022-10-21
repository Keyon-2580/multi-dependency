package cn.edu.fudan.se.multidependency.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class GraphLayoutUtil {
    private Map<Integer, JSONObject> id2Nodes;
    private Map<String, List<Integer>> sccMap;
    private Map<Integer, String> inverseSccMap = new HashMap<>();
    private Map<String, Integer> idMap;
    private List<List<Integer>> graph;

    private Map<String, List<Integer>> scc2Nodes;
    private JSONArray nodes;
    private JSONArray edges;
    private int n;
    private boolean solved;
    private int sccCount;
    private int id;
    private boolean[] visited;
    private int[] ids;
    private int[] low;
    private int[] sccs;
    private Deque<Integer> stack;
    private static final int UNVISITED = -1;

    private List<Pair<String, String>> mergeGraph() {
        List<Pair<String, String>> edgeList = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : sccMap.entrySet()) {
//            List<Integer> nodes = entry.getValue();
            Set<Integer> sccNodes = new HashSet<>(entry.getValue());
            if (sccNodes.size() != 1) {
                for (int node : sccNodes) {
                    // handle scc out edges
                    List<Integer> adjList = graph.get(node);
                    for (int adj : adjList) {
                        if (!sccNodes.contains(adj)) {
                            // if adj node is not in the same scc
                            edgeList.add(Pair.of(entry.getKey(), id2Nodes.get(adj).getString("id")));
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, List<Integer>> entry : sccMap.entrySet()) {
            // handle scc in edges
            List<Integer> nodes = entry.getValue();
            if (nodes.size() == 1) {
                int node = nodes.get(0);
                List<Integer> adjList = graph.get(nodes.get(0));
                for (int adj : adjList) {
                    if (inverseSccMap.containsKey(adj)) {
                        // target node is in a scc, add an edge connected to that scc
                        edgeList.add(Pair.of(id2Nodes.get(node).getString("id"), inverseSccMap.get(adj)));
                    } else {
                        edgeList.add(Pair.of(id2Nodes.get(node).getString("id"), id2Nodes.get(adj).getString("id")));
                    }
                }
            }
        }
        return edgeList;
    }
    private List<List<Integer>> createGraph(int n) {
        List<List<Integer>> graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
        return graph;
    }
    public GraphLayoutUtil(JSONArray nodes, JSONArray edges) {
        sccMap = new HashMap<>();
        id2Nodes = new HashMap<>();
        idMap = new HashMap<>();
        if (nodes == null || edges == null) {
            throw new IllegalArgumentException("Graph cannot be null!");
        }
        this.nodes = nodes;
        this.edges = edges;
        this.n = nodes.size();
        graph = createGraph(n);
        for (int i = 0; i < n; i++) {
            JSONObject node = nodes.getJSONObject(i);
            id2Nodes.put(i, node);
            idMap.put((String) node.get("id"), i);
        }
        for (int i = 0; i < edges.size(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            String sourceId = edge.getString("source");
            String targetId = edge.getString("target");
            int sourceGraphId = idMap.get(sourceId);
            int targetGraphId = idMap.get(targetId);
            graph.get(sourceGraphId).add(targetGraphId);
        }
    }

    @SuppressWarnings("Duplicates")
    private void tarjanScc() {
        if (solved) {
            return;
        }
        ids = new int[n];
        low = new int[n];
        sccs = new int[n];
        visited = new boolean[n];
        stack = new ArrayDeque<>();
        Arrays.fill(ids, UNVISITED);
        for (int i = 0; i < n; i++) {
            if (ids[i] == UNVISITED) {
                dfs(i);
            }
        }

        solved = true;
        for (int i = 0; i < n; i++) {
            if (!sccMap.containsKey("scc"+sccs[i]))
                sccMap.put("scc"+sccs[i], new ArrayList<>());
            sccMap.get("scc"+sccs[i]).add(i);
        }
        for (Map.Entry<String, List<Integer>> entry : sccMap.entrySet()) {
            List<Integer> nodes = entry.getValue();
            if (nodes.size() != 1) {
                // scc
                for (int n : nodes) {
                    inverseSccMap.put(n, entry.getKey());
                }
            }
        }
    }

    public void printSccResult() {
        System.out.printf("Number of Strongly Connected Components: %d\n", sccCount);
        for (Map.Entry<String, List<Integer>> entry : sccMap.entrySet()) {
            String sccKey = entry.getKey();
            List<String> nodes =
                    entry.getValue().stream().map(id -> (String)id2Nodes.get(id).get("id")).collect(Collectors.toList());
            System.out.println(sccKey + ": " + nodes + " form a Strongly Connected Component.");
        }
//        for (List<Integer> scc : sccMap.values()) {
//            List<String> nodes =
//                    scc.stream().map(id -> (String)id2Nodes.get(id).get("id")).collect(Collectors.toList());
//            System.out.println("Nodes: " + nodes + " form a Strongly Connected Component.");
//        }
    }
    private void dfs(int at) {
        ids[at] = low[at] = id++;
        stack.push(at);
        visited[at] = true;
        for (int to : graph.get(at)) {
            if (ids[to] == UNVISITED) {
                dfs(to);
            }
            if (visited[to]) {
                low[at] = min(low[at], low[to]);
            }
      /*
       TODO(william): investigate whether the proper way to update the lowlinks
       is the following bit of code. From my experience this doesn't seem to
       matter if the output is placed in a separate output array, but this needs
       further investigation.

       if (ids[to] == UNVISITED) {
         dfs(to);
         low[at] = min(low[at], low[to]);
       }
       if (visited[to]) {
         low[at] = min(low[at], ids[to]);
       }
      */

        }

        // On recursive callback, if we're at the root node (start of SCC)
        // empty the seen stack until back to root.
        if (ids[at] == low[at]) {
            for (int node = stack.pop(); ; node = stack.pop()) {
                visited[node] = false;
                sccs[node] = sccCount;
                if (node == at) break;
            }
            sccCount++;
        }
    }

    private List<List<String>> groupTopologicalSort(List<Pair<String, String>> edges) {
        List<List<String>> levels = new ArrayList<>();
        Map<String, Set<String>> nodesInEdges = new HashMap<>();
        for (Pair<String, String> edge : edges) {
            String source = edge.getLeft();
            String target = edge.getRight();
            if (source.equals(target))  continue;
            if (!nodesInEdges.containsKey(source)) {
                nodesInEdges.put(source, new HashSet<>());
            }
            if (!nodesInEdges.containsKey(target)) {
                Set<String> tmp = new HashSet<>();
                tmp.add(source);
                nodesInEdges.put(target, tmp);
            } else {
                nodesInEdges.get(target).add(source);
            }
        }
        while (true) {
            Set<String> dependencyLess = new HashSet<>();
            for (Map.Entry<String, Set<String>> entry : nodesInEdges.entrySet()) {
                if (entry.getValue().size() == 0) {
                    dependencyLess.add(entry.getKey());
                }
            }
            levels.add(new ArrayList<>(dependencyLess));
            // Remove dependencyless nodes from node_ins collection, as those dependencyless nodes have been placed.
            for (String node : dependencyLess) {
                nodesInEdges.remove(node);
            }
            if (nodesInEdges.size() == 0) {
                break;
            }
            for (Map.Entry<String, Set<String>> entry : nodesInEdges.entrySet()) {
                Set<String> tmp = entry.getValue();
                tmp.removeAll(dependencyLess);
                nodesInEdges.put(entry.getKey(), tmp);
            }
        }
        return levels;
    }

    public JSONArray levelLayout2(String parentLevel) {
        JSONArray leveledNodes = new JSONArray();
        tarjanScc();
        List<Pair<String, String>> mergedEdges = mergeGraph();
        if (mergedEdges.size() == 0) {
            JSONArray oneLevel = new JSONArray();
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject jsonNode = nodes.getJSONObject(i);
                jsonNode.put("level", parentLevel+0);
                oneLevel.add(jsonNode);
            }
            leveledNodes.add(oneLevel);
            return leveledNodes;
        }
        Set<String> idSet = new HashSet<>();
        List<List<String>> levels = groupTopologicalSort(mergedEdges);
        int totalNodes = 0;
        for (int i = 0; i < levels.size(); i++) {
            JSONArray currLevel = new JSONArray();
            for (String obj : levels.get(i)) {
                if (obj.startsWith("scc")) {
                    List<Integer> nodes = sccMap.get(obj);
                    for (int n : nodes) {
                        JSONObject jsonNode = id2Nodes.get(n);
                        jsonNode.put("level", parentLevel+i);
                        currLevel.add(jsonNode);
                        idSet.add(jsonNode.getString("id"));
                        totalNodes++;
                    }
                } else {
                    JSONObject jsonNode = id2Nodes.get(idMap.get(obj));
                    jsonNode.put("level", parentLevel+i);
                    currLevel.add(jsonNode);
                    idSet.add(jsonNode.getString("id"));
                    totalNodes++;
                }
            }
            leveledNodes.add(currLevel);
        }
//        System.out.println("Original size: " + nodes.size());
//        System.out.println("Leveled nodes size " + leveledNodes.size());
        if (totalNodes != nodes.size()) {
            JSONArray lastLevel = new JSONArray();
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject tmp = nodes.getJSONObject(i);
                if (!idSet.contains(tmp.getString("id"))) {
                    tmp.put("level", parentLevel+levels.size());
                    lastLevel.add(tmp);
                }
            }
            leveledNodes.add(lastLevel);
        }
        return leveledNodes;
//        return nodes;
    }

    private void addNodeToMap(Map<Integer, Set<JSONObject>> nodesMap, JSONObject node, int level, String parentLevel) {
        node.put("level", parentLevel + level);
        if (nodesMap.containsKey(level)) {
            nodesMap.get(level).add(node);
        } else {
            Set<JSONObject> tmp = new HashSet<>();
            tmp.add(node);
            nodesMap.put(level, tmp);
        }
    }
    @SuppressWarnings("Duplicates")
    public JSONArray levelLayout3(String parentLevel) {
        JSONArray leveledNodes = new JSONArray();
        tarjanScc();
        List<Pair<String, String>> mergedEdges = mergeGraph();
        if (mergedEdges.size() == 0) {
            JSONArray oneLevel = new JSONArray();
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject jsonNode = nodes.getJSONObject(i);
                jsonNode.put("level", parentLevel+0);
                oneLevel.add(jsonNode);
            }
            leveledNodes.add(oneLevel);
            return leveledNodes;
        }
        Set<String> idSet = new HashSet<>();
        List<List<String>> levels = groupTopologicalSort(mergedEdges);
        Map<Integer, Set<JSONObject>> nodesMap = new HashMap<>();
        Map<String, JSONObject> edgeMap = new HashMap<>();
        for (int i = 0; i < edges.size(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            edgeMap.put(edge.getString("id"), edge);
        }
        for (int i = 0; i < levels.size(); i++) {
            for (String obj : levels.get(i)) {
                if (obj.startsWith("scc")) {
                    List<Integer> nodes = sccMap.get(obj);
//                    Set<Integer> tmpSet = new HashSet<>(nodes);
                    for (int j = 0; j < nodes.size(); j++) {
                        for (int k = j+1; k < nodes.size(); k++) {
                            int nodeId1 = nodes.get(j);
                            int nodeId2 = nodes.get(k);
                            JSONObject jsonNode1 = id2Nodes.get(nodeId1);
                            JSONObject jsonNode2 = id2Nodes.get(nodeId2);
                            idSet.add(jsonNode1.getString("id"));
                            idSet.add(jsonNode2.getString("id"));
                            String edgeId1 = jsonNode1.getString("id") + "_" + jsonNode2.getString("id");
                            String edgeId2 = jsonNode2.getString("id") + "_" + jsonNode1.getString("id");
                            if (edgeMap.containsKey(edgeId1) && edgeMap.containsKey(edgeId2)) {
                                JSONObject edge1 = edgeMap.get(edgeId1);
                                JSONObject edge2 = edgeMap.get(edgeId2);
                                if (edge1.getInteger("D") > edge2.getInteger("D")) {
                                    addNodeToMap(nodesMap, jsonNode1, i, parentLevel);
                                    addNodeToMap(nodesMap, jsonNode2, i+1, parentLevel);
                                } else {
                                    addNodeToMap(nodesMap, jsonNode1, i+1, parentLevel);
                                    addNodeToMap(nodesMap, jsonNode2, i, parentLevel);
                                }
                            }
                            if (!edgeMap.containsKey(edgeId1) && edgeMap.containsKey(edgeId2)) {
                                addNodeToMap(nodesMap, jsonNode1, i+1, parentLevel);
                                addNodeToMap(nodesMap, jsonNode2, i, parentLevel);
                            }
                            if (edgeMap.containsKey(edgeId1) && !edgeMap.containsKey(edgeId2)) {
                                addNodeToMap(nodesMap, jsonNode1, i, parentLevel);
                                addNodeToMap(nodesMap, jsonNode2, i+1, parentLevel);
                            }
                        }
                    }
                } else {
                    JSONObject jsonNode = id2Nodes.get(idMap.get(obj));
                    addNodeToMap(nodesMap, jsonNode, i, parentLevel);
                    idSet.add(jsonNode.getString("id"));
                }
            }
        }
        Map<Integer, Set<JSONObject>> sorted = new TreeMap<>(nodesMap);
        int maxLevel = -1;
        for (Map.Entry<Integer, Set<JSONObject>> entry : sorted.entrySet()) {
            maxLevel = Math.max(maxLevel, entry.getKey());
            JSONArray currentLevel = new JSONArray();
            currentLevel.addAll(entry.getValue());
            leveledNodes.add(currentLevel);
        }
        if (idSet.size() != nodes.size()) {
            JSONArray lastLevel = new JSONArray();
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject tmp = nodes.getJSONObject(i);
                if (!idSet.contains(tmp.getString("id"))) {
                    tmp.put("level", parentLevel+(maxLevel+1));
                    lastLevel.add(tmp);
                }
            }
            leveledNodes.add(lastLevel);
        }
        return leveledNodes;
//        return nodes;
    }

    public JSONArray levelLayout() {
        JSONArray leveledNodes = new JSONArray();
        tarjanScc();
        List<Pair<String, String>> mergedEdges = mergeGraph();
        if (mergedEdges.size() == 0) {
            return nodes;
        }
        List<List<String>> levels = groupTopologicalSort(mergedEdges);
        for (int i = 0; i < levels.size(); i++) {
            for (String obj : levels.get(i)) {
                if (obj.startsWith("scc")) {
                    List<Integer> nodes = sccMap.get(obj);
                    for (int n : nodes) {
                        JSONObject jsonNode = id2Nodes.get(n);
                        jsonNode.put("level", i);
                        leveledNodes.add(jsonNode);
                    }
                } else {
                    JSONObject jsonNode = id2Nodes.get(idMap.get(obj));
                    jsonNode.put("level", i);
                    leveledNodes.add(jsonNode);
                }
            }
        }
//        System.out.println("Original size: " + nodes.size());
//        System.out.println("Leveled nodes size " + leveledNodes.size());
        if (leveledNodes.size() != nodes.size()) {
            for (int i = 0; i < nodes.size(); i++) {
                JSONObject tmp = nodes.getJSONObject(i);
                if (!leveledNodes.contains(tmp)) {
                    tmp.put("level", levels.size());
                    leveledNodes.add(tmp);
                }
            }
        }
        return leveledNodes;
//        return nodes;
    }

//    public static void main(String[] args) {
//        JSONObject edge1 = new JSONObject();
//        edge1.put("source", "2000");
//        edge1.put("target", "2001");
//
//        JSONObject edge2 = new JSONObject();
//        edge2.put("source", "2001");
//        edge2.put("target", "2002");
//
//        JSONObject edge3 = new JSONObject();
//        edge3.put("source", "2002");
//        edge3.put("target", "2000");
//
//        JSONObject edge4 = new JSONObject();
//        edge4.put("source", "2003");
//        edge4.put("target", "2001");
//
//        JSONObject node1 = new JSONObject();
//        node1.put("id", "2000");
//
//        JSONObject node2 = new JSONObject();
//        node2.put("id", "2001");
//
//        JSONObject node3 = new JSONObject();
//        node3.put("id", "2002");
//
//        JSONObject node4 = new JSONObject();
//        node4.put("id", "2003");
//
//        JSONArray nodes = new JSONArray();
//        nodes.add(node1);
//        nodes.add(node2);
//        nodes.add(node3);
//        nodes.add(node4);
//
//        JSONArray edges = new JSONArray();
//        edges.add(edge1);
//        edges.add(edge2);
//        edges.add(edge3);
//        edges.add(edge4);
//        GraphLayoutUtil graphLayoutUtil = new GraphLayoutUtil(nodes, edges);
//        JSONArray res = graphLayoutUtil.levelLayout();
//        for (int i = 0; i < res.size(); i++) {
//            System.out.println(res.getJSONObject(i));
//        }
//    }
}
