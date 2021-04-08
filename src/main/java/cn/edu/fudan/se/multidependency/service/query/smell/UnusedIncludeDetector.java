package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;

import java.util.List;
import java.util.Map;

public interface UnusedIncludeDetector {

    Map<Long, List<UnusedInclude>> detectUnusedInclude();

    Map<Long, List<UnusedInclude>> getUnusedIncludeFromSmell();
}
