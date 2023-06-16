package com.wskh.SOA海鸥优化算法;

import java.util.Arrays;
import java.util.Random;

/**
 * @Author：WSKH
 * @ClassName：SOA_Solve
 * @ClassType：
 * @Description：
 * @Date：2022/6/8/18:26
 * @Email：1187560563@qq.com
 * @Blog：https://blog.csdn.net/weixin_51545953?type=blog
 */
public class SOA_Solver {

    // 海鸥对象
    class Seagull {
        // 当前海鸥的坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;
        // 全参构造
        public Seagull(double[] curVars, double curObjValue, double fit) {
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
    // 海鸥群中海鸥的个数
    int seagullNum = 200;
    // 控制采用变量A的频率
    double fc = 2;
    // 定义螺旋形状的常量
    double u = 1;
    double v = 1;
    // 局部盘旋次数
    int localSearchCnt = 10;
    // 步长数组（各个维度的步长）
    double[] stepArr = new double[]{1.2, 1.2};
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 随机数对象
    Random random = new Random();
    // 海鸥群
    Seagull[] seagulls;
    // 最佳的海鸥
    Seagull bestSeagull;
    // 记录迭代过程
    public double[][][] positionArr;
    // 当前记录的行数
    int r;

    // 求解主函数
    public void solve() {
        // 初始化海鸥群
        initSeagulls();
        // 开始迭代
        for (int i = 0; i < maxGen; i++) {
            // 螺旋行为
            SpiralBehavior(i);
            report();
        }
        // 输出最好的结果
        System.out.println("变量取值为：" + Arrays.toString(bestSeagull.curVars));
        System.out.println("最优解为：" + bestSeagull.curObjValue);
    }

    // 螺旋行为
    void SpiralBehavior(int t) {
        for (int i = 0; i < seagulls.length; i++) {
            for (int j = 0; j < localSearchCnt; j++) {
                Seagull tempSeagull = copySeagull(seagulls[i]);
                double k = random.nextDouble() * 2 * Math.PI;
                double rd = random.nextDouble();
                for (int m = 0; m < varNum; m++) {
                    double r = u * Math.exp(k * v);
                    double x = r * Math.cos(k);
                    double y = r * Math.sin(k);
                    double z = r * k;
                    double ds = getDs(tempSeagull.curVars[m], t, m, rd);
                    double move = (ds * x * y * z) + bestSeagull.curVars[m] - tempSeagull.curVars[m];
                    moveSeagull(tempSeagull, m, move);
                }
                updateSeagull(tempSeagull);
                seagulls[i] = tempSeagull;
                if (tempSeagull.fit > bestSeagull.fit) {
                    bestSeagull = copySeagull(tempSeagull);
                }
            }
        }
    }

    /**
     * @param x 当前海鸥当前维度的值
     * @param t 当前迭代数
     * @param m 当前计算的维度
     * @Description 计算Ds
     */
    double getDs(double x, int t, int m, double rd) {
        double A = fc - (t * (fc / maxGen));
        double B = 2 * A * A * rd;
        double cs = A * x;
        double ms = B * (bestSeagull.curVars[m] - x);
        return Math.abs(cs + ms);
    }

    // 记录
    void report() {
        for (int i = 0; i < seagulls.length; i++) {
            for (int j = 0; j < varNum; j++) {
                positionArr[r][i][j] = seagulls[i].curVars[j];
            }
        }
        r++;
    }

    // 求两个海鸥之间的距离
    double getDistance(Seagull f1, Seagull f2) {
        double dis = 0d;
        for (int i = 0; i < varNum; i++) {
            dis += Math.pow(f1.curVars[i] - f2.curVars[i], 2);
        }
        return Math.sqrt(dis);
    }

    // 初始化海鸥群
    private void initSeagulls() {
        positionArr = new double[maxGen][seagullNum][varNum];
        seagulls = new Seagull[seagullNum];
        for (int i = 0; i < seagullNum; i++) {
            seagulls[i] = getRandomSeagull();
            if (i == 0 || bestSeagull.fit < seagulls[i].fit) {
                bestSeagull = copySeagull(seagulls[i]);
            }
        }
    }

    // 控制海鸥在第m个维度上移动n个距离
    public void moveSeagull(Seagull seagull, int m, double n) {
        // 移动
        seagull.curVars[m] += n;
        // 超出定义域的判断
        if (seagull.curVars[m] < lb[m]) {
            seagull.curVars[m] = lb[m];
        }
        if (seagull.curVars[m] > ub[m]) {
            seagull.curVars[m] = ub[m];
        }
    }

    // 更新海鸥信息
    void updateSeagull(Seagull seagull) {
        double objValue = getObjValue(seagull.curVars);
        seagull.curObjValue = objValue;
        seagull.fit = 1 / objValue;
    }

    // 获取一个随机生成的海鸥
    Seagull getRandomSeagull() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Seagull(vars.clone(), objValue, 1 / objValue);
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制海鸥
    Seagull copySeagull(Seagull old) {
        return new Seagull(old.curVars.clone(), old.curObjValue, old.fit);
    }

}
