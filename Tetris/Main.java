package Tetris;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("俄罗斯方块 - Hello Kitty版");
        GamePanel panel = new GamePanel();
        frame.add(panel);
        frame.setSize(320, 440);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 修复方法名：setLocationRelativeTo(null)
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}