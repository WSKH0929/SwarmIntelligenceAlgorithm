package com.wskh.SMO蜘蛛猴优化算法;

import java.util.*;

/**
 * @Author：WSKH
 * @ClassName：SMO_Solver
 * @ClassType：
 * @Description：
 * @Date：2022/6/8/13:42
 * @Email：1187560563@qq.com
 * @Blog：https://blog.csdn.net/weixin_51545953?type=blog
 */
public class SMO_Solver {

    // 蜘蛛猴对象
    class SpiderMonkey {
        // 当前蜘蛛猴坐标（自变量数组）
        double[] curVars;
        // 当前自变量对应的目标函数值
        double curObjValue;
        // 适应度（解决最小化问题，所以适应度为目标函数值的倒数）
        double fit;
        // 全参数构造
        public SpiderMonkey(double[] curVars, double curObjValue, double fit) {
            this.curVars = curVars;
            this.curObjValue = curObjValue;
            this.fit = fit;
        }
    }

    // 算法参数
    // 蜘蛛猴群
    List<SpiderMonkey[]> spiderMonkeyList = new ArrayList<>();
    // 局部领导者
    List<SpiderMonkey> localLeaderList = new ArrayList<>();
    // 最好的蜘蛛猴(全局领导者)
    SpiderMonkey bestSpiderMonkey;
    // 随机数对象
    Random random = new Random();
    // 最大迭代次数
    int maxGen = 500;
    // 蜘蛛猴数量
    int spiderMonkeyNum = 300;
    // 局部搜索次数(一般等于蜘蛛猴数量)
    int localSearchCount = spiderMonkeyNum;
    // 局部领导者决策阶段的更新几率
    double LLDP_PR = 0.1;
    // 局部领导者阶段的更新几率
    double LLP_PR = 0.8;
    // 变量维度数
    int varNum = 2;
    // 最大组数(一般要至少保证每组里有10个蜘蛛猴)
    int maxgroupNum = spiderMonkeyNum/10 ;
    // 变量的上下界
    double[] ub = new double[]{1000, 1000};
    double[] lb = new double[]{-1000, -1000};
    // 局部计数器
    int[] localLimitCount = new int[]{0};
    // 停止条件
    int limitCnt = 50;
    // 全局计数器
    int globalLimitCount;
    // 记录迭代过程
    public double[][][] positionArr;
    // 记录迭代器的行数
    int curC = 0;
    // 是否开启贪心机制（只接受比当前解好的解）
    boolean greedy = true;

    // 求解主函数
    public void solve() {
        // 初始化蜘蛛猴种群
        initSpiderMonkeys();
        // 开始迭代
        for (int t = 0; t < maxGen; t++) {
            // 局部领导者阶段（LLP：所有的蜘蛛猴都有机会更新自己）
            LLP();
            // 全局领导者阶段（GLP：轮盘赌，随机选取，偏向于对fit值大的蜘蛛猴进行更新）
            GLP();
            // 全局领导者学习阶段（如果全局领导者有更新，则globalLimitCount=0，否则globalLimitCount++）
            GLLP();
            // 局部领导者学习阶段（如果局部领导者有更新，则localLimitCount=0，否则localLimitCount++）
            LLLP();
            // 局部领导者决策阶段
            LLDP();
            // 全局领导者决策阶段
            GLDP();
        }
        // 输出最好的结果
        System.out.println("变量取值为：" + Arrays.toString(bestSpiderMonkey.curVars));
        System.out.println("最优解为：" + bestSpiderMonkey.curObjValue);
    }

    // 全局领导者决策阶段
    private void GLDP() {
        if (globalLimitCount >= limitCnt) {
            globalLimitCount = 0;
            if (spiderMonkeyList.size() < maxgroupNum) {
                // 分裂
                List<SpiderMonkey> tempList = new ArrayList<>();
                for (SpiderMonkey[] spiderMonkeys : spiderMonkeyList) {
                    tempList.addAll(Arrays.asList(spiderMonkeys));
                }
                tempList.sort(new Comparator<SpiderMonkey>() {
                    @Override
                    public int compare(SpiderMonkey o1, SpiderMonkey o2) {
                        return Double.compare(o2.fit,o1.fit);
                    }
                });
                //
                int groupNum = spiderMonkeyList.size() + 1;
                spiderMonkeyList = new ArrayList<>();
                int avgNum = spiderMonkeyNum / groupNum;
                for (int i = 0; i < groupNum - 1; i++) {
                    SpiderMonkey[] spiderMonkeys = new SpiderMonkey[avgNum];
                    for (int j = 0; j < avgNum; j++) {
                        spiderMonkeys[j] = copySpiderMonkey(tempList.remove(0));
                    }
                    spiderMonkeyList.add(spiderMonkeys);
                }
                spiderMonkeyList.add(tempList.toArray(new SpiderMonkey[0]));
                localLimitCount = new int[groupNum];
            } else {
                // 融合
                SpiderMonkey[] spiderMonkeys = new SpiderMonkey[spiderMonkeyNum];
                int i = 0;
                for (SpiderMonkey[] monkeys : spiderMonkeyList) {
                    for (SpiderMonkey monkey : monkeys) {
                        spiderMonkeys[i++] = copySpiderMonkey(monkey);
                    }
                }
                spiderMonkeyList = new ArrayList<>();
                spiderMonkeyList.add(spiderMonkeys);
                localLimitCount = new int[]{0};
            }
            // 更新局部领导者
            localLeaderList = new ArrayList<>();
            for (SpiderMonkey[] spiderMonkeys : spiderMonkeyList) {
                localLeaderList.add(copySpiderMonkey(spiderMonkeys[0]));
                int index = localLeaderList.size() - 1;
                for (int i = 1; i < spiderMonkeys.length; i++) {
                    if (localLeaderList.get(index).fit < spiderMonkeys[i].fit) {
                        localLeaderList.set(index, copySpiderMonkey(spiderMonkeys[i]));
                    }
                }
            }
        }
    }

