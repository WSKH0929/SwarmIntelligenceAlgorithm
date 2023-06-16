package com.wskh.AFSA人工鱼群算法;

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
public class AFSA_Solver {

    // 鱼对象
    class Fish {
        // 当前鱼的坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;
        // 全参构造
        public Fish(double[] curVars, double curObjValue, double fit) {
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
    // 鱼群中鱼的个数
    int fishNum = 300;
    // 每次的最大觅食次数
    int preyCnt = 20;
    // 鱼的最大感知距离
    double visual = 50;
    // 可接受的拥挤程度
    double crowdedRate = 0.6;
    // 步长数组（各个维度的步长）
    double[] stepArr = new double[]{1.2, 1.2};
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 随机数对象
    Random random = new Random();
    // 鱼群
    Fish[] fishes;
    // 最佳的鱼
    Fish bestFish;
    // 记录迭代过程
    public double[][][] positionArr;
    // 当前记录的行数
    int r;

    // 求解主函数
    public void solve() {
        // 初始化鱼群
        initFishes();
        // 开始迭代
        for (int t = 0; t < maxGen; t++) {
            // 聚群行为
            AFSwarm();
            report();
            // 追尾
            AFFollow();
            report();
            // 觅食
            AFPrey();
            report();
        }
        for (Fish fish : fishes) {
            if (fish.fit > bestFish.fit) {
                bestFish = fish;
            }
        }
        // 输出最好的结果
        System.out.println("变量取值为：" + Arrays.toString(bestFish.curVars));
        System.out.println("最优解为：" + bestFish.curObjValue);
    }

    // 移动行为(在鱼群的移动过程中，当一条鱼或几条鱼找到食物时，附近的伙伴会迅速移动并到达食物)
    void AFFollow() {
        for (int i = 0; i < fishes.length; i++) {
            // 探索视野内的鱼有多少条，并找到最大适应值的鱼
            int friendCount = 0;
            Fish maxFish = copyFish(fishes[i]);
            for (int j = 0; j < fishes.length; j++) {
                if (i != j && getDistance(fishes[i], fishes[j]) <= visual) {
                    friendCount++;
                    if (fishes[j].fit > maxFish.fit) {
                        maxFish = copyFish(fishes[j]);
                    }
                }
            }
            // 如果周围不拥挤，且两个鱼的目标函数不相等
            if ((double) friendCount / fishNum <= crowdedRate && Math.abs(maxFish.curObjValue - fishes[i].curObjValue) > 0.0000001) {
                // 尝试往最大适应值位置游
                Fish tempFish = copyFish(fishes[i]);
                double distance = getDistance(maxFish, tempFish);
                for (int m = 0; m < varNum; m++) {
                    double move = (maxFish.curVars[m] - tempFish.curVars[m]) / (distance) * stepArr[m] * random.nextDouble();
                    moveFish(tempFish, m, move);
                }
                tempFish.curObjValue = getObjValue(tempFish.curVars);
                tempFish.fit = 1 / tempFish.curObjValue;
                if (tempFish.fit > fishes[i].fit) {
                    fishes[i] = tempFish;
                } else {
                    // 否则，进行觅食
                    AFPrey(i);
                }
            } else {
                // 否则，进行觅食
                AFPrey(i);
            }
        }
    }

    // 聚群行为(鱼群在移动的过程中会自然地成群聚集，这是一种生活习惯，可以保证群体的存在，避免危险)
    void AFSwarm() {
        for (int i = 0; i < fishes.length; i++) {
            // 探索视野内的鱼有多少条，并计算周围鱼群的中心
            double[] midPoint = new double[varNum];
            int friendCount = 0;
            for (int j = 0; j < fishes.length; j++) {
                if (i != j && getDistance(fishes[i], fishes[j]) <= visual) {
                    friendCount++;
                    for (int m = 0; m < varNum; m++) {
                        midPoint[m] += fishes[j].curVars[m];
                    }
                }
            }
            // 计算中心
            for (int m = 0; m < midPoint.length; m++) {
                midPoint[m] /= friendCount;
            }
            double objValue = getObjValue(midPoint);
            // 获取中心的鱼
            Fish midFish = new Fish(midPoint, objValue, 1 / objValue);
            // 如果中心不拥挤
            if ((double) friendCount / fishNum <= crowdedRate) {
                // 尝试往中心位置游
                Fish tempFish = copyFish(fishes[i]);
                double distance = getDistance(midFish, tempFish);
                for (int m = 0; m < varNum; m++) {
                    double move = (midPoint[m] - tempFish.curVars[m]) / (distance) * stepArr[m] * random.nextDouble();
                    moveFish(tempFish, m, move);
                }
                tempFish.curObjValue = getObjValue(tempFish.curVars);
                tempFish.fit = 1 / tempFish.curObjValue;
                if (tempFish.fit > fishes[i].fit) {
                    fishes[i] = tempFish;
                } else {
                    // 否则，进行觅食
                    AFPrey(i);
                }
            } else {
                // 否则，进行觅食
                AFPrey(i);
            }
        }
    }

    // 觅食行为(这是鱼类对食物的基本生物学行为。一般来说，鱼通过视觉感知水中食物的浓度来决定向哪移动，然后选择移动的方向)
    void AFPrey(int i) {
        // 尝试觅食preyCnt次
        for (int j = 0; j < preyCnt; j++) {
            Fish tempFish = copyFish(fishes[i]);
            for (int m = 0; m < varNum; m++) {
                moveFish(tempFish, m, visual * (random.nextDouble() - 0.5) * 2);
            }
            tempFish.curObjValue = getObjValue(tempFish.curVars);
            tempFish.fit = 1 / tempFish.curObjValue;
            if (tempFish.fit > fishes[i].fit) {
                double distance = getDistance(tempFish, fishes[i]);
                // 觅食成功，向成功方向移动
                for (int m = 0; m < varNum; m++) {
                    double move = (tempFish.curVars[m] - fishes[i].curVars[m]) / (distance) * stepArr[m] * random.nextDouble();
                    moveFish(fishes[i], m, move);
                }
                fishes[i].curObjValue = getObjValue(fishes[i].curVars);
                fishes[i].fit = 1 / fishes[i].curObjValue;
            } else {
                // 觅食失败，随机移动
                for (int m = 0; m < varNum; m++) {
                    double move = stepArr[m] * (random.nextDouble() - 0.5) * 2;
                    moveFish(tempFish, m, move);
                }
                tempFish.curObjValue = getObjValue(tempFish.curVars);
                tempFish.fit = 1 / tempFish.curObjValue;
                if(tempFish.fit > fishes[i].fit){
                    fishes[i] = tempFish;
                }
            }
        }
    }
    void AFPrey(){
        for (int i = 0; i < fishes.length; i++) {
            AFPrey(i);
        }
    }

    // 记录
    void report(){
        for (int i = 0; i < fishes.length; i++) {
            for (int j = 0; j < varNum; j++) {
                positionArr[r][i][j] = fishes[i].curVars[j];
            }
        }
        r++;
    }

    // 初始化鱼群
    private void initFishes() {
        positionArr = new double[3*maxGen][fishNum][varNum];
        fishes = new Fish[fishNum];
        for (int i = 0; i < fishNum; i++) {
            fishes[i] = getRandomFish();
            if (i == 0 || bestFish.fit < fishes[i].fit) {
                bestFish = copyFish(fishes[i]);
            }
        }
    }

    // 控制鱼在第m个维度上移动n个距离
    public void moveFish(Fish fish, int m, double n) {
        // 移动
        fish.curVars[m] += n;
        // 超出定义域的判断
        if (fish.curVars[m] < lb[m]) {
            fish.curVars[m] = lb[m];
        }
        if (fish.curVars[m] > ub[m]) {
            fish.curVars[m] = ub[m];
        }
    }

    // 求两条鱼之间的距离
    double getDistance(Fish f1, Fish f2) {
        double dis = 0d;
        for (int i = 0; i < varNum; i++) {
            dis += Math.pow(f1.curVars[i] - f2.curVars[i], 2);
        }
        return Math.sqrt(dis);
    }

    // 求两点之间的距离
    double getDistance(double p1, double p2) {
        return Math.sqrt(Math.pow(p1 - p2, 2));
    }

    // 获取一个随机生成的鱼
    Fish getRandomFish() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Fish(vars.clone(), objValue, 1 / objValue);
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制鱼
    Fish copyFish(Fish old) {
        return new Fish(old.curVars.clone(), old.curObjValue, old.fit);
    }

}
