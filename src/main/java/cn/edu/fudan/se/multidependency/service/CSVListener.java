package cn.edu.fudan.se.multidependency.service;

import cn.edu.fudan.se.multidependency.model.ChangedFile;
import cn.edu.fudan.se.multidependency.service.query.BeanCreator;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: keyon
 * @time: 2023/1/3 21:56
 */


@Slf4j
public class CSVListener extends AnalysisEventListener<ChangedFile> {

    /**
     * 批处理阈值
     */
    private static final int BATCH_COUNT = 2;
    List<ChangedFile> list = new ArrayList<>(BATCH_COUNT);

    @Override
    public void invoke(ChangedFile ChangedFile, AnalysisContext analysisContext) {
        log.info("解析到一条数据:{}", (ChangedFile));
        if(ChangedFile.getChangedFileName().endsWith("java")){
            list.add(ChangedFile);
        }
        if (list.size() >= BATCH_COUNT) {
            saveData();
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        log.info("所有数据解析完成！");
    }

    private void saveData(){
        list.forEach(changedFile -> BeanCreator.changedFiles.add(changedFile.getChangedFileName()));
        log.info("{}条数据，开始存储数据库！", list.size());
        log.info("存储数据库成功！");
    }


}
