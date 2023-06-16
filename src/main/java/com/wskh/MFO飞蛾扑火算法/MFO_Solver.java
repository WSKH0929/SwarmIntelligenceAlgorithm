package com.wskh.MFO飞蛾扑火算法;

import java.util.Arrays;
import java.util.Comparator;
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
public class MFO_Solver {

    // 飞蛾对象
    class Moth {
        // 当前飞蛾的坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;
        // 火焰（飞蛾目前到过的最佳位置）
        double[] bestVars;
        double bestObjValue;
        double bestFit;

        // 全参构造
        public Moth(double[] curVars, double curObjValue, double fit, double[] bestVars, double bestObjValue, double bestFit) {
            this.curVars = curVars;
            this.curObjValue = curObjValue;
            this.fit = fit;
            this.bestVars = bestVars;
            this.bestObjValue = bestObjValue;
            this.bestFit = bestFit;
        }
    }

    // 算法参数
    // 变量个数
    int varNum = 2;
    // 最大迭代次数
    int maxGen = 500;
    // 飞蛾群中飞蛾的个数
    int mothNum = 500;
    // 螺旋常量
    double b = 2;
    // 每一次搜索的火焰数
    int flyToFireCnt = 20;
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 随机数对象
    Random random = new Random();
    // 飞蛾群
    Moth[] moths;
    // 最佳的飞蛾
    Moth bestMoth;
    // 记录迭代过程
    public double[][][] positionArr;
    // 当前记录的行数
    int r;

    // 求解主函数
    public void solve() {
        // 初始化飞蛾群
        initMoths();
        // 开始迭代
        for (int i = 0; i < maxGen; i++) {
            flyToFire();
            report();
        }
        // 输出最好的结果
        System.out.println("变量取值为：" + Arrays.toString(bestMoth.curVars));
        System.out.println("最优解为：" + bestMoth.curObjValue);
    }

    // 扑火
    void flyToFire() {
        // 先按照火焰热度排序，热度高的排前面
        Arrays.sort(moths, new Comparator<Moth>() {
            @Override
            public int compare(Moth o1, Moth o2) {
                return Double.compare(o2.bestFit,o1.bestFit);
            }
        });
        // 开始扑火
        for (int i = 0; i < mothNum; i++) {
            int c = 0;
            for (int j = 0; j < mothNum; j++) {
                if (i != j && c <= flyToFireCnt) {
                    Moth tempMoth = copyMoth(moths[i]);
                    for (int m = 0; m < varNum; m++) {
                        double t = (random.nextDouble() - 0.5) * 2;
                        double move = Math.abs(tempMoth.curVars[m] - moths[j].bestVars[m]) * Math.exp(b * t) * Math.cos(2 * Math.PI * t) + moths[j].bestVars[m] - tempMoth.curVars[m];
                        moveMoth(tempMoth, m, move);
                    }
                    updateMoth(tempMoth);
                    moths[i] = tempMoth;
                    if (tempMoth.fit > bestMoth.fit) {
                        bestMoth = copyMoth(tempMoth);
                    }
                    c++;
                }
            }
        }
    }

    // 记录
    void report() {
        for (int i = 0; i < moths.length; i++) {
            for (int j = 0; j < varNum; j++) {
                positionArr[r][i][j] = moths[i].curVars[j];
            }
        }
        r++;
    }

    // 初始化飞蛾群
    private void initMoths() {
        positionArr = new double[2 * maxGen][mothNum][varNum];
        moths = new Moth[mothNum];
        for (int i = 0; i < mothNum; i++) {
            moths[i] = getRandomMoth();
            if (i == 0 || bestMoth.fit < moths[i].fit) {
                bestMoth = copyMoth(moths[i]);
            }
        }
    }

    // 控制飞蛾在第m个维度上移动n个距离
    public void moveMoth(Moth moth, int m, double n) {
        // 移动
        moth.curVars[m] += n;
        // 超出定义域的判断
        if (moth.curVars[m] < lb[m]) {
            moth.curVars[m] = lb[m];
        }
        if (moth.curVars[m] > ub[m]) {
            moth.curVars[m] = ub[m];
        }
    }

    // 更新飞蛾信息
    void updateMoth(Moth moth) {
        double objValue = getObjValue(moth.curVars);
        moth.curObjValue = objValue;
        moth.fit = 1 / objValue;
        if (moth.fit > moth.bestFit) {
            moth.bestFit = moth.fit;
            moth.bestObjValue = moth.curObjValue;
            moth.bestVars = moth.curVars.clone();
        }
    }

    // 获取一个随机生成的飞蛾
    Moth getRandomMoth() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Moth(vars.clone(), objValue, 1 / objValue, vars.clone(), objValue, 1 / objValue);
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制飞蛾
    Moth copyMoth(Moth old) {
        return new Moth(old.curVars.clone(), old.curObjValue, old.fit, old.bestVars.clone(), old.bestObjValue, old.bestFit);
    }

}
