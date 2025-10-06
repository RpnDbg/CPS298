package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

public class Spike {
    public double x, y, width, height;
    private static final int SPIKE_COUNT = 5;

    // Image
    private Image spikeImage;
    private boolean useImage = false;

    public Spike(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        loadImage();
    }

    private void loadImage() {
        try {
            spikeImage = new Image(getClass().getResourceAsStream("/images/spike.png"));
            useImage = true;
        } catch (Exception e) {
            System.out.println("Could not load spike image, using default graphics");
            useImage = false;
        }
    }

    public void render(GraphicsContext gc) {
        // Draw spike image or fallback to triangles
        if (useImage && spikeImage != null) {
            // Tile the spike image across the width
            double spikeWidth = width / SPIKE_COUNT;
            for (int i = 0; i < SPIKE_COUNT; i++) {
                gc.drawImage(spikeImage, x + (i * spikeWidth), y, spikeWidth, height);
            }
        } else {
            // Fallback to original triangle graphics
            gc.setFill(Color.rgb(200, 50, 50));
            double spikeWidth = width / SPIKE_COUNT;

            for (int i = 0; i < SPIKE_COUNT; i++) {
                double baseX = x + (i * spikeWidth);
                double[] xPoints = {baseX, baseX + spikeWidth / 2, baseX + spikeWidth};
                double[] yPoints = {y + height, y, y + height};
                gc.fillPolygon(xPoints, yPoints, 3);
            }

            gc.setStroke(Color.rgb(255, 100, 100, 0.5));
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
        }
    }

    public boolean intersects(double px, double py, double pWidth, double pHeight) {
        return px < x + width &&
                px + pWidth > x &&
                py < y + height &&
                py + pHeight > y;
    }
}