package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Coupling;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.coupling.CouplingRepository;
import cn.edu.fudan.se.multidependency.service.query.BeanCreator;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.utils.DataUtil;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.GraphLayoutUtil;
import cn.edu.fudan.se.multidependency.utils.layout.GraphBlock;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class CouplingServiceImpl implements CouplingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanCreator.class);

    @Autowired
    public CouplingRepository couplingRepository;

    @Autowired
    public DependsOnRepository dependsOnRepository;

    @Autowired
    public ProjectFileRepository projectFileRepository;

    @Autowired
    private ContainRepository containRepository;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private PackageRepository packageRepository;

    @Override
    public Map<ProjectFile, Double> calGroupInstablity(List<Long> fileIdList){
        List<List<DependsOn>> listTmp = getGroupInsideAndOutDependsOnByFileIds(fileIdList);
        List<DependsOn> GroupInsideDependsOns = listTmp.get(0);
        Map<ProjectFile, Integer> instabilityInTimes = new HashMap<>();
        Map<ProjectFile, Integer> instabilityOutTimes = new HashMap<>();
        Map<ProjectFile, Double> instability = new HashMap<>();

        for(DependsOn dependsOn: GroupInsideDependsOns){
            ProjectFile startFile = (ProjectFile) dependsOn.getStartNode();
            ProjectFile endFile = (ProjectFile) dependsOn.getEndNode();
            Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                for (String type : dependsOnTypes.keySet()) {
                    if (type.equals("EXTENDS") || type.equals("IMPLEMENTS")) {
                        if(instabilityInTimes.containsKey(startFile)){
                            instabilityInTimes.put(startFile, instabilityInTimes.get(startFile) + 10);
                        }else{
                            instabilityInTimes.put(startFile, 10);
                        }

                        if(instabilityOutTimes.containsKey(endFile)){
                            instabilityOutTimes.put(endFile, instabilityOutTimes.get(endFile) + 1);
                        }else{
                            instabilityOutTimes.put(endFile, 1);
                        }
                    }else if(type.equals("USE") || type.equals("CALL") ||  type.equals("RETURN")
                            || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") ||  type.equals("CREATE")
                            || type.equals("MEMBER_VARIABLE")){
                        if(instabilityOutTimes.containsKey(startFile)){
                            instabilityOutTimes.put(startFile, instabilityOutTimes.get(startFile) + 1);
                        }else{
                            instabilityOutTimes.put(startFile, 1);
                        }

                        if(instabilityInTimes.containsKey(endFile)){
                            instabilityInTimes.put(endFile, instabilityInTimes.get(endFile) + 1);
                        }else{
                            instabilityInTimes.put(endFile, 1);
                        }
                    }
                }
        }

        for(Long fileId: fileIdList){
            ProjectFile file = projectFileRepository.findFileById(fileId);
            int allDependsOnTimes = instabilityInTimes.getOrDefault(file, 0) + instabilityOutTimes.getOrDefault(file, 0);
            int outDependsOnTimes = instabilityOutTimes.getOrDefault(file, 0);
            if(allDependsOnTimes == 0){
                instability.put(file, 0.0);
            }else{
                instability.put(file, ((double) outDependsOnTimes / (double) allDependsOnTimes));
            }
        }
        return instability;
    }

    private String getRelationTypeAndTimes(Map<String, Long> dependsOnTypes){
        StringBuilder typesAndTimes = new StringBuilder();
        Iterator<String> iterator = dependsOnTypes.keySet().iterator();
        while(iterator.hasNext()) {
            String type = iterator.next();
            if(iterator.hasNext()){
                typesAndTimes.append(type).append("(").append(dependsOnTypes.get(type)).append(")_");
            }else{
                typesAndTimes.append(type).append("(").append(dependsOnTypes.get(type)).append(")");
            }
        }
        return typesAndTimes.toString();
    }

    @Override
    public double calC1to2(int funcNum1, int funcNum2){
        if (funcNum1 + funcNum2 == 0) {
            return 0;
        } else {
            return (2 * ((double)funcNum1) * ((double)funcNum2)) / ((double)funcNum1 + (double)funcNum2);
        }
    }

    @Override
    public double calC(double C1, double C2){
        return Math.sqrt((double)(Math.pow(C1, 2) + Math.pow(C2, 2)));
    }

    @Override
    public double calU1to2(long dependsOntimes1, long dependsOntimes2){
        return ((double)dependsOntimes1 - (double)dependsOntimes2) / ((double)dependsOntimes1 + (double)dependsOntimes2);
    }

    @Override
    public double calI(long dependsOntimes1, long dependsOntimes2){
//        return Math.sqrt(Math.pow(dependsOntimes1, 2) + Math.pow(dependsOntimes2, 2));
        return (2 * ((double)dependsOntimes1 + 1) * ((double)dependsOntimes2 + 1)) / ((double)dependsOntimes1 + (double)dependsOntimes2 + 2) - 1;
    }
    @Override
    public double calPkgI(long dependsOntimes1, long dependsOntimes2){
        return Math.sqrt(Math.pow(dependsOntimes1, 2) + Math.pow(dependsOntimes2, 2));
    }

    @Override
    public double calDISP(double C_AandB, long dependsOntimes1, long dependsOntimes2){
        return C_AandB / ((double)dependsOntimes1 + (double)dependsOntimes2);
    }

    @Override
    public double calDependsOnC(long file1Id, long file2Id){
        int funcNumAAtoB = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file1Id, file2Id);
        int funcNumBAtoB = couplingRepository.queryTwoFilesDependsByFunctionsNum(file1Id, file2Id);
        int funcNumABtoA = couplingRepository.queryTwoFilesDependsByFunctionsNum(file2Id, file1Id);
        int funcNumBBtoA = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file2Id, file1Id);

        return calC(calC1to2(funcNumAAtoB,funcNumBAtoB), calC1to2(funcNumABtoA,funcNumBBtoA));
    }

    @Override
    public double calcPkgDispersion(long dA2B, long dB2A, int pkg1Files, int pkg2Files) {
        double H = 2 * (double)pkg1Files * (double)pkg2Files / ((double)pkg1Files + (double)pkg2Files);
        return H/ ((double)dA2B + (double)dB2A);
    }
    @Override
    public double calDependsOnI(DependsOn dependsOnAtoB, DependsOn dependsOnBtoA){
        long dependsOntimesAtoB = 0;
        long dependsOntimesBtoA = 0;

        if(dependsOnAtoB != null) {
            Map<String, Long> dependsOnTypesAtoB = dependsOnAtoB.getDependsOnTypes();

            for (String type : dependsOnTypesAtoB.keySet()) {
                if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN") || type.equals("CREATE")
                        || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("IMPLEMENTS")
                        || type.equals("MEMBER_VARIABLE")) {
                    dependsOntimesAtoB += dependsOnTypesAtoB.get(type);
                }
            }
        }

        if(dependsOnBtoA != null) {
            Map<String, Long> dependsOnTypesBtoA = dependsOnBtoA.getDependsOnTypes();

            for (String type : dependsOnTypesBtoA.keySet()) {
                if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN") || type.equals("CREATE")
                        || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("IMPLEMENTS")
                        || type.equals("MEMBER_VARIABLE")) {
                    dependsOntimesBtoA += dependsOnTypesBtoA.get(type);
                }
            }
        }

        return calI(dependsOntimesAtoB, dependsOntimesBtoA);
    }

    private List<Double> calGroupCI(List<DependsOn> dependsOnList){
        List<String> filePairs = new ArrayList<>();
        List<Double> result = new ArrayList<>();
        double CSum = 0.0, ISum = 0.0;

        for(DependsOn dependsOn: dependsOnList){
            long file1Id = dependsOn.getStartNode().getId();
            long file2Id = dependsOn.getEndNode().getId();
            ProjectFile file1 = (ProjectFile) dependsOn.getStartNode();
            ProjectFile file2 = (ProjectFile) dependsOn.getEndNode();
            String filesPair = file1Id + "_" + file2Id;
            String filesPairReverse = file2Id + "_" + file1Id;

            if(!filePairs.contains(filesPair) && !filePairs.contains(filesPairReverse)){
                filePairs.add(filesPair);
                DependsOn dependsOnAtoB = dependsOnRepository.findDependsOnBetweenFiles(file1Id, file2Id);
                DependsOn dependsOnBtoA = dependsOnRepository.findDependsOnBetweenFiles(file2Id, file1Id);

                String typesAndTimesAtoB = "";
                String typesAndTimesBtoA = "";

                if(dependsOnAtoB != null){
                    typesAndTimesAtoB = getRelationTypeAndTimes(dependsOnAtoB.getDependsOnTypes());
                }

                if(dependsOnBtoA != null){
                    typesAndTimesBtoA = getRelationTypeAndTimes(dependsOnBtoA.getDependsOnTypes());
                }

                double C = calDependsOnC(file1Id, file2Id);
                CSum += C;
                double I = calDependsOnI(dependsOnAtoB, dependsOnBtoA);
                ISum += I;
                System.out.println(file1Id + " " + file1.getPath() + " " + file2Id + " " + file2.getPath() + " "
                        + C + " " + I + " " + typesAndTimesAtoB + " " + typesAndTimesBtoA);
            }
        }

        result.add(CSum);
        result.add(ISum);
        return result;
    }

    @Override
    public JSONObject getCouplingValueByFileIds(List<Long> fileIds, Map<Long, Long> parentPckMap){
        JSONObject result = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();

        List<List<DependsOn>> listTmp = getGroupInsideAndOutDependsOnByFileIds(fileIds);
        List<DependsOn> GroupInsideDependsOns = listTmp.get(0);
        Map<ProjectFile, Double> instability = calGroupInstablity(fileIds);

        for (ProjectFile projectFile : instability.keySet()) {
            JSONObject fileTmp = new JSONObject();
            fileTmp.put("id", projectFile.getId().toString());
            fileTmp.put("parentPckId", parentPckMap.get(projectFile.getId()).toString());
            fileTmp.put("name", projectFile.getName());
            fileTmp.put("label", projectFile.getName());
            fileTmp.put("path", projectFile.getPath());
            fileTmp.put("LOC", projectFile.getLoc());
            fileTmp.put("nodeType", "file");
            fileTmp.put("instability", instability.get(projectFile));
            fileTmp.put("level", 0);
            nodes.add(fileTmp);
        }


        Map<String, Double> iMap = new HashMap<>();
        for(DependsOn dependsOn: GroupInsideDependsOns){
            JSONObject dependsOnTmp = new JSONObject();
            boolean flag = false;
            double i = 0.0;
            double dist = -1.0;
            int D = 0;
            double C = 0.0;
            Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
            for (String type : dependsOnTypes.keySet()) {
                if (type.equals("EXTENDS") || type.equals("IMPLEMENTS") ||
                        type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                        || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                        || type.equals("MEMBER_VARIABLE")) {
                    flag = true;
                    break;
                }
            }
            if(flag){
//                    System.out.println(dependsOn.getStartNode().getId() + "_" + dependsOn.getEndNode().getId());
                Coupling coupling = couplingRepository.queryCouplingBetweenTwoFiles(dependsOn.getStartNode().getId()
                        , dependsOn.getEndNode().getId());
                i += coupling.getI();
                dist = coupling.getDist() > 0 ? coupling.getDist() : -1.0;
                if (coupling.getStartNode().getId().equals(dependsOn.getStartNode().getId())){
                    C = coupling.getCStartToEnd();
                    D = coupling.getDAtoB();
                } else {
                    C = coupling.getCEndToStart();
                    D = coupling.getDBtoA();
                }
            }
            dependsOnTmp.put("C", C);
            dependsOnTmp.put("D", D);
            dependsOnTmp.put("I", i);
            dependsOnTmp.put("detail", dependsOn.getDependsOnTypes().toString());
            dependsOnTmp.put("dist", dist);
            dependsOnTmp.put("id", dependsOn.getStartNode().getId().toString() + "_" + dependsOn.getEndNode().getId().toString());
            dependsOnTmp.put("source", dependsOn.getStartNode().getId().toString());
            dependsOnTmp.put("target", dependsOn.getEndNode().getId().toString());
            dependsOnTmp.put("dependsOnTypes", dependsOn.getDependsOnType());
            dependsOnTmp.put("isExtendOrImplements", dependsOnRepository.findDependsOnIsExtendOrImplements(dependsOn.getId()));
            dependsOnTmp.put("isTwoWayDependsOn", dependsOnRepository.findIsTwoWayDependsOn(dependsOn.getStartNode().getId(),
                    dependsOn.getEndNode().getId()));
            iMap.put((String) dependsOnTmp.get("id"), i);
            edges.add(dependsOnTmp);
        }
        Set<String> tmp = new HashSet<>();
        for (int i = 0; i < edges.size(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            String reverseId = edge.get("target") + "_" + edge.get("source");
            int D = (int) edge.get("D");
            if (tmp.contains((String) edge.get("id"))) {
                edge.put("I", -1);
                continue;
            }
            if (iMap.containsKey(reverseId)) {
                edge.put("I", iMap.get((String) edge.get("id")));
                tmp.add(reverseId);
            } else {
                edge.put("I", D);
            }
        }
        if (edges.size() == 0) {
            result.put("nodes", nodes);
        } else {
            GraphLayoutUtil layoutUtil = new GraphLayoutUtil(nodes, edges);
            JSONArray leveledNodes = layoutUtil.levelLayout();
            result.put("nodes", leveledNodes);
        }
        result.put("edges", edges);

        return result;
    }

    @SuppressWarnings("Duplicates")
    JSONArray fileDependsToEdges(List<DependsOn> dependsOnBetweenFiles) {
        JSONArray edges = new JSONArray();
        Map<String, Double> iMap = new HashMap<>();
        for (DependsOn dependsOn : dependsOnBetweenFiles) {
            JSONObject dependsOnTmp = new JSONObject();
            boolean flag = false;
            double i = 0.0;
            double dist = -1.0;
            int D = 0;
            double C = 0.0;
            Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
            for (String type : dependsOnTypes.keySet()) {
                if (type.equals("EXTENDS") || type.equals("IMPLEMENTS") ||
                        type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                        || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                        || type.equals("MEMBER_VARIABLE")) {
                    flag = true;
                    break;
                }
            }
            if(flag){
//                    System.out.println(dependsOn.getStartNode().getId() + "_" + dependsOn.getEndNode().getId());
                Coupling coupling = couplingRepository.queryCouplingBetweenTwoFiles(dependsOn.getStartNode().getId()
                        , dependsOn.getEndNode().getId());
                i += coupling.getI();
                dist = coupling.getDist() > 0 ? coupling.getDist() : -1.0;
                if (coupling.getStartNode().getId().equals(dependsOn.getStartNode().getId())){
                    C = coupling.getCStartToEnd();
                    D = coupling.getDAtoB();
                } else {
                    C = coupling.getCEndToStart();
                    D = coupling.getDBtoA();
                }
            }
            dependsOnTmp.put("C", C);
            dependsOnTmp.put("D", D);
            dependsOnTmp.put("I", i);
            dependsOnTmp.put("detail", dependsOn.getDependsOnTypes().toString());
            dependsOnTmp.put("dist", dist);
            dependsOnTmp.put("id", dependsOn.getStartNode().getId().toString() + "_" + dependsOn.getEndNode().getId().toString());
            dependsOnTmp.put("source", dependsOn.getStartNode().getId().toString());
            dependsOnTmp.put("target", dependsOn.getEndNode().getId().toString());
            dependsOnTmp.put("isExtendOrImplements", dependsOnRepository.findDependsOnIsExtendOrImplements(dependsOn.getId()));
            dependsOnTmp.put("isTwoWayDependsOn", dependsOnRepository.findIsTwoWayDependsOn(dependsOn.getStartNode().getId(),
                    dependsOn.getEndNode().getId()));
            iMap.put((String) dependsOnTmp.get("id"), i);
            edges.add(dependsOnTmp);
        }
        // post process, calculate 'I' between files
        Set<String> tmp = new HashSet<>();
        for (int i = 0; i < edges.size(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            String reverseId = edge.get("target") + "_" + edge.get("source");
            int D = (int) edge.get("D");
            if (tmp.contains((String) edge.get("id"))) {
                edge.put("I", -1);
                continue;
            }
            if (iMap.containsKey(reverseId)) {
                edge.put("I", iMap.get((String) edge.get("id")));
                tmp.add(reverseId);
            } else {
                edge.put("I", D);
            }
        }
        return edges;
    }
    @Override
    public JSONObject getCouplingValueByPcks(Map<Package, List<Package>> pckMap,
                                             Map<Long, Double> parentPcksInstability, boolean isTopLevel,
                                             boolean needLayout){
        JSONObject result = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        List<Package> pckList = new ArrayList<>();

        for(Package parentPck: pckMap.keySet()){
            pckList.addAll(pckMap.get(parentPck));
        }

        Map<Map<Package, Package>, Set<DependsOn>> dependsOnBetweenPackages = new HashMap<>();

        for(Package pck: pckList) {
            Double parentInstability = 0.0;
            Package parentPackage = new Package();

            for(Package parentPck: pckMap.keySet()){
                if(pckMap.get(parentPck).contains(pck)){
                    parentPackage = parentPck;
                    if(!isTopLevel) {
                        parentInstability = parentPcksInstability.get(parentPck.getId());
                    }
                }
            }

            JSONObject tmpPck = new JSONObject();
            String pckName = FileUtil.extractPackagePath(pck.getDirectoryPath(), isTopLevel);
            int pckContainsFilesNum = containRepository.findPackageContainAllFilesNum(pck.getId());
//            if(pck.equals(parentPackage)){
//                // parent package下单独的文件虚拟成一个包
//                pckContainsFilesNum = containRepository.findPackageContainFilesNum(pck.getId());
//            }else{
//                pckContainsFilesNum = containRepository.findPackageContainAllFilesNum(pck.getId());
//            }
            int pckContainsFilesLOC = containRepository.findPackageContainAllFilesLOC(pck.getId());
            tmpPck.put("unfoldable", packageRepository.isPackageUnfoldable(pck.getId()));
            tmpPck.put("id", pck.getId().toString());
            tmpPck.put("path", pck.getDirectoryPath());
            tmpPck.put("name", pckName);
            tmpPck.put("NOF", pckContainsFilesNum);
            tmpPck.put("LOC", pckContainsFilesLOC);
            tmpPck.put("LooseDegree", pck.getLooseDegree());
            tmpPck.put("label", pckName);
            tmpPck.put("parentPckId", parentPackage.getId().toString());
            tmpPck.put("nodeType", "package");
            tmpPck.put("level", 0);

            List<Map<Package, List<DependsOn>>> listTmp = getGroupInsideAndOutDependsOnByPackage(pck, pckList, parentPackage);
//            List<Map<Package, List<DependsOn>>> listTmp = getDependsBetweenPackages(pckList);

            Map<Package, List<DependsOn>> GroupInsideToOutDependsOns = listTmp.get(0);
            Map<Package, List<DependsOn>> GroupOutToInsideDependsOns = listTmp.get(1);
            int GroupInsideToOutDependsOnTimes = 0;
            int GroupOutToInsideDependsOnTimes = 0;

            if (GroupInsideToOutDependsOns.size() > 0) {
                for(Package endPackage: GroupInsideToOutDependsOns.keySet()){
                    for(DependsOn dependsOn: GroupInsideToOutDependsOns.get(endPackage)){
                        Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                        for (String type : dependsOnTypes.keySet()) {
                            if (type.equals("EXTENDS") || type.equals("IMPLEMENTS")) {
                                GroupOutToInsideDependsOnTimes += 10;
                            } else if (type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                                    || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                                    || type.equals("MEMBER_VARIABLE")) {
                                GroupInsideToOutDependsOnTimes += 1;
                            }
                        }

                        Map<Package, Package> pckDependsOnTmp = new HashMap<>();
                        pckDependsOnTmp.put(pck, endPackage);
                        if (dependsOnBetweenPackages.containsKey(pckDependsOnTmp)) {
                            dependsOnBetweenPackages.get(pckDependsOnTmp).add(dependsOn);
                        } else {
                            Set<DependsOn> dependsOnsListTmp = new HashSet<>();
                            dependsOnsListTmp.add(dependsOn);
                            dependsOnBetweenPackages.put(pckDependsOnTmp, dependsOnsListTmp);
                        }
                    }
                }
            }

            if (GroupOutToInsideDependsOns.size() > 0) {
                for(Package startPackage: GroupOutToInsideDependsOns.keySet()){
                    for(DependsOn dependsOn: GroupOutToInsideDependsOns.get(startPackage)){
                        Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                        for (String type : dependsOnTypes.keySet()) {
                            if (type.equals("EXTENDS") || type.equals("IMPLEMENTS")) {
                                GroupInsideToOutDependsOnTimes += 10;
                            } else if (type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                                    || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                                    || type.equals("MEMBER_VARIABLE")) {
                                GroupOutToInsideDependsOnTimes += 1;
                            }
                        }

                        Map<Package, Package> pckDependsOnTmp = new HashMap<>();
                        pckDependsOnTmp.put(startPackage, pck);
                        if (dependsOnBetweenPackages.containsKey(pckDependsOnTmp)) {
                            dependsOnBetweenPackages.get(pckDependsOnTmp).add(dependsOn);
                        } else {
                            Set<DependsOn> dependsOnsListTmp = new HashSet<>();
                            dependsOnsListTmp.add(dependsOn);
                            dependsOnBetweenPackages.put(pckDependsOnTmp, dependsOnsListTmp);
                        }
                    }
                }
            }

//            int allDependsOnTimes = GroupInsideToOutDependsOnTimes + GroupOutToInsideDependsOnTimes;
//            double finalInstability = 0.0;
//            if (allDependsOnTimes != 0) {
//                double pckInstability = (double) GroupInsideToOutDependsOnTimes / (double) allDependsOnTimes;
//                if(parentInstability == 0.0){
//                    finalInstability = pckInstability;
//                }else{
//                    finalInstability = (parentInstability - pckInstability) * (1 - parentInstability) + pckInstability;
//                }
//            }
//            tmpPck.put("instability", finalInstability);
            nodes.add(tmpPck);
        }
        Map<String, Integer> dMap = new HashMap<>();
        for(Map<Package, Package> map: dependsOnBetweenPackages.keySet()){
            JSONObject tmpEdge = new JSONObject();
            int DAtoB = 0;
            int DBtoA = 0;
            double dist = 0.0;
            double distSum = 0;

            for(Package pck: map.keySet()){
                tmpEdge.put("id", pck.getId().toString() + "_" + map.get(pck).getId().toString());
                tmpEdge.put("source", pck.getId().toString());
                tmpEdge.put("target", map.get(pck).getId().toString());
            }
            Set<Long> fileIdSet1 = new HashSet<>();
            Set<Long> fileIdSet2 = new HashSet<>();
            for(DependsOn dependsOn: dependsOnBetweenPackages.get(map)){
                boolean flag = false;
                Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                for (String type : dependsOnTypes.keySet()) {
                    if (type.equals("EXTENDS") || type.equals("IMPLEMENTS") ||
                            type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                            || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                            || type.equals("MEMBER_VARIABLE")) {
                        flag = true;
                        break;
                    }
                }
                if(flag){
//                    LOGGER.info("checking coupling: " + dependsOn.getStartNode().getId() + " " + dependsOn.getEndNode().getId());
                    Coupling coupling = couplingRepository.queryCouplingBetweenTwoFiles(dependsOn.getStartNode().getId()
                            , dependsOn.getEndNode().getId());
                    DAtoB += coupling.getDAtoB();
                    DBtoA += coupling.getDBtoA();
                    dist += coupling.getDist();
                    distSum  += 1;
                    fileIdSet1.add(dependsOn.getStartNode().getId());
                    fileIdSet2.add(dependsOn.getEndNode().getId());
                }
            }
            double fileHMean = calcH(fileIdSet1.size(), fileIdSet2.size());
//            tmpEdge.put("I", calPkgI(DAtoB, DBtoA));

            tmpEdge.put("dist", dist / distSum);
            tmpEdge.put("dependsOnNum", dependsOnBetweenPackages.get(map).size());
            tmpEdge.put("C", DataUtil.toFixed(fileHMean));
            tmpEdge.put("f1", fileIdSet1.size());
            tmpEdge.put("f2", fileIdSet2.size());
            double logD = Math.max(0, Math.log10(DAtoB));
            tmpEdge.put("D", DAtoB);
            tmpEdge.put("logD", DataUtil.toFixed(logD));
            dMap.put((String)tmpEdge.get("id"), DAtoB);
//            String reverseId = tmpEdge.get("target") + "_" + tmpEdge.get("source");
//            if (dMap.containsKey(reverseId)) {
//                tmpEdge.put("I", calPkgI(DAtoB, dMap.get(reverseId)));
//            } else {
//                tmpEdge.put("I", -1);
//            }
//            if (DBtoA != 0) {
//                double logD = Math.max(0, Math.log10(DBtoA));
//                tmpEdge.put("D", DBtoA);
//                tmpEdge.put("logD", DataUtil.toFixed(logD));
//                DSum += logD;
//                dMap.put((String)tmpEdge.get("id"), DBtoA);
//            }
//            else {
//                double logD = Math.max(0, Math.log10(DAtoB));
//                tmpEdge.put("D", DAtoB);
//                tmpEdge.put("logD", DataUtil.toFixed(logD));
//                DSum += logD;
//                dMap.put((String)tmpEdge.get("id"), DAtoB);
//            }

//            tmpEdge.put("pkgDisp", calcPkgDispersion(DAtoB, DBtoA, fileIdSet1.size(), fileIdSet2.size()));
            edges.add(tmpEdge);
        }
        Set<String> tmp = new HashSet<>();
        for (int i = 0; i < edges.size(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            String reverseId = edge.get("target") + "_" + edge.get("source");
            int D = (int) edge.get("D");
            if (tmp.contains((String) edge.get("id"))) {
                edge.put("I", -1);
                continue;
            }
            if (dMap.containsKey(reverseId)) {
                edge.put("I", calPkgI(D, dMap.get(reverseId)));
                tmp.add(reverseId);
            } else {
                edge.put("I", D);
            }
        }
        if (needLayout) {
            if (edges.size() == 0) {
                result.put("nodes", nodes);
            } else {
                GraphLayoutUtil layoutUtil = new GraphLayoutUtil(nodes, edges);
                JSONArray leveledNodes = layoutUtil.levelLayout();
                result.put("nodes", leveledNodes);
            }
        } else {
            result.put("nodes", nodes);
            result.put("edges", edges);
        }
        return result;
    }

    @SuppressWarnings("Duplicates")
    @Deprecated
    private JSONObject pkgToNode(Package pkg, Package parentPackage, int pLevel) {
        JSONObject tmpPkg = new JSONObject();
        int pckContainsFilesLOC = containRepository.findPackageContainAllFilesLOC(pkg.getId());
        String pkgName = FileUtil.extractPackagePath(pkg.getDirectoryPath(), false);
        int pckContainsFilesNum;
        if(pkg.equals(parentPackage)) {
            // parent package下单独的文件虚拟成一个包
            pckContainsFilesNum = containRepository.findPackageContainFilesNum(pkg.getId());
        } else {
            pckContainsFilesNum = containRepository.findPackageContainAllFilesNum(pkg.getId());
        }
        tmpPkg.put("unfoldable", packageRepository.isPackageUnfoldable(pkg.getId()));
        tmpPkg.put("id", pkg.getId().toString());
        tmpPkg.put("path", pkg.getDirectoryPath());
        tmpPkg.put("name", pkgName);
        tmpPkg.put("NOF", pckContainsFilesNum);
        tmpPkg.put("LOC", pckContainsFilesLOC);
        tmpPkg.put("LooseDegree", pkg.getLooseDegree());
        tmpPkg.put("label", pkgName);
        tmpPkg.put("parentPckId", parentPackage.getId().toString());
        tmpPkg.put("nodeType", "package");
        tmpPkg.put("level", 0);
        tmpPkg.put("pLevel", pLevel);
        return tmpPkg;
    }
    @SuppressWarnings("Duplicates")
    private JSONObject pkgToNode(Package pkg) {
        JSONObject tmpPkg = new JSONObject();
        int pckContainsFilesLOC = containRepository.findPackageContainAllFilesLOC(pkg.getId());
        String pkgName = FileUtil.extractPackagePath(pkg.getDirectoryPath(), false);
        int pckContainsFilesNum = containRepository.findPackageContainAllFilesNum(pkg.getId());
        tmpPkg.put("unfoldable", packageRepository.isPackageUnfoldable(pkg.getId()));
        tmpPkg.put("id", pkg.getId().toString());
        tmpPkg.put("path", pkg.getDirectoryPath());
        tmpPkg.put("name", pkgName);
        tmpPkg.put("NOF", pckContainsFilesNum);
        tmpPkg.put("LOC", pckContainsFilesLOC);
        tmpPkg.put("LooseDegree", pkg.getLooseDegree());
        tmpPkg.put("label", pkgName);
        tmpPkg.put("nodeType", "package");
        tmpPkg.put("level", "0");
        tmpPkg.put("WMC", pkg.getWmc());
        tmpPkg.put("AMC", pkg.getAmc());
        return tmpPkg;
    }
    @SuppressWarnings("Duplicates")
    JSONArray  dependsToEdges(Map<Map<Package, Package>, Set<DependsOn>> dependsOnBetweenPackages) {
        JSONArray edges = new JSONArray();
        Map<String, Integer> dMap = new HashMap<>();
        for(Map<Package, Package> map: dependsOnBetweenPackages.keySet()){
            JSONObject tmpEdge = new JSONObject();
            int DAtoB = 0;
            int DBtoA = 0;
            double dist = 0.0;
            double distSum = 0;

            for(Package pck: map.keySet()){
                tmpEdge.put("id", pck.getId().toString() + "_" + map.get(pck).getId().toString());
                tmpEdge.put("source", pck.getId().toString());
                tmpEdge.put("target", map.get(pck).getId().toString());
            }
            Set<Long> fileIdSet1 = new HashSet<>();
            Set<Long> fileIdSet2 = new HashSet<>();
            for(DependsOn dependsOn: dependsOnBetweenPackages.get(map)){
                boolean flag = false;
                Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
                for (String type : dependsOnTypes.keySet()) {
                    if (type.equals("EXTENDS") || type.equals("IMPLEMENTS") ||
                            type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
                            || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
                            || type.equals("MEMBER_VARIABLE")) {
                        flag = true;
                        break;
                    }
                }
                if(flag){
                    Coupling coupling = couplingRepository.queryCouplingBetweenTwoFiles(dependsOn.getStartNode().getId()
                            , dependsOn.getEndNode().getId());
                    DAtoB += coupling.getDAtoB();
                    DBtoA += coupling.getDBtoA();
                    dist += coupling.getDist();
                    distSum  += 1;
                    fileIdSet1.add(dependsOn.getStartNode().getId());
                    fileIdSet2.add(dependsOn.getEndNode().getId());
                }
            }
            double fileHMean = calcH(fileIdSet1.size(), fileIdSet2.size());
            tmpEdge.put("dist", dist / distSum);
            tmpEdge.put("dependsOnNum", dependsOnBetweenPackages.get(map).size());
            tmpEdge.put("C", DataUtil.toFixed(fileHMean));
            double logD = Math.max(0, Math.log10(DAtoB));
            tmpEdge.put("D", DAtoB);
            tmpEdge.put("logD", DataUtil.toFixed(logD));
            dMap.put((String)tmpEdge.get("id"), DAtoB);
            edges.add(tmpEdge);
        }
        Set<String> tmp = new HashSet<>();
        for (int i = 0; i < edges.size(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            String reverseId = edge.get("target") + "_" + edge.get("source");
            int D = (int) edge.get("D");
            if (tmp.contains((String) edge.get("id"))) {
                edge.put("I", -1);
                continue;
            }
            if (dMap.containsKey(reverseId)) {
                edge.put("I", calPkgI(D, dMap.get(reverseId)));
                tmp.add(reverseId);
            } else {
                edge.put("I", D);
            }
        }
        return edges;
    }

    @SuppressWarnings("Duplicates")
    public JSONObject unfoldPackagesToFile(JSONArray selectedPackages, List<ProjectFile> allFiles) {
        Set<String> levelSet = new TreeSet<>();
        Map<String, List<JSONObject>> levelTable = new HashMap<>();
        for (int i = 0; i < selectedPackages.size(); i++) {
            JSONObject currentPkg = selectedPackages.getJSONObject(i);
            String level = currentPkg.getString("level");
            levelSet.add(level);
            if (levelTable.containsKey(level)) {
                levelTable.get(level).add(currentPkg);
            } else {
                List<JSONObject> tmp = new ArrayList<>();
                tmp.add(currentPkg);
                levelTable.put(level, tmp);
            }
        }
        JSONArray rowsOfBlocks = new JSONArray();
        for (String level : levelSet) {
            JSONArray currRow = new JSONArray();
            List<JSONObject> levelPackages = levelTable.get(level);
            for (JSONObject pkg : levelPackages) {
                GraphBlock graphBlock = unfoldOnePackageToFile(pkg);
                String comboLabel = FileUtil.extractPackagePath(packageRepository.findPackageById(pkg.getLong("id")).getDirectoryPath(), false);
                graphBlock.setLabel(comboLabel);
//                graphBlock.setLabel(packageRepository.findPackageById(pkg.getLong("id")).getName());
                currRow.add(graphBlock);
            }
            rowsOfBlocks.add(currRow);
        }
        JSONObject res = new JSONObject();
        res.put("blocks", rowsOfBlocks);
        List<DependsOn> allDepends = getDependsBetweenFiles(allFiles);
        res.put("edges", fileDependsToEdges(allDepends));
        return res;
    }

    @SuppressWarnings("Duplicates")
    public JSONObject unfoldPackages(JSONArray selectedPackages, JSONArray otherPackages, List<Package> allPackages) {
        Set<String> levelSet = new TreeSet<>();
        Map<String, List<JSONObject>> levelTable = new HashMap<>();
        for (int i = 0; i < selectedPackages.size(); i++) {
            JSONObject currentPkg = selectedPackages.getJSONObject(i);
            String level = currentPkg.getString("level");
            levelSet.add(level);
            currentPkg.put("unfold", true);
            if (levelTable.containsKey(level)) {
                levelTable.get(level).add(currentPkg);
            } else {
                List<JSONObject> tmp = new ArrayList<>();
                tmp.add(currentPkg);
                levelTable.put(level, tmp);
            }
        }
        for (int i = 0; i < otherPackages.size(); i++) {
            JSONObject currentPkg = otherPackages.getJSONObject(i);
            String level = currentPkg.getString("level");
            levelSet.add(level);
            currentPkg.put("unfold", false);
            if (levelTable.containsKey(level)) {
                levelTable.get(level).add(currentPkg);
            } else {
                List<JSONObject> tmp = new ArrayList<>();
                tmp.add(currentPkg);
                levelTable.put(level, tmp);
            }
        }
        int totalRows = levelSet.size();
        JSONArray rowsOfBlocks = new JSONArray();
        for (String level : levelSet) {
            JSONArray currRow = new JSONArray();
            List<JSONObject> levelPackages = levelTable.get(level);
            for (JSONObject pkg : levelPackages) {
                GraphBlock graphBlock = new GraphBlock();
                if (pkg.getBoolean("unfold")) {
                    // unfold pkg, get inside dependencies and child pkgs with level
                    graphBlock = unfoldOnePackageOneStep(pkg);
                    String comboLabel = FileUtil.extractPackagePath(packageRepository.findPackageById(pkg.getLong("id")).getDirectoryPath(), false);
                    graphBlock.setLabel(comboLabel);
                } else {
                    pkg.put("level", level + 0);
                    graphBlock.setHeight(1);
                    graphBlock.setWidth(1);
                    JSONArray pkgs = new JSONArray();
                    JSONArray inside = new JSONArray();
                    inside.add(pkg);
                    pkgs.add(inside);
                    graphBlock.setPackages(pkgs);
                    graphBlock.setLevels(1);
                    graphBlock.setLabel(pkg.getString("name"));
                }
                currRow.add(graphBlock);
            }
            rowsOfBlocks.add(currRow);
        }
        JSONObject res = new JSONObject();
        res.put("blocks", rowsOfBlocks);
//        Map<Map<Package, Package>, Set<DependsOn>> allDepends = getDependsBetweenPackages(allPackages);
//        res.put("edges", dependsToEdges(allDepends));
        res.put("edges", getEdgesBetweenPackages(allPackages));
        return res;
    }

    @Override
    public JSONObject getTopLevelPackages() {
        JSONObject result = new JSONObject();
        List<Package> topLevelPackages = packageRepository.findPackagesAtDepth1();
        JSONArray nodes = new JSONArray();
        for (Package pkg : topLevelPackages) {
            nodes.add(pkgToNode(pkg));
        }
//        Map<Map<Package, Package>, Set<DependsOn>> allDepends = getDependsBetweenPackages(topLevelPackages);
//        JSONArray edges = dependsToEdges(allDepends);
        result.put("nodes", nodes);
//        result.put("edges", edges);
        result.put("edges", getEdgesBetweenPackages(topLevelPackages));
        return result;
    }

    List<DependsOn> getDependsBetweenFiles(List<ProjectFile> files) {
        Set<Long> fileIdSet = files.parallelStream().map(ProjectFile::getId).collect(Collectors.toSet());
        Set<DependsOn> result = new HashSet<>();
        for (long fileId : fileIdSet) {
            List<DependsOn> allDependsOn = dependsOnRepository.findOneFileAllDependsOn(fileId);
            allDependsOn.parallelStream().forEach(dependsOn -> {
                Long otherId;
                if (dependsOn.getStartNode().getId() == fileId) {
                    otherId = dependsOn.getEndNodeGraphId();
                } else {
                    otherId = dependsOn.getStartNodeGraphId();
                }
                if (fileIdSet.contains(otherId)) {
                    result.add(dependsOn);
                }
            });
        }
        return new ArrayList<>(result);
    }
    private GraphBlock unfoldOnePackageToFile(JSONObject pkgJson) {
        GraphBlock graphBlock = new GraphBlock();
        JSONArray nodes = new JSONArray();
//        Map<Map<Package, Package>, Set<DependsOn>> dependsOnBetweenPackages = new HashMap<>();
        List<ProjectFile> childFiles = projectFileRepository.findPackageContainedAllFiles(pkgJson.getLong("id"));
        List<DependsOn> dependsOnBetweenFiles = getDependsBetweenFiles(childFiles);

        for (ProjectFile file : childFiles) {
            nodes.add(fileToNode(file, pkgJson.getLong("id")));
        }

        JSONArray edges = fileDependsToEdges(dependsOnBetweenFiles);

        if (edges.size() == 0) {
            int width = (int) Math.ceil(Math.sqrt(nodes.size()));
            graphBlock.setWidth(width);
            graphBlock.setHeight(width);
            JSONArray leveledNodes = new JSONArray();
            leveledNodes.add(nodes);
            for (int i = 0; i < nodes.size(); i++) {
                nodes.getJSONObject(i).put("level", pkgJson.getString("level")+0);
            }
            graphBlock.setPackages(leveledNodes);
        } else {
            GraphLayoutUtil layoutUtil = new GraphLayoutUtil(nodes, edges);
            JSONArray leveledNodes = layoutUtil.levelLayout2(pkgJson.getString("level"));
            int width = 0;
            for (int i = 0; i < leveledNodes.size(); i++) {
                width = Math.max(width, leveledNodes.getJSONArray(i).size());
            }
            graphBlock.setPackages(leveledNodes);
            graphBlock.setWidth(width);
            graphBlock.setLevels(leveledNodes.size());
            graphBlock.setHeight(leveledNodes.size());
        }
        return graphBlock;
    }

    private JSONObject fileToNode(ProjectFile projectFile, Long parentId) {
        JSONObject fileTmp = new JSONObject();
        fileTmp.put("id", projectFile.getId().toString());
        fileTmp.put("parentPckId", parentId);
        fileTmp.put("name", projectFile.getName());
        fileTmp.put("label", projectFile.getName());
        fileTmp.put("path", projectFile.getPath());
        fileTmp.put("LOC", projectFile.getLoc());
        fileTmp.put("nodeType", "file");
        fileTmp.put("level", 0);
        fileTmp.put("WMC", projectFile.getWmc());
        fileTmp.put("AMC", projectFile.getAmc());
        return fileTmp;
    }
    JSONArray getEdgesBetweenPackages(List<Package> packages) {
//        Map<String, Integer> dMap = new HashMap<>();
        JSONArray edges = new JSONArray();
        for (int i = 0; i < packages.size(); i++) {
            for (int j = i+1; j < packages.size(); j++) {
                long id1 = packages.get(i).getId();
                long id2 = packages.get(j).getId();
                List<Coupling> couplings = couplingRepository.queryCouplingBetweenTwoPkgs(id1, id2);
                if (!couplings.isEmpty()) {
                    for (Coupling coupling : couplings) {
                        JSONObject tmpEdge = new JSONObject();
                        tmpEdge.put("id", coupling.getStartNodeGraphId() + "_" + coupling.getEndNodeGraphId());
                        tmpEdge.put("source", String.valueOf(coupling.getStartNodeGraphId()));
                        tmpEdge.put("target", String.valueOf(coupling.getEndNodeGraphId()));
                        tmpEdge.put("dist", coupling.getDist());
                        tmpEdge.put("C", coupling.getC());
                        tmpEdge.put("I", coupling.getI());
                        double logD = Math.max(0, Math.log10(coupling.getDAtoB()));
                        tmpEdge.put("D", coupling.getDAtoB());
                        boolean isExtendOrImplements = coupling.getDependsOnTypeStartToEnd().contains("EXTENDS")
                                || coupling.getDependsOnTypeStartToEnd().contains("IMPLEMENTS");
                        tmpEdge.put("detail", coupling.getDependsOnTypeStartToEnd());
                        tmpEdge.put("isExtendOrImplements", isExtendOrImplements);
//                        dMap.put(tmpEdge.getString("id"), coupling.getDAtoB());
                        tmpEdge.put("logD", DataUtil.toFixed(logD));
                        edges.add(tmpEdge);
                    }
                }


            }
        }
//        Set<String> tmp = new HashSet<>();
//        for (int i = 0; i < edges.size(); i++) {
//            JSONObject edge = edges.getJSONObject(i);
//            String reverseId = edge.get("target") + "_" + edge.get("source");
//            int D = (int) edge.get("D");
//            if (tmp.contains((String) edge.get("id"))) {
//                edge.put("I", -1);
//                continue;
//            }
//            if (dMap.containsKey(reverseId)) {
//                edge.put("I", calPkgI(D, dMap.get(reverseId)));
//                tmp.add(reverseId);
//            } else {
//                edge.put("I", D);
//            }
//        }
        return edges;
    }
    @SuppressWarnings("Duplicates")
    private GraphBlock unfoldOnePackageOneStep(JSONObject pkgJson) {
        GraphBlock graphBlock = new GraphBlock();
        JSONArray nodes = new JSONArray();
//        Map<Map<Package, Package>, Set<DependsOn>> dependsOnBetweenPackages = new HashMap<>();
        List<Package> childPkgs = packageRepository.findOneStepPackagesById(pkgJson.getLong("id"));
//        Map<Map<Package, Package>, Set<DependsOn>> dependsOnBetweenPackages = getDependsBetweenPackages(childPkgs);
        for (Package pkg : childPkgs) {
            nodes.add(pkgToNode(pkg));
        }
//        JSONArray edges = dependsToEdges(dependsOnBetweenPackages);
        JSONArray edges = getEdgesBetweenPackages(childPkgs);
        if (edges.size() == 0) {
            int width = (int) Math.ceil(Math.sqrt(nodes.size()));
            graphBlock.setWidth(width);
            graphBlock.setHeight(width);
            JSONArray leveledNodes = new JSONArray();
            leveledNodes.add(nodes);
            for (int i = 0; i < nodes.size(); i++) {
                nodes.getJSONObject(i).put("level", pkgJson.getString("level")+0);
            }
            graphBlock.setPackages(leveledNodes);
        } else {
            GraphLayoutUtil layoutUtil = new GraphLayoutUtil(nodes, edges);
            JSONArray leveledNodes = layoutUtil.levelLayout2(pkgJson.getString("level"));
            int width = 0;
            for (int i = 0; i < leveledNodes.size(); i++) {
                width = Math.max(width, leveledNodes.getJSONArray(i).size());
            }
            graphBlock.setPackages(leveledNodes);
            graphBlock.setWidth(width);
            graphBlock.setLevels(leveledNodes.size());
            graphBlock.setHeight(leveledNodes.size());
        }
        return graphBlock;
    }
//    @SuppressWarnings("Duplicates")
//    @Override
//    @Deprecated
//    public JSONObject getChildPackagesCouplingValue(Map<Package, List<Package>> unfoldPckMap, JSONArray otherPkgJsonArray, Map<Long, Integer> levelMap) {
//        JSONObject result = new JSONObject();
//        JSONArray allNodes = new JSONArray();
//        JSONArray allEdges = new JSONArray();
//        for (int i = 0; i < otherPkgJsonArray.size(); i++) {
//            JSONObject node = otherPkgJsonArray.getJSONObject(i);
//            node.put("pLevel", node.getIntValue("level"));
//        }
//        for (Map.Entry<Package, List<Package>> entry : unfoldPckMap.entrySet()) {
//            Map<Map<Package, Package>, Set<DependsOn>> dependsOnBetweenPackages = new HashMap<>();
//            JSONArray nodes = new JSONArray();
//            Package parentPkg = entry.getKey();
//            List<Package> childPkgs = entry.getValue();
//            for (Package pkg : childPkgs) {
//                JSONObject tmpPkg = pkgToNode(pkg, parentPkg, levelMap.get(parentPkg.getId()));
//                nodes.add(tmpPkg);
//                List<Map<Package, List<DependsOn>>> tmpList = getGroupInsideAndOutDependsOnByPackage(pkg, childPkgs, parentPkg);
//                Map<Package, List<DependsOn>> GroupInsideToOutDependsOns = tmpList.get(0);
//                Map<Package, List<DependsOn>> GroupOutToInsideDependsOns = tmpList.get(1);
//                if (GroupInsideToOutDependsOns.size() > 0) {
//                    for(Package endPackage: GroupInsideToOutDependsOns.keySet()){
//                        for(DependsOn dependsOn: GroupInsideToOutDependsOns.get(endPackage)){
//                            Map<Package, Package> pkgDependsOnTmp = new HashMap<>();
//                            pkgDependsOnTmp.put(pkg, endPackage);
//                            if (dependsOnBetweenPackages.containsKey(pkgDependsOnTmp)) {
//                                dependsOnBetweenPackages.get(pkgDependsOnTmp).add(dependsOn);
//                            } else {
//                                Set<DependsOn> dependsOnsListTmp = new HashSet<>();
//                                dependsOnsListTmp.add(dependsOn);
//                                dependsOnBetweenPackages.put(pkgDependsOnTmp, dependsOnsListTmp);
//                            }
//                        }
//                    }
//                }
//                if (GroupOutToInsideDependsOns.size() > 0) {
//                    for(Package startPackage: GroupOutToInsideDependsOns.keySet()){
//                        for(DependsOn dependsOn: GroupOutToInsideDependsOns.get(startPackage)){
//                            Map<Package, Package> pkgDependsOnTmp = new HashMap<>();
//                            pkgDependsOnTmp.put(startPackage, pkg);
//                            if (dependsOnBetweenPackages.containsKey(pkgDependsOnTmp)) {
//                                dependsOnBetweenPackages.get(pkgDependsOnTmp).add(dependsOn);
//                            } else {
//                                Set<DependsOn> dependsOnsListTmp = new HashSet<>();
//                                dependsOnsListTmp.add(dependsOn);
//                                dependsOnBetweenPackages.put(pkgDependsOnTmp, dependsOnsListTmp);
//                            }
//                        }
//                    }
//                }
//            }
//            JSONArray edges = dependsToEdges(dependsOnBetweenPackages);
//            if (edges.size() == 0) {
////                result.put(String.valueOf(parentPkg.getId()), nodes);
//                allNodes.addAll(nodes);
//            } else {
//                GraphLayoutUtil layoutUtil = new GraphLayoutUtil(nodes, edges);
//                JSONArray leveledNodes = layoutUtil.levelLayout();
////                result.put(String.valueOf(parentPkg.getId()), leveledNodes);
//                allEdges.addAll(edges);
//                allNodes.addAll(leveledNodes);
//            }
//        }
//        allNodes.addAll(otherPkgJsonArray);
//        List<Package> topLevelPackages = new ArrayList<>();
//        unfoldPckMap.forEach((k, v) -> topLevelPackages.addAll(v));
//        List<Package> otherPkgs = new ArrayList<>();
//        for (int i = 0; i < otherPkgJsonArray.size(); i++) {
//            JSONObject pkgJson = otherPkgJsonArray.getJSONObject(i);
//            Long pkgId = pkgJson.getLong("id");
//            levelMap.put(pkgId, pkgJson.getIntValue("level"));
//            Package pkg = packageRepository.findPackageById(pkgId);
//            otherPkgs.add(pkg);
//        }
//        topLevelPackages.addAll(otherPkgs);
//        Map<Map<Package, Package>, Set<DependsOn>> dependsOn = getDependsBetweenPackages(topLevelPackages);
//        JSONArray otherEdges = dependsToEdges(dependsOn);
//        allEdges.addAll(otherEdges);
//        result.put("edges", allEdges);
//        result.put("nodes", allNodes);
//        return result;
//    }

    @SuppressWarnings("Duplicates")
    public Map<Map<Package, Package>, Set<DependsOn>> getDependsBetweenPackages(List<Package> packages) {
        Map<Map<Package, Package>, Set<DependsOn>> result = new HashMap<>();
        for (int i = 0; i < packages.size(); i++) {
            Package pkg1 = packages.get(i);
            for (int j = i + 1; j < packages.size(); j++) {
                Package pkg2 = packages.get(j);

                List<DependsOn> outDepends = dependsOnRepository.findAllDependsOnBetweenPackages(pkg1.getId(), pkg2.getId());
                List<DependsOn> inDepends = dependsOnRepository.findAllDependsOnBetweenPackages(pkg2.getId(), pkg1.getId());
                if (!outDepends.isEmpty()) {
                    Map<Package, Package> key = new HashMap<>();
                    key.put(pkg1, pkg2);
                    if (result.containsKey(key)) {
                        result.get(key).addAll(outDepends);
                    } else {
                        result.put(key, new HashSet<>(outDepends));
                    }
                }
                if (!inDepends.isEmpty()) {
                    Map<Package, Package> key = new HashMap<>();
                    key.put(pkg2, pkg1);
                    if (result.containsKey(key)) {
                        result.get(key).addAll(inDepends);
                    } else {
                        result.put(key, new HashSet<>(inDepends));
                    }
                }
            }
        }
        return result;
    }
    private List<List<DependsOn>> getGroupInsideAndOutDependsOnByFileIds(List<Long> fileIdList){
        List<List<DependsOn>> result = new ArrayList<>();
        List<DependsOn> GroupInsideDependsOns = new ArrayList<>();
        List<DependsOn> GroupInsideToOutDependsOns = new ArrayList<>();
        List<DependsOn> GroupOutToInsideDependsOns = new ArrayList<>();


        for(long fileId : fileIdList){
            long endNodeId;
            boolean direction;
            List<DependsOn> allDependsOn = dependsOnRepository.findOneFileAllDependsOn(fileId);

            for(DependsOn dependsOn: allDependsOn){
                if(dependsOn.getStartNode().getId() == fileId){
                    endNodeId = dependsOn.getEndNode().getId();
                    direction = true;
                }else{
                    endNodeId = dependsOn.getStartNode().getId();
                    direction = false;
                }

                if(fileIdList.contains(endNodeId) && !GroupInsideDependsOns.contains(dependsOn)){
                    GroupInsideDependsOns.add(dependsOn);
                }else{
                    if(direction && !GroupInsideToOutDependsOns.contains(dependsOn)){
                        GroupInsideToOutDependsOns.add(dependsOn);
                    }else if(!GroupOutToInsideDependsOns.contains(dependsOn)){
                        GroupOutToInsideDependsOns.add(dependsOn);
                    }
                }
            }
        }
        result.add(GroupInsideDependsOns);
        result.add(GroupInsideToOutDependsOns);
        result.add(GroupOutToInsideDependsOns);
        return result;
    }
    private List<Map<Package, List<DependsOn>>> getGroupInsideAndOutDependsOnByPackage(Package mainPackage, List<Package> pckList){
        List<Map<Package, List<DependsOn>>> result = new ArrayList<>();
        Map<Package, List<DependsOn>> GroupInsideToOutDependsOns = new HashMap<>();
        Map<Package, List<DependsOn>> GroupOutToInsideDependsOns = new HashMap<>();

        for(Package pck: pckList){
            if(!pck.equals(mainPackage)){
                List<DependsOn> tmpInsideToOutDependsOn;
                List<DependsOn> tmpOutToInsideDependsOn;
                tmpInsideToOutDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackages(mainPackage.getId(), pck.getId()));
                tmpOutToInsideDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackages(pck.getId(), mainPackage.getId()));
                if(tmpInsideToOutDependsOn.size() != 0){
                    GroupInsideToOutDependsOns.put(pck, tmpInsideToOutDependsOn);
                }

                if(tmpOutToInsideDependsOn.size() != 0){
                    GroupOutToInsideDependsOns.put(pck, tmpOutToInsideDependsOn);
                }
            }
        }

        result.add(GroupInsideToOutDependsOns);
        result.add(GroupOutToInsideDependsOns);

        return result;
    }
    private List<Map<Package, List<DependsOn>>> getGroupInsideAndOutDependsOnByPackage(Package mainPackage, List<Package> pckList,
                                                                                       Package parentPackage){
        List<Map<Package, List<DependsOn>>> result = new ArrayList<>();
        Map<Package, List<DependsOn>> GroupInsideToOutDependsOns = new HashMap<>();
        Map<Package, List<DependsOn>> GroupOutToInsideDependsOns = new HashMap<>();

        for(Package pck: pckList){
            if(!pck.equals(mainPackage)){
                List<DependsOn> tmpInsideToOutDependsOn;
                List<DependsOn> tmpOutToInsideDependsOn;

//                if(mainPackage.getId().equals(parentPackage.getId())){
//                    tmpInsideToOutDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenMainPackageToPackage(mainPackage.getId(), pck.getId()));
//                    tmpOutToInsideDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackageToMainPackage(mainPackage.getId(), pck.getId()));
//                }else if(pck.getId().equals(parentPackage.getId())){
//                    tmpOutToInsideDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenMainPackageToPackage(pck.getId(), mainPackage.getId()));
//                    tmpInsideToOutDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackageToMainPackage(pck.getId(), mainPackage.getId()));
////                    tmpInsideToOutDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenMainPackageToPackage(mainPackage.getId(), pck.getId()));
////                    tmpOutToInsideDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackageToMainPackage(mainPackage.getId(), pck.getId()));
////                    tmpInsideToOutDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenMainPackageToPackage(pck.getId(), mainPackage.getId()));
////                    tmpOutToInsideDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackageToMainPackage(pck.getId(), mainPackage.getId()));
//                }else{
//                    tmpInsideToOutDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackages(mainPackage.getId(), pck.getId()));
//                    tmpOutToInsideDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackages(pck.getId(), mainPackage.getId()));
//                }
                tmpInsideToOutDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackages(mainPackage.getId(), pck.getId()));
                tmpOutToInsideDependsOn = new ArrayList<>(dependsOnRepository.findAllDependsOnBetweenPackages(pck.getId(), mainPackage.getId()));
                if(tmpInsideToOutDependsOn.size() != 0){
                    GroupInsideToOutDependsOns.put(pck, tmpInsideToOutDependsOn);
                }

                if(tmpOutToInsideDependsOn.size() != 0){
                    GroupOutToInsideDependsOns.put(pck, tmpOutToInsideDependsOn);
                }
            }
        }

        result.add(GroupInsideToOutDependsOns);
        result.add(GroupOutToInsideDependsOns);

        return result;
    }

    public double calcH(int pkg1Files, int pkg2Files) {
        return 2 * (double)pkg1Files * (double)pkg2Files / ((double)pkg1Files + (double)pkg2Files);
    }

    @Override
    public JSONObject getChildPkgsCouplingValue(Long pkgId) {
        JSONObject result = new JSONObject();
        List<Package> packages = packageRepository.findOneStepPackagesById(pkgId);
        JSONArray edges = getEdgesBetweenPackages(packages);
        result.put("edges", edges);
        result.put("code", 200);
        return result;
    }
}
