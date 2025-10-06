package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {
    private static final int WINDOWED_WIDTH = 1200;
    private static final int WINDOWED_HEIGHT = 700;

    private double screenWidth;
    private double screenHeight;
    private boolean isFullscreen = true;

    private Canvas canvas;
    private GraphicsContext gc;
    private Player player;
    private Platform[] platforms;
    private Spike[] spikes;
    private Door door;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private Set<KeyCode> pressedKeys;
    private long lastFrameTime;
    private boolean gameOver = false;

    private int currentLevel = 1;
    private boolean canEnterDoor = false;

    private Image backgroundImage;
    private boolean useBackgroundImage = false;

    private Stage primaryStage;
    private StackPane root;
    private Scene scene;

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        screenWidth = screenBounds.getWidth();
        screenHeight = screenBounds.getHeight();

        canvas = new Canvas(screenWidth, screenHeight);
        gc = canvas.getGraphicsContext2D();

        pressedKeys = new HashSet<>();
        enemies = new ArrayList<>();
        projectiles = new ArrayList<>();

        loadBackgroundImage();

        player = new Player(100, 100);

        loadLevel(1);

        root = new StackPane(canvas);
        scene = new Scene(root, screenWidth, screenHeight);

        scene.setOnKeyPressed(e -> {
            pressedKeys.add(e.getCode());

            if (e.getCode() == KeyCode.F && !gameOver && player.canShoot()) {
                shootProjectile();
            }

            if (e.getCode() == KeyCode.E && canEnterDoor && !gameOver) {
                enterDoor();
            }

            if (gameOver && e.getCode() == KeyCode.R) {
                restartGame();
            }

            if (e.getCode() == KeyCode.ESCAPE) {
                toggleFullscreen();
            }
        });
        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

        primaryStage.setTitle("Platformer Game - Hollow Knight Style");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();

        lastFrameTime = System.nanoTime();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;

                if (!gameOver) {
                    update(deltaTime);
                }
                render();
            }
        }.start();
    }

    // <CHANGE> Fixed toggleFullscreen to preserve player position and not make player disappear
    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;

        // Save player position before resizing
        double playerX = player.getX();
        double playerY = player.getY();

        if (isFullscreen) {
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            screenWidth = screenBounds.getWidth();
            screenHeight = screenBounds.getHeight();
            canvas.setWidth(screenWidth);
            canvas.setHeight(screenHeight);
            primaryStage.setFullScreen(true);
        } else {
            screenWidth = WINDOWED_WIDTH;
            screenHeight = WINDOWED_HEIGHT;
            canvas.setWidth(screenWidth);
            canvas.setHeight(screenHeight);
            primaryStage.setFullScreen(false);
            primaryStage.setWidth(WINDOWED_WIDTH + 16);
            primaryStage.setHeight(WINDOWED_HEIGHT + 39);
            primaryStage.centerOnScreen();
        }

        // Reload level with new dimensions
        loadLevel(currentLevel);

        // Restore player to a safe position
        player = new Player(Math.min(playerX, screenWidth - 200), 100);
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = new Image(getClass().getResourceAsStream("/images/background.png"));
            useBackgroundImage = true;
        } catch (Exception e) {
            System.out.println("Could not load background image, using default color");
            useBackgroundImage = false;
        }
    }

    private void shootProjectile() {
        double projectileX = player.isFacingRight() ?
                player.getX() + player.getWidth() :
                player.getX() - 15;
        double projectileY = player.getY() + player.getHeight() / 2 - 7;

        projectiles.add(new Projectile(projectileX, projectileY, player.isFacingRight()));
        player.shoot();
    }

    private void loadLevel(int level) {
        currentLevel = level;
        enemies.clear();
        projectiles.clear();

        if (level == 1) {
            platforms = new Platform[] {
                    new Platform(0, screenHeight - 80, screenWidth, 80),
                    new Platform(350, screenHeight - 280, 350, 40),
                    new Platform(800, screenHeight - 420, 350, 40),
                    new Platform(1200, screenHeight - 560, 350, 40),
                    new Platform(250, screenHeight - 480, 250, 40),
                    new Platform(950, screenHeight - 680, 300, 40)
            };

            spikes = new Spike[] {
                    new Spike(500, screenHeight - 115, 150, 35),
                    new Spike(850, screenHeight - 115, 220, 35),
                    new Spike(380, screenHeight - 315, 130, 35),
            };

            enemies.add(new Enemy(600, screenHeight - 180, 550, 750));
            enemies.add(new Enemy(420, screenHeight - 380, 370, 620));
            enemies.add(new Enemy(850, screenHeight - 520, 800, 1100));

            door = new Door(screenWidth - 220, screenHeight - 190, 90, 110);

        } else if (level == 2) {
            platforms = new Platform[] {
                    new Platform(0, screenHeight - 80, screenWidth, 80),
                    new Platform(220, screenHeight - 220, 250, 40),
                    new Platform(550, screenHeight - 360, 220, 40),
                    new Platform(880, screenHeight - 500, 220, 40),
                    new Platform(1200, screenHeight - 640, 220, 40),
                    new Platform(420, screenHeight - 640, 250, 40),
                    new Platform(750, screenHeight - 780, 320, 40),
                    new Platform(150, screenHeight - 780, 220, 40)
            };

            spikes = new Spike[] {
                    new Spike(500, screenHeight - 115, 300, 35),
                    new Spike(880, screenHeight - 115, 240, 35),
                    new Spike(1200, screenHeight - 115, 160, 35),
                    new Spike(570, screenHeight - 395, 130, 35),
                    new Spike(900, screenHeight - 535, 130, 35),
                    new Spike(1220, screenHeight - 675, 130, 35)
            };

            enemies.add(new Enemy(270, screenHeight - 180, 120, 420));
            enemies.add(new Enemy(250, screenHeight - 320, 220, 420));
            enemies.add(new Enemy(600, screenHeight - 460, 550, 720));
            enemies.add(new Enemy(930, screenHeight - 600, 880, 1050));
            enemies.add(new Enemy(1250, screenHeight - 740, 1200, 1370));

            door = new Door(220, screenHeight - 870, 90, 110);
        }
    }

    private void enterDoor() {
        if (currentLevel == 1) {
            player = new Player(50, 100);
            loadLevel(2);
        } else if (currentLevel == 2) {
            player = new Player(100, 100);
            loadLevel(1);
        }
    }

    private void update(double deltaTime) {
        player.setMovingLeft(pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT));
        player.setMovingRight(pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT));

        boolean jumpHeld = pressedKeys.contains(KeyCode.SPACE) ||
                pressedKeys.contains(KeyCode.W) ||
                pressedKeys.contains(KeyCode.UP);

        player.setJumpButtonHeld(jumpHeld);

        if (jumpHeld) {
            player.jump();
        }

        if (pressedKeys.contains(KeyCode.SHIFT)) {
            player.dash();
        }

        player.update(deltaTime, platforms, screenWidth, screenHeight);

        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);

            if (enemy.checkCollisionWithPlayer(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
                player.takeDamageFromEnemy();
            }
        }

        projectiles.removeIf(projectile -> {
            projectile.update(deltaTime, screenWidth);

            for (Enemy enemy : enemies) {
                if (enemy.checkCollisionWithProjectile(
                        projectile.getX(), projectile.getY(),
                        projectile.getWidth(), projectile.getHeight())) {
                    projectile.deactivate();
                    return true;
                }
            }

            return !projectile.isActive();
        });

        enemies.removeIf(enemy -> !enemy.isAlive());

        for (Spike spike : spikes) {
            if (spike.intersects(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
                player.takeDamage(spike.x, spike.y, spike.width);
            }
        }

        canEnterDoor = door.isPlayerNearby(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        if (player.getHealth() <= 0) {
            gameOver = true;
        }
    }

    private void render() {
        if (useBackgroundImage && backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight);
        } else {
            gc.setFill(Color.rgb(20, 20, 30));
            gc.fillRect(0, 0, screenWidth, screenHeight);
        }

        gc.setFill(Color.rgb(40, 40, 50));
        gc.fillRect(0, 0, 5, screenHeight);
        gc.fillRect(screenWidth - 5, 0, 5, screenHeight);

        gc.setStroke(Color.rgb(80, 80, 100));
        gc.setLineWidth(3);
        gc.strokeLine(5, 0, 5, screenHeight);
        gc.strokeLine(screenWidth - 5, 0, screenWidth - 5, screenHeight);

        if (Platform.shouldUseImage() && Platform.getPlatformImage() != null) {
            for (Platform platform : platforms) {
                Image platformImg = Platform.getPlatformImage();
                double imgWidth = platformImg.getWidth();
                double imgHeight = platformImg.getHeight();

                for (double px = platform.x; px < platform.x + platform.width; px += imgWidth) {
                    for (double py = platform.y; py < platform.y + platform.height; py += imgHeight) {
                        double drawWidth = Math.min(imgWidth, platform.x + platform.width - px);
                        double drawHeight = Math.min(imgHeight, platform.y + platform.height - py);
                        gc.drawImage(platformImg, 0, 0, drawWidth, drawHeight, px, py, drawWidth, drawHeight);
                    }
                }
            }
        } else {
            gc.setFill(Color.rgb(60, 60, 80));
            for (Platform platform : platforms) {
                gc.fillRect(platform.x, platform.y, platform.width, platform.height);

                gc.setStroke(Color.rgb(100, 100, 120));
                gc.setLineWidth(2);
                gc.strokeRect(platform.x, platform.y, platform.width, platform.height);
            }
        }

        for (Spike spike : spikes) {
            spike.render(gc);
        }

        door.render(gc);

        for (Enemy enemy : enemies) {
            enemy.render(gc);
        }

        for (Projectile projectile : projectiles) {
            projectile.render(gc);
        }

        player.render(gc);

        drawHealthBar();
        drawHUD();

        if (gameOver) {
            drawGameOver();
        }
    }

    private void drawHUD() {
        double hudX = screenWidth - 280;
        double hudY = 80;
        double hudWidth = 260;
        double hudHeight = 140;

        gc.setFill(Color.rgb(20, 20, 30, 0.8));
        gc.fillRoundRect(hudX, hudY, hudWidth, hudHeight, 10, 10);
        gc.setStroke(Color.rgb(100, 100, 120));
        gc.setLineWidth(2);
        gc.strokeRoundRect(hudX, hudY, hudWidth, hudHeight, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.fillText("Level " + currentLevel, hudX + 15, hudY + 30);

        gc.setFill(Color.rgb(255, 150, 150));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("Enemies: " + enemies.size(), hudX + 15, hudY + 60);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("Dash:", hudX + 15, hudY + 90);

        double dashBarWidth = 150;
        double dashBarHeight = 20;
        double dashBarX = hudX + 80;
        double dashBarY = hudY + 75;

        gc.setFill(Color.rgb(60, 60, 70));
        gc.fillRect(dashBarX, dashBarY, dashBarWidth, dashBarHeight);

        if (player.getDashCooldownTimer() > 0) {
            double cooldownPercent = player.getDashCooldownTimer() / player.getDashCooldown();
            gc.setFill(Color.rgb(255, 100, 100));
            gc.fillRect(dashBarX, dashBarY, dashBarWidth * cooldownPercent, dashBarHeight);
        } else {
            gc.setFill(Color.rgb(100, 255, 100));
            gc.fillRect(dashBarX, dashBarY, dashBarWidth, dashBarHeight);
        }

        gc.setStroke(Color.rgb(150, 150, 170));
        gc.setLineWidth(2);
        gc.strokeRect(dashBarX, dashBarY, dashBarWidth, dashBarHeight);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("Shoot:", hudX + 15, hudY + 125);

        double shootBarX = hudX + 80;
        double shootBarY = hudY + 110;

        gc.setFill(Color.rgb(60, 60, 70));
        gc.fillRect(shootBarX, shootBarY, dashBarWidth, dashBarHeight);

        if (player.getShootCooldownTimer() > 0) {
            double cooldownPercent = player.getShootCooldownTimer() / player.getShootCooldown();
            gc.setFill(Color.rgb(100, 100, 255));
            gc.fillRect(shootBarX, shootBarY, dashBarWidth * cooldownPercent, dashBarHeight);
        } else {
            gc.setFill(Color.rgb(100, 200, 255));
            gc.fillRect(shootBarX, shootBarY, dashBarWidth, dashBarHeight);
        }

        gc.setStroke(Color.rgb(150, 150, 170));
        gc.setLineWidth(2);
        gc.strokeRect(shootBarX, shootBarY, dashBarWidth, dashBarHeight);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Controls: A/D - Move | SPACE/W - Jump (Hold) | SHIFT - Dash | F - Shoot | E - Enter Door | ESC - Toggle Fullscreen", 10, screenHeight - 10);
    }

    private void drawHealthBar() {
        int health = player.getHealth();
        double heartSize = 40;
        double heartSpacing = 50;
        double startX = screenWidth - 270;
        double startY = 20;

        for (int i = 0; i < 5; i++) {
            double x = startX + (i * heartSpacing);

            if (i < health) {
                gc.setFill(Color.rgb(220, 50, 50));
            } else {
                gc.setFill(Color.rgb(60, 60, 70));
            }

            gc.fillOval(x, startY, heartSize / 2, heartSize / 2);
            gc.fillOval(x + heartSize / 2, startY, heartSize / 2, heartSize / 2);

            double[] xPoints = {x, x + heartSize, x + heartSize / 2};
            double[] yPoints = {startY + heartSize / 3, startY + heartSize / 3, startY + heartSize};
            gc.fillPolygon(xPoints, yPoints, 3);

            gc.setStroke(Color.rgb(150, 30, 30));
            gc.setLineWidth(2);
            gc.strokeOval(x, startY, heartSize / 2, heartSize / 2);
            gc.strokeOval(x + heartSize / 2, startY, heartSize / 2, heartSize / 2);
            gc.strokePolygon(xPoints, yPoints, 3);
        }
    }

    private void drawGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, screenWidth, screenHeight);

        gc.setFill(Color.rgb(220, 50, 50));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 100));
        String gameOverText = "GAME OVER";
        double textWidth = gc.getFont().getSize() * gameOverText.length() * 0.5;
        gc.fillText(gameOverText, screenWidth / 2 - textWidth / 2, screenHeight / 2 - 50);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 40));
        String restartText = "Press R to Restart";
        double restartWidth = gc.getFont().getSize() * restartText.length() * 0.4;
        gc.fillText(restartText, screenWidth / 2 - restartWidth / 2, screenHeight / 2 + 50);
    }

    private void restartGame() {
        gameOver = false;
        player = new Player(100, 100);
        loadLevel(1);
    }
}