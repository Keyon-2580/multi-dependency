package cn.edu.fudan.se.multidependency.service.query.ar;

import cn.edu.fudan.se.multidependency.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KMeans {
    private List<String> dataArray;//待分类的原始值
    private int K = 20;//将要分成的类别个数
    private int maxClusterTimes = 500;//最大迭代次数
    private List<List<String>> clusterList;//聚类的结果
    private List<String> clusteringCenterT;//质心

    @Autowired
    private DependencyMatrix dependencyMatrix;

    public void init() {
        dependencyMatrix.init();
        Map<String, Map<String, Double>> adjacencyList = dependencyMatrix.getAdjacencyList();
        Map<String, Map<String, Double>> inverseAdjacencyList = dependencyMatrix.getInverseAdjacencyList();
        dependencyMatrix.exportMatrix("C:\\Users\\SongJee\\Desktop\\res\\adjacencyList.txt", adjacencyList);
        dependencyMatrix.exportMatrix("C:\\Users\\SongJee\\Desktop\\res\\inverseAdjacencyList.txt", inverseAdjacencyList);
        dataArray = new ArrayList<>(inverseAdjacencyList.keySet());
//        dataArray.sort((s1, s2) -> {
//            Map<String, Double> e1 = inverseAdjacencyList.get(s1);
//            Map<String, Double> e2 = inverseAdjacencyList.get(s2);
//            double sum1 = 0;
//            double sum2 = 0;
//            for (Double e : e1.values()) {
//                sum1 += e;
//            }
//            for (Double e : e2.values()) {
//                sum2 += e;
//            }
//            return Double.compare(sum2, sum1);
//        });
    }
    public int getK() {
        return K;
    }
    public void setK(int K) {
        if (K < 1) {
            throw new IllegalArgumentException("K must greater than 0");
        }
        this.K = K;
    }
    public int getMaxClusterTimes() {
        return maxClusterTimes;
    }
    public void setMaxClusterTimes(int maxClusterTimes) {
        if (maxClusterTimes < 10) {
            throw new IllegalArgumentException("maxClusterTimes must greater than 10");
        }
        this.maxClusterTimes = maxClusterTimes;
    }
    public List<String> getClusteringCenterT() {
        return clusteringCenterT;
    }
    /**
     * 对数据进行聚类
     */
    public List<List<String>> clustering() {
        if (dataArray == null) {
            return null;
        }
        //初始K个点为数组中的前K个点
        int size = Math.min(K, dataArray.size());
        List<String> centerT = new ArrayList<>(size);
        //对数据进行打乱
        for (int i = 0; i < size; i++) {
            centerT.add(dataArray.get(i));
        }
        clustering(centerT, 0);
        return clusterList;
    }

    /**
     * 一轮聚类
     */
    private void clustering(List<String> preCenter, int times) {
        if (preCenter == null || preCenter.size() < 2) {
            return;
        }
        //打乱质心的顺序
        Collections.shuffle(preCenter);
        List<List<String>> clusterList =  getListT(preCenter.size());
        for (String s1 : this.dataArray) {
            //寻找最相似的质心
            int max = 0;
            double maxScore = similarScore(s1, preCenter.get(0));
            for (int i = 1; i < preCenter.size(); i++) {
                double score = similarScore(s1, preCenter.get(i));
                if (maxScore < score) {
                    maxScore = score;
                    max = i;
                }
            }
            clusterList.get(max).add(s1);
        }
        //计算本次聚类结果每个类别的质心
        List<String> nowCenter = new ArrayList<>();
        for (List<String> list : clusterList) {
            nowCenter.add(getCenterT(list));
        }
        //是否达到最大迭代次数
        if (times >= this.maxClusterTimes || preCenter.size() < this.K) {
            this.clusterList = clusterList;
            return;
        }
        this.clusteringCenterT = nowCenter;
        //判断质心是否发生移动，如果没有移动，结束本次聚类，否则进行下一轮
        if (isCenterChange(preCenter, nowCenter)) {
            clear(clusterList);
            clustering(nowCenter, times + 1);
        } else {
            this.clusterList = clusterList;
        }
    }

    /**
     * 初始化一个聚类结果
     */
    private List<List<String>> getListT(int size) {
        List<List<String>> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(new ArrayList<>());
        }
        return list;
    }

    /**
     * 清空无用数组
     */
    private void clear(List<List<String>> lists) {
        for (List<String> list : lists) {
            list.clear();
        }
        lists.clear();
    }

    /**
     * 向模型中添加记录
     */
    public void addRecord(String value) {
        if (dataArray == null) {
            dataArray = new ArrayList<String>();
        }
        dataArray.add(value);
    }

    /**
     * 判断质心是否发生移动
     */
    private boolean isCenterChange(List<String> preT, List<String> nowT) {
        if (preT == null || nowT == null) {
            return false;
        }
        for (String t1 : preT) {
            boolean bol = true;
            for (String t2 : nowT) {
                if (t1.equals(t2)) {//t1在t2中有相等的，认为该质心未移动
                    bol = false;
                    break;
                }
            }
            //有一个质心发生移动，认为需要进行下一次计算
            if (bol) {
                return bol;
            }
        }
        return false;
    }

    /**
     * o1 o2之间的相似度
     */
    public double similarScore(String s1, String s2) {
        Map<String, Double> edge = dependencyMatrix.getAdjacencyList().get(s1);
        return edge.getOrDefault(s2, 0.0);
    }

    /**
     * 判断o1 o2是否相等
     */
    public boolean equals(String o1, String o2) {
//        return o1.getX() == o2.getX() && o1.getY() == o2.getY();
        return false;
    }

    /**
     * 求一组数据的质心
     */
    public String getCenterT(List<String> list) {
        String center = "";
        double maxVal = 0;
        for (String file1 : list) {
            double degrees = 0;
            Map<String, Double> intoEdges = dependencyMatrix.getInverseAdjacencyList().get(file1);
            for (Map.Entry<String, Double> e : intoEdges.entrySet()) {
                degrees += e.getValue();
            }
//            for (String file2 : list) {
//                degrees += intoEdges.getOrDefault(file2, 0.0);
//            }

            if (degrees > maxVal) {
                maxVal = degrees;
                center = file1;
            }
        }
        return center;
    }

    public void exportClusterRes(String filePath, List<List<String>> res) {
        StringBuffer str = new StringBuffer();
        int cnt = 0;
        for (List<String> list : res) {
            str.append("cluster" + cnt + "\n");
            int idx = 0;
            for (String f : list) {
                str.append(idx + ":" + f + "\n");
                idx++;
            }
            cnt++;
        }
        FileUtil.exportToFile(filePath, str.toString());
    }
}
