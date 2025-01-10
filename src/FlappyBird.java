// There are some pipes that are impossible to clear. Address that.
// Make gravity more realistic/jump mechanic. Look at Celeste??
// Art Changes: Cat with a jetpack. Ketchup-fuelled.

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    private final int boardWidth = 360;
    private final int boardHeight = 640;

    private final Image imgBackground;
    private final Image imgBird;
    private final Image imgPipeTop;
    private final Image imgPipeBot;

    private final Bird bird;
    private final ArrayList<Pipe> pipes;

    private final Timer gameLoop;
    private final Timer pipeTimer;

    private int velocityY = 0;
    private final int gravity = 1;
    private final int velocityX = -4;

    private boolean gameOver = false;
    private double score = 0;

    // Constructor
    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        imgBackground   = loadImage("./image_background.png");
        imgBird         = loadImage("./image_bird.png");
        imgPipeTop      = loadImage("./image_pipe_top.png");
        imgPipeBot      = loadImage("./image_pipe_bot.png");

        bird = new Bird(boardWidth / 8, boardHeight / 2, 34, 24, imgBird);
        pipes = new ArrayList<>();

        pipeTimer = new Timer(1500, e -> placePipes());
        pipeTimer.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    private Image loadImage(String path) {
        return new ImageIcon(getClass().getResource(path)).getImage();
    }

    private void placePipes() {
        int minPipeY = -480;
        int maxPipeY = 0;
        int randomPipeY = minPipeY + (int) (Math.random() * (maxPipeY - minPipeY));

        int openingSpace = boardHeight / 4;

        pipes.add(new Pipe(boardWidth, randomPipeY, 64, 512, imgPipeTop));
        pipes.add(new Pipe(boardWidth, randomPipeY + 512 + openingSpace, 64, 512, imgPipeBot));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        g.drawImage(imgBackground, 0, 0, boardWidth, boardHeight, null);
        bird.draw(g);
        for (Pipe pipe : pipes) {
            pipe.draw(g);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString(gameOver ? "Game Over: " + (int) score : String.valueOf((int) score), 10, 35);
    }

    private void move() {
        // Apply gravity to the bird's velocity.
        velocityY += gravity;

        // Update the bird's position.
        bird.y += velocityY;

        // Prevent the bird from moving above the screen.
        bird.y = Math.max(bird.y, 0);

        // Handle pipes movement and collision detection.
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.move(velocityX);

            if (!pipe.isPassed() && bird.getX() > pipe.getX() + pipe.getWidth()) {
                pipe.setPassed(true);
                score += 0.5;
            }

            if (bird.collidesWith(pipe)) {
                gameOver = true;
            }
        }

        // Remove pipes that are off-screen.
        pipes.removeIf(pipe -> pipe.getX() + pipe.getWidth() < 0);

        // Check if the bird falls below the screen.
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            pipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if (gameOver) {
                resetGame();
            }
        }
    }

    private void resetGame() {
        bird.reset(boardWidth / 8, boardHeight / 2);
        pipes.clear();
        score = 0;
        gameOver = false;
        velocityY = 0;
        pipeTimer.start();
        gameLoop.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    // Bird Class
    private static class Bird {
        private int x, y, width, height;
        private final Image img;

        Bird(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }

        void draw(Graphics g) {
            g.drawImage(img, x, y, width, height, null);
        }

        boolean collidesWith(Pipe pipe) {
            return x < pipe.getX() + pipe.getWidth() &&
                   x + width > pipe.getX() &&
                   y < pipe.getY() + pipe.getHeight() &&
                   y + height > pipe.getY();
        }

        void reset(int startX, int startY) {
            this.x = startX;
            this.y = startY;
        }

        int getX() { return x; }
        int getY() { return y; }
    }

    // Pipe Class
    private static class Pipe {
        private int x, y, width, height;
        private final Image img;
        private boolean passed;

        Pipe(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
            this.passed = false;
        }

        void draw(Graphics g) {
            g.drawImage(img, x, y, width, height, null);
        }

        void move(int velocityX) {
            x += velocityX;
        }

        boolean isPassed() {
            return passed;
        }

        void setPassed(boolean passed) {
            this.passed = passed;
        }

        int getX() { return x; }
        int getY() { return y; }
        int getWidth() { return width; }
        int getHeight() { return height; }
    }
}