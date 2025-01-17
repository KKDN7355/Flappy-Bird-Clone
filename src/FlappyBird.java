import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private final int boardWidth = 360;
    private final int boardHeight = 640;

    private final Image imgBackground;
    private final Image imgBird;
    private final Image imgBirdTop;
    private final Image imgBirdBot;
    private final Image imgPipeTop;
    private final Image imgPipeBot;

    private final Bird bird;
    private final ArrayList<Pipe> pipes;

    private final Timer gameLoop;
    private final Timer pipeTimer;
    private final Timer activityTimer;

    private long lastActivityTime = System.currentTimeMillis();

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

        imgBackground = loadImage("./images/background.png");
        imgBird = loadImage("./images/cat.png");
        imgBirdTop = loadImage("./images/cat_top.png");
        imgBirdBot = loadImage("./images/cat_bot.png");
        imgPipeTop = loadImage("./images/pipe_top.png");
        imgPipeBot = loadImage("./images/pipe_bot.png");

        initializeCustomFont();

        bird = new Bird(boardWidth / 8, boardHeight / 2, 30, 30, imgBird, imgBirdTop, imgBirdBot);
        pipes = new ArrayList<>();

        pipeTimer = new Timer(1800, e -> placePipes());
        pipeTimer.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        activityTimer = new Timer(100, e -> checkInactivity());
        activityTimer.start();
    }

    private void initializeCustomFont() {
        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/bit5x3.ttf");
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
        System.out.println(new File("src/fonts/bit5x3.ttf").getAbsolutePath());

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

    private void checkInactivity() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastActivity = currentTime - lastActivityTime;
    
        if (timeSinceLastActivity < 250) {
            // Set bird's image to top position when active (recent key press)
            System.out.println("setImageToTop()");
            bird.setImageToTop();
        } else if (timeSinceLastActivity >= 250 && timeSinceLastActivity <= 500) {
            // Set bird's image to default position after a short delay
            System.out.println("setImageToDefault()");
            bird.setImageToDefault();
        } else if (timeSinceLastActivity > 500) {
            // Set bird's image to bottom position after inactivity
            System.out.println("setImageToBot()");
            bird.setImageToBot();
        }
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
            pipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -10;
            lastActivityTime = System.currentTimeMillis();
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

    private static class Bird {
        private int x, y, width, height;
        private Image imgCurrent;
        private final Image img;
        private final Image imgTop;
        private final Image imgBot;

        Bird(int x, int y, int width, int height, Image img, Image imgTop, Image imgBot) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.imgCurrent = img;
            this.img = img;
            this.imgTop = imgTop;
            this.imgBot = imgBot;
        }

        void setImageToTop() {
            this.imgCurrent = imgTop;
        }

        void setImageToDefault() {
            this.imgCurrent = img;
        }

        void setImageToBot() {
            this.imgCurrent = imgBot;
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
            this.imgCurrent = img;
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