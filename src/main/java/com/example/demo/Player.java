package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

public class Player {
    private double x, y;
    private double velocityX, velocityY;
    // <CHANGE> Separated collision hitbox from visual display size
    // Collision hitbox - tight to actual character body
    private double width = 55;
    private double height = 95;
    // Visual display size - much bigger for better visibility
    private double renderWidth = 110;
    private double renderHeight = 150;

    private static final double GRAVITY = 1200;
    private static final double MOVE_SPEED = 350;
    private static final double JUMP_FORCE = -600;
    private static final double JUMP_HOLD_FORCE = -900;
    private static final double MAX_JUMP_HOLD_TIME = 0.25;
    private static final double DASH_SPEED = 700;
    private static final double DASH_DURATION = 0.2;
    private static final double DASH_COOLDOWN = 0.8;
    private static final double DAMAGE_COOLDOWN = 1.0;
    private static final double KNOCKBACK_FORCE_X = 450;
    private static final double KNOCKBACK_FORCE_Y = -450;
    private static final double SHOOT_COOLDOWN = 0.3;
    private static final double ATTACK_ANIMATION_DURATION = 0.2;
    private static final double WALK_ANIMATION_SPEED = 0.15;

    private boolean onGround = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean facingRight = true;

    private boolean isDashing = false;
    private double dashTimer = 0;
    private double dashCooldownTimer = 0;
    private int dashDirection = 1;

    private int health = 5;
    private double damageCooldownTimer = 0;
    private boolean isKnockedBack = false;
    private double knockbackTimer = 0;
    private static final double KNOCKBACK_DURATION = 0.3;

    private double shootCooldownTimer = 0;

    private boolean isJumping = false;
    private double jumpHoldTimer = 0;
    private boolean jumpButtonHeld = false;

    private Image playerIdleImage;
    private Image playerWalk1Image;
    private Image playerWalk2Image;
    private Image playerWalk3Image;
    private Image playerAttackImage;
    private Image playerJumpImage;
    private boolean useImage = false;

