package com.wskh;

import com.wskh.SSA樽海鞘优化算法.SSA_Solver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @Author：WSKH
 * @ClassName：PlotUtil
 * @ClassType：
 * @Description：
 * @Date：2022/6/6/18:31
 * @Email：1187560563@qq.com
 * @Blog：https://blog.csdn.net/weixin_51545953?type=blog
 */
public class PlotUtil extends Application {

    //当前的时间轴
    private Timeline nowTimeline;
    //绘图位置坐标
    private double[][][] positionArr;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // 调用算法获取绘图数据
        SSA_Solver solver = new SSA_Solver();
        solver.solve();
        positionArr = solver.positionArr;

        // 画图
        try {
            BorderPane root = new BorderPane();
            root.setStyle("-fx-padding: 20;");
            Scene scene = new Scene(root, 1600, 900);
            double canvasWid = 800;
            double canvasHei = 800;
            //根据画布大小缩放坐标值
            this.fixPosition(canvasWid - 100, canvasHei - 100);

            //画布和画笔
            HBox canvasHbox = new HBox();
            Canvas canvas = new Canvas();
            canvas.setWidth(canvasWid);
            canvas.setHeight(canvasHei);
            canvasHbox.setPrefWidth(canvasWid);
            canvasHbox.getChildren().add(canvas);
            canvasHbox.setAlignment(Pos.CENTER);
            canvasHbox.setStyle("-fx-spacing: 20;" +
                    "-fx-background-color: #87e775;");
            root.setTop(canvasHbox);
            GraphicsContext paintBrush = canvas.getGraphicsContext2D();

            //启动
            HBox hBox2 = new HBox();
            Button beginButton = new Button("播放迭代过程");
            hBox2.getChildren().add(beginButton);
            root.setBottom(hBox2);
            hBox2.setAlignment(Pos.CENTER);
            //启动仿真以及暂停仿真
            beginButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                nowTimeline.play();
            });

            //创建扫描线连接动画
            nowTimeline = new Timeline();
            createAnimation(paintBrush);

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修正cityPositionArr的坐标，让画出来的点在画布内
     *
     * @param width
     * @param height
     */
    private void fixPosition(double width, double height) {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (int i = 0; i < this.positionArr.length; i++) {
            for (int j = 0; j < this.positionArr[0].length; j++) {
                minX = Math.min(minX, this.positionArr[i][j][0]);
                maxX = Math.max(maxX, this.positionArr[i][j][0]);
                minY = Math.min(minY, this.positionArr[i][j][1]);
                maxY = Math.max(maxY, this.positionArr[i][j][1]);
            }
        }

        double multiple = Math.max((maxX - minX) / width, (maxY - minY) / height);

        //转化为正数数
        for (int i = 0; i < this.positionArr.length; i++) {
            for (int j = 0; j < this.positionArr[0].length; j++) {
                if (minX < 0) {
                    this.positionArr[i][j][0] = this.positionArr[i][j][0] - minX;
                }
                if (minY < 0) {
                    this.positionArr[i][j][1] = this.positionArr[i][j][1] - minY;
                }
            }
        }

        for (int i = 0; i < this.positionArr.length; i++) {
            for (int j = 0; j < this.positionArr[0].length; j++) {
                this.positionArr[i][j][0] = this.positionArr[i][j][0] / multiple;
                this.positionArr[i][j][1] = this.positionArr[i][j][1] / multiple;
            }
        }

    }

    /**
     * 用画笔在画布上画出所有的孔
     * 画第i代的所有粒子
     */
    private void drawAllCircle(GraphicsContext paintBrush, int i) {
        paintBrush.clearRect(0, 0, 2000, 2000);
        paintBrush.setFill(Color.RED);
        for (int j = 0; j < this.positionArr[i].length; j++) {
            drawCircle(paintBrush, i, j);
        }
    }

    /**
     * 用画笔在画布上画出一个孔
     * 画第i代的第j个粒子
     */
    private void drawCircle(GraphicsContext paintBrush, int i, int j) {
        double x = this.positionArr[i][j][0];
        double y = this.positionArr[i][j][1];
        double radius = 2;
        // 圆的直径
        double diameter = radius * 2;
        paintBrush.fillOval(x, y, diameter, diameter);
    }

    /**
     * 创建动画
     */
    private void createAnimation(GraphicsContext paintBrush) {
        for (int i = 0; i < this.positionArr[0].length; i++) {
            int finalI = i;
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(i * 0.05), event -> drawAllCircle(paintBrush, finalI));
            nowTimeline.getKeyFrames().add(keyFrame);
        }
    }

}
