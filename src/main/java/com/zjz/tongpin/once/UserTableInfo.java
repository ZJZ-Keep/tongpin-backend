package com.zjz.tongpin.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class UserTableInfo {
    /**
     * 星球编号
     */
    @ExcelProperty("星球编号")
    private String planetCode;
    /**
     * 用户昵称
     */
    @ExcelProperty("用户昵称")
    private String username;
}