    private boolean isAttacking = false;
    private double attackAnimationTimer = 0;
    private double walkAnimationTimer = 0;
    private int currentWalkFrame = 0;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        loadImages();
    }

    private void loadImages() {
        try {
            playerIdleImage = new Image(getClass().getResourceAsStream("/images/player_idle.png"));
            useImage = true;

            try {
                playerWalk1Image = new Image(getClass().getResourceAsStream("/images/player_walk1.png"));
            } catch (Exception e) {
                playerWalk1Image = playerIdleImage;
            }

            try {
                playerWalk2Image = new Image(getClass().getResourceAsStream("/images/player_walk2.png"));
            } catch (Exception e) {
                playerWalk2Image = playerWalk1Image;
            }

            try {
                playerWalk3Image = new Image(getClass().getResourceAsStream("/images/player_walk3.png"));
            } catch (Exception e) {
                playerWalk3Image = playerWalk2Image;
            }

            try {
                playerAttackImage = new Image(getClass().getResourceAsStream("/images/player_attack.png"));
            } catch (Exception e) {
                playerAttackImage = playerIdleImage;
            }

            try {
                playerJumpImage = new Image(getClass().getResourceAsStream("/images/player_jump.png"));
            } catch (Exception e) {
                playerJumpImage = playerIdleImage;
            }

        } catch (Exception e) {
            System.out.println("Could not load player images, using default graphics");
            useImage = false;
        }
    }

    public void update(double deltaTime, Platform[] platforms, double screenWidth, double screenHeight) {
        if (damageCooldownTimer > 0) {
            damageCooldownTimer -= deltaTime;
        }

        if (knockbackTimer > 0) {
            knockbackTimer -= deltaTime;
            if (knockbackTimer <= 0) {
                isKnockedBack = false;
            }
        }

        if (isDashing) {
            dashTimer -= deltaTime;
            if (dashTimer <= 0) {
                isDashing = false;
                dashCooldownTimer = DASH_COOLDOWN;
            }
        }

        if (dashCooldownTimer > 0) {
            dashCooldownTimer -= deltaTime;
        }

        if (shootCooldownTimer > 0) {
            shootCooldownTimer -= deltaTime;
        }

        if (isAttacking) {
            attackAnimationTimer -= deltaTime;
            if (attackAnimationTimer <= 0) {
                isAttacking = false;
            }
        }

        // <CHANGE> Updated walk animation to cycle through 3 frames instead of 2
        if ((movingLeft || movingRight) && onGround) {
            walkAnimationTimer += deltaTime;
            if (walkAnimationTimer >= WALK_ANIMATION_SPEED) {
                walkAnimationTimer = 0;
                currentWalkFrame = (currentWalkFrame + 1) % 3;
            }
        } else {
            walkAnimationTimer = 0;
            currentWalkFrame = 0;
        }

        if (isDashing) {
            velocityX = DASH_SPEED * dashDirection;
            velocityY = 0;
        } else if (!isKnockedBack) {
            if (movingLeft) {
                velocityX = -MOVE_SPEED;
                facingRight = false;
            } else if (movingRight) {
                velocityX = MOVE_SPEED;
                facingRight = true;
            } else {
                velocityX = 0;
            }

            velocityY += GRAVITY * deltaTime;

            if (isJumping && jumpButtonHeld && jumpHoldTimer < MAX_JUMP_HOLD_TIME && velocityY < 0) {
                velocityY += JUMP_HOLD_FORCE * deltaTime;
                jumpHoldTimer += deltaTime;
            }

            if (!jumpButtonHeld || jumpHoldTimer >= MAX_JUMP_HOLD_TIME) {
                if (isJumping && velocityY < 0) {
                    isJumping = false;
                }
            }

        } else {
            velocityY += GRAVITY * deltaTime;
            velocityX *= 0.95;
        }

        x += velocityX * deltaTime;
        y += velocityY * deltaTime;

        if (x < 0) {
            x = 0;
            velocityX = 0;
        }
        if (x + width > screenWidth) {
            x = screenWidth - width;
            velocityX = 0;
        }

        onGround = false;

        for (Platform platform : platforms) {
            if (intersects(platform)) {
                double overlapLeft = (x + width) - platform.x;
                double overlapRight = (platform.x + platform.width) - x;
                double overlapTop = (y + height) - platform.y;
                double overlapBottom = (platform.y + platform.height) - y;

                double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                        Math.min(overlapTop, overlapBottom));

                if (minOverlap == overlapTop && velocityY > 0) {
                    y = platform.y - height;
                    velocityY = 0;
                    onGround = true;
                    isJumping = false;
                    jumpHoldTimer = 0;
                } else if (minOverlap == overlapBottom && velocityY < 0) {
                    y = platform.y + platform.height;
                    velocityY = 0;
                    isJumping = false;
                } else if (minOverlap == overlapLeft) {
                    x = platform.x - width;
                    if (isDashing) isDashing = false;
                } else if (minOverlap == overlapRight) {
                    x = platform.x + platform.width;
                    if (isDashing) isDashing = false;
                }
            }
        }

        if (velocityY > 1000) velocityY = 1000;
    }

    private boolean intersects(Platform platform) {
        return x < platform.x + platform.width &&
                x + width > platform.x &&
                y < platform.y + platform.height &&
                y + height > platform.y;
    }

    public void jump() {
        if (onGround && !isKnockedBack) {
            velocityY = JUMP_FORCE;
            onGround = false;
            isJumping = true;
            jumpHoldTimer = 0;
        }
    }

    public void setJumpButtonHeld(boolean held) {
        this.jumpButtonHeld = held;
    }

    public void dash() {
        if (!isDashing && dashCooldownTimer <= 0 && !isKnockedBack) {
            isDashing = true;
            dashTimer = DASH_DURATION;
            dashDirection = facingRight ? 1 : -1;
        }
    }

    public boolean canShoot() {
        return shootCooldownTimer <= 0;
    }

    public void shoot() {
        shootCooldownTimer = SHOOT_COOLDOWN;
        isAttacking = true;
        attackAnimationTimer = ATTACK_ANIMATION_DURATION;
    }

    public void takeDamage(double spikeX, double spikeY, double spikeWidth) {
        if (damageCooldownTimer <= 0 && health > 0) {
            health--;
            damageCooldownTimer = DAMAGE_COOLDOWN;
            applyKnockback(spikeX, spikeY, spikeWidth);
        }
    }

    public void takeDamageFromEnemy() {
        if (damageCooldownTimer <= 0 && health > 0) {
            health--;
            damageCooldownTimer = DAMAGE_COOLDOWN;
        }
    }

    private void applyKnockback(double spikeX, double spikeY, double spikeWidth) {
        isKnockedBack = true;
        knockbackTimer = KNOCKBACK_DURATION;
        isDashing = false;

        double spikeCenterX = spikeX + spikeWidth / 2;
        double playerCenterX = x + width / 2;

        if (playerCenterX < spikeCenterX) {
            velocityX = -KNOCKBACK_FORCE_X;
        } else {
            velocityX = KNOCKBACK_FORCE_X;
        }

        velocityY = KNOCKBACK_FORCE_Y;
    }

    public void render(GraphicsContext gc) {
        boolean isInvulnerable = damageCooldownTimer > 0;

        // <CHANGE> Calculate render position to center the larger sprite on the hitbox
        double renderX = x - (renderWidth - width) / 2;
        double renderY = y - (renderHeight - height);

        if (isDashing) {
            gc.setFill(Color.rgb(0, 255, 255, 0.3));
            gc.fillRect(renderX - 5, renderY - 5, renderWidth + 10, renderHeight + 10);
        }

        if (isKnockedBack) {
            gc.setFill(Color.rgb(255, 150, 0, 0.4));
            gc.fillRect(renderX - 8, renderY - 8, renderWidth + 16, renderHeight + 16);
        }

        if (useImage && playerIdleImage != null) {
            gc.save();

            Image currentSprite;
            if (isAttacking) {
                currentSprite = playerAttackImage;
            } else if (!onGround) {
                currentSprite = playerJumpImage;
            } else if (movingLeft || movingRight) {
                // <CHANGE> Added third walk frame to animation cycle
                if (currentWalkFrame == 0) {
                    currentSprite = playerWalk1Image;
                } else if (currentWalkFrame == 1) {
                    currentSprite = playerWalk2Image;
                } else {
                    currentSprite = playerWalk3Image;
                }
            } else {
                currentSprite = playerIdleImage;
            }

            if (!facingRight) {
                gc.translate(renderX + renderWidth, renderY);
                gc.scale(-1, 1);
                gc.drawImage(currentSprite, 0, 0, renderWidth, renderHeight);
            } else {
                gc.drawImage(currentSprite, renderX, renderY, renderWidth, renderHeight);
            }

            if (isInvulnerable && ((int)(damageCooldownTimer * 10) % 2 == 0)) {
                gc.setFill(Color.rgb(255, 100, 100, 0.5));
                if (!facingRight) {
                    gc.fillRect(0, 0, renderWidth, renderHeight);
                } else {
                    gc.fillRect(renderX, renderY, renderWidth, renderHeight);
                }
            }

            gc.restore();
        } else {
            if (isInvulnerable && ((int)(damageCooldownTimer * 10) % 2 == 0)) {
                gc.setFill(Color.rgb(255, 100, 100));
            } else {
                gc.setFill(Color.WHITE);
            }
            gc.fillRect(renderX, renderY, renderWidth, renderHeight);

            gc.setFill(Color.rgb(100, 150, 255));
            if (facingRight) {
                gc.fillRect(renderX + renderWidth - 8, renderY + 10, 5, 8);
            } else {
                gc.fillRect(renderX + 3, renderY + 10, 5, 8);
            }
        }
    }

    public void setMovingLeft(boolean moving) {
        this.movingLeft = moving;
    }

    public void setMovingRight(boolean moving) {
        this.movingRight = moving;
    }

    public int getHealth() {
        return health;
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

    public boolean isFacingRight() {
        return facingRight;
    }

    public double getDashCooldownTimer() {
        return dashCooldownTimer;
    }

    public double getShootCooldownTimer() {
        return shootCooldownTimer;
    }

    public double getDashCooldown() {
        return DASH_COOLDOWN;
    }

    public double getShootCooldown() {
        return SHOOT_COOLDOWN;
    }
}