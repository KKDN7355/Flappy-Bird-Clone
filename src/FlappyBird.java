import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Constants
    private static final int BOARD_WIDTH = 360;
    private static final int BOARD_HEIGHT = 640;
    private static final int VELOCITY_X = -4;
    
    // Resources
    private BufferedImage imgGameBackground, imgGameTutorial, imgGameOver;
    private BufferedImage imgBirdA, imgBirdB, imgBirdC;
    private BufferedImage imgPipeTop, imgPipeBot;
    private BufferedImage imgGravityInverter;
    private Font customFont;
    private Clip backgroundMusic;

    // Game Objects
    private Bird bird;
    private final ArrayList<GameObject> pipes = new ArrayList<>();

    // Timers
    private Timer gameTimer, birdTimer, pipeTimer, fadeTimer;

    // Game state
    private int gravity = 1;
    private int velocityY = 0;
    private boolean showTutorial = true;
    private boolean gameOver = false;
    private boolean gravityInverted = false;
    private float tutorialAlpha = 1.0f;
    private double score = 0;
    private double bestScore = 0;
    private int pipeCounter = 0;
    
    public FlappyBird() {
        initUI();
        initResources();
        initGameObjects();
        resetGame();
    }

    private void initUI() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
    }

    private void initResources() {
        imgGameBackground = loadImage("/images/game_background.png");
        imgGameTutorial = loadImage("/images/game_tutorial.png");
        imgGameOver = loadImage("/images/game_over.png");
        imgBirdA = loadImage("/images/bird_a.png");
        imgBirdB = loadImage("/images/bird_b.png");
        imgBirdC = loadImage("/images/bird_c.png");
        imgPipeTop = loadImage("/images/pipe_top.png");
        imgPipeBot = loadImage("/images/pipe_bot.png");
        imgGravityInverter = loadImage("/images/gravity_inverter.png");
        loadCustomFont();
    }

    private void initGameObjects() {
        int birdWidth = imgBirdB.getWidth() * 2 / 3;
        int birdHeight = imgBirdB.getHeight() * 2 / 3;
        bird = new Bird(BOARD_WIDTH / 8, BOARD_HEIGHT / 2, birdWidth, birdHeight, imgBirdA, imgBirdB, imgBirdC);
    }

    private void initGameTimers() {
        pipeTimer = new Timer(1800, e -> spawnPipes());
        pipeTimer.start();

        gameTimer = new Timer(1000 / 60, this);
        gameTimer.start();

        birdTimer = new Timer(100, e -> bird.nextFrame());
        birdTimer.start();
    }

    // Game Logic
    private void resetGame() {
        stopTimers();
        stopBackgroundMusic();
        
        bird.reset(BOARD_WIDTH / 8, BOARD_HEIGHT / 2);
        pipes.clear();
        score = 0;
        gameOver = false;
        velocityY = 0;
        tutorialAlpha = 1.0f;
        showTutorial = true;
        gravityInverted = false;
        gravity = 1;
        pipeCounter = 0;

        playBackgroundMusic("bg_normal.wav");
        initGameTimers();
    }

    private void moveGameObjects() {
        velocityY += gravity;
        bird.move(velocityY);

        pipes.forEach(obj -> {
            obj.move(VELOCITY_X);

            if (obj instanceof Pipe) {
                handlePipeCollision((Pipe) obj);
            } else if (obj instanceof GravityInverter) {
                handleGravityInverterCollision((GravityInverter) obj);
            }
        });

        pipes.removeIf(GameObject::isOutOfBounds);
        if (bird.isOutOfBounds(BOARD_HEIGHT)) {
            handleGameOver();
        }
    }

    private void spawnPipes() {
        int pipeMinY = -480;
        int pipeMaxY = -40;
        int pipeRandomY = pipeMinY + (int) (Math.random() * (pipeMaxY - pipeMinY));
        int pipeOpening = BOARD_HEIGHT / 4;
    
        pipes.add(new Pipe(BOARD_WIDTH, pipeRandomY, 50, 500, imgPipeTop));
        pipes.add(new Pipe(BOARD_WIDTH, pipeRandomY + 500 + pipeOpening, 50, 500, imgPipeBot));
        
        if (pipeCounter % 10 == 9) {
            int gravityInverterY = pipeRandomY + 500 + (pipeOpening / 2) - 20;
            pipes.add(new GravityInverter(BOARD_WIDTH, gravityInverterY, 40, 40, imgGravityInverter));
        }
    
        if (showTutorial) startFadeTimer();
        
        pipeCounter++;
    }

    private void handlePipeCollision(Pipe pipe) {
        if (!pipe.isPassed() && bird.passed(pipe)) {
            playSound("point.wav");
            pipe.markPassed();
            score += 0.5;
        }
        if (bird.collidesWith(pipe)) {
            handleGameOver();
        }
    }

    private void handleGravityInverterCollision(GravityInverter inverter) {
        if (!inverter.isPassed() && bird.collidesWith(inverter)) {
            inverter.markPassed();
            gravityInverted = !gravityInverted;
            gravity = gravityInverted ? -1 : 1;
            playBackgroundMusic(gravityInverted ? "bg_inverted.wav" : "bg_normal.wav");
            playSound("swoosh.wav");
        }
    }

    private void handleGameOver() {
        playSound("die.wav");
        gameOver = true;
        stopTimers();
        if (score > bestScore) bestScore = score;
    }

    private void startFadeTimer() {
        fadeTimer = new Timer(50, e -> {
            tutorialAlpha = Math.max(0, tutorialAlpha - 0.05f);
            if (tutorialAlpha <= 0) {
                showTutorial = false;
                fadeTimer.stop();
            }
        });
        fadeTimer.start();
    }

    // Rendering
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGame(g);
    }

    private void drawGame(Graphics g) {
        int tutorialWidth = imgGameTutorial.getWidth() * 2 / 3;
        int tutorialHeight = imgGameTutorial.getHeight() * 2 / 3;
        int tutorialX = (BOARD_WIDTH - tutorialWidth) / 2;
        int tutorialY = (BOARD_HEIGHT - tutorialHeight) / 2;

        int gameOverWidth = imgGameOver.getWidth() * 2 / 3;
        int gameOverHeight = imgGameOver.getHeight() * 2 / 3;
        int gameOverX = (BOARD_WIDTH - gameOverWidth) / 2;
        int gameOverY = (BOARD_HEIGHT - tutorialHeight) / 2;

        Graphics2D g2d = (Graphics2D) g;
        g.drawImage(imgGameBackground, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);

        if (gameOver) {
            g.drawImage(imgGameOver, gameOverX, gameOverY, gameOverWidth, gameOverHeight, null);
            drawRightAlignedText(g, "Last Score: " + (int) score, gameOverY + gameOverHeight + 50, customFont.deriveFont(36f));
            drawRightAlignedText(g, "Best Score: " + (int) bestScore, gameOverY + gameOverHeight + 100, customFont.deriveFont(36f));
        } else {
            bird.draw(g2d);
            pipes.forEach(pipe -> pipe.draw(g2d));

            if (tutorialAlpha > 0) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tutorialAlpha));
                g2d.drawImage(imgGameTutorial, tutorialX, tutorialY, tutorialWidth, tutorialHeight, null);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            if (!showTutorial) {
                drawCenteredText(g2d, "Score: " + (int) score, BOARD_HEIGHT / 8, customFont.deriveFont(36f));
            }
        }
    }

    private void drawCenteredText(Graphics g, String text, int y, Font font) {
        g.setFont(font);
        g.setColor(Color.WHITE);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (BOARD_WIDTH - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private void drawRightAlignedText(Graphics g, String text, int y, Font font) {
        g.setFont(font);
        g.setColor(Color.WHITE);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = BOARD_WIDTH - metrics.stringWidth(text) - 50;
        g.drawString(text, x, y);
    }

    // Utility Methods
    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadCustomFont() {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/flappyfont.TTF")) {
            if (fontStream == null) throw new IOException("Font file not found");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(32f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (FontFormatException | IOException e) {
            System.err.println("Using default font: " + e.getMessage());
            customFont = new Font("Arial", Font.PLAIN, 32);
        }
    }

    private void playSound(String soundFilePath) {
        try {
            URL soundURL = getClass().getResource("/audio/" + soundFilePath);
            if (soundURL == null) throw new IOException("Sound file not found");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    private void playBackgroundMusic(String musicFile) {
        try {
            URL soundURL = getClass().getResource("/audio/" + musicFile);
            if (soundURL == null) throw new IOException("Background music file not found: " + musicFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            if (backgroundMusic != null && backgroundMusic.isRunning()) {
                backgroundMusic.stop();
                backgroundMusic.close();
            }
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
        } catch (Exception e) {
            System.err.println("Error playing background music: " + e.getMessage());
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }

    private void stopTimers() {
        if (pipeTimer != null) pipeTimer.stop();
        if (gameTimer != null) gameTimer.stop();
        if (birdTimer != null) birdTimer.stop();
        if (fadeTimer != null) fadeTimer.stop();
        if (score > bestScore) bestScore = score;
    }

    // Event Handlers
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) moveGameObjects();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            playSound("wing.wav");
            velocityY = gravityInverted ? 10 : -10;
            if (gameOver) resetGame();
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    // Nested Class
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

        boolean collidesWith(GameObject obj) {
            return x < obj.getX() + obj.getWidth() &&
                   x + width > obj.getX() &&
                   y < obj.getY() + obj.getHeight() &&
                   y + height > obj.getY();
        }
        
        boolean passed(Pipe pipe) {
            return x > pipe.getX();
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

    private static class Pipe extends GameObject {
        private boolean passed;
    
        Pipe(int x, int y, int width, int height, Image img) {
            super(x, y, width, height, img);
        }
    
        @Override
        void move(int velocityX) {
            x += velocityX;
        }
    
        @Override
        void draw(Graphics g) {
            g.drawImage(img, x, y, width, height, null);
        }
    
        boolean isPassed() {
            return passed;
        }
    
        void markPassed() {
            passed = true;
        }
    }
    
    private static class GravityInverter extends GameObject {
        private boolean passed;
    
        GravityInverter(int x, int y, int width, int height, Image img) {
            super(x, y, width, height, img);
        }
    
        @Override
        void move(int velocityX) {
            x += velocityX;
        }
    
        @Override
        void draw(Graphics g) {
            g.drawImage(img, x, y, width, height, null);
        }
    
        boolean isPassed() {
            return passed;
        }
    
        void markPassed() {
            passed = true;
        }
    }
    
    static abstract class GameObject {
        protected int x, y, width, height;
        protected final Image img;
    
        GameObject(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    
        abstract void move(int velocityX);
        abstract void draw(Graphics g);
    
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