    // 局部领导者决策阶段
    private void LLDP() {
        int c = 0;
        for (int i = 0; i < spiderMonkeyList.size(); i++) {
            SpiderMonkey[] spiderMonkeys = spiderMonkeyList.get(i);
            if (localLimitCount[i] < limitCnt) {
                for (int j = 0; j < spiderMonkeys.length; j++) {
                    SpiderMonkey tempSpiderMonkey = copySpiderMonkey(spiderMonkeys[j]);
                    for (int m = 0; m < varNum; m++) {
                        if (random.nextDouble() <= LLDP_PR) {
                            tempSpiderMonkey.curVars[m] = lb[m] + random.nextDouble() * (ub[m] - lb[m]);
                        } else {
                            double moveDist = random.nextDouble() * (bestSpiderMonkey.curVars[m] - tempSpiderMonkey.curVars[m])
                                    + random.nextDouble() * (spiderMonkeys[random.nextInt(spiderMonkeys.length)].curVars[m] - tempSpiderMonkey.curVars[m]);
                            moveSpiderMonkey(tempSpiderMonkey, m, moveDist);
                        }
                    }
                    tempSpiderMonkey.curObjValue = getObjValue(tempSpiderMonkey.curVars);
                    tempSpiderMonkey.fit = 1 / tempSpiderMonkey.curObjValue;
                    if(greedy){
                        if(tempSpiderMonkey.fit > spiderMonkeys[j].fit){
                            spiderMonkeys[j] = tempSpiderMonkey;
                        }
                    }else{
                        spiderMonkeys[j] = tempSpiderMonkey;
                    }
                }
            }
            for (int j = 0; j < spiderMonkeys.length; j++) {
                for (int m = 0; m < spiderMonkeys[j].curVars.length; m++) {
                    positionArr[curC][c][m] = spiderMonkeys[j].curVars[m];
                }
                c++;
            }
        }
        curC++;
    }

    // 局部领导者学习阶段（如果局部领导者有更新，则localLimitCount=0，否则localLimitCount++）
    private void LLLP() {
        for (int i = 0; i < spiderMonkeyList.size(); i++) {
            boolean isUpdate = false;
            for (SpiderMonkey spiderMonkey : spiderMonkeyList.get(i)) {
                if (localLeaderList.get(i).fit < spiderMonkey.fit) {
                    localLeaderList.set(i, copySpiderMonkey(spiderMonkey));
                    isUpdate = true;
                }
            }
            if (isUpdate) {
                localLimitCount[i] = 0;
            } else {
                localLimitCount[i]++;
            }
        }
    }

    // 全局领导者学习阶段（如果全局领导者有更新，则globalLimitCount=0，否则globalLimitCount++）
    private void GLLP() {
        boolean isUpdate = false;
        for (int i = 0; i < spiderMonkeyList.size(); i++) {
            for (SpiderMonkey spiderMonkey : spiderMonkeyList.get(i)) {
                if (spiderMonkey.fit > bestSpiderMonkey.fit) {
                    bestSpiderMonkey = copySpiderMonkey(spiderMonkey);
                    isUpdate = true;
                }
            }
        }
        if (isUpdate) {
            globalLimitCount = 0;
        } else {
            globalLimitCount++;
        }
    }

