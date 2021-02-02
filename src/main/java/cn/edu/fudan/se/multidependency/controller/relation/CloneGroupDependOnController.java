package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/relation/dependsdetail/{name}")
public class CloneGroupDependOnController {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private BasicCloneQueryService basicCloneQueryService;

    @Autowired
    private CloneAnalyseService cloneAnalyse;

    @Autowired
    private StaticAnalyseService staticAnalyseService;

    @Autowired
    private DependsOnRepository dependsOnRepository;

    @GetMapping("")
    public String index(HttpServletRequest request, @PathVariable("name") String name){
        request.setAttribute("name",name);
        return"relation/dependsdetail";
    }

    @GetMapping("/dependsmatrix")
    @ResponseBody
    public JSONObject getDependsMatrix(@PathVariable("name") String name){
        Collection<CodeNode> filegroup = new ArrayList<>();
        if(name.contains("file_group")){
            CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
            cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
            filegroup.addAll(cloneGroup.getNodes());
        }else{
            ProjectFile file = nodeService.queryFile(Long.parseLong(name));
            filegroup.add(file);
        }
        Set<CodeNode> nodes = new TreeSet<CodeNode>(new Comparator<CodeNode>() {
            @Override
            public int compare(CodeNode o1, CodeNode o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        nodes.addAll(filegroup);
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
        String[][] dependsonMatrix = new String[nodes.size()][allNodes.size()];
        int i = 0;
        for (CodeNode node:
             nodes) {
            int j = 0;
            for (Node dependsNode :
                 allNodes) {
                if(dependsOnRepository.findSureDependsOnInFiles(node.getId(),dependsNode.getId()) != null){
                    Map<String, Long> dependson = dependsOnRepository.findSureDependsOnInFiles(node.getId(),dependsNode.getId()).getDependsOnTypes();
                    Set<String> keyset = dependson.keySet();
                    for (String key:
                         keyset) {
                        if(dependsonMatrix[i][j] == null){
                            dependsonMatrix[i][j] = "";
                        }
                        if(!dependsonMatrix[i][j].equals("")){
                            dependsonMatrix[i][j] += "/";
                        }
                        dependsonMatrix[i][j] += RelationType.relationAbbreviation.get(RelationType.valueOf(key));
                        dependsonMatrix[i][j] += "(" + dependson.get(key).toString() + ")";
                    }
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
        Collection<CodeNode> filegroup = new ArrayList<>();
        if(name.contains("file_group")){
            CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
            cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
            filegroup.addAll(cloneGroup.getNodes());
        }else{
            ProjectFile file = nodeService.queryFile(Long.parseLong(name));
            filegroup.add(file);
        }
        Set<CodeNode> nodes = new TreeSet<CodeNode>(new Comparator<CodeNode>() {
            @Override
            public int compare(CodeNode o1, CodeNode o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        nodes.addAll(filegroup);
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
        String[][] dependedonMatrix = new String[nodes.size()][allNodes.size()];
        int i = 0;
        for (CodeNode node:
                nodes) {
            int j = 0;
            for (Node dependsNode :
                    allNodes) {
                if(dependsOnRepository.findSureDependsOnInFiles(dependsNode.getId(),node.getId()) != null){
                    Map<String, Long> dependson = dependsOnRepository.findSureDependsOnInFiles(dependsNode.getId(),node.getId()).getDependsOnTypes();
                    Set<String> keyset = dependson.keySet();
                    for (String key:
                            keyset) {
                        if(dependedonMatrix[i][j] == null){
                            dependedonMatrix[i][j] = "";
                        }
                        if(!dependedonMatrix[i][j].equals("")){
                            dependedonMatrix[i][j] += "/";
                        }
                        dependedonMatrix[i][j] += RelationType.relationAbbreviation.get(RelationType.valueOf(key));
                        dependedonMatrix[i][j] += "(" + dependson.get(key).toString() + ")";
                    }
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
        Collection<CodeNode> filegroup = new ArrayList<>();
        if(name.contains("file_group")){
            CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
            cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
            filegroup.addAll(cloneGroup.getNodes());
        }else{
            ProjectFile file = nodeService.queryFile(Long.parseLong(name));
            filegroup.add(file);
        }
        Set<CodeNode> nodes = new TreeSet<CodeNode>(new Comparator<CodeNode>() {
            @Override
            public int compare(CodeNode o1, CodeNode o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        nodes.addAll(filegroup);
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
        Collection<CodeNode> filegroup = new ArrayList<>();
        if(name.contains("file_group")){
            CloneGroup cloneGroup = basicCloneQueryService.queryCloneGroup(name);
            cloneGroup = cloneAnalyse.addNodeAndRelationToCloneGroup(cloneGroup);
            filegroup.addAll(cloneGroup.getNodes());
        }else{
            ProjectFile file = nodeService.queryFile(Long.parseLong(name));
            filegroup.add(file);
        }
        Set<CodeNode> nodes = new TreeSet<CodeNode>(new Comparator<CodeNode>() {
            @Override
            public int compare(CodeNode o1, CodeNode o2) {
                return ((ProjectFile)o1).getPath().compareTo(((ProjectFile)o2).getPath());
            }
        });
        nodes.addAll(filegroup);
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
