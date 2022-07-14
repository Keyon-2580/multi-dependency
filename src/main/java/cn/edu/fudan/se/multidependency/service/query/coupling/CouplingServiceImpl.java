package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.coupling.CouplingRepository;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.csvreader.CsvWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Service
public class CouplingServiceImpl implements CouplingService {

    @Autowired
    public CouplingRepository couplingRepository;

    @Autowired
    public DependsOnRepository dependsOnRepository;

    @Autowired
    public ProjectFileRepository projectFileRepository;

//    @Override
//    public void calCouplingValue(String couplingValuePath) throws IOException {
//        CsvWriter csvWriter = new CsvWriter(couplingValuePath, ',', StandardCharsets.UTF_8);
//        String[]  headers = {"file1.id", "file1.path", "file2.id", "file2.path", "MA(A->B)", "MB(A->B)",
//                "MA(B->A)", "MB(B->A)", "D(A->B)", "D(B->A)", "C(A->B)", "C(B->A)", "C(A,B)", "U(A->B)", "U(B->A)",
//                "I(A,B)", "Disp(A,B)", "DependsOnType(A->B)", "DependsOnType(B->A)"};
//        csvWriter.writeRecord(headers);
//
//        List<DependsOn> allDependsOn = dependsOnRepository.findFileDepends();
//        HashSet<String> allFilesPair = new HashSet<>();
//
//        for(DependsOn dependsOn: allDependsOn){
//            long file1Id = dependsOn.getStartNode().getId();
//            long file2Id = dependsOn.getEndNode().getId();
//            ProjectFile file1 = (ProjectFile) dependsOn.getStartNode();
//            ProjectFile file2 = (ProjectFile) dependsOn.getEndNode();
//            String filesPair = file1Id + "_" + file2Id;
//            String filesPairReverse = file2Id + "_" + file1Id;
//
//            if(!allFilesPair.contains(filesPair) && !allFilesPair.contains(filesPairReverse)){
//                allFilesPair.add(filesPair);
//                System.out.println(file1Id + "  " + file2Id);
//
//                int funcNumAAtoB = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file1Id, file2Id);
//                int funcNumBAtoB = couplingRepository.queryTwoFilesDependsByFunctionsNum(file1Id, file2Id);
//                int funcNumABtoA = couplingRepository.queryTwoFilesDependsByFunctionsNum(file2Id, file1Id);
//                int funcNumBBtoA = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file2Id, file1Id);
//
//                DependsOn dependsOnAtoB = dependsOnRepository.findDependsOnBetweenFiles(file1Id, file2Id);
//                DependsOn dependsOnBtoA = dependsOnRepository.findDependsOnBetweenFiles(file2Id, file1Id);
//                String typesAndTimesAtoB = "";
//                String typesAndTimesBtoA = "";
//
//                if(dependsOnAtoB != null){
//                    typesAndTimesAtoB = getRelationTypeAndTimes(dependsOnAtoB.getDependsOnTypes());
//                }
//
//                if(dependsOnBtoA != null){
//                    typesAndTimesBtoA = getRelationTypeAndTimes(dependsOnBtoA.getDependsOnTypes());
//                }
//
//                long dependsOntimesAtoB = 0;
//                long dependsOntimesBtoA = 0;
//
//                if(dependsOnAtoB != null) {
//                    Map<String, Long> dependsOnTypesAtoB = dependsOnAtoB.getDependsOnTypes();
//
//                    for (String type : dependsOnTypesAtoB.keySet()) {
//                        if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN")
//                                || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("IMPLEMENTS")
//                                || type.equals("MEMBER_VARIABLE")) {
//                            dependsOntimesAtoB += dependsOnTypesAtoB.get(type);
//                        }
//                    }
//                }
//
//                if(dependsOnBtoA != null) {
//                    Map<String, Long> dependsOnTypesBtoA = dependsOnBtoA.getDependsOnTypes();
//
//                    for (String type : dependsOnTypesBtoA.keySet()) {
//                        if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN")
//                                || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("IMPLEMENTS")
//                                || type.equals("MEMBER_VARIABLE")) {
//                            dependsOntimesBtoA += dependsOnTypesBtoA.get(type);
//                        }
//                    }
//                }
//
//                if(dependsOntimesAtoB != 0 || dependsOntimesBtoA != 0) {
//                    double C_AtoB = calC1to2(funcNumAAtoB,funcNumBAtoB);
//                    double C_BtoA = calC1to2(funcNumABtoA,funcNumBBtoA);
//                    double C_AandB = calC(C_AtoB, C_BtoA);
//                    double U_AtoB = calU1to2(dependsOntimesAtoB, dependsOntimesBtoA);
//                    double U_BtoA = calU1to2(dependsOntimesBtoA, dependsOntimesAtoB);
//                    double I_AandB = calI(dependsOntimesAtoB, dependsOntimesBtoA);
//                    double disp_AandB = calDISP(C_AandB, dependsOntimesAtoB, dependsOntimesBtoA);
//
//                    String[] content = {Long.toString(file1Id), file1.getPath(), Long.toString(file2Id), file2.getPath(),
//                            String.valueOf(funcNumAAtoB), String.valueOf(funcNumBAtoB), String.valueOf(funcNumABtoA), String.valueOf(funcNumBBtoA),
//                            String.valueOf(dependsOntimesAtoB), String.valueOf(dependsOntimesBtoA), String.valueOf(C_AtoB), String.valueOf(C_BtoA),
//                            String.valueOf(C_AandB), String.valueOf(U_AtoB), String.valueOf(U_BtoA), String.valueOf(I_AandB), String.valueOf(disp_AandB),
//                            typesAndTimesAtoB, typesAndTimesBtoA};
//
//                    csvWriter.writeRecord(content);
//                }
//            }
//        }
//        csvWriter.close();
//    }

