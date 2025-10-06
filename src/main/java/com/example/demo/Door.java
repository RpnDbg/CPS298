package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.image.Image;

public class Door {
    public double x, y, width, height;
    private boolean playerNearby = false;

    // Image
    private Image doorImage;
    private boolean useImage = false;

    public Door(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        loadImage();
    }

    private void loadImage() {
        try {
            doorImage = new Image(getClass().getResourceAsStream("/images/door.png"));
            useImage = true;
        } catch (Exception e) {
            System.out.println("Could not load door image, using default graphics");
            useImage = false;
        }
    }

    public boolean isPlayerNearby(double px, double py, double pWidth, double pHeight) {
        double interactionRange = 20;
        playerNearby = px < x + width + interactionRange &&
                px + pWidth > x - interactionRange &&
                py < y + height + interactionRange &&
                py + pHeight > y - interactionRange;
        return playerNearby;
    }

    public void render(GraphicsContext gc) {
        // Draw door image or fallback to rectangle
        if (useImage && doorImage != null) {
            gc.drawImage(doorImage, x, y, width, height);
        } else {
            // Fallback to original rectangle graphics
            gc.setFill(Color.rgb(80, 80, 90));
            gc.fillRect(x, y, width, height);

            gc.setFill(Color.rgb(40, 30, 20));
            gc.fillRect(x + 5, y + 5, width - 10, height - 10);

            gc.setStroke(Color.rgb(30, 20, 10));
            gc.setLineWidth(2);
            gc.strokeLine(x + 5, y + height / 3, x + width - 5, y + height / 3);
            gc.strokeLine(x + 5, y + 2 * height / 3, x + width - 5, y + 2 * height / 3);
            gc.strokeLine(x + width / 2, y + 5, x + width / 2, y + height - 5);

            gc.setFill(Color.rgb(150, 120, 80));
            gc.fillOval(x + width - 20, y + height / 2 - 5, 8, 8);
        }

        // Glowing outline when player is nearby
        if (playerNearby) {
            gc.setStroke(Color.rgb(100, 200, 255, 0.8));
            gc.setLineWidth(3);
            gc.strokeRect(x, y, width, height);

            gc.setFill(Color.rgb(255, 255, 255));
            gc.setFont(Font.font("Arial", 14));
            gc.fillText("Press E to Enter", x - 10, y - 10);
        }
    }
}