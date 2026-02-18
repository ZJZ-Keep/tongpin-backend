package com.zjz.tongpin.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

public class ImprotInfo {
    public static void main(String[] args) {
       String fileName = "C:\\Users\\23021\\Desktop\\星球用户信息表.xlsx";
        //readByListener(fileName);
        synchronousRead(fileName);
    }
    /**
     * 监听器读取
     * @param fileName
     */
    public static void readByListener(String fileName) {

        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(fileName, UserTableInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<UserTableInfo> list = EasyExcel.read(fileName).head(UserTableInfo.class).sheet().doReadSync();
        for (UserTableInfo TableUserInfo : list) {
            System.out.println(TableUserInfo);
        }

    }

}
