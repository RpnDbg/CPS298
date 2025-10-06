package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Projectile {
    private double x, y;
    private double width = 12;  // Increased from 8
    private double height = 12; // Increased from 8
    private double velocityX;
    private double velocityY = 0;
    private boolean active = true;
    private static final double SPEED = 500;

    public Projectile(double x, double y, boolean facingRight) {
        this.x = x;
        this.y = y;
        this.velocityX = facingRight ? SPEED : -SPEED;
    }

    public void update(double deltaTime, double screenWidth) {
        x += velocityX * deltaTime;

        if (x < 0 || x > screenWidth) {
            active = false;
        }
    }

    public void render(GraphicsContext gc) {
        if (!active) return;

        gc.setFill(Color.rgb(100, 200, 255, 0.3));
        gc.fillOval(x - 6, y - 6, width + 12, height + 12);

        gc.setFill(Color.rgb(150, 220, 255));
        gc.fillOval(x, y, width, height);

        gc.setFill(Color.rgb(200, 240, 255));
        gc.fillOval(x + 3, y + 3, width - 6, height - 6);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}