    @Override
    public void calGroupCouplingValue(List<Long> fileIdList, String couplingValuePath) throws IOException {
//        CsvWriter csvWriter = new CsvWriter(couplingValuePath, ',', StandardCharsets.UTF_8);
//        String[]  headers = {"file1.id", "file1.path", "file2.id", "file2.path", "C(A,B)", "I(A,B)",
//                "DependsOnType(A->B)", "DependsOnType(B->A)"};
//        csvWriter.writeRecord(headers);

        double CInsideSum, CInsideAvg, IInsideSum, IInsideAvg;
        double CInsideToOutSum, CInsideToOutAvg, IInsideToOutSum, IInsideToOutAvg;
        double COutToInsideSum, COutToInsideAvg, IOutToInsideSum, IOutToInsideAvg;

        List<List<DependsOn>> listTmp = getGroupInsideAndOutDependsOn(fileIdList);
        List<DependsOn> GroupInsideDependsOns = listTmp.get(0);
        List<DependsOn> GroupInsideToOutDependsOns = listTmp.get(1);
        List<DependsOn> GroupOutToInsideDependsOns = listTmp.get(2);

        List<Double> resultInside = calGroupCI(GroupInsideDependsOns);
        Map<Long, Integer> instablityTimes = new HashMap<>();
        CInsideSum = resultInside.get(0);
        IInsideSum = resultInside.get(1);

        CInsideAvg = CInsideSum / (double)GroupInsideDependsOns.size();
        IInsideAvg = IInsideSum / (double)GroupInsideDependsOns.size();
        String report = "Inside  CSum:" + CInsideSum + " CAvg:" + CInsideAvg + " ISum:" + IInsideSum + " IAvg:" + IInsideAvg;
        System.out.println(report);
//        String[] content = {report};

//        csvWriter.writeRecord(content);

        List<Double> resultInsideToOut = calGroupCI(GroupInsideToOutDependsOns);
        CInsideToOutSum = resultInsideToOut.get(0);
        IInsideToOutSum = resultInsideToOut.get(1);

        CInsideToOutAvg = CInsideToOutSum / (double)GroupInsideToOutDependsOns.size();
        IInsideToOutAvg = IInsideToOutSum / (double)GroupInsideToOutDependsOns.size();
        report = "InsideToOut  CSum:" + CInsideToOutSum + " CAvg:" + CInsideToOutAvg + " ISum:"
                + IInsideToOutSum + " IAvg:" + IInsideToOutAvg;
        System.out.println(report);
//        content = new String[]{report};
//        csvWriter.writeRecord(content);

        List<Double> resultOutToInside = calGroupCI(GroupOutToInsideDependsOns);
        COutToInsideSum = resultOutToInside.get(0);
        IOutToInsideSum = resultOutToInside.get(1);

        COutToInsideAvg = COutToInsideSum / (double)GroupOutToInsideDependsOns.size();
        IOutToInsideAvg = IOutToInsideSum / (double)GroupOutToInsideDependsOns.size();
        report = "OutToInside  CSum:" + COutToInsideSum + " CAvg:" + COutToInsideAvg + " ISum:"
                + IOutToInsideSum + " IAvg:" + IOutToInsideAvg;
        System.out.println(report);
//        content = new String[]{report};
//        csvWriter.writeRecord(content);
//
//        csvWriter.close();
    }

