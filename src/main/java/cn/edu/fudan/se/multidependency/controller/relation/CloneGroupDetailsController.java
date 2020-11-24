package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneAnalyseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/relation/clonegroup/{name}")
public class CloneGroupDetailsController {

    @Autowired
    private BasicCloneQueryService basicCloneQueryService;

    @Autowired
    private CloneAnalyseService cloneAnalyse;

    @Autowired
    private StaticAnalyseService staticAnalyseService;

    @GetMapping("")
    public String index(HttpServletRequest request, @PathVariable("name") String name){
        CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
        cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
        request.setAttribute("group",cloneGroup);
//        request.setAttribute("project",containRelationService.findPackageBelongToProject(pck));
        return"relation/clonegroupdepends";
    }

    @GetMapping("/dependsmatrix")
    @ResponseBody
    public JSONObject getDependsMatrix(@PathVariable("name") String name){
        CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
        cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
        Set<CodeNode> nodes = new TreeSet<CodeNode>(new Comparator<CodeNode>() {
            @Override
            public int compare(CodeNode o1, CodeNode o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        nodes.addAll(cloneGroup.getNodes());
        Set<Node> allNodes = new TreeSet<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        for (CodeNode node:
             nodes) {
            Collection<Node> endNodes = staticAnalyseService.findFileDependsOn( (ProjectFile)node).stream().map(DependsOn::getEndNode).collect(Collectors.toList());
            allNodes.addAll(endNodes);
        }
        boolean[][] dependsonMatrix = new boolean[nodes.size()][allNodes.size()];
        int i = 0;
        for (CodeNode node:
             nodes) {
            int j = 0;
            Collection<Node> endNodes = staticAnalyseService.findFileDependsOn( (ProjectFile)node).stream().map(DependsOn::getEndNode).collect(Collectors.toList());
            for (Node dependsNode :
                 allNodes) {
                if(endNodes.contains(dependsNode)){
                    dependsonMatrix[i][j] = true;
                }else{
                    dependsonMatrix[i][j] = false;
                }
                j++;
            }
            i++;
        }
        JSONObject result = new JSONObject();
        result.put("nodes",nodes);
        result.put("dependsnodes",allNodes);
        result.put("matrix",dependsonMatrix);
        return result;
    }

    @GetMapping("/dependedmatrix")
    @ResponseBody
    public JSONObject getDependedMatrix(@PathVariable("name") String name){
        CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
        cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
        Set<CodeNode> nodes = new TreeSet<CodeNode>(new Comparator<CodeNode>() {
            @Override
            public int compare(CodeNode o1, CodeNode o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        nodes.addAll(cloneGroup.getNodes());
        Set<Node> allNodes = new TreeSet<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        for (CodeNode node:
                nodes) {
            Collection<Node> startNodes = staticAnalyseService.findFileDependedOnBy( (ProjectFile)node).stream().map(DependsOn::getStartNode).collect(Collectors.toList());
            allNodes.addAll(startNodes);
        }
        boolean[][] dependedonMatrix = new boolean[nodes.size()][allNodes.size()];
        int i = 0;
        for (CodeNode node:
                nodes) {
            int j = 0;
            Collection<Node> startNodes = staticAnalyseService.findFileDependedOnBy( (ProjectFile)node).stream().map(DependsOn::getStartNode).collect(Collectors.toList());
            for (Node dependsNode :
                    allNodes) {
                if(startNodes.contains(dependsNode)){
                    dependedonMatrix[i][j] = true;
                }else{
                    dependedonMatrix[i][j] = false;
                }
                j++;
            }
            i++;
        }
        JSONObject result = new JSONObject();
        result.put("nodes",nodes);
        result.put("dependsnodes",allNodes);
        result.put("matrix",dependedonMatrix);
        return result;
    }

    @GetMapping("/alldependsonnodes")
    @ResponseBody
    public Object getAlldependsNodes(@PathVariable("name") String name){
        CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
        cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
        Set<CodeNode> nodes = new TreeSet<CodeNode>(new Comparator<CodeNode>() {
            @Override
            public int compare(CodeNode o1, CodeNode o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        nodes.addAll(cloneGroup.getNodes());
        Set<Node> allNodes = new TreeSet<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        for (CodeNode node:
                nodes) {
            Collection<Node> endNodes = staticAnalyseService.findFileDependsOn( (ProjectFile)node).stream().map(DependsOn::getEndNode).collect(Collectors.toList());
            allNodes.addAll(endNodes);
        }
        return allNodes;
    }

    @GetMapping("/alldependednodes")
    @ResponseBody
    public Object getAlldependedNodes(@PathVariable("name") String name){
        CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
        cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
        Set<CodeNode> nodes = new TreeSet<CodeNode>(new Comparator<CodeNode>() {
            @Override
            public int compare(CodeNode o1, CodeNode o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        nodes.addAll(cloneGroup.getNodes());
        Set<Node> allNodes = new TreeSet<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        for (CodeNode node:
                nodes) {
            Collection<Node> startNodes = staticAnalyseService.findFileDependedOnBy( (ProjectFile)node).stream().map(DependsOn::getStartNode).collect(Collectors.toList());
            allNodes.addAll(startNodes);
        }
        return allNodes;
    }
}
