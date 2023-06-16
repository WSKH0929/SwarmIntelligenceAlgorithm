package com.wskh.SSA樽海鞘优化算法;

import java.util.Arrays;
import java.util.Random;

/**
 * @Author：WSKH
 * @ClassName：SSA_Solve
 * @ClassType：
 * @Description：
 * @Date：2022/6/8/18:26
 * @Email：1187560563@qq.com
 * @Blog：https://blog.csdn.net/weixin_51545953?type=blog
 */
public class SSA_Solver {

    // 樽海鞘对象
    class Salp {
        // 当前樽海鞘的坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;

        // 全参构造
        public Salp(double[] curVars, double curObjValue, double fit) {
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
    // 樽海鞘群中樽海鞘的个数
    int salpNum = 200;
    // 领导者数量和追随者数量（领导者数量+追随者数量=樽海鞘群中樽海鞘的个数）
    int leaderNum = 1; // 开始我理解错了，其实领导者只有一个，所以领导者数量设置为1就好
    int followNum = salpNum - leaderNum;
    // 步长数组（各个维度的步长）
    double[] stepArr = new double[]{1.2, 1.2};
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 随机数对象
    Random random = new Random();
    // 樽海鞘群
    Salp[] salps;
    // 最佳的樽海鞘
    Salp bestSalp;
    // 记录迭代过程
    public double[][][] positionArr;
    // 当前记录的行数
    int r;

    // 求解主函数
    public void solve() {
        // 初始化樽海鞘群
        initSalps();
        // 开始迭代
        for (int t = 0; t < maxGen; t++) {
            updateLeaderPos(t);
            updateFollowPos();
            report();
        }
        // 输出最好的结果
        System.out.println("变量取值为：" + Arrays.toString(bestSalp.curVars));
        System.out.println("最优解为：" + bestSalp.curObjValue);
    }

    // 记录
    void report() {
        for (int i = 0; i < salps.length; i++) {
            for (int j = 0; j < varNum; j++) {
                positionArr[r][i][j] = salps[i].curVars[j];
            }
        }
        r++;
    }

    // 更新追随者位置(t：当前迭代次数)
    void updateFollowPos(){
        for (int i = leaderNum; i < salpNum; i++) {
            Salp tempSalp = copySalp(salps[i]);
            for (int m = 0; m < varNum; m++) {
                double move = 0.5*(tempSalp.curVars[m]+salps[i-1].curVars[m]) - tempSalp.curVars[m];
                moveSalp(tempSalp,m,move);
            }
            updateSalp(tempSalp);
            // 不贪心的追随者（只跟着领导走）
            salps[i] = tempSalp;
            if(salps[i].fit > bestSalp.fit){
                bestSalp = copySalp(salps[i]);
            }
        }
    }

    // 更新领导者位置 (t；当前迭代次数)
    void updateLeaderPos(int t) {
        for (int i = 0; i < leaderNum; i++) {
            Salp tempSalp = copySalp(salps[i]);
            for (int m = 0; m < varNum; m++) {
                double c1 = 2 * Math.exp(-(Math.pow(4d * t / (double) maxGen, 2)));
                double c2 = (random.nextDouble() - 0.5) * 2;
                double c3 = (random.nextDouble() - 0.5) * 2;
                double move = 0d;
                if (c3 >= 0) {
                    move = c1 * ((ub[m] - lb[m] * c2 + lb[m]));
                } else {
                    move = -c1 * ((ub[m] - lb[m] * c2 + lb[m]));
                }
                moveSalp(tempSalp,m,move);
            }
            updateSalp(tempSalp);
            // 贪心的领导（只去往比当前结果好的位置，如果领导不贪心，则不容易收敛，到处跑）
            if(tempSalp.fit > salps[i].fit){
                salps[i] = tempSalp;
                if(salps[i].fit > bestSalp.fit){
                    bestSalp = copySalp(salps[i]);
                }
            }
        }
    }

    // 求两个樽海鞘之间的距离
    double getDistance(Salp f1, Salp f2) {
        double dis = 0d;
        for (int i = 0; i < varNum; i++) {
            dis += Math.pow(f1.curVars[i] - f2.curVars[i], 2);
        }
        return Math.sqrt(dis);
    }

    // 初始化樽海鞘群
    private void initSalps() {
        positionArr = new double[maxGen][salpNum][varNum];
        salps = new Salp[salpNum];
        for (int i = 0; i < salpNum; i++) {
            salps[i] = getRandomSalp();
            if (i == 0 || bestSalp.fit < salps[i].fit) {
                bestSalp = copySalp(salps[i]);
            }
        }
    }

    // 控制樽海鞘在第m个维度上移动n个距离
    public void moveSalp(Salp salp, int m, double n) {
        // 移动
        salp.curVars[m] += n;
        // 超出定义域的判断
        if (salp.curVars[m] < lb[m]) {
            salp.curVars[m] = lb[m];
        }
        if (salp.curVars[m] > ub[m]) {
            salp.curVars[m] = ub[m];
        }
    }

    // 更新樽海鞘信息
    void updateSalp(Salp salp) {
        double objValue = getObjValue(salp.curVars);
        salp.curObjValue = objValue;
        salp.fit = 1 / objValue;
    }

    // 获取一个随机生成的樽海鞘
    Salp getRandomSalp() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Salp(vars.clone(), objValue, 1 / objValue);
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制樽海鞘
    Salp copySalp(Salp old) {
        return new Salp(old.curVars.clone(), old.curObjValue, old.fit);
    }

}
