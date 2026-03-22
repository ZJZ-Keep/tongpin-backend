package com.zjz.tongpin.service;

import com.zjz.tongpin.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class AlgorithmUtilsTest {
    //编辑距离算法（用于计算最相似的两组标签）
    @Test
    public void test(){
        List<String> strings1 = Arrays.asList("java", "大二", "男");
        List<String> strings12 =Arrays.asList("python","大二","男");
        List<String> strings13 =Arrays.asList("python","大一","女");
        int result1 = AlgorithmUtils.minDistance(strings1, strings12);
        System.out.println("result1 = " + result1);
        int result2 = AlgorithmUtils.minDistance(strings1,strings13);
        System.out.println("result2 = " + result2);
    }
}
