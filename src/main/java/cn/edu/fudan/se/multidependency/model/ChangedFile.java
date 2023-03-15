package cn.edu.fudan.se.multidependency.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.*;

/**
 * @description:
 * @author: keyon
 * @time: 2022/11/24 15:04
 */
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ChangedFile {
    @ExcelProperty("修改文件")
    String changedFileName;
    @ExcelProperty("修改文件次数")
    int changedTime;
    @ExcelProperty("修改的commit")
    String commitList;

}