    @Override
    public Map<ProjectFile, Double> calGroupInstablity(List<Long> fileIdList){
        List<List<DependsOn>> listTmp = getGroupInsideAndOutDependsOn(fileIdList);
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
        return (2 * ((double)funcNum1 + 1) * ((double)funcNum2 + 1)) / ((double)funcNum1 + (double)funcNum2 + 2) - 1;
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
        return (2 * ((double)dependsOntimes1 + 1) * ((double)dependsOntimes2 + 1)) / ((double)dependsOntimes1 + (double)dependsOntimes2 + 2) - 1;
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
    public double calDependsOnI(DependsOn dependsOnAtoB, DependsOn dependsOnBtoA){
        long dependsOntimesAtoB = 0;
        long dependsOntimesBtoA = 0;

        if(dependsOnAtoB != null) {
            Map<String, Long> dependsOnTypesAtoB = dependsOnAtoB.getDependsOnTypes();

            for (String type : dependsOnTypesAtoB.keySet()) {
                if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN")
                        || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("IMPLEMENTS")
                        || type.equals("MEMBER_VARIABLE")) {
                    dependsOntimesAtoB += dependsOnTypesAtoB.get(type);
                }
            }
        }

        if(dependsOnBtoA != null) {
            Map<String, Long> dependsOnTypesBtoA = dependsOnBtoA.getDependsOnTypes();

            for (String type : dependsOnTypesBtoA.keySet()) {
                if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN")
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
    public List<List<DependsOn>> getGroupInsideAndOutDependsOn(List<Long> fileIdList){
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

                if(fileIdList.contains(endNodeId)){
                    GroupInsideDependsOns.add(dependsOn);
                }else{
                    if(direction){
                        GroupInsideToOutDependsOns.add(dependsOn);
                    }else{
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

    @Override
    public JSONObject getCouplingValueByFileIds(List<Long> fileIds){
        JSONObject result = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();

        List<List<DependsOn>> listTmp = getGroupInsideAndOutDependsOn(fileIds);
        List<DependsOn> GroupInsideDependsOns = listTmp.get(0);
        Map<ProjectFile, Double> instability = calGroupInstablity(fileIds);

        for (ProjectFile projectFile : instability.keySet()) {
            JSONObject fileTmp = new JSONObject();
            fileTmp.put("id", projectFile.getId().toString());
            fileTmp.put("name", projectFile.getName());
            fileTmp.put("label", projectFile.getName());
            fileTmp.put("path", projectFile.getPath());
            fileTmp.put("instability", instability.get(projectFile));
            nodes.add(fileTmp);
        }
        result.put("nodes", nodes);

        for(DependsOn dependsOn: GroupInsideDependsOns){
            JSONObject dependsOnTmp = new JSONObject();
            dependsOnTmp.put("source", dependsOn.getStartNode().getId().toString());
            dependsOnTmp.put("target", dependsOn.getEndNode().getId().toString());
            dependsOnTmp.put("dependsOnTypes", dependsOn.getDependsOnType());
            dependsOnTmp.put("isExtendOrImplements", dependsOnRepository.findDependsOnIsExtendOrImplements(dependsOn.getId()));
            edges.add(dependsOnTmp);
        }
        result.put("edges", edges);

        return result;
    }
}
