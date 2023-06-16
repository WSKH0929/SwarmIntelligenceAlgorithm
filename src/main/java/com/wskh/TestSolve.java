package com.wskh;


import com.wskh.SSA樽海鞘优化算法.SSA_Solver;

/**
 * @Author：WSKH
 * @ClassName：TestSMO
 * @ClassType：
 * @Description：
 * @Date：2022/6/8/14:11
 * @Email：1187560563@qq.com
 * @Blog：https://blog.csdn.net/weixin_51545953?type=blog
 */
public class TestSolve {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new SSA_Solver().solve();
        System.out.println("求解用时："+(System.currentTimeMillis()-start)/1000d+" s");
    }
}