    // 全局领导者阶段（GLP：轮盘赌，随机选取，偏向于对fit值大的蜘蛛猴进行更新）
    private void GLP() {
        int c = 0;
        for (int i = 0; i < spiderMonkeyList.size(); i++) {
            SpiderMonkey[] spiderMonkeys = spiderMonkeyList.get(i);
            // 计算fit总和
            double totalFit = 0;
            for (SpiderMonkey spiderMonkey : spiderMonkeys) {
                totalFit += spiderMonkey.fit;
            }
            // 轮盘赌的累计概率数组
            double[] p = new double[spiderMonkeys.length];
            for (int j = 0; j < p.length; j++) {
                p[j] = (spiderMonkeys[j].fit / totalFit) + (j == 0 ? 0 : p[j - 1]);
            }
            // 局部搜索
            for (int j = 0; j < localSearchCount; j++) {
                double r = random.nextDouble();
                for (int k = 0; k < p.length; k++) {
                    if (r <= p[k]) {
                        for (int m = 0; m < varNum; m++) {
                            double moveDist = random.nextDouble() * (bestSpiderMonkey.curVars[m] - spiderMonkeys[k].curVars[m])
                                    + (random.nextDouble() - 0.5) * 2 * (spiderMonkeys[random.nextInt(spiderMonkeys.length)].curVars[m] - spiderMonkeys[k].curVars[m]);
                            moveSpiderMonkey(spiderMonkeys[k], m, moveDist);
                        }
                        spiderMonkeys[k].curObjValue = getObjValue(spiderMonkeys[k].curVars);
                        spiderMonkeys[k].fit = 1 / spiderMonkeys[k].curObjValue;
                        break;
                    }
                }
            }
            for (int j = 0; j < spiderMonkeys.length; j++) {
                for (int m = 0; m < spiderMonkeys[j].curVars.length; m++) {
                    positionArr[curC][c][m] = spiderMonkeys[j].curVars[m];
                }
                c++;
            }
            spiderMonkeyList.set(i, spiderMonkeys);
        }
        curC++;
    }

    // 局部领导者阶段（LLP：所有的蜘蛛猴都有机会更新自己）
    private void LLP() {
        int c = 0;
        for (int i = 0; i < spiderMonkeyList.size(); i++) {
            SpiderMonkey[] spiderMonkeys = spiderMonkeyList.get(i);
            SpiderMonkey localLeader = localLeaderList.get(i);
            for (int j = 0; j < spiderMonkeys.length; j++) {
                // 以一定几率更新自己
                if (random.nextDouble() <= LLP_PR) {
                    SpiderMonkey tempSpiderMonkey = copySpiderMonkey(spiderMonkeys[j]);
                    for (int m = 0; m < varNum; m++) {
                        double moveDist = random.nextDouble() * (localLeader.curVars[m] - tempSpiderMonkey.curVars[m])
                                + (random.nextDouble() - 0.5) * 2 * (spiderMonkeys[random.nextInt(spiderMonkeys.length)].curVars[m] - tempSpiderMonkey.curVars[m]);
                        moveSpiderMonkey(tempSpiderMonkey, m, moveDist);
                    }
                    tempSpiderMonkey.curObjValue = getObjValue(tempSpiderMonkey.curVars);
                    tempSpiderMonkey.fit = 1 / tempSpiderMonkey.curObjValue;
                    if(greedy){
                        if(tempSpiderMonkey.fit > spiderMonkeys[j].fit){
                            spiderMonkeys[j] = tempSpiderMonkey;
                        }
                    }else{
                        spiderMonkeys[j] = tempSpiderMonkey;
                    }
                }
                for (int m = 0; m < spiderMonkeys[j].curVars.length; m++) {
                    positionArr[curC][c][m] = spiderMonkeys[j].curVars[m];
                }
                c++;
            }
            spiderMonkeyList.set(i, spiderMonkeys);
        }
        curC++;
    }

    // 初始化蜘蛛猴种群
    private void initSpiderMonkeys() {
        positionArr = new double[3 * maxGen][spiderMonkeyNum][varNum];
        SpiderMonkey[] spiderMonkeys = new SpiderMonkey[spiderMonkeyNum];
        SpiderMonkey localLeader = null;
        for (int i = 0; i < spiderMonkeyNum; i++) {
            spiderMonkeys[i] = getRandomSpiderMonkey();
            if (i == 0) {
                bestSpiderMonkey = copySpiderMonkey(spiderMonkeys[0]);
                localLeader = copySpiderMonkey(spiderMonkeys[0]);
            } else {
                if (bestSpiderMonkey.fit < spiderMonkeys[i].fit) {
                    bestSpiderMonkey = copySpiderMonkey(spiderMonkeys[i]);
                    localLeader = copySpiderMonkey(spiderMonkeys[0]);
                }
            }
        }
        spiderMonkeyList.add(spiderMonkeys);
        localLeaderList.add(localLeader);
    }

    // 获取一个随机生成的蜘蛛猴
    SpiderMonkey getRandomSpiderMonkey() {
        double[] vars = new double[varNum];
        for (int j = 0; j < vars.length; j++) {
            vars[j] = lb[j] + random.nextDouble() * (ub[j] - lb[j]);
        }
        double objValue = getObjValue(vars);
        return new SpiderMonkey(vars.clone(), objValue, 1 / objValue);
    }

    // 控制spiderMonkey在第m个维度上移动n个距离
    public void moveSpiderMonkey(SpiderMonkey spiderMonkey, int m, double n) {
        // 移动
        spiderMonkey.curVars[m] += n;
        // 超出定义域的判断
        if (spiderMonkey.curVars[m] < lb[m]) {
            spiderMonkey.curVars[m] = lb[m];
        }
        if (spiderMonkey.curVars[m] > ub[m]) {
            spiderMonkey.curVars[m] = ub[m];
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

    // 复制蜘蛛猴
    SpiderMonkey copySpiderMonkey(SpiderMonkey old) {
        return new SpiderMonkey(old.curVars.clone(), old.curObjValue, old.fit);
    }
}
