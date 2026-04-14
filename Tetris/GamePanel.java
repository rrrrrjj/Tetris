package Tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Game game;
    private Timer timer;
    private boolean isPaused = false;
    private Image kitty;

    public GamePanel() {
        game = new Game();
        kitty = new ImageIcon("src/Tetris/kitty.png").getImage();
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(320, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(0,0,0, 210));
        g.fillRect(0,0, getWidth(), getHeight());
        g.setColor(new Color(255, 190, 200));

        // 画所有方块
        for (int i = 0; i < Game.HEIGHT; i++)
            for (int j = 0; j < Game.WIDTH; j++)
                if (game.getMap()[i][j] == 1)
                    g.fillRect(j*Game.BLOCK, i*Game.BLOCK, Game.BLOCK-1, Game.BLOCK-1);

        // 画当前下落方块
        int[][] shape = game.getCurrentShape();
        int x = game.getCurX();
        int y = game.getCurY();
        for (int i = 0; i < shape.length; i++)
            for (int j = 0; j < shape[i].length; j++)
                if (shape[i][j] == 1)
                    g.fillRect((x+j)*Game.BLOCK, (y+i)*Game.BLOCK, Game.BLOCK-1, Game.BLOCK-1);

        // 显示所有已经落下的Kitty（多个都保留）
        if (kitty != null) {
            boolean[][] kittyMap = game.getKittyMap();
            for (int i = 0; i < Game.HEIGHT; i++) {
                for (int j = 0; j < Game.WIDTH; j++) {
                    if (kittyMap[i][j]) {
                        g.drawImage(kitty,
                                j*Game.BLOCK + 2,
                                i*Game.BLOCK + 2,
                                Game.BLOCK - 5,
                                Game.BLOCK - 5, this);
                    }
                }
            }
        }

        // 当前Kitty方块显示
        if (game.hasKitty() && kitty != null) {
            int ki = game.getKittyI();
            int kj = game.getKittyJ();
            if (ki >=0 && kj >=0 && ki < shape.length && kj < shape[ki].length && shape[ki][kj] == 1) {
                int px = (x + kj) * Game.BLOCK + 2;
                int py = (y + ki) * Game.BLOCK + 2;
                g.drawImage(kitty, px, py, Game.BLOCK - 5, Game.BLOCK - 5, this);
            }
        }

        g.setColor(Color.WHITE);
        g.drawString("Score: " + game.getScore(), 10, 20);
        g.drawString("Next:", 220, 30);

        int n = 15;
        int[][] next = game.getNextShape();
        for (int i = 0; i < next.length; i++)
            for (int j = 0; j < next[i].length; j++)
                if (next[i][j] == 1)
                    g.fillRect(220 + j*n, 40 + i*n, n-1, n-1);

        if (isPaused) {
            g.setColor(Color.PINK);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("PAUSED", 120, 220);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;
        try {
            if (game.isValid(game.getCurX(), game.getCurY()+1))
                game.setCurY(game.getCurY()+1);
            else
                game.fixShape();
            repaint();
        } catch (Exception ex) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "游戏结束！");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_P) { isPaused = !isPaused; repaint(); return; }
        if (k == KeyEvent.VK_R) { game.restartGame(); timer.start(); isPaused=false; repaint(); return; }
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
        int[][] rotated = new int[w][h];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                rotated[j][h-1-i] = game.getCurrentShape()[i][j];
        if (game.isValid(game.getCurX(), game.getCurY()))
            game.setCurrentShape(rotated);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}