import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;


public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private final int boardWidth = 360;
    private final int boardHeight = 640;

    private final Image imgGameBackground;
    private final Image imgGameTutorial;
    private final Image imgGameOver;

    private final Image imgBirdA;
    private final Image imgBirdB;
    private final Image imgBirdC;

    private final Image imgPipeTop;
    private final Image imgPipeBot;

    private final Bird bird;
    private final ArrayList<Pipe> pipes;

    private final Timer gameLoop;
    private final Timer birdAnimationTimer;
    private final Timer pipeTimer;
    
    private Font customFont;

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

        imgGameBackground = loadImage("./images/game_background.png");
        imgGameTutorial = loadImage("./images/game_tutorial.png");
        imgGameOver  = loadImage("./images/game_over.png");
        
        imgPipeTop = loadImage("./images/pipe_top.png");
        imgPipeBot = loadImage("./images/pipe_bot.png");

        imgBirdA = loadImage("./images/bird_a.png");
        imgBirdB = loadImage("./images/bird_b.png");
        imgBirdC = loadImage("./images/bird_c.png");

        initialiseCustomFont();

        bird = new Bird(boardWidth / 8, boardHeight / 2, 30, 30, imgBirdA, imgBirdB, imgBirdC);

        pipes = new ArrayList<>();

        pipeTimer = new Timer(1800, e -> placePipes());
        pipeTimer.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        birdAnimationTimer = new Timer(100, e -> bird.nextFrame());
        birdAnimationTimer.start();
    }

    private void initialiseCustomFont() {
        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/flappyfont.TTF");
            if (fontStream == null) {
                throw new IOException("Font file not found in resources");
            }
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(32f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            System.out.println("Using default font due to error: " + e.getMessage());
            customFont = new Font("Arial", Font.PLAIN, 32);
        }
    }
    
    private Image loadImage(String path) {
        return new ImageIcon(getClass().getResource(path)).getImage();
    }

    private void placePipes() {
        int minPipeY = -480;
        int maxPipeY = -40;
        int randomPipeY = minPipeY + (int) (Math.random() * (maxPipeY - minPipeY));
        int openingSpace = boardHeight / 4;

        pipes.add(new Pipe(boardWidth, randomPipeY, 50, 500, imgPipeTop));
        pipes.add(new Pipe(boardWidth, randomPipeY + 500 + openingSpace, 50, 500, imgPipeBot));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        g.drawImage(imgGameBackground, 0, 0, boardWidth, boardHeight, null);
        bird.draw(g);
        for (Pipe pipe : pipes) {
            pipe.draw(g);
        }

        g.setColor(Color.WHITE);
        g.setFont(customFont.deriveFont(48f));
        String scoreText = gameOver ? "Game Over: " + (int) score : String.valueOf((int) score);
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int textWidth = metrics.stringWidth(scoreText);
        int x = (boardWidth - textWidth) / 2;
        int y = boardHeight / 4;
        g.drawString(scoreText, x, y);
    }

    private void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

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

        pipes.removeIf(pipe -> pipe.getX() + pipe.getWidth() < 0);

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            birdAnimationTimer.stop();
            pipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -10;
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
        birdAnimationTimer.start();
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    private static class Bird {
        private int x, y, width, height;
        private Image[] animationFrames;
        private int currentFrame;
        private Image imgCurrent;
    
        Bird(int x, int y, int width, int height, Image... frames) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.animationFrames = frames;
            this.currentFrame = 0;
            this.imgCurrent = frames[0];
        }
    
        void nextFrame() {
            currentFrame = (currentFrame + 1) % animationFrames.length;
            imgCurrent = animationFrames[currentFrame];
        }
    
        void draw(Graphics g) {
            g.drawImage(imgCurrent, x, y, width, height, null);
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
            this.currentFrame = 0;
            this.imgCurrent = animationFrames[0];
        }
    
        int getX() {
            return x;
        }
    }
    
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

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        int getWidth() {
            return width;
        }

        int getHeight() {
            return height;
        }
    }
}