package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.node.smell.Smell;

import java.util.Arrays;
import java.util.List;

public class SmellUtils {
    public static void sortSmellByName(List<Smell> smells) {
        smells.sort((smell1, smell2) -> {
            List<String> namePart1 = Arrays.asList(smell1.getName().split("_"));
            List<String> namePart2 = Arrays.asList(smell2.getName().split("_"));
            int partition1 = Integer.parseInt(namePart1.get(namePart1.size() - 1));
            int partition2 = Integer.parseInt(namePart2.get(namePart2.size() - 1));
            return Integer.compare(partition1, partition2);
        });
    }
}
