package com.wskh.FA萤火虫算法;

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
public class FA_Solver {

    // 萤火虫对象
    class Firefly {
        // 当前萤火虫的坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;

        // 全参构造
        public Firefly(double[] curVars, double curObjValue, double fit) {
            this.curVars = curVars;
            this.curObjValue = curObjValue;
            this.fit = fit;
        }
    }

    // 算法参数
    // 变量个数
    int varNum = 2;
    // 最大迭代次数
    int maxGen = 500;
    // 萤火虫群中萤火虫的个数
    int fireflyNum = 200;
    // 光吸收系数
    double gamma = 0.3;
    // 步长数组（各个维度的步长）
    double[] stepArr = new double[]{1.2, 1.2};
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 随机数对象
    Random random = new Random();
    // 萤火虫群
    Firefly[] fireflies;
    // 最佳的萤火虫
    Firefly bestFirefly;
    // 记录迭代过程
    public double[][][] positionArr;
    // 当前记录的行数
    int r;

    // 求解主函数
    public void solve() {
        // 初始化萤火虫群
        initFireflyes();
        // 开始迭代
        for (int t = 0; t < maxGen; t++) {
            for (int i = 0; i < fireflyNum; i++) {
                for (int j = 0; j < fireflyNum; j++) {
                    double light = getLight(getDistance(fireflies[i], fireflies[j]), fireflies[j].fit);
                    if (i != j && fireflies[i].fit < fireflies[j].fit) {
                        Firefly tempFirefly = copyFirefly(fireflies[i]);
                        for (int m = 0; m < varNum; m++) {
                            double move = light * (fireflies[j].curVars[m] - fireflies[i].curVars[m]) + stepArr[m] * (random.nextDouble()-0.5);
                            moveFirefly(tempFirefly, m,move);
                        }
                        updateFireFly(tempFirefly);
                        if(tempFirefly.fit > fireflies[i].fit){
                            fireflies[i] = tempFirefly;
                            if(bestFirefly.fit < tempFirefly.fit){
                                bestFirefly = copyFirefly(tempFirefly);
                            }
                        }
                    }
                }
            }
            report();
        }
        // 输出最好的结果
        System.out.println("变量取值为：" + Arrays.toString(bestFirefly.curVars));
        System.out.println("最优解为：" + bestFirefly.curObjValue);
    }

    // 记录
    void report() {
        for (int i = 0; i < fireflies.length; i++) {
            for (int j = 0; j < varNum; j++) {
                positionArr[r][i][j] = fireflies[i].curVars[j];
            }
        }
        r++;
    }

    // 获取光强
    double getLight(double r, double fit) {
        return fit * Math.exp(-gamma * Math.pow(r, 2));
    }

    // 求两个萤火虫之间的距离
    double getDistance(Firefly f1, Firefly f2) {
        double dis = 0d;
        for (int i = 0; i < varNum; i++) {
            dis += Math.pow(f1.curVars[i] - f2.curVars[i], 2);
        }
        return Math.sqrt(dis);
    }

    // 初始化萤火虫群
    private void initFireflyes() {
        positionArr = new double[maxGen][fireflyNum][varNum];
        fireflies = new Firefly[fireflyNum];
        for (int i = 0; i < fireflyNum; i++) {
            fireflies[i] = getRandomFirefly();
            if (i == 0 || bestFirefly.fit < fireflies[i].fit) {
                bestFirefly = copyFirefly(fireflies[i]);
            }
        }
    }

    // 控制萤火虫在第m个维度上移动n个距离
    public void moveFirefly(Firefly firefly, int m, double n) {
        // 移动
        firefly.curVars[m] += n;
        // 超出定义域的判断
        if (firefly.curVars[m] < lb[m]) {
            firefly.curVars[m] = lb[m];
        }
        if (firefly.curVars[m] > ub[m]) {
            firefly.curVars[m] = ub[m];
        }
    }

    // 更新萤火虫信息
    void updateFireFly(Firefly firefly) {
        double objValue = getObjValue(firefly.curVars);
        firefly.curObjValue = objValue;
        firefly.fit = 1 / objValue;
    }

    // 获取一个随机生成的萤火虫
    Firefly getRandomFirefly() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Firefly(vars.clone(), objValue, 1 / objValue);
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制萤火虫
    Firefly copyFirefly(Firefly old) {
        return new Firefly(old.curVars.clone(), old.curObjValue, old.fit);
    }

}
