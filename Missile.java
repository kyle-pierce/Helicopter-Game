package com.kp.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Missile extends GameObject {
    private int score;
    private int speed;
    private Animation animation;

    public Missile (Bitmap res, int x, int y, int width, int height, int score, int numFrames) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.score = score;
        speed = 10 + (int) (Math.random() * (score / 30));
        if (speed >= 40) { // cap missile speed
            speed = 40;
        }
        Bitmap[] image = new Bitmap[numFrames];
        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(res, 0, i * height, width, height);
        }
        animation = new Animation(image);
        animation.setDelay(100 - speed);
    }

    public void update() {
        x -= speed;
        animation.update();
    }

    public void draw(Canvas canvas) {
        try {
            canvas.drawBitmap(animation.getImage(), x, y, null);
        } catch (Exception e) {}
    }

    public int getWidth() {
        return width - 10;
    }
}
