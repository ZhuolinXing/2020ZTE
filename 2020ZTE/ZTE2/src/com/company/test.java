package com.company;

import org.junit.Test;

import java.util.TreeSet;

/**
 * Created by Mrxing on 2020/5/4 12:08
 */
public class test {
    @Test
    public void test1(){
        Main.debug();
    }
    @Test
    public void test3(){
        for (int i=1;i<9;i++){
            Solution solution =new Solution();
            solution.read("test"+i+".txt");
        }
    }
    @Test
    public void test4(){
        Solution solution = new Solution();
        solution.read("test1.txt");
        solution.classify();
        solution.testbfs();
    }


    @Test
    public void test6(){
        Solution solution = new Solution();
        solution.read("test0.txt");
        solution.classify();
        solution.testdfsms();
    }
    @Test
    public void test7(){
        Solution solution = new Solution();
        solution.read("test5.txt");
        solution.classify();
        TreeSet<Solution.Items> falseItems = new TreeSet<>();
        solution.findpath(falseItems);
        solution.printfalse(falseItems);
    }




}
