import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private final int boardWidth = 360;
    private final int boardHeight = 640;
    private final int gravity = 1;
    private final int velocityX = -4;

    private BufferedImage imgGameBackground;
    private BufferedImage imgGameTutorial;
    private BufferedImage imgGameOver;

    private BufferedImage imgBirdA;
    private BufferedImage imgBirdB;
    private BufferedImage imgBirdC;

    private BufferedImage imgPipeTop;
    private BufferedImage imgPipeBot;

    private Bird bird;
    private final ArrayList<Pipe> pipes = new ArrayList<>();

    private Timer gameLoop;
    private Timer birdAnimationTimer;
    private Timer pipeTimer;

    private Font customFont;

    private int velocityY = 0;
    private boolean gameOver = false;
    private boolean showTutorial = true;
    private double score = 0;

    public FlappyBird() {
        initUI();
        initResources();
        initGameObjects();
        initGameTimers();
    }

    private void initUI() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);
    }

    private void initResources() {
        imgGameBackground = loadImage("/images/game_background.png");
        imgGameTutorial = loadImage("/images/game_tutorial.png");
        imgGameOver = loadImage("/images/game_over.png");
        imgPipeTop = loadImage("/images/pipe_top.png");
        imgPipeBot = loadImage("/images/pipe_bot.png");
        imgBirdA = loadImage("/images/bird_a.png");
        imgBirdB = loadImage("/images/bird_b.png");
        imgBirdC = loadImage("/images/bird_c.png");
        initCustomFont();
    }

    private void initGameObjects() {
        int birdWidth = imgBirdB.getWidth() * 2 / 3;
        int birdHeight = imgBirdB.getHeight() * 2 / 3;
        bird = new Bird(boardWidth / 8, boardHeight / 2, birdWidth, birdHeight, imgBirdA, imgBirdB, imgBirdC);
    }

    private void initGameTimers() {
        pipeTimer = new Timer(1800, e -> placePipes());
        pipeTimer.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        birdAnimationTimer = new Timer(100, e -> bird.nextFrame());
        birdAnimationTimer.start();
    }

    private void initCustomFont() {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/flappyfont.TTF")) {
            if (fontStream == null) throw new IOException("Font file not found");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(32f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (FontFormatException | IOException e) {
            System.err.println("Using default font: " + e.getMessage());
            customFont = new Font("Arial", Font.PLAIN, 32);
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void placePipes() {
        int pipeMinY = -480;
        int pipeMaxY = -40;
        int pipeRandomY = pipeMinY + (int) (Math.random() * (pipeMaxY - pipeMinY));
        int pipeOpening = boardHeight / 4;

        pipes.add(new Pipe(boardWidth, pipeRandomY, 50, 500, imgPipeTop));
        pipes.add(new Pipe(boardWidth, pipeRandomY + 500 + pipeOpening, 50, 500, imgPipeBot));

        if (pipes.size() > 1) showTutorial = false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGame(g);
    }

    private void drawGame(Graphics g) {
        int gameTutorialWidth = imgGameTutorial.getWidth() * 2 / 3;
        int gameTutorialHeight = imgGameTutorial.getHeight() * 2 / 3;
        int gameTutorialX = (boardWidth - gameTutorialWidth) / 2;
        int gameTutorialY = (boardHeight - gameTutorialHeight) / 2;

        int gameOverWidth = imgGameOver.getWidth() * 2 / 3;
        int gameOverHeight = imgGameOver.getHeight() * 2 / 3;
        int gameOverX = (boardWidth - gameOverWidth) / 2;
        int gameOverY = gameTutorialY;

        g.drawImage(imgGameBackground, 0, 0, boardWidth, boardHeight, null);

        if (gameOver) {
            g.drawImage(imgGameOver, gameOverX, gameOverY, gameOverWidth, gameOverHeight, null);
            drawCenteredText(g, "Score: " + (int) score, gameOverY + gameOverHeight + 50, customFont.deriveFont(36f));
        } else {
            bird.draw(g);
            if (showTutorial) {
                g.drawImage(imgGameTutorial, gameTutorialX, gameTutorialY, gameTutorialWidth, gameTutorialHeight, null);
            }
            pipes.forEach(pipe -> pipe.draw(g));
            if (!showTutorial) {
                drawCenteredText(g, "Score: " + (int) score, boardHeight / 8, customFont.deriveFont(36f));
            }
        }
    }

    private void drawCenteredText(Graphics g, String text, int y, Font font) {
        g.setFont(font);
        g.setColor(Color.WHITE);
        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int x = (boardWidth - textWidth) / 2;
        g.drawString(text, x, y);
    }

    private void moveGameObjects() {
        velocityY += gravity;
        bird.move(velocityY);

        pipes.forEach(pipe -> {
            pipe.move(velocityX);
            if (!pipe.isPassed() && bird.passed(pipe)) {
                pipe.markPassed();
                score += 0.5;
            }
            if (bird.collidesWith(pipe)) gameOver = true;
        });

        pipes.removeIf(pipe -> pipe.isOutOfBounds());
        if (bird.isOutOfBounds(boardHeight)) gameOver = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) moveGameObjects();
        repaint();
        if (gameOver) stopTimers();
    }

    private void stopTimers() {
        birdAnimationTimer.stop();
        pipeTimer.stop();
        gameLoop.stop();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -10;
            if (gameOver) resetGame();
        }
    }

    private void resetGame() {
        bird.reset(boardWidth / 8, boardHeight / 2);
        pipes.clear();
        score = 0;
        gameOver = false;
        velocityY = 0;
        initGameTimers();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    private static class Bird {
        private int x, y, width, height;
        private final Image[] animationFrames;
        private int currentFrame;

        Bird(int x, int y, int width, int height, Image... frames) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.animationFrames = frames;
        }

        void move(int velocityY) {
            y += velocityY;
        }

        void draw(Graphics g) {
            g.drawImage(animationFrames[currentFrame], x, y, width, height, null);
        }

        void nextFrame() {
            currentFrame = (currentFrame + 1) % animationFrames.length;
        }

        boolean collidesWith(Pipe pipe) {
            return x < pipe.getX() + pipe.getWidth() && x + width > pipe.getX() && y < pipe.getY() + pipe.getHeight() && y + height > pipe.getY();
        }

        boolean passed(Pipe pipe) {
            return x > pipe.getX() + pipe.getWidth();
        }

        void reset(int startX, int startY) {
            x = startX;
            y = startY;
            currentFrame = 0;
        }

        boolean isOutOfBounds(int boardHeight) {
            return y > boardHeight || y < 0;
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

        void markPassed() {
            passed = true;
        }

        boolean isOutOfBounds() {
            return x + width < 0;
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
