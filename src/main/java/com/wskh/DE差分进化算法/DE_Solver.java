package com.wskh.DE差分进化算法;

import java.util.Arrays;
import java.util.Random;

/**
 * @Author：WSKH
 * @ClassName：DE_Solve
 * @ClassType：
 * @Description：
 * @Date：2022/6/8/18:26
 * @Email：1187560563@qq.com
 * @Blog：https://blog.csdn.net/weixin_51545953?type=blog
 */
public class DE_Solver {

    // 个体对象
    class Individual {
        // 当前个体的坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;
        // 全参构造
        public Individual(double[] curVars, double curObjValue, double fit) {
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
    // 个体群中个体的个数
    int individualNum = 200;
    // 常数因子（用来控制差分变化的放大率）
    double F = 0.4;
    // 贪婪系数（用来控制向当前最优解变异的程度，设置太大会容易陷入局部最优，设置太小，会导致收敛较慢）
    double greedyRate = 0.25;
    // 局部变异次数
    int variationCnt = 10;
    // 局部交叉概率
    double crossRate = 0.5;
    // 步长数组（各个维度的步长）
    double[] stepArr = new double[]{1.2, 1.2};
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 随机数对象
    Random random = new Random();
    // 个体群
    Individual[] individuals;
    // 最佳的个体
    Individual bestIndividual;
    // 记录迭代过程
    public double[][][] positionArr;
    // 当前记录的行数
    int r;

    // 求解主函数
    public void solve() {
        // 初始化个体群
        initIndividuals();
        // 开始迭代
        for (int i = 0; i < maxGen; i++) {
            variation();
            report();
            cross();
            report();
        }
        // 输出最好的结果
        System.out.println("变量取值为：" + Arrays.toString(bestIndividual.curVars));
        System.out.println("最优解为：" + bestIndividual.curObjValue);
    }

    // 变异
    void variation() {
        for (int r1 = 0; r1 < individuals.length; r1++) {
            for (int j = 0; j < variationCnt; j++) {
                // 随机选择两个其他个体
                int r2 = random.nextInt(individualNum);
                int r3 = random.nextInt(individualNum);
                while (r1 == r2 || r1 == r3 || r2 == r3) {
                    r2 = random.nextInt(individualNum);
                    r3 = random.nextInt(individualNum);
                }
                for (int m = 0; m < varNum; m++) {
                    double move = F * (individuals[r2].curVars[m] - individuals[r3].curVars[m]) + greedyRate * (bestIndividual.curVars[m] - individuals[r1].curVars[m]);
                    moveIndividual(individuals[r1], m, move);
                }
                updateIndividual(individuals[r1]);
                if (bestIndividual.fit < individuals[r1].fit) {
                    bestIndividual = copyIndividual(individuals[r1]);
                }
            }
        }
    }

    // 交叉
    void cross() {
        for (int r1 = 0; r1 < individualNum; r1++) {
            if(random.nextDouble() <= crossRate){
                int r2 = random.nextInt(individualNum);
                while (r1 == r2){
                    r2 = random.nextInt(individualNum);
                }
                int m1 = random.nextInt(varNum);
                int m2 = random.nextInt(varNum);
                double temp = individuals[r1].curVars[m1];
                individuals[r1].curVars[m1] = individuals[r2].curVars[m2];
                individuals[r2].curVars[m2] = temp;
                updateIndividual(individuals[r1]);
                updateIndividual(individuals[r2]);
                if(individuals[r1].fit > bestIndividual.fit){
                    bestIndividual = copyIndividual(individuals[r1]);
                }
                if(individuals[r2].fit > bestIndividual.fit){
                    bestIndividual = copyIndividual(individuals[r2]);
                }
            }
        }
    }

    // 记录
    void report() {
        for (int i = 0; i < individuals.length; i++) {
            for (int j = 0; j < varNum; j++) {
                positionArr[r][i][j] = individuals[i].curVars[j];
            }
        }
        r++;
    }

    // 求两个个体之间的距离
    double getDistance(Individual f1, Individual f2) {
        double dis = 0d;
        for (int i = 0; i < varNum; i++) {
            dis += Math.pow(f1.curVars[i] - f2.curVars[i], 2);
        }
        return Math.sqrt(dis);
    }

    // 初始化个体群
    private void initIndividuals() {
        positionArr = new double[2*maxGen][individualNum][varNum];
        individuals = new Individual[individualNum];
        for (int i = 0; i < individualNum; i++) {
            individuals[i] = getRandomIndividual();
            if (i == 0 || bestIndividual.fit < individuals[i].fit) {
                bestIndividual = copyIndividual(individuals[i]);
            }
        }
    }

    // 控制个体在第m个维度上移动n个距离
    public void moveIndividual(Individual individual, int m, double n) {
        // 移动
        individual.curVars[m] += n;
        // 超出定义域的判断
        if (individual.curVars[m] < lb[m]) {
            individual.curVars[m] = lb[m];
        }
        if (individual.curVars[m] > ub[m]) {
            individual.curVars[m] = ub[m];
        }
    }

    // 更新个体信息
    void updateIndividual(Individual individual) {
        double objValue = getObjValue(individual.curVars);
        individual.curObjValue = objValue;
        individual.fit = 1 / objValue;
    }

    // 获取一个随机生成的个体
    Individual getRandomIndividual() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Individual(vars.clone(), objValue, 1 / objValue);
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制个体
    Individual copyIndividual(Individual old) {
        return new Individual(old.curVars.clone(), old.curObjValue, old.fit);
    }

}
