package com.example.demo;

import javafx.scene.image.Image;

public class Platform {
    public double x, y, width, height;

    // Image
    private static Image platformImage;
    private static boolean useImage = false;
    private static boolean imageLoadAttempted = false;

    public Platform(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Load image only once for all platforms
        if (!imageLoadAttempted) {
            loadImage();
            imageLoadAttempted = true;
        }
    }

    private static void loadImage() {
        try {
            platformImage = new Image(Platform.class.getResourceAsStream("/images/platform.png"));
            useImage = true;
        } catch (Exception e) {
            System.out.println("Could not load platform image, using default graphics");
            useImage = false;
        }
    }

    public static Image getPlatformImage() {
        return platformImage;
    }

    public static boolean shouldUseImage() {
        return useImage;
    }
}