package cn.edu.fudan.se.multidependency.utils.layout;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

@Data
public class GraphBlock {
    private int height;
    private int width;
    private int levels;
    private JSONArray packages;
    private String label;
}
