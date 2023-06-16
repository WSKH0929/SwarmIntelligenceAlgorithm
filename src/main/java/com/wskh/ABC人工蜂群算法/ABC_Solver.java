package com.wskh.ABC人工蜂群算法;


import java.util.Arrays;
import java.util.Random;

/**
 * @Author：WSKH
 * @ClassName：ABC_Solver
 * @ClassType：
 * @Description：
 * @Date：2022/6/8/09:40
 * @Email：1187560563@qq.com
 * @Blog：https://blog.csdn.net/weixin_51545953?type=blog
 */
public class ABC_Solver {

    // 蜜蜂类
    class Bee {
        // 当前蜜蜂坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;
        // 搜索/试验次数
        int searchCount;
        // 全参构造
        public Bee(double[] curVars, double curObjValue, double fit, int searchCount) {
            this.curVars = curVars;
            this.curObjValue = curObjValue;
            this.fit = fit;
            this.searchCount = searchCount;
        }
    }

    // 算法参数
    // 蜂群数量
    int beeNum = 100;
    // 蜂群
    Bee[] bees;
    // 最大迭代次数
    int maxGen = 100;
    // 局部搜索次数
    int localSearchCount = 5;
    // 观察蜂阶段随机搜索次数
    int observeTimeSearchCount = 30;
    // 最大试验次数(超过此次数就说明该蜜源已经没有蜂蜜了，尽量设置大一点，可以更快的收敛)
    int maxSearchCount = 5000;
    // 随机数对象
    Random random = new Random();
    // 变量维度数
    int varNum = 2;
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 每个维度的速度限制
    double[] vMaxArr = new double[]{1.2, 1.2};
    // 最好的蜜蜂
    Bee bestBee;
    // 最佳迭代次数
    int bestT;
    // 记录迭代过程
    public double[][][] positionArr;

    // 初始化蜂群
    void initBees() {
        positionArr = new double[maxGen][beeNum][varNum];
        bees = new Bee[beeNum];
        for (int i = 0; i < beeNum; i++) {
            bees[i] = getRandomBee();
            if (i == 0) {
                bestBee = copyBee(bees[0]);
            } else {
                if (bestBee.fit < bees[i].fit) {
                    bestBee = copyBee(bees[i]);
                }
            }
        }
    }

    // 求解主函数
    public void solve() {
        // 初始化蜂群
        initBees();
        // 开始迭代
        for (int t = 0; t < maxGen; t++) {
            // 采蜜蜂阶段(随机局部搜索)
            honeyTime(t);
            // 观察蜂阶段
            observeTime(t);
            // 侦察蜂阶段
            scoutTime(t);
        }
        // 输出最好的结果
        System.out.println("最佳迭代次数：" + bestT);
        System.out.println("变量取值为：" + Arrays.toString(bestBee.curVars));
        System.out.println("最优解为：" + bestBee.curObjValue);
    }

    // 侦察蜂阶段
    private void scoutTime(int t) {
        for (int i = 0; i < bees.length; i++) {
            if(bees[i].searchCount >= maxSearchCount){
                bees[i] = getRandomBee();
                if(bees[i].fit > bestBee.fit){
                    bestBee = copyBee(bees[i]);
                    bestT = t;
                }
            }
        }
    }

    // 根据fit值进行轮盘赌随机选取蜜蜂进行局部搜索(fit值越大越可能被选中)
    private void observeTime(int t) {
        // 计算fit总和
        double totalFit = 0;
        for (Bee honeyBee : bees) {
            totalFit += honeyBee.fit;
        }
        // 轮盘赌的累计概率数组
        double[] p = new double[beeNum];
        for (int i = 0; i < p.length; i++) {
            p[i] = (bees[i].fit / totalFit) + (i == 0 ? 0 : p[i - 1]);
        }
        // 随机选取蜜蜂进行局部搜索
        for (int i = 0; i < observeTimeSearchCount; i++) {
            double r = random.nextDouble();
            for (int j = 0; j < p.length; j++) {
                if (r <= p[j]) {
                    localSearch(t, j);
                    break;
                }
            }
        }
    }

    // 采蜜蜂阶段(对每个蜜蜂进行随机局部搜索)
    private void honeyTime(int t) {
        for (int j = 0; j < beeNum; j++) {
            localSearch(t, j);
        }
    }

    // 随机局部搜索
    void localSearch(int t, int j) {
        Bee localBee = copyBee(bees[j]);
        for (int k = 0; k < localSearchCount; k++) {
            Bee tempBee = copyBee(localBee);
            for (int m = 0; m < tempBee.curVars.length; m++) {
                moveBee(tempBee, m, (random.nextDouble() - 0.5) * 2 * vMaxArr[m]);
            }
            tempBee.curObjValue = getObjValue(tempBee.curVars);
            tempBee.fit = 1 / tempBee.curObjValue;
            if (tempBee.fit > localBee.fit) {
                localBee = copyBee(tempBee);
                k = -1;
            }else {
                localBee.searchCount++;
            }
        }
        if (localBee.fit > bestBee.fit) {
            bestBee = copyBee(localBee);
            bestT = t;
        }
        bees[j] = copyBee(localBee);
        for (int m = 0; m < localBee.curVars.length; m++) {
            positionArr[t][j][m] = localBee.curVars[m];
        }
    }

    // 获取一个随机生成的蜜蜂
    Bee getRandomBee() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Bee(vars.clone(), objValue, 1 / objValue, 0);
    }

    // 控制蜜蜂在第m个维度上移动n个距离
    public void moveBee(Bee bee, int m, double n) {
        // 移动
        bee.curVars[m] += n;
        // 超出定义域的判断
        if (bee.curVars[m] < lb[m]) {
            bee.curVars[m] = lb[m];
        }
        if (bee.curVars[m] > ub[m]) {
            bee.curVars[m] = ub[m];
        }
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制蜜蜂
    Bee copyBee(Bee old) {
        return new Bee(old.curVars.clone(), old.curObjValue, old.fit, old.searchCount);
    }

}
