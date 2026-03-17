package com.zjz.tongpin.common;

import lombok.Data;

import java.io.Serializable;
@Data
public class PageRequest implements Serializable {
    /**
     * 当前页
     */
    protected Integer pageNum=1;
    /**
     * 每页大小
     */
    protected Integer pageSize=10;
}
