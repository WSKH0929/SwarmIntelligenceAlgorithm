package com.wskh.CSO蟑螂算法;

import java.util.Arrays;
import java.util.Random;

/**
 * @Author：WSKH
 * @ClassName：AFSA_Solve
 * @ClassType：
 * @Description：
 * @Date：2022/6/8/18:26
 * @Email：1187560563@qq.com
 * @Blog：https://blog.csdn.net/weixin_51545953?type=blog
 */
public class CSO_Solver {

    // 蟑螂对象
    class Cockroach {
        // 当前蟑螂的坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;

        // 全参构造
        public Cockroach(double[] curVars, double curObjValue, double fit) {
            this.curVars = curVars;
            this.curObjValue = curObjValue;
            this.fit = fit;
        }
    }

    // 算法参数
    // 变量个数
    int varNum = 2;
    // 最大迭代次数
    int maxGen = 1000;
    // 蟑螂群中蟑螂的个数
    int cockroachNum = 500;
    // 视野
    double visual = 50;
    // 残忍行为次数
    int ruthlessBehaviorCnt = 1;
    // 分散行为次数
    int dispersingBehaviorCnt = 30;
    // 步长数组（各个维度的步长）
    double[] stepArr = new double[]{1.2, 1.2};
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 随机数对象
    Random random = new Random();
    // 蟑螂群
    Cockroach[] cockroaches;
    // 最佳的蟑螂
    Cockroach bestCockroach;
    // 记录迭代过程
    public double[][][] positionArr;
    // 当前记录的行数
    int r;

    // 求解主函数
    public void solve() {
        // 初始化蟑螂群
        initCockroaches();
        // 开始迭代
        for (int t = 0; t < maxGen; t++) {
            // 聚集行为
            ChaseSwarmingBehavior();
            report();
            // 分散行为
            DispersingBehavior();
            report();
            // 残忍行为
            RuthlessBehavior();
            report();
        }
        // 输出最好的结果
//        for (int i = 0; i < cockroaches.length; i++) {
//            if(bestCockroach.fit < cockroaches[i].fit){
//                bestCockroach = copyCockroach(cockroaches[i]);
//            }
//        }
        System.out.println("变量取值为：" + Arrays.toString(bestCockroach.curVars));
        System.out.println("最优解为：" + bestCockroach.curObjValue);
    }

    // 聚集行为
    void ChaseSwarmingBehavior() {
        // 找到个体最优
        Cockroach[] localBestCockroaches = new Cockroach[cockroachNum];
        for (int i = 0; i < cockroaches.length; i++) {
            localBestCockroaches[i] = findLocalBestCockroach(i);
            if(bestCockroach.fit < cockroaches[i].fit){
                bestCockroach = copyCockroach(cockroaches[i]);
            }
        }
        for (int i = 0; i < cockroaches.length; i++) {
            // 说明当前蟑螂就是个体最优（或接近个体最优）
            if(Math.abs(localBestCockroaches[i].curObjValue-cockroaches[i].curObjValue) <= 0.00001){
                // 那就朝着全局最优靠近
                Cockroach tempCockroach = copyCockroach(cockroaches[i]);
                for (int m = 0; m < varNum; m++) {
                    moveCockroach(tempCockroach,m,stepArr[m]*(bestCockroach.curVars[m]-tempCockroach.curVars[m]));
                }
                tempCockroach.curObjValue = getObjValue(tempCockroach.curVars);
                tempCockroach.fit = 1/tempCockroach.curObjValue;
                if(tempCockroach.fit > cockroaches[i].fit){
                    cockroaches[i] = tempCockroach;
                }
            }else{
                // 朝着个体最优移动
                Cockroach tempCockroach = copyCockroach(cockroaches[i]);
                for (int m = 0; m < varNum; m++) {
                    moveCockroach(tempCockroach,m,stepArr[m]*(localBestCockroaches[i].curVars[m]-tempCockroach.curVars[m]));
                }
                tempCockroach.curObjValue = getObjValue(tempCockroach.curVars);
                tempCockroach.fit = 1/tempCockroach.curObjValue;
                if(tempCockroach.fit > cockroaches[i].fit){
                    cockroaches[i] = tempCockroach;
                }
            }
        }
    }

    // 分散行为
    void DispersingBehavior() {
        for (int i = 0; i < cockroaches.length; i++) {
            for (int j = 0; j < dispersingBehaviorCnt; j++) {
                Cockroach tempCockroach = copyCockroach(cockroaches[i]);
                for (int m = 0; m < varNum; m++) {
                    double move = stepArr[m] * (random.nextDouble()-0.5)*2;
                    moveCockroach(tempCockroach,m,move);
                }
                if(tempCockroach.fit > cockroaches[i].fit){
                    cockroaches[i] = tempCockroach;
                    if(tempCockroach.fit > bestCockroach.fit){
                        bestCockroach = copyCockroach(tempCockroach);
                    }
                }
            }
        }
    }

    // 残忍行为
    void RuthlessBehavior() {
        for (int i = 0; i < ruthlessBehaviorCnt; i++) {
            cockroaches[random.nextInt(cockroachNum)] = copyCockroach(bestCockroach);
        }
    }

    // 寻找个体最优蟑螂
    Cockroach findLocalBestCockroach(int j) {
        Cockroach localBest = copyCockroach(cockroaches[j]);
        for (int i = 0; i < cockroaches.length; i++) {
            if (i != j) {
                if (localBest.fit < cockroaches[i].fit && getDistance(cockroaches[j], cockroaches[i]) <= visual) {
                    localBest = copyCockroach(cockroaches[i]);
                }
            }
        }
        return localBest;
    }

    // 记录
    void report() {
        for (int i = 0; i < cockroaches.length; i++) {
            for (int j = 0; j < varNum; j++) {
                positionArr[r][i][j] = cockroaches[i].curVars[j];
            }
        }
        r++;
    }

    // 求两个蟑螂之间的距离
    double getDistance(Cockroach f1, Cockroach f2) {
        double dis = 0d;
        for (int i = 0; i < varNum; i++) {
            dis += Math.pow(f1.curVars[i] - f2.curVars[i], 2);
        }
        return Math.sqrt(dis);
    }

    // 初始化蟑螂群
    private void initCockroaches() {
        positionArr = new double[3 * maxGen][cockroachNum][varNum];
        cockroaches = new Cockroach[cockroachNum];
        for (int i = 0; i < cockroachNum; i++) {
            cockroaches[i] = getRandomCockroach();
            if (i == 0 || bestCockroach.fit < cockroaches[i].fit) {
                bestCockroach = copyCockroach(cockroaches[i]);
            }
        }
    }

    // 控制蟑螂在第m个维度上移动n个距离
    public void moveCockroach(Cockroach cockroach, int m, double n) {
        // 移动
        cockroach.curVars[m] += n;
        // 超出定义域的判断
        if (cockroach.curVars[m] < lb[m]) {
            cockroach.curVars[m] = lb[m];
        }
        if (cockroach.curVars[m] > ub[m]) {
            cockroach.curVars[m] = ub[m];
        }
    }

    // 获取一个随机生成的蟑螂
    Cockroach getRandomCockroach() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Cockroach(vars.clone(), objValue, 1 / objValue);
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制蟑螂
    Cockroach copyCockroach(Cockroach old) {
        return new Cockroach(old.curVars.clone(), old.curObjValue, old.fit);
    }

}
