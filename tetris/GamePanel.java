package com.tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Game game;
    private Timer timer;
    private boolean isPaused = false;
    private Image kitty;
    private Clip bgm;

    // 主题：0=深色 1=粉色 2=浅色
    private int theme = 0;
    private Color bgColor, blockColor;

    public GamePanel() {
        game = new Game();
        kitty = new ImageIcon(getClass().getResource("/kitty.png")).getImage();
        setFocusable(true);
        addKeyListener(this);
        updateTheme();
        // 动态速度
        timer = new Timer(game.getSpeed(), this);
        timer.start();
        playBGM();
    }

    private void updateTheme() {
        if (theme == 0) {
            bgColor = new Color(0,0,0,210);
            blockColor = new Color(255,190,200);
        } else if (theme == 1) {
            bgColor = new Color(255,230,235,230);
            blockColor = new Color(255,80,140);
        } else {
            bgColor = new Color(255,255,255,230);
            blockColor = new Color(60,60,60);
        }
        setBackground(Color.BLACK);
    }

    private void playBGM() {
        try {
            InputStream audioSrc = getClass().getResourceAsStream("/bgm.wav");
            if (audioSrc == null) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(audioSrc);
            bgm = AudioSystem.getClip();
            bgm.open(ais);
            bgm.loop(Clip.LOOP_CONTINUOUSLY);
            bgm.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(bgColor);
        g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(blockColor);

        // 地图
        for (int i=0; i<Game.HEIGHT; i++)
            for (int j=0; j<Game.WIDTH; j++)
                if (game.getMap()[i][j]==1)
                    g.fillRect(j*Game.BLOCK, i*Game.BLOCK, Game.BLOCK-1, Game.BLOCK-1);

        // 当前方块
        int[][] s = game.getCurrentShape();
        int x = game.getCurX(), y = game.getCurY();
        for (int i=0; i<s.length; i++)
            for (int j=0; j<s[i].length; j++)
                if (s[i][j]==1)
                    g.fillRect((x+j)*Game.BLOCK, (y+i)*Game.BLOCK, Game.BLOCK-1, Game.BLOCK-1);

        // Kitty永久显示
        if (kitty != null) {
            boolean[][] km = game.getKittyMap();
            for (int i=0; i<Game.HEIGHT; i++)
                for (int j=0; j<Game.WIDTH; j++)
                    if (km[i][j])
                        g.drawImage(kitty, j*Game.BLOCK+2, i*Game.BLOCK+2, Game.BLOCK-5, Game.BLOCK-5, this);
        }

        // 当前Kitty
        if (game.hasKitty() && kitty != null) {
            int ki = game.getKittyI(), kj = game.getKittyJ();
            if (ki>=0 && kj>=0 && ki<s.length && kj<s[ki].length && s[ki][kj]==1)
                g.drawImage(kitty, (x+kj)*Game.BLOCK+2, (y+ki)*Game.BLOCK+2, Game.BLOCK-5, Game.BLOCK-5, this);
        }

        // 炸弹文字提示
        if (game.getItemType() == 1) {
            g.setColor(Color.RED);
            g.setFont(new Font("黑体", Font.BOLD, 14));
            g.drawString("炸弹", 10, 45);
        }

        g.setColor(Color.WHITE);
        g.drawString("Score: " + game.getScore(), 10, 20);
        // 绘制下一个方块预览
        g.setColor(Color.WHITE);
        g.drawString("Next:", 10, 100);  // 预览文字位置

        int[][] next = game.getNextShape();
        int previewX = 10;      // 预览区域起始 X
        int previewY = 110;     // 预览区域起始 Y
        int blockSize = 20;     // 与主游戏方块大小一致

        for (int i = 0; i < next.length; i++) {
            for (int j = 0; j < next[i].length; j++) {
                if (next[i][j] == 1) {
                    g.fillRect(previewX + j * blockSize, previewY + i * blockSize, blockSize - 1, blockSize - 1);
                }
            }
        }
        g.drawString("按C切换主题", 10, 70);

        if (isPaused) {
            g.setColor(Color.PINK);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("PAUSED", 120, 220);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;
        // 动态难度：实时更新速度
        timer.setDelay(game.getSpeed());
        try {
            if (game.isValid(game.getCurX(), game.getCurY()+1))
                game.setCurY(game.getCurY()+1);
            else game.fixShape();
            repaint();
        } catch (Exception ex) {
            timer.stop();
            if (bgm != null) bgm.stop();
            JOptionPane.showMessageDialog(this, "游戏结束！");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_C) {
            theme = (theme + 1) % 3;
            updateTheme();
            repaint();
            return;
        }
        if (k == KeyEvent.VK_P) {
            isPaused = !isPaused;
            if (bgm != null) { if (isPaused) bgm.stop(); else bgm.start(); }
            repaint(); return;
        }
        if (k == KeyEvent.VK_R) {
            game.restartGame(); isPaused = false;
            timer.setDelay(game.getSpeed());
            if (bgm != null) { bgm.setMicrosecondPosition(0); bgm.start(); }
            repaint(); return;
        }
        if (isPaused) return;

        if (k == KeyEvent.VK_LEFT && game.isValid(game.getCurX()-1, game.getCurY()))
            game.setCurX(game.getCurX()-1);
        if (k == KeyEvent.VK_RIGHT && game.isValid(game.getCurX()+1, game.getCurY()))
            game.setCurX(game.getCurX()+1);
        if (k == KeyEvent.VK_DOWN && game.isValid(game.getCurX(), game.getCurY()+1))
            game.setCurY(game.getCurY()+1);
        if (k == KeyEvent.VK_UP) rotate();
        if (k == KeyEvent.VK_SPACE)
            while (game.isValid(game.getCurX(), game.getCurY()+1))
                game.setCurY(game.getCurY()+1);
        repaint();
    }

    private void rotate() {
        int h = game.getCurrentShape().length;
        int w = game.getCurrentShape()[0].length;
        int[][] r = new int[w][h];
        for (int i=0; i<h; i++)
            for (int j=0; j<w; j++)
                r[j][h-1-i] = game.getCurrentShape()[i][j];
        int[][] old = game.getCurrentShape();
        game.setCurrentShape(r);
        if (!game.isValid(game.getCurX(), game.getCurY()))
            game.setCurrentShape(old);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}