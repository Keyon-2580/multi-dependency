package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface UnusedIncludeDetector {

    Map<Long, List<UnusedInclude>> detectUnusedInclude();

    Map<Long, List<UnusedInclude>> getUnusedIncludeFromSmell();

    /**
     * 根据file的Id生成文件所在的Unused Include的json格式信息
     * @return
     */
    JSONObject getUnusedIncludeJson(Long fileId);
}
