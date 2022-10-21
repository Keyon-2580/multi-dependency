package cn.edu.fudan.se.multidependency.utils;

/**
 * @description:
 * @author: keyon
 * @time: 2022/10/21 10:05
 */

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CsvExportUtil {
    public CsvExportUtil() {
    }

    public static void doExport(String absoluteTableName, List<String[]> contents, String... header) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(absoluteTableName);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(header);
        CSVPrinter csvPrinter = new CSVPrinter(outputStreamWriter, csvFormat);
        Iterator var7 = contents.iterator();

        while(var7.hasNext()) {
            String[] content = (String[])var7.next();
            csvPrinter.printRecord(content);
        }

        csvPrinter.flush();
        csvPrinter.close();
    }
}
