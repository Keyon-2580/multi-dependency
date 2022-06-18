package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.coupling.CouplingRepository;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@Service
public class CouplingServiceImpl implements CouplingService {

    @Autowired
    public CouplingRepository couplingRepository;

    @Autowired
    public DependsOnRepository dependsOnRepository;

    @Override
    public void calCouplingValue() throws IOException {
        CsvReader csvReader = new CsvReader("/Users/fulco/Desktop/Data/FDUPro/毕设/数据/依赖文件对统计(tomcat).csv", ',', StandardCharsets.UTF_8);
        CsvWriter csvWriter = new CsvWriter("/Users/fulco/Desktop/Data/FDUPro/毕设/数据/依赖文件对统计(tomcat)_import.csv", ',', StandardCharsets.UTF_8);

        String[]  headers = {"file1.id", "file1.path", "file2.id", "file2.path", "MA(A->B)", "MB(A->B)",
                "MA(B->A)", "MB(B->A)", "D(A->B)", "D(B->A)"};
        csvWriter.writeRecord(headers);

        csvReader.readHeaders();

        // 读取每行的内容
        while (csvReader.readRecord()) {
            // 获取内容的两种方式
            // 1. 通过下标获取
//            System.out.print(csvReader.get(0));

            long file1Id = Long.parseLong(csvReader.get(0));
            long file2Id = Long.parseLong(csvReader.get(2));
            System.out.println(file1Id + "  " + file2Id);

            int funcNumAAtoB = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file1Id, file2Id);
            int funcNumBAtoB = couplingRepository.queryTwoFilesDependsByFunctionsNum(file1Id, file2Id);
            int funcNumABtoA = couplingRepository.queryTwoFilesDependsByFunctionsNum(file2Id, file1Id);
            int funcNumBBtoA = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file2Id, file1Id);

            DependsOn dependsOnAtoB = dependsOnRepository.findDependsOnBetweenFiles(file1Id, file2Id);
            DependsOn dependsOnBtoA = dependsOnRepository.findDependsOnBetweenFiles(file2Id, file1Id);
            long dependsOntimesAtoB = 0;
            long dependsOntimesBtoA = 0;

            if(dependsOnAtoB != null) {
                Map<String, Long> dependsOnTypesAtoB = dependsOnAtoB.getDependsOnTypes();

                for (String type : dependsOnTypesAtoB.keySet()) {
                    if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN")
                            || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("MEMBER_VARIABLE")) {
                        dependsOntimesAtoB += dependsOnTypesAtoB.get(type);
                    }
                }
            }

            if(dependsOnBtoA != null) {
                Map<String, Long> dependsOnTypesBtoA = dependsOnBtoA.getDependsOnTypes();

                for (String type : dependsOnTypesBtoA.keySet()) {
                    if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN")
                            || type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("MEMBER_VARIABLE")) {
                        dependsOntimesBtoA += dependsOnTypesBtoA.get(type);
                    }
                }
            }

            String[] content = {csvReader.get(0), csvReader.get(1), csvReader.get(2), csvReader.get(3),
                    String.valueOf(funcNumAAtoB), String.valueOf(funcNumBAtoB), String.valueOf(funcNumABtoA), String.valueOf(funcNumBBtoA),
                    String.valueOf(dependsOntimesAtoB), String.valueOf(dependsOntimesBtoA)};

            csvWriter.writeRecord(content);
        }
        csvWriter.close();
    }
}
