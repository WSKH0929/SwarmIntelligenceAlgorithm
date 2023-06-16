package com.wskh.SSA社会蜘蛛算法;

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

    // 蜘蛛对象
    class Spider {
        // 当前蜘蛛的坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;
        // 蜘蛛自身的振动
        double selfVibration;
        // 上一次迭代时的目标振动
        double lastVibration;
        // 自上一次改变目标振动后的迭代次数
        int c;
        // 上一次迭代时的移动
        double[] lastMove = new double[varNum];
        // 上一次迭代中用于移动的维度掩码
        int[] maskArr = new int[varNum];

        // 全参构造
        public Spider(double[] curVars, double curObjValue, double fit, double selfVibration, double lastVibration, int c, double[] lastMove, int[] maskArr) {
            this.curVars = curVars;
            this.curObjValue = curObjValue;
            this.fit = fit;
            this.selfVibration = selfVibration;
            this.lastVibration = lastVibration;
            this.c = c;
            this.lastMove = lastMove;
            this.maskArr = maskArr;
        }
    }

    // 算法参数
    // 变量个数
    int varNum = 2;
    // 最大迭代次数
    int maxGen = 500;
    // 蜘蛛群中蜘蛛的个数
    int spiderNum = 300;
    // C为一足够小的数，所有可能的适应度值都大于C值（考虑最小化问题）
    double C = -999999;
    // 振动抗衰率(ra越大，振动衰减越弱)
    double ra = 9999;
    // 掩码改变概率
    double pc = 0.95;
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 随机数对象
    Random random = new Random();
    // 蜘蛛群
    Spider[] spiders;
    // 最佳的蜘蛛
    Spider bestSpider;
    // 记录迭代过程
    public double[][][] positionArr;
    // 当前记录的行数
    int r;

    // 求解主函数
    public void solve() {
        // 初始化蜘蛛群
        initSpiders();
        // 开始迭代
        for (int i = 0; i < maxGen; i++) {
            // 更新所有蜘蛛自身的振动
            updateSpiderVibration();
            // 随机行走
            randomMove();
            report();
        }
        // 输出最好的结果
        System.out.println("变量取值为：" + Arrays.toString(bestSpider.curVars));
        System.out.println("最优解为：" + bestSpider.curObjValue);
    }

    // 更新所有蜘蛛自身的振动
    void updateSpiderVibration() {
        for (int i = 0; i < spiderNum; i++) {
            spiders[i].selfVibration = Math.log(1d / (spiders[i].fit - C) + 1);
        }
    }

    // 随机行走
    private void randomMove() {
        // 获取标准差
        double sd = calculateStandardDeviation();
        // 开始遍历蜘蛛群
        for (int i = 0; i < spiders.length; i++) {
            // 计算其他蜘蛛对蜘蛛i的最大振动
            double bestVibration = 0d;
            for (int j = 0; j < spiders.length; j++) {
                if (i != j) {
                    // 计算蜘蛛j对蜘蛛i产生的振动
                    double vibrationJ_I = spiders[j].selfVibration * Math.exp(-(getDistance(spiders[i], spiders[j])) / (sd * ra));
                    bestVibration = Math.max(vibrationJ_I, bestVibration);
                }
            }
            // 判断当前最大振动是否比蜘蛛i存储的最大振动大
            if (spiders[i].lastVibration < bestVibration) {
                spiders[i].lastVibration = bestVibration;
                spiders[i].c = 0;
            } else {
                spiders[i].c++;
            }
            // 改变掩码
            for (int m = 0; m < varNum; m++) {
                if (random.nextDouble() <= Math.pow(pc, spiders[i].c)) {
                    spiders[i].maskArr[m] = (spiders[i].maskArr[m] == 0 ? 1 : 0);
                }
            }
            // 基于掩码生成新位置
            Spider tempSpider = copySpider(spiders[i]);
            for (int m = 0; m < varNum; m++) {
                double pfo = (tempSpider.maskArr[m] == 0 ? tempSpider.lastVibration : spiders[random.nextInt(spiderNum)].curVars[m]);
                double move = (tempSpider.curVars[m] - tempSpider.lastMove[m]) * random.nextDouble() + (pfo - tempSpider.curVars[m]) * random.nextDouble();
                moveSpider(tempSpider, m, move);
                tempSpider.lastMove[m] = move;
            }
            updateSpider(tempSpider);
            if(tempSpider.fit > spiders[i].fit){
                spiders[i] = tempSpider;
                if (spiders[i].fit > bestSpider.fit) {
                    bestSpider = copySpider(spiders[i]);
                }
            }
        }
    }

    // 计算标准差
    double calculateStandardDeviation() {
        double[] averageArray = new double[varNum];
        for (int i = 0; i < spiderNum; i++) {
            for (int m = 0; m < varNum; m++) {
                averageArray[m] += spiders[i].curVars[m];
            }
        }
        for (int m = 0; m < averageArray.length; m++) {
            averageArray[m] /= spiderNum;
        }
        double sd = 0d;
        for (int i = 0; i < spiderNum; i++) {
            for (int m = 0; m < varNum; m++) {
                sd += (Math.pow(spiders[i].curVars[m] - averageArray[m], 2));
            }
        }
        return Math.sqrt(sd);
    }

    // 记录
    void report() {
        for (int i = 0; i < spiders.length; i++) {
            for (int j = 0; j < varNum; j++) {
                positionArr[r][i][j] = spiders[i].curVars[j];
            }
        }
        r++;
    }

    // 求两个蜘蛛之间的距离
    double getDistance(Spider f1, Spider f2) {
        double dis = 0d;
        for (int i = 0; i < varNum; i++) {
            dis += Math.pow(f1.curVars[i] - f2.curVars[i], 2);
        }
        return Math.sqrt(dis);
    }

    // 初始化蜘蛛群
    private void initSpiders() {
        positionArr = new double[maxGen][spiderNum][varNum];
        spiders = new Spider[spiderNum];
        for (int i = 0; i < spiderNum; i++) {
            spiders[i] = getRandomSpider();
            if (i == 0 || bestSpider.fit < spiders[i].fit) {
                bestSpider = copySpider(spiders[i]);
            }
        }
    }

    // 控制蜘蛛在第m个维度上移动n个距离
    public void moveSpider(Spider spider, int m, double n) {
        if (spider.curVars[m] + n > ub[m]) {
            spider.curVars[m] = (ub[m] - spider.curVars[m]) * random.nextDouble();
        } else if (spider.curVars[m] + n < lb[m]) {
            spider.curVars[m] = (spider.curVars[m] - lb[m]) * random.nextDouble();
        } else {
            spider.curVars[m] += n;
        }
    }

    // 更新蜘蛛信息
    void updateSpider(Spider spider) {
        double objValue = getObjValue(spider.curVars);
        spider.curObjValue = objValue;
        spider.fit = 1 / objValue;
    }

    // 获取一个随机生成的蜘蛛
    Spider getRandomSpider() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new Spider(vars.clone(), objValue, 1 / objValue, 0, 0, 0, new double[varNum], new int[varNum]);
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制蜘蛛
    Spider copySpider(Spider old) {
        return new Spider(old.curVars.clone(), old.curObjValue, old.fit, old.selfVibration, old.lastVibration, old.c, old.lastMove.clone(), old.maskArr.clone());
    }

}
