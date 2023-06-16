package com.wskh.SCSO沙丘猫群优化算法;

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
public class SCSO_Solver {

    // 沙丘猫对象
    class Cat {
        // 当前沙丘猫的坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;
        // 灵敏度
        double sensitivity;

        // 全参构造
        public Cat(double[] curVars, double curObjValue, double fit, double sensitivity) {
            this.curVars = curVars;
            this.curObjValue = curObjValue;
            this.fit = fit;
            this.sensitivity = sensitivity;
        }
    }

    // 算法参数
    // 变量个数
    int varNum = 2;
    // 最大迭代次数
    int maxGen = 500;
    // 沙丘猫群中沙丘猫的个数
    int catNum = 500;
    // 猫的最大灵敏度
    double maxSensitivity = 2;
    // 步长数组（各个维度的步长）
    double[] stepArr = new double[]{1.2, 1.2};
    // 搜索猎物的次数
    int localSearchCnt = 1;
    // 搜索猎物的次数
    int localAttackCnt = 1;
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 随机数对象
    Random random = new Random();
    // 沙丘猫群
    Cat[] cats;
    // 最佳的沙丘猫
    Cat bestCat;
    // 记录迭代过程
    public double[][][] positionArr;
    // 当前记录的行数
    int r;

    // 求解主函数
    public void solve() {
        // 初始化沙丘猫群
        initCats();
        // 开始迭代
        for (int i = 0; i < maxGen; i++) {
            localSearch();
            report();
            localAttack();
            report();
        }
        // 输出最好的结果
        System.out.println("变量取值为：" + Arrays.toString(bestCat.curVars));
        System.out.println("最优解为：" + bestCat.curObjValue);
    }

    // 攻击猎物
    void localAttack() {
        for (int i = 0; i < cats.length; i++) {
            for (int n = 0; n < localAttackCnt; n++) {
                Cat tempCat = copyCat(cats[i]);
                for (int m = 0; m < varNum; m++) {
                    double move = 0d;
                    if (tempCat.sensitivity > 1) {
                        move = tempCat.sensitivity * (bestCat.curVars[m] - random.nextDouble() * tempCat.curVars[m]) - tempCat.curVars[m];
                    } else {
                        move = bestCat.curVars[m] - Math.abs(random.nextDouble() * (bestCat.curVars[m] - tempCat.curVars[m])) * random.nextDouble() * tempCat.sensitivity - tempCat.curVars[m];
                    }
                    moveCat(tempCat, m, move);
                }
                updateCat(tempCat);
                if (tempCat.fit > cats[i].fit) {
                    cats[i] = tempCat;
                    if (tempCat.fit > bestCat.fit) {
                        bestCat = copyCat(tempCat);
                    }
                }
            }
        }
    }

    // 搜寻猎物
    void localSearch() {
        for (int i = 0; i < cats.length; i++) {
            for (int n = 0; n < localSearchCnt; n++) {
                Cat tempCat = copyCat(cats[i]);
                for (int m = 0; m < varNum; m++) {
                    double move = tempCat.sensitivity * (bestCat.curVars[m] - random.nextDouble() * tempCat.curVars[m]) - tempCat.curVars[m];
                    moveCat(tempCat, m, move);
                }
                updateCat(tempCat);
                if (tempCat.fit > cats[i].fit) {
                    cats[i] = tempCat;
                    if (tempCat.fit > bestCat.fit) {
                        bestCat = copyCat(tempCat);
                    }
                }
            }
        }
    }

    // 记录
    void report() {
        for (int i = 0; i < cats.length; i++) {
            for (int j = 0; j < varNum; j++) {
                positionArr[r][i][j] = cats[i].curVars[j];
            }
        }
        r++;
    }

    // 求两个沙丘猫之间的距离
    double getDistance(Cat f1, Cat f2) {
        double dis = 0d;
        for (int i = 0; i < varNum; i++) {
            dis += Math.pow(f1.curVars[i] - f2.curVars[i], 2);
        }
        return Math.sqrt(dis);
    }

    // 初始化沙丘猫群
    private void initCats() {
        positionArr = new double[2 * maxGen][catNum][varNum];
        cats = new Cat[catNum];
        for (int i = 0; i < catNum; i++) {
            cats[i] = getRandomCat();
            if (i == 0 || bestCat.fit < cats[i].fit) {
                bestCat = copyCat(cats[i]);
            }
        }
    }

    // 控制沙丘猫在第m个维度上移动n个距离
    public void moveCat(Cat cat, int m, double n) {
        // 移动
        cat.curVars[m] += n;
        // 超出定义域的判断
        if (cat.curVars[m] < lb[m]) {
            cat.curVars[m] = lb[m];
        }
        if (cat.curVars[m] > ub[m]) {
            cat.curVars[m] = ub[m];
        }
    }

    // 更新沙丘猫信息
    void updateCat(Cat cat) {
        double objValue = getObjValue(cat.curVars);
        cat.curObjValue = objValue;
        cat.fit = 1 / objValue;
    }

    // 获取一个随机生成的沙丘猫
    Cat getRandomCat() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Cat(vars.clone(), objValue, 1 / objValue, random.nextDouble() * maxSensitivity);
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制沙丘猫
    Cat copyCat(Cat old) {
        return new Cat(old.curVars.clone(), old.curObjValue, old.fit, old.sensitivity);
    }

}
