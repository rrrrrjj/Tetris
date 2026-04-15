package com.tetris;

import java.util.Random;
import java.util.Arrays;

public class Game {
    public static final int WIDTH = 15;
    public static final int HEIGHT = 20;
    public static final int BLOCK = 20;

    private int[][] map = new int[HEIGHT][WIDTH];
    private boolean[][] kittyMap = new boolean[HEIGHT][WIDTH];
    private int[][] currentShape;
    private int[][] nextShape;
    private int curX, curY;
    private int score = 0;

    private int shapeCount = 0;
    private boolean hasKitty;
    private int kittyI, kittyJ;

    // 新增：道具类型 0=普通 1=炸弹
    private int itemType;

    public Game() {
        nextShape = createRandomShape();
        newShape();
    }

    public int[][] createRandomShape() {
        int[][][] shapes = {
                {{1,1},{1,1}},
                {{1,1,1,1}},
                {{0,1,0},{1,1,1}},
                {{1,0,0},{1,1,1}},
                {{0,0,1},{1,1,1}},
                {{0,1,1},{1,1,0}},
                {{1,1,0},{0,1,1}}
        };
        return shapes[new Random().nextInt(shapes.length)];
    }

    public void newShape() {
        currentShape = nextShape;
        nextShape = createRandomShape();
        curX = WIDTH / 2 - currentShape[0].length / 2;
        curY = 0;

        shapeCount++;
        // 保留：每2普通 + 1Kitty
        hasKitty = (shapeCount % 2 == 0);

        // 新增：道具方块（非Kitty时随机炸弹）
        if (!hasKitty && new Random().nextInt(5) == 0) {
            itemType = 1; // 炸弹
        } else {
            itemType = 0;
        }

        if (hasKitty) {
            Random rand = new Random();
            java.util.List<int[]> list = new java.util.ArrayList<>();
            for (int i = 0; i < currentShape.length; i++)
                for (int j = 0; j < currentShape[i].length; j++)
                    if (currentShape[i][j] == 1)
                        list.add(new int[]{i,j});
            if (!list.isEmpty()) {
                int[] pos = list.get(rand.nextInt(list.size()));
                kittyI = pos[0];
                kittyJ = pos[1];
            }
        }

        if (!isValid(curX, curY)) throw new RuntimeException("游戏结束");
    }

    public boolean isValid(int x, int y) {
        for (int i = 0; i < currentShape.length; i++) {
            for (int j = 0; j < currentShape[i].length; j++) {
                if (currentShape[i][j] == 1) {
                    int nx = x + j, ny = y + i;
                    if (nx < 0 || nx >= WIDTH || ny >= HEIGHT) return false;
                    if (ny >= 0 && map[ny][nx] == 1) return false;
                }
            }
        }
        return true;
    }

    // 新增：炸弹道具生效 —— 消除最下方一行
    public void useBomb() {
        int row = HEIGHT - 1;
        for (int j = 0; j < WIDTH; j++) {
            map[row][j] = 0;
            kittyMap[row][j] = false;
        }
        for (int k = row; k > 0; k--) {
            map[k] = Arrays.copyOf(map[k-1], WIDTH);
            kittyMap[k] = Arrays.copyOf(kittyMap[k-1], WIDTH);
        }
        map[0] = new int[WIDTH];
        kittyMap[0] = new boolean[WIDTH];
        score += 200;
    }

    public void fixShape() {
        // 炸弹效果
        if (itemType == 1) {
            useBomb();
            newShape();   // ← 加上这一行，让炸弹后生成新方块
            return;
        }

        // 正常固定方块（非炸弹）
        for (int i = 0; i < currentShape.length; i++) {
            for (int j = 0; j < currentShape[i].length; j++) {
                if (currentShape[i][j] == 1) {
                    int mx = curX + j, my = curY + i;
                    if (my >= 0) {
                        map[my][mx] = 1;
                        if (hasKitty && i == kittyI && j == kittyJ) {
                            kittyMap[my][mx] = true;
                        }
                    }
                }
            }
        }
        clearLines();
        newShape();
    }
    public void clearLines() {
        int lines = 0;
        for (int i = HEIGHT-1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < WIDTH; j++) if (map[i][j] == 0) { full = false; break; }
            if (full) {
                for (int k = i; k > 0; k--) {
                    map[k] = Arrays.copyOf(map[k-1], WIDTH);
                    kittyMap[k] = Arrays.copyOf(kittyMap[k-1], WIDTH);
                }
                map[0] = new int[WIDTH];
                kittyMap[0] = new boolean[WIDTH];
                lines++; i++;
            }
        }
        score += lines * 100;
    }

    public void restartGame() {
        map = new int[HEIGHT][WIDTH];
        kittyMap = new boolean[HEIGHT][WIDTH];
        score = 0; shapeCount = 0;
        hasKitty = false; itemType = 0;
        nextShape = createRandomShape();
        newShape();
    }

    // 动态难度：根据分数获取下落速度（越小越快）
    public int getSpeed() {
        if (score < 500) return 800;
        if (score < 1000) return 600;
        if (score < 2000) return 400;
        if (score < 3000) return 300;
        return 200;
    }

    // getter
    public int[][] getMap() { return map; }
    public boolean[][] getKittyMap() { return kittyMap; }
    public int[][] getCurrentShape() { return currentShape; }
    public int[][] getNextShape() { return nextShape; }
    public int getCurX() { return curX; }
    public int getCurY() { return curY; }
    public int getScore() { return score; }
    public boolean hasKitty() { return hasKitty; }
    public int getKittyI() { return kittyI; }
    public int getKittyJ() { return kittyJ; }
    public int getItemType() { return itemType; }

    public void setCurX(int curX) { this.curX = curX; }
    public void setCurY(int curY) { this.curY = curY; }
    public void setCurrentShape(int[][] s) { this.currentShape = s; }
}