package com.wskh.PSO粒子群;

import java.util.Arrays;
import java.util.Random;

/**
 * @Author：WSKH
 * @ClassName：PSO_Solve
 * @ClassType：
 * @Description：
 * @Date：2022/6/6/14:59
 * @Email：1187560563@qq.com
 * @Blog：https://blog.csdn.net/weixin_51545953?type=blog
 */
public class PSO_Solver {

    // 粒子对象
    class Particle {
        // 粒子速度数组（每个方向都有一个速度）
        double[] vArr;
        // 当前粒子坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 该粒子找到过的最佳目标函数值
        double bestObjValue;
        // 该粒子最好位置时的坐标
        double[] bestVars;
        // 全参构造
        public Particle(double[] vArr, double[] curVars, double curObjValue, double bestObjValue, double[] bestVars) {
            this.vArr = vArr;
            this.curVars = curVars;
            this.curObjValue = curObjValue;
            this.bestObjValue = bestObjValue;
            this.bestVars = bestVars;
        }
    }

    // 粒子数量
    int n = 500;
    // 每个粒子的个体学习因子：自我认知，设置较大则不容易被群体带入局部最优，但会减缓收敛速度
    double c1 = 2;
    // 每个粒子的社会学习因子：社会认知，设置较大则加快收敛，但容易陷入局部最优
    double c2 = 2;
    // 粒子的惯性权重
    double w = 0.9;
    // 迭代的次数
    int MaxGen = 500;
    // 粒子的每个维度上的最大速度（数组）
    double[] vMaxArr = new double[]{6,6};
    // 随机数对象
    Random random = new Random();
    // 自变量个数
    int varNum = 2;
    // 自变量的上下界数组
    double[] lbArr = new double[]{-1000, -1000};
    double[] ubArr = new double[]{1000, 1000};
    // 粒子群
    Particle[] particles;
    // 最佳粒子
    Particle bestParticle;
    // 记录迭代过程
    public double[][][] positionArr;

    /**
     * @Description 初始化粒子的位置和速度
     */
    private void initParticles() {
        // 初始化粒子群
        particles = new Particle[n];
        // 随机生成粒子
        for (int i = 0; i < particles.length; i++) {
            // 随机生成坐标和速度
            double[] vars = new double[varNum];
            double[] vArr = new double[varNum];
            for (int j = 0; j < varNum; j++) {
                vars[j] = random.nextDouble() * (ubArr[j] - lbArr[j]) + lbArr[j];
                vArr[j] = (random.nextDouble() - 0.5) * 2 * vMaxArr[j];
            }
            // 目标函数值
            double objValue = getObjValue(vars);
            particles[i] = new Particle(vArr.clone(), vars.clone(), objValue, objValue, vars.clone());
        }
        // 找到初始化粒子群中的最佳粒子
        bestParticle = copyParticle(particles[0]);
        for (int i = 1; i < particles.length; i++) {
            if (bestParticle.bestObjValue > particles[i].bestObjValue) {
                bestParticle = copyParticle(particles[i]);
            }
        }
    }

    /**
     * @Description 主要求解函数
     */
    public void solve() {
        // 变量设置初步判断
        if(varNum != vMaxArr.length || varNum != lbArr.length || varNum != ubArr.length){
            throw new RuntimeException("变量维度不一致");
        }
        positionArr = new double[MaxGen][n][varNum];
        // 初始化粒子的位置和速度
        initParticles();
        // 开始迭代
        for (int i = 0; i < MaxGen; i++) {
            // 依次更新第i个粒子的速度与位置
            for (int j = 0; j < particles.length; j++) {
                // 针对不同维度进行处理
                for (int k = 0; k < varNum; k++) {
                    // 更新速度
                    double newV = particles[j].vArr[k] * w
                            + c1 * random.nextDouble() * (particles[j].bestVars[k] - particles[j].curVars[k])
                            + c2 * random.nextDouble() * (bestParticle.bestVars[k] - particles[j].curVars[k]);
                    // 如果速度超过了最大限制，就对其进行调整
                    if (newV < -vMaxArr[k]) {
                        newV = -vMaxArr[k];
                    } else if (newV > vMaxArr[k]) {
                        newV = vMaxArr[k];
                    }
                    // 更新第j个粒子第k个维度上的位置
                    double newPos = particles[j].curVars[k] + newV;
                    // 记录迭代过程
                    positionArr[i][j][k] = newPos;
                    // 如果位置超出了定义域，就对其进行调整
                    if(newPos < lbArr[k]){
                        newPos = lbArr[k];
                    }else if(newPos > ubArr[k]){
                        newPos = ubArr[k];
                    }
                    // 赋值回去
                    particles[j].curVars[k] = newPos;
                    particles[j].vArr[k] = newV;
                }
                // 更新完所有维度后，再计算第j个粒子的函数值
                double objValueJ = getObjValue(particles[j].curVars);
                particles[j].curObjValue = objValueJ;
                if(objValueJ < particles[j].bestObjValue){
                    particles[j].bestVars = particles[j].curVars.clone();
                    particles[j].bestObjValue = particles[j].curObjValue;
                }
                if(objValueJ < bestParticle.bestObjValue){
                    bestParticle = copyParticle(particles[j]);
                }
            }
        }
        // 迭代结束，输出最优粒子位置和函数值
        System.out.println("最优解为："+ bestParticle.bestObjValue);
        System.out.println("最优解坐标为："+ Arrays.toString(bestParticle.bestVars));
    }

    /**
     * @param vars 自变量数组
     * @return 返回目标函数值
     */
    public double getObjValue(double[] vars) {
        //目标：在变量区间范围最小化 Z = x^2 + y^2 - xy - 10x - 4y +60
        return Math.pow(vars[0], 2) + Math.pow(vars[1], 2) - vars[0] * vars[1] - 10 * vars[0] - 4 * vars[1] + 60;
    }

    // 复制粒子
    public Particle copyParticle(Particle old) {
        return new Particle(old.vArr.clone(), old.curVars.clone(), old.curObjValue, old.bestObjValue, old.bestVars.clone());
    }

}
