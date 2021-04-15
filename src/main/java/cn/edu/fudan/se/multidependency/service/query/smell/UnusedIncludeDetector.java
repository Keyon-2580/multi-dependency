package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface UnusedIncludeDetector {

    /**
     * 获取未使用引入
     */
    Map<Long, List<UnusedInclude>> getFileUnusedInclude();

    /**
     * 检测未使用引入
     */
    Map<Long, List<UnusedInclude>> detectFileUnusedInclude();

    /**
     * 根据file的Id生成文件所在的Unused Include的json格式信息
     */
    JSONObject getUnusedIncludeJson(Long fileId);
}
