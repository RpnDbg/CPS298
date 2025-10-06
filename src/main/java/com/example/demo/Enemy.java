package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

public class Enemy {
    private double x, y;
    // <CHANGE> Increased enemy size significantly for much bigger enemy models
    private double width = 100;  // Increased from 70
    private double height = 100; // Increased from 70
    private double velocityX = 60;
    private double minX, maxX;
    private boolean movingRight = true;
    private boolean alive = true;

    private Image enemyImage;
    private boolean useImage = false;

    public Enemy(double x, double y, double patrolRangeLeft, double patrolRangeRight) {
        this.x = x;
        this.y = y;
        this.minX = patrolRangeLeft;
        this.maxX = patrolRangeRight;
        loadImage();
    }

    private void loadImage() {
        try {
            enemyImage = new Image(getClass().getResourceAsStream("/images/enemy.png"));
            useImage = true;
        } catch (Exception e) {
            System.out.println("Could not load enemy image, using default graphics");
            useImage = false;
        }
    }

    public void update(double deltaTime) {
        if (!alive) return;

        if (movingRight) {
            x += velocityX * deltaTime;
            if (x >= maxX) {
                x = maxX;
                movingRight = false;
            }
        } else {
            x -= velocityX * deltaTime;
            if (x <= minX) {
                x = minX;
                movingRight = true;
            }
        }
    }

    public void render(GraphicsContext gc) {
        if (!alive) return;

        if (useImage && enemyImage != null) {
            gc.save();

            if (!movingRight) {
                gc.translate(x + width, y);
                gc.scale(-1, 1);
                gc.drawImage(enemyImage, 0, 0, width, height);
            } else {
                gc.drawImage(enemyImage, x, y, width, height);
            }

            gc.restore();
        } else {
            gc.setFill(Color.rgb(120, 40, 80));
            gc.fillRect(x, y, width, height);

            gc.setStroke(Color.rgb(180, 60, 120));
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);

            gc.setFill(Color.rgb(255, 50, 50));
            if (movingRight) {
                gc.fillOval(x + width - 25, y + 18, 15, 15);
                gc.fillOval(x + width - 25, y + 40, 15, 15);
            } else {
                gc.fillOval(x + 10, y + 18, 15, 15);
                gc.fillOval(x + 10, y + 40, 15, 15);
            }

            gc.setFill(Color.rgb(200, 80, 140));
            if (movingRight) {
                gc.fillPolygon(
                        new double[]{x + width, x + width - 12, x + width - 12},
                        new double[]{y + height / 2, y + height / 2 - 8, y + height / 2 + 8},
                        3
                );
            } else {
                gc.fillPolygon(
                        new double[]{x, x + 12, x + 12},
                        new double[]{y + height / 2, y + height / 2 - 8, y + height / 2 + 8},
                        3
                );
            }
        }
    }

    public boolean checkCollisionWithProjectile(double px, double py, double pWidth, double pHeight) {
        if (!alive) return false;

        boolean collision = px < x + width &&
                px + pWidth > x &&
                py < y + height &&
                py + pHeight > y;

        if (collision) {
            alive = false;
        }

        return collision;
    }

    public boolean checkCollisionWithPlayer(double px, double py, double pWidth, double pHeight) {
        if (!alive) return false;

        return px < x + width &&
                px + pWidth > x &&
                py < y + height &&
                py + pHeight > y;
    }

    public boolean isAlive() {
        return alive;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}